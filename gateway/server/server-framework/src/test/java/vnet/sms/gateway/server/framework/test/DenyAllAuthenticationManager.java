package vnet.sms.gateway.server.framework.test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class DenyAllAuthenticationManager implements AuthenticationManager {

	@Override
	public Authentication authenticate(final Authentication authentication)
	        throws AuthenticationException {
		throw new BadCredentialsException("Reject all");
	}
}
