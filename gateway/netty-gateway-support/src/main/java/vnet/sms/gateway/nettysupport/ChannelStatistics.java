package vnet.sms.gateway.nettysupport;

import java.net.SocketAddress;
import java.util.Date;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;

/**
 * @author obergner
 * 
 */
public interface ChannelStatistics {

	/**
	 * @return
	 */
	Gauge<Integer> getId();

	/**
	 * @return
	 */
	Gauge<Date> getConnectedSince();

	/**
	 * @return
	 */
	Gauge<SocketAddress> getLocalAddress();

	/**
	 * @return
	 */
	Gauge<SocketAddress> getRemoteAddress();

	/**
	 * @return
	 */
	Gauge<Integer> getConnectTimeoutMillis();

	/**
	 * @return the numberOfReceivedBytes
	 */
	Histogram getNumberOfReceivedBytes();

	/**
	 * @return the totalNumberOfReceivedBytes
	 */
	Counter getTotalNumberOfReceivedBytes();

	/**
	 * @return the numberOfReceivedPdus
	 */
	Meter getNumberOfReceivedPdus();

	/**
	 * @return the numberOfReceivedLoginRequests
	 */
	Meter getNumberOfReceivedLoginRequests();

	/**
	 * @return the numberOfReceivedLoginResponses
	 */
	Meter getNumberOfReceivedLoginResponses();

	/**
	 * @return the numberOfReceivedPingRequests
	 */
	Meter getNumberOfReceivedPingRequests();

	/**
	 * @return the numberOfReceivedPingResponses
	 */
	Meter getNumberOfReceivedPingResponses();

	/**
	 * @return the numberOfReceivedSms
	 */
	Meter getNumberOfReceivedSms();

	/**
	 * @return the numberOfAcceptedLoginRequests
	 */
	Meter getNumberOfAcceptedLoginRequests();

	/**
	 * @return the numberOfRejectedLoginRequests
	 */
	Meter getNumberOfRejectedLoginRequests();

	/**
	 * @return the numberOfSentBytes
	 */
	Histogram getNumberOfSentBytes();

	/**
	 * @return the totalNumberOfSentBytes
	 */
	Counter getTotalNumberOfSentBytes();

	/**
	 * @return the numberOfSentPdus
	 */
	Meter getNumberOfSentPdus();

	/**
	 * @return the numberOfSentPingRequests
	 */
	Meter getNumberOfSentPingRequests();

}
