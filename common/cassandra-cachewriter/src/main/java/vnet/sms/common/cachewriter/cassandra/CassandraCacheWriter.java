/**
 * 
 */
package vnet.sms.common.cachewriter.cassandra;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.cachewriter.cassandra.internal.DefaultCacheWriterAnnotations;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.mapping.Mapping;
import com.netflix.astyanax.mapping.MappingCache;
import com.netflix.astyanax.mapping.MappingUtil;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.ObjectSerializer;
import com.netflix.astyanax.serializers.StringSerializer;

/**
 * @author obergner
 * 
 */
public class CassandraCacheWriter implements CacheWriter {

	private final Logger	                log	= LoggerFactory
	                                                    .getLogger(getClass());

	private final AstyanaxContext<Keyspace>	context;

	private CacheWriterImpl	                delegate;

	/**
	 * @param context
	 */
	CassandraCacheWriter(final AstyanaxContext<Keyspace> context) {
		notNull(context, "Argument 'context' must not be null");
		this.context = context;
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#clone(net.sf.ehcache.Ehcache)
	 */
	@Override
	public CacheWriter clone(final Ehcache cache)
	        throws CloneNotSupportedException {
		throw new CloneNotSupportedException(
		        "CassandraCacheWriter cannot be cloned");
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#init()
	 */
	@Override
	public void init() {
		if (this.delegate != null) {
			throw new IllegalStateException(
			        "Illegal attempt to re-initialize already initialized CacheWriter "
			                + this);
		}
		this.log.info("Initializing {}", this);

		this.context.start();
		this.log.info("Started Astyanax Cassandra Context {}", this.context);

		this.delegate = new CacheWriterImpl(this.context.getEntity());

		this.log.info("{} successfully initialized", this);
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#dispose()
	 */
	@Override
	public void dispose() throws CacheException {
		this.log.info("Shutting down {} ...", this);
		this.context.shutdown();
		this.log.info("{} shut down", this);

	}

	private CacheWriterImpl getMandatoryDelegate() {
		checkInitialized();
		return this.delegate;
	}

	private final void checkInitialized() throws IllegalStateException {
		if (this.delegate == null) {
			throw new IllegalStateException(
			        "CacheWriter "
			                + this
			                + " has not yet been initialized - did you remember to call init() before using this CacheWriter instance?");
		}
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#write(net.sf.ehcache.Element)
	 */
	@Override
	public void write(final Element element) throws CacheException {
		getMandatoryDelegate().write(element);
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#writeAll(java.util.Collection)
	 */
	@Override
	public void writeAll(final Collection<Element> elements)
	        throws CacheException {
		getMandatoryDelegate().writeAll(elements);
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#delete(net.sf.ehcache.CacheEntry)
	 */
	@Override
	public void delete(final CacheEntry entry) throws CacheException {
		getMandatoryDelegate().delete(entry);
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#deleteAll(java.util.Collection)
	 */
	@Override
	public void deleteAll(final Collection<CacheEntry> entries)
	        throws CacheException {
		getMandatoryDelegate().deleteAll(entries);
	}

	/**
	 * @see net.sf.ehcache.writer.CacheWriter#throwAway(net.sf.ehcache.Element,
	 *      net.sf.ehcache.writer.writebehind.operations.SingleOperationType,
	 *      java.lang.RuntimeException)
	 */
	@Override
	public void throwAway(final Element element,
	        final SingleOperationType operationType, final RuntimeException e) {
		getMandatoryDelegate().throwAway(element, operationType, e);
	}

	@Override
	public String toString() {
		return "CassandraCacheWriter@" + this.hashCode() + "[context: "
		        + this.context + "|delegate: " + this.delegate + "]";
	}

	private static final class CacheWriterImpl implements CacheWriter {

		private final Logger		                                                        log		           = LoggerFactory
		                                                                                                               .getLogger(getClass());

		private final Keyspace		                                                        keyspace;

		private final MappingUtil		                                                    mapper;

		private final CacheWriterAnnotations<? extends Annotation, ? extends Annotation, ?>	mappingAnnotations	= new DefaultCacheWriterAnnotations();

		CacheWriterImpl(final Keyspace keyspace) {
			this.keyspace = keyspace;
			this.mapper = new MappingUtil(keyspace, new MappingCache(),
			        this.mappingAnnotations);
		}

		@Override
		public CacheWriter clone(final Ehcache cache)
		        throws CloneNotSupportedException {
			throw new CloneNotSupportedException(
			        "CassandraCacheWriter cannot be cloned");
		}

		@Override
		public void init() {
			// Noop
		}

		@Override
		public void dispose() throws CacheException {
		}

		@Override
		public void write(final Element element) throws CacheException {
			try {
				checkElement(element);
				this.log.debug(
				        "Persisting {} to backend Cassandra Keyspace {} ...",
				        new Object[] { element, this.keyspace });
				final ColumnFamily<Object, String> cf = columnFamilyFor(
				        element, this.mappingAnnotations);
				this.mapper.put(cf, element.getObjectValue());
				this.log.debug(
				        "Finished persisting {} to backend Cassandra Keyspace {}",
				        new Object[] { element, this.keyspace });
			} catch (final Exception e) {
				throw new CacheException("Failed to persist " + element
				        + " into keyspace " + this.keyspace + ": "
				        + e.getMessage(), e);
			}
		}

		private void checkElement(final Element element) {
			notNull(element, "Argument 'element' must not be null");
			checkElement(element.getObjectKey(), element.getObjectValue());
		}

		private <T> void checkElement(final Object key, final T value)
		        throws IllegalArgumentException {
			notNull(key, "Argument 'id' must not be null");
			notNull(value, "Argument 'value' must not be null");

			isTrue(value
			        .getClass()
			        .isAnnotationPresent(
			                vnet.sms.common.cachewriter.cassandra.ColumnFamily.class),
			        "Value ["
			                + value
			                + "] is missing mandatory annotation ["
			                + vnet.sms.common.cachewriter.cassandra.ColumnFamily.class
			                        .getName() + "]");

			final Mapping<T> valueMapping = (Mapping<T>) Mapping.make(
			        value.getClass(), this.mappingAnnotations);
			final Object valueKey = valueMapping
			        .getIdValue(value, Object.class);
			isTrue(key.equals(valueKey), "The element id [" + key
			        + "] does not match the value [" + valueKey
			        + "] of the field annotated with @Id");

		}

		private <ID extends Annotation, COLUMN extends Annotation, COLUMNFAMILY extends Annotation> ColumnFamily<Object, String> columnFamilyFor(
		        final Element element,
		        final CacheWriterAnnotations<ID, COLUMN, COLUMNFAMILY> annotations) {
			isTrue(element
			        .getObjectValue()
			        .getClass()
			        .isAnnotationPresent(
			                vnet.sms.common.cachewriter.cassandra.ColumnFamily.class),
			        "Value ["
			                + element.getObjectValue()
			                + "] is missing mandatory annotation ["
			                + vnet.sms.common.cachewriter.cassandra.ColumnFamily.class
			                        .getName() + "]");
			final Object value = element.getObjectValue();
			final COLUMNFAMILY columnFamilyAnnotation = value.getClass()
			        .getAnnotation(annotations.getColumnFamilyAnnotation());
			final String columnFamilyName = annotations.getColumnFamilyName(
			        value.getClass(), columnFamilyAnnotation);

			return new ColumnFamily<Object, String>(columnFamilyName,
			        ObjectSerializer.get(), StringSerializer.get());
		}

		@Override
		public void writeAll(final Collection<Element> elements)
		        throws CacheException {
			this.log.debug(
			        "Persisting {} to backend Cassandra Keyspace {} ...",
			        new Object[] { elements, this.keyspace });
			for (final Element elm : elements) {
				write(elm);
			}
			this.log.debug(
			        "Finished persisting {} to backend Cassandra Keyspace {}",
			        new Object[] { elements, this.keyspace });
		}

		@Override
		public void delete(final CacheEntry entry) throws CacheException {
			try {
				notNull(entry.getElement(),
				        "Can only delete CacheEntry that contains an Element to delete. Got: "
				                + entry);
				checkElement(entry.getElement());
				this.log.debug(
				        "Deleting {} from backend Cassandra Keyspace {} ...",
				        new Object[] { entry, this.keyspace });
				final Element elementToDelete = entry.getElement();
				final ColumnFamily<Object, String> cf = columnFamilyFor(
				        elementToDelete, this.mappingAnnotations);
				this.mapper.remove(cf, elementToDelete.getObjectValue());
				this.log.debug(
				        "Finished deleting {} from backend Cassandra Keyspace {}",
				        new Object[] { entry, this.keyspace });
			} catch (final Exception e) {
				throw new CacheException("Failed to delete " + entry
				        + " from backend keyspace " + this.keyspace + ": "
				        + e.getMessage(), e);
			}
		}

		@Override
		public void deleteAll(final Collection<CacheEntry> entries)
		        throws CacheException {
			this.log.debug(
			        "Deleting {} from backend Cassandra Keyspace {} ...",
			        new Object[] { entries, this.keyspace });
			for (final CacheEntry entry : entries) {
				delete(entry);
			}
			this.log.debug(
			        "Finished deleting {} from backend Cassandra Keyspace {}",
			        new Object[] { entries, this.keyspace });
		}

		@Override
		public void throwAway(final Element element,
		        final SingleOperationType operationType,
		        final RuntimeException e) {
			this.log.error(
			        "THROWING AWAY [" + element + "] after operation ["
			                + operationType + "] repeatedly failed due to ["
			                + e.getMessage() + "]", e);
		}

		@Override
		public String toString() {
			return "CacheWriterImpl@" + this.hashCode() + "[keyspace: "
			        + this.keyspace + "]";
		}
	}
}
