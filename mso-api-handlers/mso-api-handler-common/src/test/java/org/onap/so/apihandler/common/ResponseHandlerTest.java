/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.apihandler.common;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ResponseHandlerTest {

    @Spy
    @InjectMocks
    private ResponseHandler responseHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void acceptedResponseTest() throws BPMNFailureException {
        ResponseEntity<String> camundaResponse = ResponseEntity.noContent().build();
        thrown.expect(BPMNFailureException.class);
        thrown.expectMessage("Request Failed due to BPEL error with HTTP Status = 204");
        responseHandler.acceptedResponse(camundaResponse);
    }

    @Test
    public void acceptedOrNoContentResponseTest() throws BPMNFailureException {
        ResponseEntity<String> camundaResponse = ResponseEntity.badRequest().build();
        thrown.expect(BPMNFailureException.class);
        thrown.expectMessage("Request Failed due to BPEL error with HTTP Status = 400");
        responseHandler.acceptedOrNoContentResponse(camundaResponse);

    }

}
