/**
 * 
 */
package vnet.sms.notcluster.cachewriter.cassandra;

import java.util.Properties;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterFactory;

/**
 * @author obergner
 * 
 */
public class CassandraCacheWriterFactory extends CacheWriterFactory {

	/**
	 * @see net.sf.ehcache.writer.CacheWriterFactory#createCacheWriter(net.sf.ehcache.Ehcache,
	 *      java.util.Properties)
	 */
	@Override
	public CacheWriter createCacheWriter(final Ehcache cache,
	        final Properties properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
