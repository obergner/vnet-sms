package vnet.sms.routingengine.jmsbroker.internal.broker;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import vnet.sms.routingengine.blueprint.test.BlueprintTestSupport;

public class ActiveMQBrokerContextIT extends BlueprintTestSupport {

	@Override
	protected String getBlueprintDescriptor() {
		return "OSGI-INF/blueprint/routing-engine-broker.xml";
	}

	@Before
	public void configureActiveMQBroker() throws IOException,
	        InvalidSyntaxException {
		getLog().info("Updating JMS broker configuration ...");
		final ConfigurationAdmin ca = getOsgiService(ConfigurationAdmin.class);
		final Configuration jmsBroker = ca.getConfiguration(
		        "vnet.sms.routing_engine.jms_broker", getTestBundleDescriptor()
		                .getUrl().toExternalForm());
		final Properties props = new Properties();
		props.put("routing-engine.activemq.host", "localhost");
		props.put("routing-engine.activemq.openwire.port", "44444");
		props.put("routing-engine.activemq.stomp.port", "55555");
		props.put("routing-engine.activemq.queue.incomingMtSms",
		        "QUEUE.INCOMING_MT_SMS");
		props.put("routing-engine.activemq.queue.outgoingMtSmsAck",
		        "QUEUE.OUTGOING_MT_SMS_ACK");
		props.put("routing-engine.activemq.queue.outgoingMtSmsNack",
		        "QUEUE.OUTGOING_MT_SMS_NACK");
		props.put("routing-engine.activemq.queue.outgoingMoSms",
		        "QUEUE.OUTGOING_MO_SMS");
		jmsBroker.setBundleLocation(null);
		jmsBroker.update(props);
		getLog().info("Updated JMS broker configuration -> " + props);
	}

	@Test
	public void assertThatBlueprintContainerExportsJmsConnectionFactory()
	        throws Exception {
		final ConnectionFactory exportedActiveMQConnectionFactory = getOsgiService(
		        ConnectionFactory.class,
		        "(name=vnet.sms.routing-engine.jms-broker)");
		assertNotNull(
		        "Blueprint container does not export ActiveMQ connection factory defined in blueprint context",
		        exportedActiveMQConnectionFactory);
	}
}
