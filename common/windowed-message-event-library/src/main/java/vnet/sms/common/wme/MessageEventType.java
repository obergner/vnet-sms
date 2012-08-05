package vnet.sms.common.wme;

/**
 * @author obergner
 * 
 */
public enum MessageEventType {

	RECEIVED_LOGIN_REQUEST,

	SEND_LOGIN_REQUEST_ACK,

	SEND_LOGIN_REQUEST_NACK,

	RECEIVED_LOGIN_REQUEST_ACKNOWLEDGEMENT,

	RECEIVED_PING_REQUEST,

	SEND_PING_REQUEST_ACK,

	SEND_PING_REQUEST,

	RECEIVED_PING_REQUEST_ACKNOWLEDGEMENT,

	RECEIVED_SMS,

	NON_LOGIN_MESSAGE_RECEIVED_ON_UNAUTHENTICATED_CHANNEL,

	SEND_SMS_ACK,

	SEND_SMS_NACK,

	SEND_SMS,
}
