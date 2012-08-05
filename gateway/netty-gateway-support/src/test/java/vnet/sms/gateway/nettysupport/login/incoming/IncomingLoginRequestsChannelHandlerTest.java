package vnet.sms.gateway.nettysupport.login.incoming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.acknowledge.SendLoginRequestAckEvent;
import vnet.sms.common.wme.acknowledge.SendLoginRequestNackEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.gateway.nettysupport.MessageProcessingContext;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettytest.embedded.ChannelEventFilter;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.MessageEventFilter;

public class IncomingLoginRequestsChannelHandlerTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullAuthenticationManager() {
		new IncomingLoginRequestsChannelHandler<Long>(null, 10);
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsLoginRequestAcceptedEventDownstreamIfLoginSucceeds()
	        throws Throwable {
		final AuthenticationManager acceptAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				return new TestingAuthenticationToken(
				        authentication.getPrincipal(),
				        authentication.getCredentials(), "test-role");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        acceptAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestAcceptedEventDownstreamIfLoginSucceeds",
		                "secret"));
		final MessageEvent sentReply = embeddedPipeline.nextSentMessageEvent();

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send a reply after successful login",
		        sentReply);
		assertEquals(
		        "IncomingLoginRequestsChannelHandler sent unexpected reply after successful login",
		        SendLoginRequestAckEvent.class, sentReply.getClass());
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsLoginRequestRejectedEventDownstreamIfLoginThrowsBadCredentialsException()
	        throws Throwable {
		final int negativeResponseDelayMillis = 5;
		final AuthenticationManager rejectAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				throw new BadCredentialsException("Bad credentials");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        rejectAll, negativeResponseDelayMillis);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestRejectedEventDownstreamIfLoginThrowsBadCredentialsException",
		                "secret"));
		Thread.sleep(negativeResponseDelayMillis + 100);
		final MessageEvent sentReply = embeddedPipeline.nextSentMessageEvent();

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send a reply after rejected login",
		        sentReply);
		assertEquals(
		        "IncomingLoginRequestsChannelHandler sent unexpected reply after rejected login",
		        SendLoginRequestNackEvent.class, sentReply.getClass());
	}

	@Test
	public final void assertThatLoginChannelHandlerDelaysResponseForConfiguredNumberOfMillisecondsIfLoginRequestFails()
	        throws Throwable {
		final int negativeResponseDelayMillis = 500;
		final AuthenticationManager rejectAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				throw new BadCredentialsException("Bad credentials");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        rejectAll, negativeResponseDelayMillis);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerDelaysResponseForConfiguredNumberOfMillisecondsIfLoginRequestFails",
		                "secret"));

		assertNull(
		        "IncomingLoginRequestsChannelHandler did NOT delay response to failed login attempt",
		        embeddedPipeline.nextSentMessageEvent());
		Thread.sleep(negativeResponseDelayMillis + 200);
		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send a response to failed login attempt after delay period has elapsed",
		        embeddedPipeline.nextSentMessageEvent());
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsNonLoginMessageReceivedOnUnauthenticatedChannelEventDownstreamIfReceivingANonLoginMessageOnUnauthenticatedChannel()
	        throws Throwable {
		final AuthenticationManager rejectAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				throw new BadCredentialsException("Bad credentials");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        rejectAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.receive(new PingRequest());
		final MessageEvent sentReply = embeddedPipeline.nextSentMessageEvent();

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send a reply after receiving non-login message on unauthenticated channel",
		        sentReply);
		assertEquals(
		        "IncomingLoginRequestsChannelHandler sent unexpected reply after receiving non-login message on unauthenticated channel",
		        NonLoginMessageReceivedOnUnauthenticatedChannelEvent.class,
		        sentReply.getClass());
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsNonLoginRequestsReceivedOnAnAuthenticatedChannelFurtherUpstream()
	        throws Throwable {
		final AuthenticationManager acceptAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				return new TestingAuthenticationToken(
				        authentication.getPrincipal(),
				        authentication.getCredentials(), "test-role");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        acceptAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestAcceptedEventDownstreamIfLoginSucceeds",
		                "secret"));
		embeddedPipeline.receive(new PingRequest());
		final MessageEvent propagatedMessage = embeddedPipeline
		        .nextReceivedMessageEvent(MessageEventFilter.FILTERS
		                .ofType(ReceivedPingRequestEvent.class));

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not propagate non-login message received on authenticated channel",
		        propagatedMessage);
		assertEquals(
		        "IncomingLoginRequestsChannelHandler propagated unexpected message after receiving non-login message on authenticated channel",
		        ReceivedPingRequestEvent.class, propagatedMessage.getClass());
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsChannelSuccessfullyAuthenticatedEventUpstreamIfLoginSucceeds()
	        throws Throwable {
		final AuthenticationManager acceptAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				return new TestingAuthenticationToken(
				        authentication.getPrincipal(),
				        authentication.getCredentials(), "test-role");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        acceptAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsChannelSuccessfullyAuthenticatedEventUpstreamIfLoginSucceeds",
		                "secret"));
		final ChannelEvent upstreamChannelEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(new ChannelEventFilter() {
			        @Override
			        public boolean matches(final ChannelEvent event) {
				        return event instanceof ChannelSuccessfullyAuthenticatedEvent;
			        }
		        });

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send expected "
		                + ChannelSuccessfullyAuthenticatedEvent.class.getName()
		                + " upstream after successful login",
		        upstreamChannelEvent);
	}

	@Test
	public final void assertThatLoginChannelHandlerSendsChannelAuthenticationFailedEventUpstreamIfLoginFailes()
	        throws Throwable {
		final AuthenticationManager rejectAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				throw new BadCredentialsException("Bad credentials");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        rejectAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsChannelAuthenticationFailedEventUpstreamIfLoginFailes",
		                "secret"));
		final ChannelEvent upstreamChannelEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(new ChannelEventFilter() {
			        @Override
			        public boolean matches(final ChannelEvent event) {
				        return event instanceof ChannelAuthenticationFailedEvent;
			        }
		        });

		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send expected "
		                + ChannelAuthenticationFailedEvent.class.getName()
		                + " upstream after failed login", upstreamChannelEvent);
	}

	@Test
	public final void assertThatLoginChannelHandlerRemovesAuthenticatedUserFromMDCAfterReturning()
	        throws Throwable {
		final AuthenticationManager acceptAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				return new TestingAuthenticationToken(
				        authentication.getPrincipal(),
				        authentication.getCredentials(), "test-role");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        acceptAll, 10);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatLoginChannelHandlerRemovesAuthenticatedUserFromMDCAfterReturning",
		        "secret");
		embeddedPipeline.receive(loginRequest);
		final String currentUserInMdc = MessageProcessingContext.INSTANCE
		        .currentUserName();

		assertNull(
		        "IncomingLoginRequestsChannelHandler did not remove authenticated user from MDC after returning",
		        currentUserInMdc);
	}
}
