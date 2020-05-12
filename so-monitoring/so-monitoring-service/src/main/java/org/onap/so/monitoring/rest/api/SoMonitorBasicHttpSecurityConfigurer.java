/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix. All rights reserved.
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

package org.onap.so.monitoring.rest.api;

import org.onap.so.security.HttpSecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Primary
@Component
public class SoMonitorBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoMonitorBasicHttpSecurityConfigurer.class);

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        LOGGER.warn("Disabling basic http security ....");
        http.csrf().disable().authorizeRequests().anyRequest().permitAll();

    }
}

