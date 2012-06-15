package vnet.sms.gateway.server.framework.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.DisposableBean;

public class IntegrationTestClientFactory implements DisposableBean {

	private final ConcurrentMap<String, IntegrationTestClient>	namedClients	= new ConcurrentHashMap<String, IntegrationTestClient>();

	private final String	                                   host;

	private final int	                                       port;

	public IntegrationTestClientFactory(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public IntegrationTestClient clientNamed(final String name) {
		if (!this.namedClients.containsKey(name)) {
			final IntegrationTestClient result = new IntegrationTestClient(
			        this.host, this.port);
			this.namedClients.putIfAbsent(name, result);
		}
		return this.namedClients.get(name);
	}

	@Override
	public void destroy() throws Exception {
		for (final Map.Entry<String, IntegrationTestClient> namedClient : this.namedClients
		        .entrySet()) {
			namedClient.getValue().disconnect();
		}
	}
}
