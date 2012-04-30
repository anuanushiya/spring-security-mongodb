package com.sustia.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sustia.UserAccountStatus;
import com.sustia.domain.UserAccount;

/*
 Extend AbstractUserDetailsAuthenticationProvider when you want to
 prehandle authentication, as in throwing custom exception messages,
 checking status, etc. 
 */
@Component
public class LocalAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	
	@Autowired
    UserService userService;
	
    @Autowired
    private transient PasswordEncoder encoder = null;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		// TODO Auto-generated method stub
	}

	@Override
	public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
        String password = (String) authentication.getCredentials();
        if (!StringUtils.hasText(password)) {
            throw new BadCredentialsException("Please enter password");
        }

        UserAccount user = userService.getByUsernameAndPassword(username, encoder.encodePassword(password, null));
        if (user == null) {
            throw new BadCredentialsException("Invalid Username/Password");
        }
        if (!(UserAccountStatus.STATUS_APPROVED.name().equals(user.getStatus()))) {
            throw new BadCredentialsException("User has not been approved");
        }
        if (!user.getEnabled()) {
            throw new BadCredentialsException("User disabled");
        }

        final List<GrantedAuthority> auths;
        if (!user.getRoles().isEmpty()) {
	    	auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRolesCSV());
        } else {
        	auths = AuthorityUtils.NO_AUTHORITIES;
        }

        return new User(username, password, user.getEnabled(), // enabled
                true, // account not expired
                true, // credentials not expired
                true, // account not locked
                auths);
	}

}