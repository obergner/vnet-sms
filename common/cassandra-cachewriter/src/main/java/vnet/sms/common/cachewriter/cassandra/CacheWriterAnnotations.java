/**
 * 
 */
package vnet.sms.common.cachewriter.cassandra;

import java.lang.annotation.Annotation;

import com.netflix.astyanax.mapping.AnnotationSet;

/**
 * @author obergner
 * 
 */
public interface CacheWriterAnnotations<ID extends Annotation, COLUMN extends Annotation, COLUMNFAMILY extends Annotation>
        extends AnnotationSet<ID, COLUMN> {

	Class<COLUMNFAMILY> getColumnFamilyAnnotation();

	String getColumnFamilyName(Class<?> type, COLUMNFAMILY annotation);
}
