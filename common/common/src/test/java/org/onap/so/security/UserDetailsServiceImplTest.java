package org.onap.so.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.ArrayList;
import org.junit.Test;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserDetailsServiceImplTest {

    private static final String USERNAME = "usernameTest";
    private static final String PASSWORD = "passTest";
    private static final String ROLE = "roleTest";

    @Test
    public void loadUserByUsername_Success() {
        UserDetailsServiceImpl testedObject = new UserDetailsServiceImpl();
        testedObject.setUsercredentials(prepareUserCredentials());
        UserDetails result = testedObject.loadUserByUsername(USERNAME);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(new BCryptPasswordEncoder().matches(PASSWORD, result.getPassword())).isTrue();
    }

    @Test
    public void loadUserByUsername_keepsBcryptHash() {
        String hash = new BCryptPasswordEncoder().encode(PASSWORD);
        List<UserCredentials> credentials = prepareUserCredentials();
        credentials.get(0).setPassword(hash);
        UserDetailsServiceImpl testedObject = new UserDetailsServiceImpl();
        testedObject.setUsercredentials(credentials);
        assertThat(testedObject.loadUserByUsername(USERNAME).getPassword()).isEqualTo(hash);
    }

    @Test
    public void loadUserByUsername_defaultsRole() {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUsername(USERNAME);
        userCredentials.setPassword(PASSWORD);
        UserDetailsServiceImpl testedObject = new UserDetailsServiceImpl();
        testedObject.setUsercredentials(new ArrayList<>(List.of(userCredentials)));
        assertThat(testedObject.loadUserByUsername(USERNAME).getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_" + UserCredentials.DEFAULT_ROLE);
    }

    @Test
    public void loadUserByUsername_userNotFoundEx() {
        UserDetailsServiceImpl testedObject = new UserDetailsServiceImpl();
        testedObject.setUsercredentials(prepareUserCredentials());
        assertThatThrownBy(() -> testedObject.loadUserByUsername("notExistingUser"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private List<UserCredentials> prepareUserCredentials() {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUsername(USERNAME);
        userCredentials.setPassword(PASSWORD);
        userCredentials.setRole(ROLE);
        List<UserCredentials> list = new ArrayList<>();
        list.add(userCredentials);
        return list;
    }

}
