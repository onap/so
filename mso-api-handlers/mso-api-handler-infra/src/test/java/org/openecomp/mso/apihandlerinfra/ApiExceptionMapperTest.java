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

package org.openecomp.mso.apihandlerinfra;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiExceptionMapper;
import org.openecomp.mso.apihandlerinfra.exceptions.BPMNFailureException;
import org.openecomp.mso.apihandlerinfra.exceptions.ClientConnectionException;
import org.openecomp.mso.apihandlerinfra.exceptions.DuplicateRequestException;
import org.openecomp.mso.apihandlerinfra.exceptions.ValidateException;
import org.openecomp.mso.apihandlerinfra.exceptions.VfModuleNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiExceptionMapperTest extends BaseTest {

    ApiExceptionMapper mapper = new ApiExceptionMapper();


    @Test
    public void testObjectMapperError() throws JsonProcessingException {
        ObjectMapper mockedMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(mockedMapper.writeValueAsString(anyObject())).thenThrow(JsonProcessingException.class);
        ValidateException validateException = new ValidateException.Builder("Test", 0 , null).build();
        ApiExceptionMapper mockedException = Mockito.spy(new ApiExceptionMapper());
        Mockito.doReturn(mockedMapper).when(mockedException).createObjectMapper();
        Response resp = mockedException.toResponse((ApiException) validateException);

       /// assertEquals(resp.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertThat(resp.getEntity().toString(),startsWith("Exception in buildServiceErrorResponse writing exceptionType to string"));
    }

    @Test
    public void testValidateResponse(){
        ValidateException validateException = new ValidateException.Builder("Test Message", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).build();
        Response resp = mapper.toResponse((ApiException) validateException);

        assertEquals(resp.getStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testBPMNFailureResponse(){
        BPMNFailureException bpmnException = new BPMNFailureException.Builder("Test Message", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).build();
        Response resp = mapper.toResponse((ApiException) bpmnException);

        assertEquals(resp.getStatus(), HttpStatus.SC_NOT_FOUND);
    }
    @Test
    public void testClientConnectionResponse(){
        ClientConnectionException clientConnectionException = new ClientConnectionException.Builder("test", HttpStatus.SC_INTERNAL_SERVER_ERROR,ErrorNumbers.SVC_BAD_PARAMETER).build();
        Response resp =  mapper.toResponse((ApiException) clientConnectionException);

        assertEquals(resp.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    @Test
    public void testVFModuleResponse() {
        VfModuleNotFoundException vfModuleException = new VfModuleNotFoundException.Builder("Test Message", HttpStatus.SC_CONFLICT,ErrorNumbers.SVC_BAD_PARAMETER).build();
        Response resp =  mapper.toResponse((ApiException) vfModuleException);

        assertEquals(resp.getStatus(), HttpStatus.SC_CONFLICT);
    }
    @Test
    public void testDuplicateRequestResponse() throws IOException {
        DuplicateRequestException duplicateRequestException = new DuplicateRequestException.Builder("Test1", "Test2","Test3","Test4", HttpStatus.SC_BAD_GATEWAY,ErrorNumbers.SVC_BAD_PARAMETER).build();
        Response resp =  mapper.toResponse((ApiException) duplicateRequestException);

        assertEquals(resp.getStatus(), HttpStatus.SC_BAD_GATEWAY);
    }
}
