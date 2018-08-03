/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.montoring.configuration.rest;

import org.onap.so.montoring.configuration.AuthenticationDetails;
import org.onap.so.montoring.rest.service.HttpRestServiceProvider;
import org.onap.so.montoring.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
@Configuration
public class HttpServiceProviderConfiguration {

    @Bean
    public HttpRestServiceProvider httpRestServiceProvider(@Autowired final RestTemplate restTemplate,
            @Autowired final AuthenticationDetails authenticationDetails) {
        if (authenticationDetails.isValid()) {
            final BasicAuthorizationInterceptor authorizationInterceptor = new BasicAuthorizationInterceptor(
                    authenticationDetails.getUsername(), authenticationDetails.getPassword());
            restTemplate.getInterceptors().add(authorizationInterceptor);
        }
        return new HttpRestServiceProviderImpl(restTemplate);
    }

}
