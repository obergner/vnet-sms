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
 * This is a marker annotation for the field that should act as the ID column
 * 
 * @author obergner
 * 
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}
