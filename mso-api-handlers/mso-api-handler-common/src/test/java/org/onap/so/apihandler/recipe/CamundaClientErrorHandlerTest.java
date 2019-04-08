/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandler.recipe;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CamundaClientErrorHandlerTest {

    private ClientHttpResponse clientHttpResponse;
    private CamundaClientErrorHandler clientErrorHandler;

    @Before
    public void before() {
        clientHttpResponse = Mockito.mock(ClientHttpResponse.class);
        clientErrorHandler = new CamundaClientErrorHandler();
    }

    @Test
    public void handleError_SERVER_ERROR_Test() throws IOException {
        Mockito.when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        clientErrorHandler.handleError(clientHttpResponse);
        boolean serverHasError = clientErrorHandler.hasError(clientHttpResponse);
        assertEquals(true, serverHasError);
    }

    @Test
    public void handleError_CLIENT_ERROR_Test() throws IOException {
        Mockito.when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        Mockito.when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        clientErrorHandler.handleError(clientHttpResponse);
        boolean clientHasError = clientErrorHandler.hasError(clientHttpResponse);
        assertEquals(true, clientHasError);
    }

    @Test
    public void handleError_SUCCESS_Test() throws IOException {
        Mockito.when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        Mockito.when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        clientErrorHandler.handleError(clientHttpResponse);
        boolean hasNoError = clientErrorHandler.hasError(clientHttpResponse);
        assertEquals(false, hasNoError);
    }

}
