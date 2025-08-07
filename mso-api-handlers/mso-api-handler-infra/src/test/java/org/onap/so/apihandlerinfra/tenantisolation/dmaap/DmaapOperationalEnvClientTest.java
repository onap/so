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
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DmaapOperationalEnvClientTest extends BaseTest {

    private final String fileLocation = "src/test/resources/org/onap/so/client/asdc/create-ecompoe/";
    private static final String operationalEnvironmentId = "28122015552391";
    private static final String operationalEnvironmentName = "OpEnv-name";
    private static final String operationalEnvironmentType = "VNF";
    private static final String tenantContext = "Test";
    private static final String workloadContext = "VNF_E2E-IST";
    private static final String action = "Create";
    @Autowired
    private DmaapOperationalEnvClient client;

    @Test
    public void verifyCreateEcompOperationEnvironmentRequest() throws IOException, ApiException {
        String content = this.getJson("ecomp-openv-request.json");
        ObjectMapper mapper = new ObjectMapper();
        CreateEcompOperationEnvironmentBean expected =
                mapper.readValue(content, CreateEcompOperationEnvironmentBean.class);

        String actual = client.buildRequest(operationalEnvironmentId, operationalEnvironmentName,
                operationalEnvironmentType, tenantContext, workloadContext, action);

        assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);
    }


    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
    }

}

