/**
 * 
 */
package vnet.sms.common.cachewriter.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.Element;

import org.cassandraunit.CassandraUnit;
import org.cassandraunit.dataset.json.ClassPathJsonDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter;
import vnet.sms.common.cachewriter.cassandra.Column;
import vnet.sms.common.cachewriter.cassandra.ColumnFamily;
import vnet.sms.common.cachewriter.cassandra.Id;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.ObjectSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * @author obergner
 * 
 */
public class CassandraCacheWriterIT {

	private static final String	            KEYSPACE_NAME	 = "cassandraCacheWriterIT";

	private static final int	            CASSANDRA_PORT	 = 9171;

	@Rule
	public final CassandraUnit	            cassandraCluster	= new CassandraUnit(
	                                                                 new ClassPathJsonDataSet(
	                                                                         "cassandraCacheWriterIT.json"));

	private final AstyanaxContext<Keyspace>	context	         = new AstyanaxContext.Builder()
	                                                                 .forCluster(
	                                                                         CassandraUnit.clusterName)
	                                                                 .forKeyspace(
	                                                                         KEYSPACE_NAME)
	                                                                 .withAstyanaxConfiguration(
	                                                                         new AstyanaxConfigurationImpl()
	                                                                                 .setDiscoveryType(NodeDiscoveryType.NONE))
	                                                                 .withConnectionPoolConfiguration(
	                                                                         new ConnectionPoolConfigurationImpl(
	                                                                                 "MyConnectionPool")
	                                                                                 .setPort(
	                                                                                         CASSANDRA_PORT)
	                                                                                 .setMaxConnsPerHost(
	                                                                                         1)
	                                                                                 .setSeeds(
	                                                                                         "127.0.0.1:"
	                                                                                                 + CASSANDRA_PORT))
	                                                                 .withConnectionPoolMonitor(
	                                                                         new CountingConnectionPoolMonitor())
	                                                                 .buildKeyspace(
	                                                                         ThriftFamilyFactory
	                                                                                 .getInstance());

	private CassandraCacheWriter	        objectUnderTest;

	@Before
	public void createObjectUnderTest() {
		this.objectUnderTest = new CassandraCacheWriter(this.context);
	}

	public void disposeObjectUnderTest() {
		this.objectUnderTest.dispose();
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#clone(net.sf.ehcache.Ehcache)}
	 * .
	 * 
	 * @throws CloneNotSupportedException
	 */
	@Test(expected = CloneNotSupportedException.class)
	public final void assertThatCloneIsNotSupported()
	        throws CloneNotSupportedException {
		this.objectUnderTest.clone(null);
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#write(net.sf.ehcache.Element)}
	 * .
	 * 
	 * @throws ConnectionException
	 */
	@Test
	public final void assertThatWritePersistsBeanToColumnFamilyNamedInColumnFamilyAnnotation()
	        throws ConnectionException {
		final SimpleTestBean simpleTestBean = new SimpleTestBean(
		        "assertThatWritePersistsBeanToColumnFamilyNamedInColumnFamilyAnnotation",
		        "assertThatWritePersistsBeanToColumnFamilyNamedInColumnFamilyAnnotation");

		this.objectUnderTest.init();
		this.objectUnderTest.write(new Element(simpleTestBean.id,
		        simpleTestBean, 1));

		final Keyspace keyspace = this.context.getEntity();
		final com.netflix.astyanax.model.ColumnFamily<Object, String> cf = new com.netflix.astyanax.model.ColumnFamily<Object, String>(
		        SimpleTestBean.CF_NAME, ObjectSerializer.get(),
		        StringSerializer.get());
		final ColumnList<String> columnList = keyspace.prepareQuery(cf)
		        .getKey(simpleTestBean.id).execute().getResult();
		assertFalse("write(" + simpleTestBean + ") did not persist bean",
		        columnList.isEmpty());
	}

	@ColumnFamily(SimpleTestBean.CF_NAME)
	public static class SimpleTestBean {

		public static final String	CF_NAME	= "simpleTestBean";

		@Id
		public String		       id;

		@Column("fieldOne")
		public String		       fieldOne;

		/**
		 * @param fieldOne
		 */
		SimpleTestBean(final String key, final String fieldOne) {
			this.id = key;
			this.fieldOne = fieldOne;
		}
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#write(net.sf.ehcache.Element)}
	 * .
	 * 
	 * @throws ConnectionException
	 */
	@Test
	public final void assertThatWritePersistsStringFieldToColumnNamedInColumnAnnotation()
	        throws ConnectionException {
		final SimpleTestBean simpleTestBean = new SimpleTestBean(
		        "assertThatWritePersistsStringFieldToColumnNamedInColumnAnnotation",
		        "assertThatWritePersistsStringFieldToColumnNamedInColumnAnnotation");

		this.objectUnderTest.init();
		this.objectUnderTest.write(new Element(simpleTestBean.id,
		        simpleTestBean, 1));

		final Keyspace keyspace = this.context.getEntity();
		final com.netflix.astyanax.model.ColumnFamily<Object, String> cf = new com.netflix.astyanax.model.ColumnFamily<Object, String>(
		        SimpleTestBean.CF_NAME, ObjectSerializer.get(),
		        StringSerializer.get());
		final ColumnList<String> columnList = keyspace.prepareQuery(cf)
		        .getKey(simpleTestBean.id).execute().getResult();
		final String persistedColumnValue = columnList.getStringValue(
		        "fieldOne", null);
		assertEquals(
		        "write("
		                + simpleTestBean
		                + ") did not write String column value to column named in @Column annotation",
		        simpleTestBean.fieldOne, persistedColumnValue);
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#write(net.sf.ehcache.Element)}
	 * .
	 * 
	 * @throws ConnectionException
	 */
	@Test
	public final void assertThatWritePersistsDateField()
	        throws ConnectionException {
		final ComplexTestBean complexTestBean = new ComplexTestBean(
		        "assertThatWritePersistsDateField",
		        "assertThatWritePersistsDateField", new Date(), (byte) 1);

		this.objectUnderTest.init();
		this.objectUnderTest.write(new Element(complexTestBean.id,
		        complexTestBean, 1));

		final Keyspace keyspace = this.context.getEntity();
		final com.netflix.astyanax.model.ColumnFamily<Object, String> cf = new com.netflix.astyanax.model.ColumnFamily<Object, String>(
		        ComplexTestBean.CF_NAME, ObjectSerializer.get(),
		        StringSerializer.get());
		final ColumnList<String> columnList = keyspace.prepareQuery(cf)
		        .getKey(complexTestBean.id).execute().getResult();
		final Date persistedColumnValue = columnList.getDateValue("fieldTwo",
		        null);
		assertEquals(
		        "write("
		                + complexTestBean
		                + ") did not write Date column value to column named in @Column annotation",
		        complexTestBean.fieldTwo, persistedColumnValue);
	}

	@ColumnFamily(ComplexTestBean.CF_NAME)
	public static class ComplexTestBean {

		public static final String	CF_NAME	= "complexTestBean";

		@Id
		public String		       id;

		@Column("stringField")
		public String		       fieldOne;

		@Column
		public Date		           fieldTwo;

		@Column("byte")
		public byte		           b;

		/**
		 * @param id
		 * @param fieldOne
		 * @param fieldTwo
		 * @param byteArray
		 */
		ComplexTestBean(final String id, final String fieldOne,
		        final Date fieldTwo, final byte b) {
			this.id = id;
			this.fieldOne = fieldOne;
			this.fieldTwo = fieldTwo;
			this.b = b;
		}
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#write(net.sf.ehcache.Element)}
	 * .
	 * 
	 * @throws ConnectionException
	 */
	@Test
	public final void assertThatWritePersistsByteField()
	        throws ConnectionException {
		final ComplexTestBean complexTestBean = new ComplexTestBean(
		        "assertThatWritePersistsByteField",
		        "assertThatWritePersistsByteField", new Date(), (byte) 5);

		this.objectUnderTest.init();
		this.objectUnderTest.write(new Element(complexTestBean.id,
		        complexTestBean, 1));

		final Keyspace keyspace = this.context.getEntity();
		final com.netflix.astyanax.model.ColumnFamily<Object, String> cf = new com.netflix.astyanax.model.ColumnFamily<Object, String>(
		        ComplexTestBean.CF_NAME, ObjectSerializer.get(),
		        StringSerializer.get());
		final ColumnList<String> columnList = keyspace.prepareQuery(cf)
		        .getKey(complexTestBean.id).execute().getResult();
		final int persistedColumnValue = columnList.getIntegerValue("byte",
		        null);
		assertEquals(
		        "write("
		                + complexTestBean
		                + ") did not write byte column value to column named in @Column annotation",
		        complexTestBean.b, persistedColumnValue);
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.cachewriter.cassandra.CassandraCacheWriter#delete(net.sf.ehcache.CacheEntry)}
	 * .
	 * 
	 * @throws ConnectionException
	 */
	@Test
	public final void assertThatDeleteRemovesPreviouslyStoredBean()
	        throws ConnectionException {
		final SimpleTestBean simpleTestBean = new SimpleTestBean(
		        "assertThatDeleteRemovesPreviouslyStoredBean",
		        "assertThatDeleteRemovesPreviouslyStoredBean");
		final Element element = new Element(simpleTestBean.id, simpleTestBean,
		        1);
		final CacheEntry cacheEntry = new CacheEntry(element.getObjectKey(),
		        element);

		this.objectUnderTest.init();
		this.objectUnderTest.write(element);
		this.objectUnderTest.delete(cacheEntry);

		final Keyspace keyspace = this.context.getEntity();
		final com.netflix.astyanax.model.ColumnFamily<Object, String> cf = new com.netflix.astyanax.model.ColumnFamily<Object, String>(
		        SimpleTestBean.CF_NAME, ObjectSerializer.get(),
		        StringSerializer.get());
		final ColumnList<String> columnList = keyspace.prepareQuery(cf)
		        .getKey(simpleTestBean.id).execute().getResult();
		assertTrue("delete(" + cacheEntry + ") did not remove bean",
		        columnList.isEmpty());
	}
}
