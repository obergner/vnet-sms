/**
 * 
 */
package vnet.routing.netty.server.support.ping;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * @author obergner
 * 
 */
public interface PingFactory<P extends Serializable> {

	@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER,
			ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface Default {
	}

	P newPing();
}
