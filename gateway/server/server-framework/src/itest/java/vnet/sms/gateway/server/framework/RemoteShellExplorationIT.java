package vnet.sms.gateway.server.framework;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.gateway.server.framework.test.IntegrationTestClient;

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
public class RemoteShellExplorationIT {

	@Value("#{ '${gateway.server.host}' }")
	private String	serverHost;

	@Value("#{ '${gateway.server.port}' }")
	private int	   serverPort;

	@Test
	public final void assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final IntegrationTestClient testClient1 = newClient();
		testClient1.connect();
		testClient1.login(1, "test-client-1", "password");

		final IntegrationTestClient testClient2 = newClient();
		testClient2.connect();
		testClient2.login(2, "test-client-2", "password");

		final IntegrationTestClient testClient3 = newClient();
		testClient3.connect();
		testClient3.login(3, "test-client-3", "password");

		Thread.sleep(600000L);

		testClient3.disconnect();
		testClient2.disconnect();
		testClient1.disconnect();
	}

	private IntegrationTestClient newClient() {
		return new IntegrationTestClient(this.serverHost, this.serverPort);
	}
}
