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

package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.entities.AAIError;
import org.onap.aaiclient.client.aai.entities.RequestError;
import org.onap.aaiclient.client.aai.entities.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIClientResponseExceptionMapperTest {

    AAIClientResponseExceptionMapper mapper;
    String errorMsg;

    @Before
    public void before() {
        mapper = new AAIClientResponseExceptionMapper();
        errorMsg = "Error calling A&AI. Request-Id=" + mapper.getRequestId() + " ";
    }

    @Test
    public void testExtractMessageWithEntity() throws JsonProcessingException {
        ServiceException svcException = new ServiceException();
        svcException.setText("test %1 message - %2");
        svcException.setVariables(Arrays.asList("error", "service exception %1 test"));

        RequestError requestError = new RequestError();
        requestError.setServiceException(svcException);

        AAIError error = new AAIError();
        error.setRequestError(requestError);

        ObjectMapper objMapper = new ObjectMapper();
        String strRequestError = objMapper.writeValueAsString(error);

        assertEquals(errorMsg + "test error message - service exception error test",
                mapper.extractMessage(strRequestError).get());
    }

    @Test
    public void testExtractMessageWithoutEntity() {
        assertEquals(errorMsg, mapper.extractMessage("").get());
    }
}
