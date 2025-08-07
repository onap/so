/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@RunWith(MockitoJUnitRunner.class)
public class CnfmHttpServiceProviderImplTest {

    @Mock
    private CnfmUrlProvider cnfmUrlProvider;
    @Mock
    private HttpRestServiceProviderImpl httpRestServiceProvider;
    @Mock
    private ResponseEntity<AsInstance> responseEntity;
    @Mock
    private ResponseEntity<Void> responseEntityVoid;

    private ResponseEntity<AsLcmOpOcc> responseEntityAsLCM;
    private final String createURL = "http://so-cnfm-lcm.onap:9888/so/so-cnfm/v1/api/aslcm/v1/as_instances";
    private final String instantiateURL = "http://so-cnfm-lcm.onap:9888/so/so-cnfm/v1/api/aslcm/v1/as_instances"
            + getAsInstance().getAsInstanceid() + "/instantiate";
    private CnfmHttpServiceProviderImpl cnfmHttpServiceProviderImpl;
    private final URI uri = URI.create("sample");
    private final CreateAsRequest createAsRequest = new CreateAsRequest();
    private final InstantiateAsRequest instantiateAsRequest = new InstantiateAsRequest();
    private final TerminateAsRequest terminateAsRequest = new TerminateAsRequest();

    @Before
    public void setup() {
        cnfmHttpServiceProviderImpl = new CnfmHttpServiceProviderImpl(cnfmUrlProvider, httpRestServiceProvider);
        when(httpRestServiceProvider.postHttpRequest(Mockito.any(), Mockito.anyString(), eq(AsInstance.class)))
                .thenReturn(responseEntity);
    }

    @Test
    public void invokeCreateAsRequest_withStatuscode_Success_hasBody() {

        when(cnfmUrlProvider.getCreateAsRequestUrl()).thenReturn(createURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntity.getBody()).thenReturn(getAsInstance());
        when(responseEntity.hasBody()).thenReturn(true);
        final Optional<AsInstance> returnedContent = cnfmHttpServiceProviderImpl.invokeCreateAsRequest(createAsRequest);
        assertEquals(returnedContent.orElseThrow().getAsInstanceid(), getAsInstance().getAsInstanceid());
    }

    @Test
    public void invokeCreateAsRequest_withStatuscodeSuccess_hasNoBody() {

        when(cnfmUrlProvider.getCreateAsRequestUrl()).thenReturn(createURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        final Optional<AsInstance> returnedContent = cnfmHttpServiceProviderImpl.invokeCreateAsRequest(createAsRequest);
        assertFalse(returnedContent.isPresent());
    }


    @Test
    public void invokeCreateAsRequest_withStatuscode_Success_hasBody_emptyInstanceID() {

        AsInstance emptyInstanceID = getAsInstance();
        emptyInstanceID.setAsInstanceid(null);
        when(cnfmUrlProvider.getCreateAsRequestUrl()).thenReturn(createURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntity.getBody()).thenReturn(emptyInstanceID);
        when(responseEntity.hasBody()).thenReturn(true);
        final Optional<AsInstance> returnedContent = cnfmHttpServiceProviderImpl.invokeCreateAsRequest(createAsRequest);
        assertFalse(returnedContent.isPresent());
    }

    @Test
    public void invokeCreateAsRequest_withStatuscode_NotSuccess_hasBody_emptyInstanceID() {

        AsInstance emptyInstanceID = getAsInstance();
        emptyInstanceID.setAsInstanceid(null);
        when(cnfmUrlProvider.getCreateAsRequestUrl()).thenReturn(createURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        final Optional<AsInstance> returnedContent = cnfmHttpServiceProviderImpl.invokeCreateAsRequest(createAsRequest);
        assertFalse(returnedContent.isPresent());
    }


    @Test
    public void invokeInstantiateAsRequest_withStatuscode_Success() {

        final String asinstanceId = getAsInstance().getAsInstanceid();
        when(cnfmUrlProvider.getInstantiateAsRequestUrl(asinstanceId)).thenReturn(instantiateURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntity.getHeaders()).thenReturn(getHttpHeaders());
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeInstantiateAsRequest(instantiateAsRequest, asinstanceId);
        assertEquals(uri.toString(), returnedContent.orElseThrow().toString());
    }

    @Test
    public void invokeInstantiateAsRequest_withStatuscode_Success_NoStatusURI() {

        final String asinstanceId = getAsInstance().getAsInstanceid();
        final HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setLocation(null);
        when(cnfmUrlProvider.getInstantiateAsRequestUrl(asinstanceId)).thenReturn(instantiateURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeInstantiateAsRequest(instantiateAsRequest, asinstanceId);
        assertFalse(returnedContent.isPresent());
    }

    @Test
    public void invokeInstantiateAsRequest_withStatuscode_NotSuccess_NoStatusURI() {

        final String asinstanceId = getAsInstance().getAsInstanceid();
        when(cnfmUrlProvider.getInstantiateAsRequestUrl(asinstanceId)).thenReturn(instantiateURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeInstantiateAsRequest(instantiateAsRequest, asinstanceId);
        assertFalse(returnedContent.isPresent());
    }

    @Test
    public void test_getOperationJobStatus_statuscode_Accepted() {

        responseEntityAsLCM = getResponseEntityAsLCM(HttpStatus.ACCEPTED);
        when(httpRestServiceProvider.getHttpResponse(Mockito.anyString(), eq(AsLcmOpOcc.class)))
                .thenReturn(responseEntityAsLCM);
        final Optional<AsLcmOpOcc> returnedContent = cnfmHttpServiceProviderImpl.getOperationJobStatus("sample URL");
        assertEquals(returnedContent.orElseThrow().getAsInstanceId(), getAsLcmOpOcc().getAsInstanceId());
    }

    @Test
    public void test_getOperationJobStatus_statuscode_NotFound() {

        responseEntityAsLCM = getResponseEntityAsLCM(HttpStatus.NOT_FOUND);
        when(httpRestServiceProvider.getHttpResponse(Mockito.anyString(), eq(AsLcmOpOcc.class)))
                .thenReturn(responseEntityAsLCM);
        final Optional<AsLcmOpOcc> returnedContent = cnfmHttpServiceProviderImpl.getOperationJobStatus("sample URL");
        assertFalse(returnedContent.isPresent());
    }

    @Test
    public void invokeDeleteAsRequest_withStatuscode_Success() {

        when(cnfmUrlProvider.getDeleteAsRequestUrl(Mockito.anyString())).thenReturn("deleteURL");
        when(httpRestServiceProvider.deleteHttpRequest(Mockito.anyString(), eq(Void.class)))
                .thenReturn(responseEntityVoid);
        when(responseEntityVoid.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        final Optional<Boolean> returnedContent = cnfmHttpServiceProviderImpl.invokeDeleteAsRequest("2345");
        assertTrue(returnedContent.orElseThrow());
    }

    @Test
    public void invokeDeleteAsRequest_withStatuscode_BadRequest() {

        when(cnfmUrlProvider.getDeleteAsRequestUrl(Mockito.anyString())).thenReturn("deleteURL");
        when(httpRestServiceProvider.deleteHttpRequest(Mockito.anyString(), eq(Void.class)))
                .thenReturn(responseEntityVoid);
        when(responseEntityVoid.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        final Optional<Boolean> returnedContent = cnfmHttpServiceProviderImpl.invokeDeleteAsRequest("2345");
        assertTrue(returnedContent.isEmpty());
    }

    @Test
    public void invokeTerminateAsRequest_withStatuscode_Success() {

        when(cnfmUrlProvider.getTerminateAsRequestUrl(Mockito.anyString())).thenReturn("terminateURL");
        when(httpRestServiceProvider.postHttpRequest(Mockito.any(TerminateAsRequest.class), Mockito.anyString(),
                eq(Void.class))).thenReturn(responseEntityVoid);
        when(responseEntityVoid.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntityVoid.getHeaders()).thenReturn(getHttpHeaders());
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeTerminateAsRequest("12356", terminateAsRequest);
        assertTrue(returnedContent.isPresent());
        assertEquals(uri.getPath(), returnedContent.orElseThrow().getPath());
    }

    @Test
    public void invokeTerminateAsRequest_withStatuscode_Success_NullStatusURI() {

        final HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setLocation(null);
        when(cnfmUrlProvider.getTerminateAsRequestUrl(Mockito.anyString())).thenReturn("terminateURL");
        when(httpRestServiceProvider.postHttpRequest(Mockito.any(TerminateAsRequest.class), Mockito.anyString(),
                eq(Void.class))).thenReturn(responseEntityVoid);
        when(responseEntityVoid.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntityVoid.getHeaders()).thenReturn(httpHeaders);
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeTerminateAsRequest("12356", terminateAsRequest);
        assertTrue(returnedContent.isEmpty());
    }

    @Test
    public void invokeTerminateAsRequest_withStatuscode_BadRequest() {

        when(cnfmUrlProvider.getTerminateAsRequestUrl(Mockito.anyString())).thenReturn("terminateURL");
        when(httpRestServiceProvider.postHttpRequest(Mockito.any(TerminateAsRequest.class), Mockito.anyString(),
                eq(Void.class))).thenReturn(responseEntityVoid);
        when(responseEntityVoid.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        // when(responseEntityVoid.getHeaders()).thenReturn(getHttpHeaders());
        final Optional<URI> returnedContent =
                cnfmHttpServiceProviderImpl.invokeTerminateAsRequest("12356", terminateAsRequest);
        assertTrue(returnedContent.isEmpty());
    }

    private AsInstance getAsInstance() {
        AsInstance asInstance = new AsInstance();
        asInstance.setAsInstanceid("12345");
        return asInstance;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(uri);
        return httpHeaders;
    }

    private ResponseEntity<AsLcmOpOcc> getResponseEntityAsLCM(HttpStatus httpStatus) {
        ResponseEntity<AsLcmOpOcc> asLcmOpOccResponseEntity =
                new ResponseEntity<AsLcmOpOcc>(getAsLcmOpOcc(), httpStatus);
        return asLcmOpOccResponseEntity;
    }

    private AsLcmOpOcc getAsLcmOpOcc() {
        AsLcmOpOcc asLcmOpOcc = new AsLcmOpOcc();
        asLcmOpOcc.setAsInstanceId("12345");
        return asLcmOpOcc;
    }
}
