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

package org.onap.so.apihandlerinfra.validation;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Test;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomWorkflowValidationTest extends BaseTest {

    @Test
    public void testCustomWorkflowValidation() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/SuccessfulValidation/v1ExecuteCustomWorkflow.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.inPlaceSoftwareUpdate, 1, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vnf");

        CustomWorkflowValidation validation = new CustomWorkflowValidation();
        validation.validate(info);

        assertEquals(info.getSir().getRequestDetails().getCloudConfiguration().getCloudOwner(), "att-aic");
    }
}
