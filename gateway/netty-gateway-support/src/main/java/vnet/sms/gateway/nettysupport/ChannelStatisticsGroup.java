/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Iterator;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;

/**
 * @author obergner
 * 
 */
public class ChannelStatisticsGroup {

	private final ChannelGroup	channels;

	/**
	 * @param channels
	 */
	public ChannelStatisticsGroup(final ChannelGroup channels) {
		notNull(channels, "Argument 'channels' must not be null");
		this.channels = channels;
	}

	/**
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#getName()
	 */
	public String getName() {
		return this.channels.getName();
	}

	/**
	 * @param id
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#find(java.lang.Integer)
	 */
	public Channel find(final Integer id) {
		return this.channels.find(id);
	}

	/**
	 * @return
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		return this.channels.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Set#iterator()
	 */
	public Iterator<ChannelStatistics> iterator() {
		final Iterator<Channel> channelsIterator = this.channels.iterator();
		final Iterator<ChannelStatistics> channelStatisticsIterator = new Iterator<ChannelStatistics>() {

			@Override
			public boolean hasNext() {
				return channelsIterator.hasNext();
			}

			@Override
			public ChannelStatistics next() {
				return new ChannelPipelineBackedChannelStatistics(
				        channelsIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
				        "ChannelStatistics instances may not be removed");

			}
		};
		return channelStatisticsIterator;
	}

	/**
	 * @return
	 * @see java.util.Set#size()
	 */
	public int size() {
		return this.channels.size();
	}

	/**
	 * @return
	 * @see java.util.Set#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.channels.hashCode();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		return this.channels.equals(o);
	}
}
