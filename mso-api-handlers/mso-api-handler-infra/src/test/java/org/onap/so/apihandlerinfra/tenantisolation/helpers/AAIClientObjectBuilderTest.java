/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestParameters;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIClientObjectBuilderTest extends BaseTest {

    private CloudOrchestrationRequest request;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void testSetUp() {
        request = getCloudOrchestrationRequest();
    }

    public CloudOrchestrationRequest getCloudOrchestrationRequest() {
        CloudOrchestrationRequest cor = new CloudOrchestrationRequest();
        RequestDetails reqDetails = new RequestDetails();
        RequestInfo reqInfo = new RequestInfo();
        RequestParameters reqParams = new RequestParameters();
        reqParams.setTenantContext("TEST");
        reqParams.setWorkloadContext("ECOMP_TEST");
        reqParams.setOperationalEnvironmentType(OperationalEnvironment.ECOMP);
        reqInfo.setInstanceName("TEST_ECOMP_ENVIRONMENT");
        reqDetails.setRequestInfo(reqInfo);
        reqDetails.setRequestParameters(reqParams);
        cor.setRequestDetails(reqDetails);
        return cor;
    }

    @Test
    public void testGetAaiClientObjectBuilder() {
        AAIClientObjectBuilder builder = new AAIClientObjectBuilder();
        org.onap.aai.domain.yang.OperationalEnvironment operEnv =
                builder.buildAAIOperationalEnvironment("Active", request);
        assertEquals("TEST", operEnv.getTenantContext());
        assertEquals("ECOMP_TEST", operEnv.getWorkloadContext());
        assertEquals("TEST_ECOMP_ENVIRONMENT", operEnv.getOperationalEnvironmentName());
        assertEquals(OperationalEnvironment.ECOMP.toString(), operEnv.getOperationalEnvironmentType());
    }


}
