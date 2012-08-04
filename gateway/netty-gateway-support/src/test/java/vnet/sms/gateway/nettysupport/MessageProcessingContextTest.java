package vnet.sms.gateway.nettysupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;

public class MessageProcessingContextTest {

	@Before
	public void clearMdcBefore() {
		MDC.clear();
	}

	@After
	public void clearMdcAfter() {
		MDC.clear();
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatOnUserEnterRejectsNullUser() {
		MessageProcessingContext.INSTANCE.onUserEnter(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatOnUserEnterRejectsToAssociateAUserMoreThanOnce() {
		final Authentication user = new UsernamePasswordAuthenticationToken(
		        "assertThatOnUserEnterRejectsToAssociateAUserMoreThanOnce",
		        "secret");
		MessageProcessingContext.INSTANCE.onUserEnter(user);
		MessageProcessingContext.INSTANCE.onUserEnter(user);
	}

	@Test
	public final void assertThatOnUserEnterStoresCurrentUserInMDC() {
		final Authentication user = new UsernamePasswordAuthenticationToken(
		        "assertThatOnUserEnterStoresCurrentUserInMDC", "secret");
		MessageProcessingContext.INSTANCE.onUserEnter(user);

		assertEquals("onUserEnter(" + user
		        + ") should have stored our current user in our MDC",
		        user.getName(),
		        MDC.get(MessageProcessingContext.CURRENT_USER_MDC_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatOnUserExitRejectsNullUser() {
		MessageProcessingContext.INSTANCE.onUserExit(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatOnUserExitRejectsToDisassociateAUserThatHasNotBeenAssociatedBefore() {
		final Authentication user = new UsernamePasswordAuthenticationToken(
		        "assertThatOnUserExitRejectsToDisassociateAUserThatHasNotBeenAssociatedBefore",
		        "secret");
		MessageProcessingContext.INSTANCE.onUserExit(user);
	}

	@Test
	public final void assertThatOnUserExitRemovesCurrentUserFromMDC() {
		final Authentication user = new UsernamePasswordAuthenticationToken(
		        "assertThatOnUserExitRemovesCurrentUserFromMDC", "secret");
		MessageProcessingContext.INSTANCE.onUserEnter(user);
		MessageProcessingContext.INSTANCE.onUserExit(user);

		assertNull("onUserExit(" + user
		        + ") should have removed current user from MDC",
		        MDC.get(MessageProcessingContext.CURRENT_USER_MDC_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatOnMessageEnterRejectsNullMessage() {
		MessageProcessingContext.INSTANCE.onMessageEnter(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatOnMessageEnterRejectsToAssociateAMessageMoreThanOnce() {
		final GsmPdu message = new LoginRequest(
		        "assertThatOnMessageEnterRejectsToAssociateAMessageMoreThanOnce",
		        "secret");
		MessageProcessingContext.INSTANCE.onMessageEnter(message);
		MessageProcessingContext.INSTANCE.onMessageEnter(message);
	}

	@Test
	public final void assertThatOnMessageEnterStoresCurrentMessageInMDC() {
		final GsmPdu message = new LoginRequest(
		        "assertThatOnMessageEnterRejectsToAssociateAMessageMoreThanOnce",
		        "secret");
		MessageProcessingContext.INSTANCE.onMessageEnter(message);

		assertEquals("onMessageEnter(" + message
		        + ") should have stored our current message's UUID in our MDC",
		        message.getId().toString(),
		        MDC.get(MessageProcessingContext.MESSAGE_UUID_MDC_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatOnMessageExitRejectsNullMessage() {
		MessageProcessingContext.INSTANCE.onMessageExit(null);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatOnMessageExitRejectsToDisassociateAMessageThatHasNotBeenAssociatedBefore() {
		final GsmPdu message = new LoginRequest(
		        "assertThatOnMessageExitRejectsToDisassociateAMessageThatHasNotBeenAssociatedBefore",
		        "secret");
		MessageProcessingContext.INSTANCE.onMessageExit(message);
	}

	@Test
	public final void assertThatOnMessageExitRemovesCurrentMessageFromMDC() {
		final GsmPdu message = new LoginRequest(
		        "assertThatOnMessageExitRemovesCurrentMessageFromMDC", "secret");
		MessageProcessingContext.INSTANCE.onMessageEnter(message);
		MessageProcessingContext.INSTANCE.onMessageExit(message);

		assertNull("onMessageExit(" + message
		        + ") should have removed current message from MDC",
		        MDC.get(MessageProcessingContext.MESSAGE_UUID_MDC_KEY));
	}
}
