package vnet.sms.gateway.nettysupport.publish.outgoing.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.junit.Test;

import vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender;

public class OutgoingMessagesSenderFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetAllConnectedChannelsRejectsNullChannelGroup() {
		new OutgoingMessagesSenderFactory<Serializable>()
		        .setAllConnectedChannels(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetListenersRejectsNullListenersList() {
		new OutgoingMessagesSenderFactory<Serializable>().setListeners(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatAfterPropertiesSetRecognizesMissingChannelGroup()
	        throws Exception {
		new OutgoingMessagesSenderFactory<Serializable>().afterPropertiesSet();
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatAfterPropertiesSetRefusesToBeCalledTwice()
	        throws Exception {
		final OutgoingMessagesSenderFactory<Serializable> objectUnderTest = new OutgoingMessagesSenderFactory<Serializable>();
		objectUnderTest.setAllConnectedChannels(new DefaultChannelGroup(
		        "assertThatAfterPropertiesSetRefusesToBeCalledTwice"));

		objectUnderTest.afterPropertiesSet();
		objectUnderTest.afterPropertiesSet();
	}

	@Test
	public final void assertThatAfterPropertiesSetConstructsNonNullOutgoingMessagesSender()
	        throws Exception {
		final OutgoingMessagesSenderFactory<Serializable> objectUnderTest = new OutgoingMessagesSenderFactory<Serializable>();
		objectUnderTest.setAllConnectedChannels(new DefaultChannelGroup(
		        "assertThatAfterPropertiesSetRefusesToBeCalledTwice"));

		objectUnderTest.afterPropertiesSet();

		assertNotNull(
		        "afterPropertiesSet() should have produced a non-null OutgoingMessagesSender",
		        objectUnderTest.getObject());
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectRefusesToBeCalledPriorToAfterPropertiesSet()
	        throws Exception {
		new OutgoingMessagesSenderFactory<Serializable>().getObject();
	}

	@Test
	public final void assertThatGetObjectTypeReturnsSubclassOfOutgoingMessagesSender() {
		final Class<?> objectType = new OutgoingMessagesSenderFactory<Serializable>()
		        .getObjectType();

		assertTrue("getObjectType() did not return subtype of "
		        + OutgoingMessagesSender.class.getName(),
		        OutgoingMessagesSender.class.isAssignableFrom(objectType));
	}

	@Test
	public final void assertThatIsSingletonReturnsTrue() {
		assertTrue("isSingleton() should return true",
		        new OutgoingMessagesSenderFactory<Serializable>().isSingleton());
	}
}
