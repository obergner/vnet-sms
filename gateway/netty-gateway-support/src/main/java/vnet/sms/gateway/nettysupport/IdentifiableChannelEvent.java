/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.ChannelEvent;

/**
 * @author obergner
 * 
 */
public interface IdentifiableChannelEvent extends ChannelEvent {

	UUID getId();

	long getCreationTimestamp();

	Date getCreationTime();
}
