/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import java.util.concurrent.Future;

import org.jboss.netty.channel.MessageEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
public interface MessageEvents extends Iterable<MessageEvent> {

	boolean isEmpty();

	MessageEvent nextMessageEvent();

	MessageEvent nextMatchingMessageEvent(Predicate<MessageEvent> predicate);

	MessageEvent[] allMessageEvents();

	Future<MessageEvent> waitForMatchingMessageEvent(
	        Predicate<MessageEvent> predicate);

	TimedFuture<MessageEvent> timedWaitForMatchingMessageEvent(
	        Predicate<MessageEvent> predicate);
}
