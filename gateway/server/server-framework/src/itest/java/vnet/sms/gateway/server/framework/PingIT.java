package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
import vnet.sms.gateway.server.framework.test.MessageEventPredicate;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration({
        "classpath:META-INF/services/gateway-server-application-context.xml",
        "classpath:META-INF/services/gateway-server-authentication-manager-context.xml",
        "classpath:META-INF/services/gateway-server-jms-client-context.xml",
        "classpath:META-INF/services/gateway-server-shell-context.xml",
        "classpath*:META-INF/module/module-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-embedded-activemq-broker-context.xml",
        "classpath:META-INF/itest/itest-serialization-transport-plugin-context.xml",
        "classpath:META-INF/itest/itest-test-client-context.xml",
        "classpath:META-INF/itest/itest-test-jms-listener-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-description-context.xml" })
public class PingIT {

	@Autowired
	private IntegrationTestClient	testClient;

	@Value("#{ '${gateway.server.pingIntervalSeconds}' }")
	private long	              pingIntervalSeconds;

	@Test
	public final void assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed()
	        throws Throwable {
		final MessageEventPredicate matchesPingEvent = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				return ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest;
			}
		};
		this.testClient.connect();

		// Login starts ping timeout
		this.testClient
		        .login(1,
		                "assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed",
		                "whatever");

		final CountDownLatch pingReceived = this.testClient
		        .listen(matchesPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));
		this.testClient.disconnect();
	}

	@Test
	public final void assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse()
	        throws Throwable {
		final MessageEventPredicate matchesReceivedPingEvent = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				return ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest;
			}
		};
		this.testClient.connect();

		// Login starts ping timeout
		this.testClient
		        .login(1,
		                "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		                "whatever");

		final CountDownLatch pingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));
		this.testClient.disconnect();

		// Should start ping timeout
		this.testClient.connect();

		// Login. Otherwise, our LoginResponse will be discarded
		this.testClient
		        .login(1,
		                "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		                "whatever");

		final CountDownLatch firstPingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        firstPingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));

		// Send ping response
		final CountDownLatch secondPingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		this.testClient.sendMessage(2, PingResponse.accept(new PingRequest()));

		assertTrue(
		        "Expected to receive  second Ping after sending PingResponse and waiting for "
		                + this.pingIntervalSeconds + " seconds",
		        secondPingReceived.await(
		                this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));

		this.testClient.disconnect();
	}
}
