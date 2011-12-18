/**
 * 
 */
package vnet.sms.gateway.nettysupport.ping.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettysupport.PingResponseReceivedEvent;
import vnet.sms.gateway.nettysupport.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

/**
 * @author obergner
 * 
 */
public class OutgoingPingChannelHandler<ID extends Serializable> extends
        UpstreamWindowedChannelHandler<ID> {

	private final int	                        pingIntervalSeconds;

	private final int	                        pingResponseTimeoutMillis;

	private final MessageReferenceGenerator<ID>	windowIdGenerator;

	private final Timer	                        pingIntervalTimer	     = new HashedWheelTimer();

	private final Timer	                        pingResponseTimeoutTimer	= new HashedWheelTimer();

	private volatile PingSender	                pingSender;

	public OutgoingPingChannelHandler(final int pingIntervalSeconds,
	        final int pingResponseTimeoutMillis,
	        final MessageReferenceGenerator<ID> windowIdGenerator) {
		notNull(windowIdGenerator,
		        "Argument 'windowIdGenerator' must not be null");
		this.pingIntervalSeconds = pingIntervalSeconds;
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
		this.windowIdGenerator = windowIdGenerator;
	}

	public int getPingIntervalSeconds() {
		return this.pingIntervalSeconds;
	}

	public int getPingResponseTimeoutMillis() {
		return this.pingResponseTimeoutMillis;
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		getLog().info(
		        "Channel {} has been connected - will start to ping in [{}] seconds",
		        ctx.getChannel(), this.pingIntervalSeconds);
		this.pingSender = startPingSenderTask(ctx);

		super.channelConnected(ctx, e);
	}

	private PingSender startPingSenderTask(final ChannelHandlerContext ctx) {
		final PingSender pingSender = new PingSender(ctx);
		this.pingIntervalTimer.newTimeout(pingSender, this.pingIntervalSeconds,
		        TimeUnit.SECONDS);
		return pingSender;
	}

	@Override
	public void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) {
		if (this.pingSender == null) {
			throw new IllegalStateException(
			        "Cannot cancel ping response timout since no PingSender has been started - have you started a PingSender in channelConnected(...)?");
		}
		getLog().debug(
		        "Received response {} to previously sent ping request within timeout of [{}] milliseconds - will send out next ping after [{}] seconds",
		        new Object[] { e, this.pingResponseTimeoutMillis,
		                this.pingIntervalSeconds });
		this.pingSender.cancelPingResponseTimeout();
		restartPingSenderTask();

		super.pingResponseReceived(ctx, e);
	}

	private void restartPingSenderTask() {
		if (this.pingSender == null) {
			throw new IllegalStateException(
			        "Cannot restart a null PingSender - have you started a PingSender in channelConnected(...)?");
		}
		this.pingIntervalTimer.newTimeout(this.pingSender,
		        this.pingIntervalSeconds, TimeUnit.SECONDS);
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		getLog().info(
		        "Channel {} has been disconnected - will stop ping sender task");
		shutdownTimers();
		super.channelDisconnected(ctx, e);
	}

	private void shutdownTimers() {
		this.pingResponseTimeoutTimer.stop();
		this.pingIntervalTimer.stop();
		getLog().debug("All timers have been shut down");
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
	        final ExceptionEvent e) throws Exception {
		getLog().error(
		        "Received exception ['{}'] on channel {} - will stop ping sender task. This channel handler needs to be shut down as soon as possible.");
		shutdownTimers();

		super.exceptionCaught(ctx, e);
	}

	private final class PingSender implements TimerTask {

		private final ChannelHandlerContext	ctx;

		private volatile Timeout		    pingResponseTimeout;

		PingSender(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run(final Timeout timeout) throws Exception {
			if (timeout.isCancelled() || !this.ctx.getChannel().isOpen()) {
				return;
			}
			sendPingRequestDownstream();

			this.pingResponseTimeout = OutgoingPingChannelHandler.this.pingResponseTimeoutTimer
			        .newTimeout(
			                new PingResponseTimeout(this.ctx),
			                OutgoingPingChannelHandler.this.pingResponseTimeoutMillis,
			                TimeUnit.MILLISECONDS);
		}

		private void sendPingRequestDownstream() {
			final SendPingRequestEvent<ID> sendPingRequestEvent = createSendPingRequestEvent();
			getLog().debug(
			        "Sending {} on channel {} - will wait [{}] milliseconds for a response before issuing a request to close this channel",
			        new Object[] {
			                sendPingRequestEvent,
			                this.ctx.getChannel(),
			                OutgoingPingChannelHandler.this.pingResponseTimeoutMillis });
			this.ctx.sendDownstream(sendPingRequestEvent);
		}

		private SendPingRequestEvent<ID> createSendPingRequestEvent() {
			final PingRequest pingRequest = new PingRequest(this.ctx
			        .getChannel().getLocalAddress(), this.ctx.getChannel()
			        .getRemoteAddress());
			return new SendPingRequestEvent<ID>(
			        OutgoingPingChannelHandler.this.windowIdGenerator
			                .nextMessageReference(),
			        this.ctx.getChannel(), pingRequest, this.ctx.getChannel()
			                .getRemoteAddress());
		}

		boolean cancelPingResponseTimeout() {
			if ((this.pingResponseTimeout == null)
			        || this.pingResponseTimeout.isCancelled()
			        || this.pingResponseTimeout.isExpired()) {
				getLog().warn(
				        "Attempt to cancel a ping response timeout [{}] that is either null or cancelled or already expired",
				        this.pingResponseTimeout);
				return false;
			}
			this.pingResponseTimeout.cancel();
			getLog().debug("Cancelled ping response timeout {}",
			        this.pingResponseTimeout);
			this.pingResponseTimeout = null;
			return true;
		}
	}

	private final class PingResponseTimeout implements TimerTask {

		private final ChannelHandlerContext	ctx;

		PingResponseTimeout(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run(final Timeout timeout) throws Exception {
			if (timeout.isCancelled() || !this.ctx.getChannel().isOpen()) {
				return;
			}
			getLog().warn(
			        "Did not receive response to ping request after timeout of [{}] milliseconds - will issue a request to close this channel");
			this.ctx.sendUpstream(new NoPingResponseReceivedWithinTimeoutEvent(
			        this.ctx.getChannel(),
			        OutgoingPingChannelHandler.this.pingIntervalSeconds,
			        OutgoingPingChannelHandler.this.pingResponseTimeoutMillis));
		}
	}
}
