/**
 * 
 */
package vnet.sms.common.cachewriter.cassandra;

import java.util.Properties;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * @author obergner
 * 
 */
public class CassandraCacheWriterFactory extends CacheWriterFactory {

	public static final String	CLUSTER_NAME_DEFAULT	        = "TestCluster";

	public static final String	KEYSPACE_NAME_DEFAULT	        = "DEFAULT";

	public static final String	CASSANDRA_HOST_DEFAULT	        = "127.0.0.1";

	public static final int	   CASSANDRA_PORT_DEFAULT	        = 9161;

	public static final String	CASSANDRA_SEEDS_DEFAULT	        = CASSANDRA_HOST_DEFAULT
	                                                                    + ":"
	                                                                    + CASSANDRA_PORT_DEFAULT;

	public static final int	   MAX_CONNECTIONS_PER_HOST_DEFAULT	= 1;

	public static final String	CONNECTION_POOL_NAME_DEFAULT	= "CassandraCacheWriter-Pool";

	// ------------------------------------------------------------------------------------------------------------------------------

	public static final String	CLUSTER_NAME_PROP	            = "vnet.sms.common.cachewriter.cassandra.clusterName";

	public static final String	KEYSPACE_NAME_PROP	            = "vnet.sms.common.cachewriter.cassandra.keyspaceName";

	public static final String	CASSANDRA_HOST_PROP	            = "vnet.sms.common.cachewriter.cassandra.cassandraHost";

	public static final String	CASSANDRA_PORT_PROP	            = "vnet.sms.common.cachewriter.cassandra.cassandraPort";

	public static final String	CASSANDRA_SEEDS_PROP	        = "vnet.sms.common.cachewriter.cassandra.cassandraSeeds";

	public static final String	MAX_CONNECTIONS_PER_HOST_PROP	= "vnet.sms.common.cachewriter.cassandra.maxConnectionsPerHost";

	public static final String	CONNECTION_POOL_NAME_PROP	    = "vnet.sms.common.cachewriter.cassandra.connectionPoolName";

	private final Logger	   log	                            = LoggerFactory
	                                                                    .getLogger(getClass());

	/**
	 * @see net.sf.ehcache.writer.CacheWriterFactory#createCacheWriter(net.sf.ehcache.Ehcache,
	 *      java.util.Properties)
	 */
	@Override
	public CacheWriter createCacheWriter(final Ehcache cache,
	        final Properties properties) {
		this.log.info(
		        "Creating new CassandraCacheWriter for Ehcache {} using configuration properties {} ...",
		        cache, properties);
		final CassandraConfig config = CassandraConfig
		        .readFromProperties(properties);
		this.log.debug("Configuration properties resolved to {}", config);
		final AstyanaxContext<Keyspace> context = buildAstyanaxContext(config);
		final CassandraCacheWriter cassandraCacheWriter = new CassandraCacheWriter(
		        context);
		this.log.info("Created new CassandraCacheWriter {} for Ehcache {}",
		        cassandraCacheWriter, cache);
		return cassandraCacheWriter;
	}

	private AstyanaxContext<Keyspace> buildAstyanaxContext(
	        final CassandraConfig config) {
		final AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
		        .forCluster(config.clusterName)
		        .forKeyspace(config.keyspaceName)
		        .withAstyanaxConfiguration(
		                new AstyanaxConfigurationImpl()
		                        .setDiscoveryType(NodeDiscoveryType.NONE))
		        .withConnectionPoolConfiguration(
		                new ConnectionPoolConfigurationImpl(
		                        config.connectionPoolName)
		                        .setPort(config.cassandraPort)
		                        .setMaxConnsPerHost(
		                                config.maxConnectionsPerHost)
		                        .setSeeds(config.cassandraSeeds))
		        .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
		        .buildKeyspace(ThriftFamilyFactory.getInstance());

		return context;
	}

	private static final class CassandraConfig {

		static CassandraConfig readFromProperties(final Properties properties) {
			final String clusterName = properties.getProperty(
			        CLUSTER_NAME_PROP, CLUSTER_NAME_DEFAULT);
			final String keyspaceName = properties.getProperty(
			        KEYSPACE_NAME_PROP, KEYSPACE_NAME_DEFAULT);
			final String cassandraHost = properties.getProperty(
			        CASSANDRA_HOST_PROP, CASSANDRA_HOST_DEFAULT);
			final String portStr = properties.getProperty(CASSANDRA_PORT_PROP);
			final int cassandraPort = portStr != null ? Integer
			        .parseInt(portStr) : CASSANDRA_PORT_DEFAULT;
			final String cassandraSeeds = properties.getProperty(
			        CASSANDRA_SEEDS_PROP, CASSANDRA_SEEDS_DEFAULT);
			final String maxConnsStr = properties
			        .getProperty(MAX_CONNECTIONS_PER_HOST_PROP);
			final int maxConnectionsPerHost = maxConnsStr != null ? Integer
			        .parseInt(maxConnsStr) : MAX_CONNECTIONS_PER_HOST_DEFAULT;
			final String connectionPoolName = properties.getProperty(
			        CONNECTION_POOL_NAME_PROP, CONNECTION_POOL_NAME_DEFAULT);

			return new CassandraConfig(clusterName, keyspaceName,
			        cassandraHost, cassandraPort, cassandraSeeds,
			        maxConnectionsPerHost, connectionPoolName);
		}

		final String	clusterName;

		final String	keyspaceName;

		final String	cassandraHost;

		final int		cassandraPort;

		final String	cassandraSeeds;

		final int		maxConnectionsPerHost;

		final String	connectionPoolName;

		private CassandraConfig(final String clusterName,
		        final String keyspaceName, final String cassandraHost,
		        final int cassandraPort, final String cassandraSeeds,
		        final int maxConnectionsPerHost, final String connectionPoolName) {
			this.clusterName = clusterName;
			this.keyspaceName = keyspaceName;
			this.cassandraHost = cassandraHost;
			this.cassandraPort = cassandraPort;
			this.cassandraSeeds = cassandraSeeds;
			this.maxConnectionsPerHost = maxConnectionsPerHost;
			this.connectionPoolName = connectionPoolName;
		}

		@Override
		public String toString() {
			return "CassandraConfig@" + this.hashCode() + "[clusterName: "
			        + this.clusterName + "|keyspaceName: " + this.keyspaceName
			        + "|cassandraHost: " + this.cassandraHost
			        + "|cassandraPort: " + this.cassandraPort
			        + "|cassandraSeeds: " + this.cassandraSeeds
			        + "|maxConnectionsPerHost: " + this.maxConnectionsPerHost
			        + "|connectionPoolName: " + this.connectionPoolName + "]";
		}
	}
}
