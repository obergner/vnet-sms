/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.outgoing.spring;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.group.ChannelGroup;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import vnet.sms.gateway.nettysupport.publish.outgoing.DefaultOutgoingMessagesSender;
import vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesSenderFactory<ID extends Serializable> implements
        FactoryBean<OutgoingMessagesSender<ID>>, DisposableBean,
        InitializingBean {

	// ------------------------------------------------------------------------
	// Instance
	// ------------------------------------------------------------------------

	private ChannelGroup	                            allConnectedChannels;

	private final List<OutgoingMessagesSender.Listener>	listeners	= new ArrayList<OutgoingMessagesSender.Listener>();

	private OutgoingMessagesSender<ID>	                product;

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/**
	 * @param allConnectedChannels
	 *            the allConnectedChannels to set
	 */
	@Required
	public final void setAllConnectedChannels(
	        final ChannelGroup allConnectedChannels) {
		notNull(allConnectedChannels,
		        "Argument 'allConnectedChannels' must not be null");
		this.allConnectedChannels = allConnectedChannels;
	}

	/**
	 * @param listeners
	 *            the listeners to set
	 */
	public final void setListeners(
	        final List<OutgoingMessagesSender.Listener> listeners) {
		notNull(listeners, "Argument 'listeners' must not be null");
		this.listeners.clear();
		this.listeners.addAll(listeners);
	}

	// ------------------------------------------------------------------------
	// InitializingBean
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		checkState();
		final OutgoingMessagesSender<ID> sender = new DefaultOutgoingMessagesSender<ID>(
		        this.allConnectedChannels);
		for (final OutgoingMessagesSender.Listener listener : this.listeners) {
			sender.addListener(listener);
		}
		this.product = sender;
	}

	private void checkState() throws IllegalStateException {
		if (this.product != null) {
			throw new IllegalStateException(
			        "Illegal attempt to build OutgoingMessagesSender more than once");
		}
		if (this.allConnectedChannels == null) {
			throw new IllegalStateException("No ChannelGroup has been set");
		}
	}

	// ------------------------------------------------------------------------
	// FactoryBean
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public OutgoingMessagesSender<ID> getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No OutgoingMessagesSender has been built yet - did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		}
		return this.product;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass()
		        : OutgoingMessagesSender.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	// ------------------------------------------------------------------------
	// DisposableBean
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No OutgoingMessagesSender has been built yet - did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		}
		this.product.close();
	}
}
