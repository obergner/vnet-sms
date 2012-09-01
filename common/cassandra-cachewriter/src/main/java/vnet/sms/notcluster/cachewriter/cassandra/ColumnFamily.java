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
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnFamily {
	/**
	 * The name of the Cassandra ColumnFamily this type should be persisted to.
	 * By default, the type's simple name is used.
	 * 
	 * @return column name
	 */
	String value() default "";
}
