package vnet.sms.gateway.server.framework.dummy;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class DummyAuthenticationProvider implements AuthenticationProvider {

	public static final String	REJECTED_PASSWORD	= "wrong-password";

	@Override
	public Authentication authenticate(final Authentication authentication)
	        throws AuthenticationException {
		if (authentication.getCredentials().equals(REJECTED_PASSWORD)) {
			throw new BadCredentialsException(
			        "Rejecting predefined bad password");
		}
		return authentication;
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class
		        .isAssignableFrom(authentication);
	}
}
