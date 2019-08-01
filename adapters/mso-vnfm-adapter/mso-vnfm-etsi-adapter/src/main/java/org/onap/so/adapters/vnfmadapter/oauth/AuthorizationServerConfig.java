/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.oauth;

import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@Configuration
@EnableAuthorizationServer
/**
 * Configures the authorization server for oauth token based authentication.
 */
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final int ONE_DAY = 60 * 60 * 24;

    @Value("${vnfmadapter.auth:E39823AAB2739CC654C4E92B52C05BC34149342D0A46451B00CA508C8EDC62242CE4E9DA9445D3C01A3F13}")
    private String vnfmAdapterAuth;

    @Value("${mso.key}")
    private String msoEncryptionKey;

    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        final String[] decrypedAuth = CryptoUtils.decrypt(vnfmAdapterAuth, msoEncryptionKey).split(":");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        clients.inMemory().withClient(decrypedAuth[0]).secret(passwordEncoder.encode(decrypedAuth[1]))
                .authorizedGrantTypes("client_credentials").scopes("write").accessTokenValiditySeconds(ONE_DAY)
                .refreshTokenValiditySeconds(ONE_DAY);
    }

}
