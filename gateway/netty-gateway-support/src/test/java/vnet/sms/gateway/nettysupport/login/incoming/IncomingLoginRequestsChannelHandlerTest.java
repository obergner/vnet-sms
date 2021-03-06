package vnet.sms.gateway.nettysupport.login.incoming;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.util.HashedWheelTimer;
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
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.MessageEventFilters;
import vnet.sms.gateway.nettytest.embedded.TimedFuture;

import com.google.common.base.Predicate;

public class IncomingLoginRequestsChannelHandlerTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullAuthenticationManager() {
		new IncomingLoginRequestsChannelHandler<Long>(null, 10,
		        new HashedWheelTimer());
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
		        acceptAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestAcceptedEventDownstreamIfLoginSucceeds",
		                "secret"));
		final MessageEvent sentReply = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();

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
		final int negativeResponseDelayMillis = 200;
		final long confidenceIntervalMillis = 100L;
		final AuthenticationManager rejectAll = new AuthenticationManager() {
			@Override
			public Authentication authenticate(
			        final Authentication authentication)
			        throws AuthenticationException {
				throw new BadCredentialsException("Bad credentials");
			}
		};
		final IncomingLoginRequestsChannelHandler<Integer> objectUnderTest = new IncomingLoginRequestsChannelHandler<Integer>(
		        rejectAll, negativeResponseDelayMillis, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		final TimedFuture<MessageEvent> loginRequestNack = embeddedPipeline
		        .downstreamMessageEvents().timedWaitForMatchingMessageEvent(
		                new Predicate<MessageEvent>() {
			                @Override
			                public boolean apply(final MessageEvent input) {
				                return SendLoginRequestNackEvent.class
				                        .isInstance(input);
			                }
		                });

		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestRejectedEventDownstreamIfLoginThrowsBadCredentialsException",
		                "secret"));
		final TimedFuture.Value<MessageEvent> sentReplyValue = loginRequestNack
		        .get(negativeResponseDelayMillis + 100L, MILLISECONDS);
		final MessageEvent sentReply = sentReplyValue.get();
		final long elapsedDurationMillis = sentReplyValue
		        .elapsedDurationMillis();

		assertTrue(
		        "IncomingLoginRequestsChannelHandler did not send a reply after rejected login",
		        (elapsedDurationMillis > negativeResponseDelayMillis
		                - confidenceIntervalMillis)
		                && (elapsedDurationMillis < negativeResponseDelayMillis
		                        + confidenceIntervalMillis));
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
		        rejectAll, negativeResponseDelayMillis, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerDelaysResponseForConfiguredNumberOfMillisecondsIfLoginRequestFails",
		                "secret"));

		assertNull(
		        "IncomingLoginRequestsChannelHandler did NOT delay response to failed login attempt",
		        embeddedPipeline.downstreamMessageEvents().nextMessageEvent());
		Thread.sleep(negativeResponseDelayMillis + 200);
		assertNotNull(
		        "IncomingLoginRequestsChannelHandler did not send a response to failed login attempt after delay period has elapsed",
		        embeddedPipeline.downstreamMessageEvents().nextMessageEvent());
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
		        rejectAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline.receive(new PingRequest());
		final MessageEvent sentReply = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();

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
		        acceptAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsLoginRequestAcceptedEventDownstreamIfLoginSucceeds",
		                "secret"));
		embeddedPipeline.receive(new PingRequest());
		final MessageEvent propagatedMessage = embeddedPipeline
		        .upstreamMessageEvents().nextMatchingMessageEvent(
		                MessageEventFilters
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
		        acceptAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsChannelSuccessfullyAuthenticatedEventUpstreamIfLoginSucceeds",
		                "secret"));
		final ChannelEvent upstreamChannelEvent = embeddedPipeline
		        .upstreamChannelEvents().nextMatchingChannelEvent(
		                new Predicate<ChannelEvent>() {
			                @Override
			                public boolean apply(final ChannelEvent event) {
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
		        rejectAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatLoginChannelHandlerSendsChannelAuthenticationFailedEventUpstreamIfLoginFailes",
		                "secret"));
		final ChannelEvent upstreamChannelEvent = embeddedPipeline
		        .upstreamChannelEvents().nextMatchingChannelEvent(
		                new Predicate<ChannelEvent>() {
			                @Override
			                public boolean apply(final ChannelEvent event) {
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
		        acceptAll, 10, new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
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
