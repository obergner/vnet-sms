/**
 * 
 */
package vnet.sms.routingengine.core.internal.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * @author obergner
 * 
 */
public class SmsProcessingRoutes extends RouteBuilder {

	@EndpointInject(ref = "vnet.sms.routing-engine.core.incomingMtSmsEp")
	private Endpoint	incomingMtSmsEp;

	/**
	 * @see org.apache.camel.builder.RouteBuilder#configure()
	 */
	@Override
	public void configure() throws Exception {
		// from(this.incomingMtSmsEp).routeId(
		// "vnet.sms.routing-engine.core.receivedSmsRoutes").process(bean(""));
	}

}
