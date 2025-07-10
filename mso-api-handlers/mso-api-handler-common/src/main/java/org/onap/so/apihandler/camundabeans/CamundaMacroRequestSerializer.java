/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandler.camundabeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Class used to create data object and serialize it to JSON which the BPEL macro flow understands.
 */
public class CamundaMacroRequestSerializer {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    }

    private CamundaMacroRequestSerializer() {}

    public static String getJsonRequest(String requestId, String action, String serviceInstanceId)
            throws JsonProcessingException {
        CamundaMacroRequest macroRequest = new CamundaMacroRequest();
        macroRequest.setAction(getCamundaInput(action));
        macroRequest.setRequestId(getCamundaInput(requestId));
        macroRequest.setServiceInstanceId(getCamundaInput(serviceInstanceId));
        return mapper.writeValueAsString(macroRequest);
    }

    private static CamundaInput getCamundaInput(String value) {
        CamundaInput input = new CamundaInput();
        input.setType("String");
        input.setValue(value);
        return input;
    }
}
