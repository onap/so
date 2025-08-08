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

package org.onap.so.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDCDmaapClientTest extends BaseTest {

    private final String fileLocation = "src/test/resources/org/onap/so/client/asdc/create-ecompoe/";

    private static final String operationalEnvironmentId = "28122015552391";
    private static final String operationalEnvironmentName = "Operational Environment Name";
    private static final String operationalEnvironmentType = "ECOMP";
    private static final String tenantContext = "TEST";
    private static final String workloadContext = "ECOMP_E2E-IST";
    private static final String action = "Create";



    @Test
    public void verifyasdcCreateoeRequest() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String expected =
                "{\"operationalEnvironmentId\":\"28122015552391\",\"operationalEnvironmentName\":\"Operational Environment Name\",\"operationalEnvironmentType\":\"ECOMP\",\"tenantContext\":\"TEST\",\"workloadContext\":\"ECOMP_E2E-IST\",\"action\":\"Create\"}";


        CreateEcompOperationEnvironmentBean cBean = new CreateEcompOperationEnvironmentBean();
        cBean.setOperationalEnvironmentId(operationalEnvironmentId);
        cBean.setoperationalEnvironmentName(operationalEnvironmentName);
        cBean.setoperationalEnvironmentType(operationalEnvironmentType);
        cBean.settenantContext(tenantContext);
        cBean.setworkloadContext(workloadContext);
        cBean.setaction(action);

        String actual = mapper.writeValueAsString(cBean);

        assertEquals("payloads are equal", expected, actual);
    }



}
