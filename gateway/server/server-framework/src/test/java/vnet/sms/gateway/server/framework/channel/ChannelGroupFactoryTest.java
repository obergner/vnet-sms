package vnet.sms.gateway.server.framework.channel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.group.ChannelGroup;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;

import vnet.sms.gateway.server.framework.internal.channel.ChannelGroupFactory;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;
import vnet.sms.gateway.server.framework.spi.Version;

import com.yammer.metrics.Metrics;

public class ChannelGroupFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetGatewayServerDescriptionRejectsNullDescription() {
		new ChannelGroupFactory().setGatewayServerDescription(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetMBeanExportOperationsRejectsNullMBeanExportOperations() {
		new ChannelGroupFactory().setMBeanExportOperations(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetMetricsRegistryRejectsNullMetricsRegistry() {
		new ChannelGroupFactory().setMetricsRegistry(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectThrowsIllegalStateExceptionIfAfterPropertiesSetHasNotBeenCalledBefore()
	        throws Exception {
		final ChannelGroupFactory objectUnderTest = new ChannelGroupFactory();
		objectUnderTest.getObject();
	}

	@Test
	public final void assertThatGetObjectTypeReturnsTypeCompatibleWithGatewayServerChannelPipelineFactory() {
		final ChannelGroupFactory objectUnderTest = new ChannelGroupFactory();
		final Class<?> objectType = objectUnderTest.getObjectType();

		assertTrue(
		        "getObjectType() should have returned a type that is compatible with "
		                + ChannelGroup.class + ", yet it didn't",
		        ChannelGroup.class.isAssignableFrom(objectType));
	}

	@Test
	public final void assertThatIsSingletonReturnsTrue() {
		final ChannelGroupFactory objectUnderTest = new ChannelGroupFactory();

		assertTrue(
		        "isSingleton() should have returned true since a ChannelGroup is a singleton",
		        objectUnderTest.isSingleton());
	}

	@Test
	public final void assertThatAfterPropertiesSetProperlyConstructsAChannelGroupIfBuilderIsCorrectlyConfigured()
	        throws Exception {
		final ChannelGroupFactory objectUnderTest = new ChannelGroupFactory();
		objectUnderTest
		        .setGatewayServerDescription(new GatewayServerDescription(
		                "TEST", new Version(1, 2, 3, "TEST", 66)));
		objectUnderTest.setMBeanExportOperations(new MBeanExporter());
		objectUnderTest.setMetricsRegistry(Metrics.defaultRegistry());
		objectUnderTest.afterPropertiesSet();

		final ChannelGroup product = objectUnderTest.getObject();

		assertNotNull(
		        "afterPropertiesSet() should have built a proper ChannelGroup",
		        product);
	}
}
