package vnet.sms.gateway.server.framework.test;

import javax.jms.Message;

public interface MessagePredicate {

	boolean evaluate(Message message);
}
