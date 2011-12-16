/**
 * 
 */
package vnet.routing.netty.server.support.ping;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Qualifier;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = PingService.OBJECT_NAME, description = "A service for periodically sending a configurable ping message via a set of channels")
@PingService.Default
@Service
public class PingService<P extends Serializable> {

	@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER,
			ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface Default {
	}

	public static final String OBJECT_NAME = "vnet.routing.netty.server.support:service=PingService,version=1.0.0-SNAPSHOT";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ChannelGroup pingChannels;

	private final PingFactory<P> pingFactory;

	private final ScheduledExecutorService periodicTaskScheduler;

	private int pingIntervalInSeconds;

	private final PingSender pingSender = new PingSender();

	private ScheduledFuture<?> pingSenderHandle;

	public PingService(final ChannelGroup pingChannels,
			final PingFactory<P> pingFactory,
			final ScheduledExecutorService periodicTaskScheduler,
			final int pingIntervalInSeconds) {
		notNull(pingChannels, "Argument 'pingChannels' must not be null");
		notNull(pingFactory, "Argument 'pingFactory' must not be null");
		notNull(periodicTaskScheduler,
				"Argument 'periodicTaskScheduler' must not be null");
		this.pingChannels = pingChannels;
		this.pingFactory = pingFactory;
		this.periodicTaskScheduler = periodicTaskScheduler;
		this.pingIntervalInSeconds = pingIntervalInSeconds;
	}

	@ManagedAttribute(description = "The interval between two pings in seconds")
	public int getPingIntervalInSeconds() {
		return this.pingIntervalInSeconds;
	}

	@ManagedOperation(description = "Start this service")
	public synchronized void start() {
		if (this.pingSenderHandle != null) {
			// Already started
			return;
		}
		this.log.info("Starting PingService {} ...", this);

		this.pingSenderHandle = this.periodicTaskScheduler.scheduleAtFixedRate(
				this.pingSender, 0L, this.pingIntervalInSeconds,
				TimeUnit.SECONDS);

		this.log.info("PingService {} started", this);
	}

	@ManagedOperation(description = "Pause this service")
	public synchronized void pause() throws IllegalStateException {
		if (this.pingSenderHandle == null) {
			// Already paused/stopped
			return;
		}
		this.log.info("Pausing PingService {} ...", this);
		final boolean successfullyCancelled = this.pingSenderHandle
				.cancel(false);
		this.pingSenderHandle = null;
		if (!successfullyCancelled) {
			throw new IllegalStateException("Failed to cancel ping sender");
		}
		this.log.info("PingService {} paused", this);
	}

	@ManagedOperation(description = "Modify this service's ping interval")
	@ManagedOperationParameters(@ManagedOperationParameter(name = "newPingIntervalInSeconds", description = "The new interval between two pings in seconds"))
	public synchronized void reschedule(final int newPingIntervalInSeconds)
			throws IllegalStateException {
		this.log.info("Rescheduling PingSender to new interval [{}] ...",
				newPingIntervalInSeconds);

		pause();
		this.pingIntervalInSeconds = newPingIntervalInSeconds;
		start();

		this.log.info("PingSender rescheduled to new interval [{}]",
				newPingIntervalInSeconds);
	}

	@ManagedOperation(description = "Stop this service")
	public synchronized void stop() {
		if (this.periodicTaskScheduler.isShutdown()) {
			// Already stopped
			return;
		}
		try {
			this.log.info("Stopping PingService {} ...", this);

			pause();
			this.periodicTaskScheduler.shutdown();
			this.periodicTaskScheduler.awaitTermination(2, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			this.log.warn(
					"Caught InterruptedException while in the process of cancelling periodic ping sending: "
							+ e.getMessage(), e);
			Thread.currentThread().interrupt();
		} finally {
			this.periodicTaskScheduler.shutdownNow();

			this.log.info("PingService {} stopped", this);
		}
	}

	@Override
	public String toString() {
		return "PingService@" + hashCode() + "[pingChannels = "
				+ this.pingChannels + "|pingFactory = " + this.pingFactory
				+ "|pingIntervalInSeconds = " + this.pingIntervalInSeconds
				+ "|pingSender = " + this.pingSender + "]";
	}

	private class PingSender implements Runnable {

		@Override
		public void run() {
			final P pingMessage = PingService.this.pingFactory.newPing();
			final ChannelGroupFuture allPingsHaveBeenSent = PingService.this.pingChannels
					.write(pingMessage);
			allPingsHaveBeenSent.addListener(new ChannelGroupFutureListener() {
				@Override
				public void operationComplete(
						final ChannelGroupFuture allPingsSent) throws Exception {
					for (final ChannelFuture pingSent : allPingsSent) {
						if (!pingSent.isSuccess()) {
							PingService.this.log.warn(
									"Failed to send ping via channel "
											+ pingSent.getChannel() + ": "
											+ pingSent.getCause().getMessage(),
									pingSent.getCause());
						} else {
							PingService.this.log.trace(
									"Sent ping via Channel {}",
									pingSent.getChannel());
						}
					}
				}
			});
		}
	}
}
