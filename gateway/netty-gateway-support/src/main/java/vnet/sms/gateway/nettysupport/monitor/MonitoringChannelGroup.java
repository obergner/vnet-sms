/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;

/**
 * @author obergner
 * 
 */
public class MonitoringChannelGroup implements ChannelGroup {

	private final ChannelGroup	         delegate;

	private final ManagedChannelRegistry	channelMonitorRegistry	= new ManagedChannelRegistry();

	public MonitoringChannelGroup() {
		this.delegate = new DefaultChannelGroup();
	}

	/**
	 * @param name
	 */
	public MonitoringChannelGroup(final String name) {
		notEmpty(name, "Argument 'name' must not be null");
		this.delegate = new DefaultChannelGroup(name);
	}

	/**
	 * @param channel
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	@Override
	public boolean add(final Channel channel) {
		this.channelMonitorRegistry.registerChannel(channel);
		return this.delegate.add(channel);
	}

	/**
	 * @param channels
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends Channel> channels) {
		notNull(channels, "Argument 'channels' must not be null");
		for (final Channel channel : channels) {
			this.channelMonitorRegistry.registerChannel(channel);
		}
		return this.delegate.addAll(channels);
	}

	/**
	 * 
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear() {
		for (final Channel channel : this) {
			this.channelMonitorRegistry.unregisterChannel(channel);
		}
		this.delegate.clear();
	}

	/**
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#close()
	 */
	@Override
	public ChannelGroupFuture close() {
		return this.delegate.close();
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final ChannelGroup arg0) {
		return this.delegate.compareTo(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object arg0) {
		return this.delegate.contains(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		return this.delegate.containsAll(arg0);
	}

	/**
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#disconnect()
	 */
	@Override
	public ChannelGroupFuture disconnect() {
		return this.delegate.disconnect();
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object arg0) {
		return this.delegate.equals(arg0);
	}

	/**
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#getName()
	 */
	@Override
	public String getName() {
		return this.delegate.getName();
	}

	/**
	 * @param id
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#find(java.lang.Integer)
	 */
	@Override
	public Channel find(final Integer id) {
		return this.delegate.find(id);
	}

	/**
	 * @return
	 * @see java.util.Set#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.delegate.hashCode();
	}

	/**
	 * @return
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<Channel> iterator() {
		return this.delegate.iterator();
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object arg0) {
		if (arg0 instanceof Channel) {
			this.channelMonitorRegistry.unregisterChannel(Channel.class
			        .cast(arg0));
		}
		return this.delegate.remove(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		for (final Object obj : arg0) {
			if (obj instanceof Channel) {
				this.channelMonitorRegistry.unregisterChannel(Channel.class
				        .cast(obj));
			}
		}
		return this.delegate.removeAll(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		for (final Object obj : this) {
			if ((obj instanceof Channel) && !contains(obj)) {
				this.channelMonitorRegistry.unregisterChannel(Channel.class
				        .cast(obj));
			}
		}
		return this.delegate.retainAll(arg0);
	}

	/**
	 * @param interestOps
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#setInterestOps(int)
	 */
	@Override
	public ChannelGroupFuture setInterestOps(final int interestOps) {
		return this.delegate.setInterestOps(interestOps);
	}

	/**
	 * @param readable
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#setReadable(boolean)
	 */
	@Override
	public ChannelGroupFuture setReadable(final boolean readable) {
		return this.delegate.setReadable(readable);
	}

	/**
	 * @return
	 * @see java.util.Set#size()
	 */
	@Override
	public int size() {
		return this.delegate.size();
	}

	/**
	 * @return
	 * @see java.util.Set#toArray()
	 */
	@Override
	public Object[] toArray() {
		return this.delegate.toArray();
	}

	/**
	 * @param <T>
	 * @param arg0
	 * @return
	 * @see java.util.Set#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		return this.delegate.toArray(arg0);
	}

	/**
	 * @param message
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#write(java.lang.Object)
	 */
	@Override
	public ChannelGroupFuture write(final Object message) {
		return this.delegate.write(message);
	}

	/**
	 * @param message
	 * @param remoteAddress
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#write(java.lang.Object,
	 *      java.net.SocketAddress)
	 */
	@Override
	public ChannelGroupFuture write(final Object message,
	        final SocketAddress remoteAddress) {
		return this.delegate.write(message, remoteAddress);
	}

	/**
	 * @return
	 * @see org.jboss.netty.channel.group.ChannelGroup#unbind()
	 */
	@Override
	public ChannelGroupFuture unbind() {
		return this.delegate.unbind();
	}

}
