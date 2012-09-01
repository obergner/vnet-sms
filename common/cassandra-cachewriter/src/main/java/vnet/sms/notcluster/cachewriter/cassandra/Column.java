/**
 * 
 */
package vnet.sms.notcluster.cachewriter.cassandra;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author obergner
 * 
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * The name by which this particular field should be persisted as. By
	 * default, the name of the field is used
	 * 
	 * @return column name
	 */
	String value() default "";
}
