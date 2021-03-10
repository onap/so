/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@EnableWebSecurity
@Configuration
@Order(1)
@Profile({"basic", "test"})
public class SoWebSecurityConfigurerAdapter extends BaseWebSecurityConfigurerAdapter {
    @Autowired
    @Qualifier("basic")
    protected HttpSecurityConfigurer httpSecurityConfigurer;

    @Override
    HttpSecurityConfigurer getHttpSecurityConfigurer() {
        return httpSecurityConfigurer;
    }
}
