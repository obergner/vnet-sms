package vnet.sms.gateway.server.framework.test;

import javax.jms.Message;

public interface JmsMessagePredicate {

	boolean evaluate(Message message);
}
