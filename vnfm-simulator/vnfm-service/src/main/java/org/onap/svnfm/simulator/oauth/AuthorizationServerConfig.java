package org.onap.svnfm.simulator.oauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@Configuration
@EnableAuthorizationServer
@Profile("oauth-authentication")
/**
 * Configures the authorization server for oauth token based authentication when the spring profile
 * "oauth-authentication" is active
 */
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final int ONE_DAY = 60 * 60 * 24;

    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("vnfmadapter")
                .secret("$2a$10$dHzTlqSBcm8hdO52LBvnX./zNTvUzzJy.lZrc4bCBL5gkln0wX6T6")
                .authorizedGrantTypes("client_credentials")
                .scopes("write")
                .accessTokenValiditySeconds(ONE_DAY)
                .refreshTokenValiditySeconds(ONE_DAY);
    }

}
