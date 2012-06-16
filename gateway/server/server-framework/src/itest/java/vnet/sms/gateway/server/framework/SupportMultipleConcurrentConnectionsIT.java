package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
import vnet.sms.gateway.server.framework.test.IntegrationTestClientFactory;
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
public class SupportMultipleConcurrentConnectionsIT {

	@Autowired
	private IntegrationTestClientFactory	testClientFactory;

	@Test
	public final void assertThatGatewayServerRespondsWithSuccessfulLoginResponsesToMultipleConcurrentLoginRequests()
	        throws Throwable {
		ExecutorService exec = null;
		try {
			final int numberOfConcurrentLogins = 30;

			final CyclicBarrier allClientsConnected = new CyclicBarrier(
			        numberOfConcurrentLogins);

			final List<LoginSender> loginSenders = new ArrayList<LoginSender>();
			for (int i = 1; i <= numberOfConcurrentLogins; i++) {
				final LoginSender loginSender = new LoginSender(
				        allClientsConnected,
				        this.testClientFactory
				                .clientNamed("assertThatGatewayServerRespondsWithSuccessfulLoginResponsesToMultipleConcurrentLoginRequests:"
				                        + i), i);
				loginSenders.add(loginSender);
			}

			exec = Executors.newFixedThreadPool(numberOfConcurrentLogins);
			final CompletionService<ReferenceableMessageContainer> completionService = new ExecutorCompletionService<ReferenceableMessageContainer>(
			        exec);
			for (final LoginSender loginSender : loginSenders) {
				completionService.submit(loginSender);
			}

			for (int i = 1; i <= numberOfConcurrentLogins; i++) {
				final Future<ReferenceableMessageContainer> loginResponseFuture = completionService
				        .take();
				final LoginResponse loginResponse = LoginResponse.class
				        .cast(loginResponseFuture.get().getMessage());
				assertTrue(
				        "GatewayServer should have returned a SUCCESSFUL LoginResponse",
				        loginResponse.loginSucceeded());
			}
		} finally {
			if (exec != null) {
				exec.shutdown();
			}
		}
	}

	private static class LoginSender implements
	        Callable<ReferenceableMessageContainer> {

		private final CyclicBarrier		    waitBarrier;

		private final IntegrationTestClient	testClient;

		private final int		            messageReference;

		LoginSender(final CyclicBarrier waitBarrier,
		        final IntegrationTestClient testClient,
		        final int messageReference) {
			this.waitBarrier = waitBarrier;
			this.testClient = testClient;
			this.messageReference = messageReference;
		}

		@Override
		public ReferenceableMessageContainer call() throws Exception {
			try {
				this.testClient.connect(true);

				this.waitBarrier.await();

				final LoginRequest successfulLoginRequest = new LoginRequest(
				        "assertThatGatewayServerRespondsWithSuccessfulLoginResponsesToMultipleConcurrentLoginRequests:"
				                + this.messageReference, "whatever");
				final ReferenceableMessageContainer loginResponse = this.testClient
				        .sendMessageAndWaitForResponse(this.messageReference,
				                successfulLoginRequest);

				this.waitBarrier.await();

				return loginResponse;
			} catch (final Throwable e) {
				throw new RuntimeException(e);
			} finally {
				this.testClient.disconnect();
			}
		}

	}
}
