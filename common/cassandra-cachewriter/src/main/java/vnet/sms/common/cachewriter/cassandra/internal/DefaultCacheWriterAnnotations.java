/**
 * 
 */
package vnet.sms.common.cachewriter.cassandra.internal;

import java.lang.reflect.Field;

import vnet.sms.common.cachewriter.cassandra.CacheWriterAnnotations;
import vnet.sms.common.cachewriter.cassandra.Column;
import vnet.sms.common.cachewriter.cassandra.ColumnFamily;
import vnet.sms.common.cachewriter.cassandra.Id;

/**
 * @author obergner
 * 
 */
public class DefaultCacheWriterAnnotations implements
        CacheWriterAnnotations<Id, Column, ColumnFamily> {

	/**
	 * @see com.netflix.astyanax.mapping.AnnotationSet#getIdAnnotation()
	 */
	@Override
	public Class<Id> getIdAnnotation() {
		return Id.class;
	}

	/**
	 * @see com.netflix.astyanax.mapping.AnnotationSet#getColumnAnnotation()
	 */
	@Override
	public Class<Column> getColumnAnnotation() {
		return Column.class;
	}

	/**
	 * @see com.netflix.astyanax.mapping.AnnotationSet#getIdName(java.lang.reflect.Field,
	 *      java.lang.annotation.Annotation)
	 */
	@Override
	public String getIdName(final Field field, final Id annotation) {
		return field.getName();
	}

	/**
	 * @see com.netflix.astyanax.mapping.AnnotationSet#getColumnName(java.lang.reflect.Field,
	 *      java.lang.annotation.Annotation)
	 */
	@Override
	public String getColumnName(final Field field, final Column annotation) {
		final String name = annotation.value();
		return (name.length() > 0) ? name : field.getName();
	}

	@Override
	public Class<ColumnFamily> getColumnFamilyAnnotation() {
		return ColumnFamily.class;
	}

	/**
	 * @see vnet.sms.common.cachewriter.cassandra.CacheWriterAnnotations#getColumnFamilyName(java.lang.Class,
	 *      java.lang.annotation.Annotation)
	 */
	@Override
	public String getColumnFamilyName(final Class<?> type,
	        final ColumnFamily annotation) {
		final String name = annotation.value();
		return (name.length() > 0) ? name : type.getSimpleName();
	}

}
