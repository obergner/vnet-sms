package vnet.sms.gateway.server.framework.test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AcceptAllAuthenticationManager implements AuthenticationManager {

	@Override
	public Authentication authenticate(final Authentication authentication)
	        throws AuthenticationException {
		return new TestingAuthenticationToken(authentication.getPrincipal(),
		        authentication.getCredentials(), "test-role");
	}
}
