/**
 * 
 */
package vnet.sms.gateway.server.framework;

/**
 * @author obergner
 * 
 */
public final class Context {

	public static final String	 APPLICATION_CONTEXT	        = "classpath:META-INF/services/gateway-server-application-context.xml";

	public static final String	 AUTHENTICATION_MANAGER_CONTEXT	= "classpath:META-INF/services/gateway-server-authentication-manager-context.xml";

	public static final String	 JMS_CLIENT_CONTEXT	            = "classpath:META-INF/services/gateway-server-jms-client-context.xml";

	public static final String	 MODULE_CONTEXTS	            = "classpath*:META-INF/module/module-context.xml";

	public static final String[]	ALL_CONTEXTS	            = new String[] {
	        APPLICATION_CONTEXT, AUTHENTICATION_MANAGER_CONTEXT,
	        JMS_CLIENT_CONTEXT, MODULE_CONTEXTS	            };

	private Context() {
		// Noop
	}
}
