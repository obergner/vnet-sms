/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;

/**
 * @author obergner
 * 
 */
public final class ChannelUtils {

	public static String toString(final Channel channel) {
		final StringBuilder buf = new StringBuilder(128);
		buf.append("\"[");

		final SocketAddress localAddress = channel.getLocalAddress();
		final SocketAddress remoteAddress = channel.getRemoteAddress();
		if (remoteAddress != null) {
			if (channel.getParent() == null) {
				buf.append(localAddress);
				buf.append(" => ");
				buf.append(remoteAddress);
			} else {
				buf.append(remoteAddress);
				buf.append(" => ");
				buf.append(localAddress);
			}
		} else if (localAddress != null) {
			buf.append(localAddress);
		}

		buf.append("]\"");

		return buf.toString();
	}

	private ChannelUtils() {
		// Noop
	}
}
