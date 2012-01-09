/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

/**
 * @author obergner
 * 
 */
public interface MonitoredChannel {

	void addMonitor(ChannelMonitor monitor);

	void removeMonitor(ChannelMonitor monitor);

	void clearMonitors();
}
