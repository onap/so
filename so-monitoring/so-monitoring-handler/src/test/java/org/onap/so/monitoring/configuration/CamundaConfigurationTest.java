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
package org.onap.so.monitoring.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.so.monitoring.configuration.camunda.CamundaConfiguration;
import org.onap.so.monitoring.configuration.camunda.CamundaRestUrlProvider;


/**
 * @author waqas.ikram@ericsson.com
 *
 */
public class CamundaConfigurationTest {

    @Test
    public void test_CamundaRestURIConfiguration_ValidUrl() {
        final CamundaConfiguration objUnderTest = new CamundaConfiguration();
        final CamundaRestUrlProvider provider = objUnderTest.camundaRestUrlProvider("http://localhost:8080", "default");
        assertEquals(
                "http://localhost:8080/default/history/activity-instance?processInstanceId=Deadpool&sortBy=startTime&sortOrder=asc",
                provider.getActivityInstanceUrl("Deadpool"));
    }

}
