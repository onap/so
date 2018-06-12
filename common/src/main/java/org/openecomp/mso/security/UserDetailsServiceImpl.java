package org.openecomp.mso.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.List;

@ConfigurationProperties(prefix = "spring.security")
public class UserDetailsServiceImpl implements UserDetailsService {

	private List<UserCredentials> usercredentials;

	public List<UserCredentials> getUsercredentials() {
		return usercredentials;
	}

	public void setUsercredentials(List<UserCredentials> usercredentials) {
		this.usercredentials = usercredentials;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		for (int i = 0; usercredentials != null && i < usercredentials.size(); i++) {
			if (usercredentials.get(i).getUsername().equals(username)) {
				return User.withUsername(username).password(usercredentials.get(i).getPassword())
						.roles(usercredentials.get(i).getRole()).build();
			}
		}

		throw new UsernameNotFoundException("User not found, username: " + username);
	}

}
