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
package org.onap.so.montoring.configuration.camunda;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author waqas.ikram@ericsson.com
 */

@Configuration
public class CamundaConfiguration {

    @Bean
    public CamundaRestUrlProvider camundaRestUrlProvider(@Value(value = "${camunda.rest.api.url}") final String httpURL,
            @Value(value = "${camunda.rest.api.engine:default}") final String engineName) {
        return new CamundaRestUrlProvider(httpURL, engineName);
    }
}
