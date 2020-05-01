/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.DUMMY_GENERIC_VND_ID;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.DUMMY_JOB_ID;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.getVnfmBasicHttpConfigProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.google.common.base.Optional;


/**
 * @author waqas.ikram@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class VnfmAdapterServiceProviderImplTest {

    private static final String EMPTY_JOB_ID = "";

    private static final CreateVnfRequest CREATE_VNF_REQUEST = new CreateVnfRequest();

    @Mock
    private HttpRestServiceProvider mockedHttpServiceProvider;

    @Mock
    private ResponseEntity<CreateVnfResponse> mockedResponseEntity;

    @Mock
    private ResponseEntity<DeleteVnfResponse> deleteVnfResponse;
    @Mock
    private ResponseEntity<QueryJobResponse> mockedQueryJobResponseResponseEntity;

    @Test
    public void testInvokeCreateInstantiationRequest_httpServiceProviderReturnsStatusAcceptedWithBody_validResponse() {

        when(mockedHttpServiceProvider.postHttpRequest(eq(CREATE_VNF_REQUEST), anyString(),
                eq(CreateVnfResponse.class))).thenReturn(mockedResponseEntity);
        when(mockedResponseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockedResponseEntity.hasBody()).thenReturn(true);
        final CreateVnfResponse response = getCreateVnfResponse(DUMMY_JOB_ID);
        when(mockedResponseEntity.getBody()).thenReturn(response);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<CreateVnfResponse> actual =
                objUnderTest.invokeCreateInstantiationRequest(DUMMY_GENERIC_VND_ID, CREATE_VNF_REQUEST);
        assertTrue(actual.isPresent());
        assertEquals(actual.get(), response);
    }

    @Test
    public void testInvokeCreateInstantiationRequest_httpServiceProviderReturnsStatusAcceptedWithNoBody_noResponse() {
        assertWithStatuCode(HttpStatus.ACCEPTED);
    }

    @Test
    public void testInvokeCreateInstantiationRequest_httpServiceProviderReturnsStatusNotOkWithNoBody_noResponse() {
        assertWithStatuCode(HttpStatus.UNAUTHORIZED);
    }


    @Test
    public void testInvokeCreateInstantiationRequest_httpServiceProviderReturnsStatusAcceptedWithBodyWithInvalidJobId_noResponse() {
        assertWithJobId(null);
        assertWithJobId(EMPTY_JOB_ID);
    }

    @Test
    public void testInvokeCreateInstantiationRequest_httpServiceProviderThrowException_httpRestServiceProviderNotNull() {

        when(mockedHttpServiceProvider.postHttpRequest(eq(CREATE_VNF_REQUEST), anyString(),
                eq(CreateVnfResponse.class))).thenThrow(RestProcessingException.class);


        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<CreateVnfResponse> actual =
                objUnderTest.invokeCreateInstantiationRequest(DUMMY_GENERIC_VND_ID, CREATE_VNF_REQUEST);
        assertFalse(actual.isPresent());

    }

    @Test
    public void testInvokeDeleteRequest() {
        when(mockedHttpServiceProvider.deleteHttpRequest(anyString(), eq(DeleteVnfResponse.class)))
                .thenReturn(deleteVnfResponse);
        when(deleteVnfResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(deleteVnfResponse.hasBody()).thenReturn(true);
        final DeleteVnfResponse response = getDeleteVnfResponse(DUMMY_JOB_ID);
        when(deleteVnfResponse.getBody()).thenReturn(response);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<DeleteVnfResponse> actual = objUnderTest.invokeDeleteRequest(DUMMY_GENERIC_VND_ID);
        assertTrue(actual.isPresent());
    }

    @Test
    public void testInvokeDeleteRequestNotAccepted() {
        when(mockedHttpServiceProvider.deleteHttpRequest(anyString(), eq(DeleteVnfResponse.class)))
                .thenReturn(deleteVnfResponse);
        when(deleteVnfResponse.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<DeleteVnfResponse> actual = objUnderTest.invokeDeleteRequest(DUMMY_GENERIC_VND_ID);
        assertFalse(actual.isPresent());
    }

    @Test
    public void testInvokeDeleteRequestNoBody() {
        when(mockedHttpServiceProvider.deleteHttpRequest(anyString(), eq(DeleteVnfResponse.class)))
                .thenReturn(deleteVnfResponse);
        when(deleteVnfResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(deleteVnfResponse.hasBody()).thenReturn(false);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<DeleteVnfResponse> actual = objUnderTest.invokeDeleteRequest(DUMMY_GENERIC_VND_ID);
        assertFalse(actual.isPresent());
    }

    @Test
    public void testInvokeDeleteRequestNoJobId() {
        when(mockedHttpServiceProvider.deleteHttpRequest(anyString(), eq(DeleteVnfResponse.class)))
                .thenReturn(deleteVnfResponse);
        when(deleteVnfResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(deleteVnfResponse.hasBody()).thenReturn(true);
        final DeleteVnfResponse response = getDeleteVnfResponse("");
        when(deleteVnfResponse.getBody()).thenReturn(response);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<DeleteVnfResponse> actual = objUnderTest.invokeDeleteRequest(DUMMY_GENERIC_VND_ID);
        assertFalse(actual.isPresent());
    }

    @Test
    public void testInvokeDeleteRequestException() {
        when(mockedHttpServiceProvider.deleteHttpRequest(anyString(), eq(DeleteVnfResponse.class)))
                .thenThrow(RestProcessingException.class);
        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);
        final Optional<DeleteVnfResponse> actual = objUnderTest.invokeDeleteRequest(DUMMY_GENERIC_VND_ID);
        assertFalse(actual.isPresent());
    }

    @Test
    public void testGetInstantiateOperationJobStatus_httpServiceProviderReturnsStatusOkWithBody_validResponse() {

        when(mockedQueryJobResponseResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockedQueryJobResponseResponseEntity.hasBody()).thenReturn(true);
        when(mockedQueryJobResponseResponseEntity.getBody()).thenReturn(getQueryJobResponse());

        when(mockedHttpServiceProvider.getHttpResponse(eq(TestConstants.JOB_STATUS_EXPECTED_URL),
                eq(QueryJobResponse.class))).thenReturn(mockedQueryJobResponseResponseEntity);

        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<QueryJobResponse> actual = objUnderTest.getInstantiateOperationJobStatus(DUMMY_JOB_ID);
        assertTrue(actual.isPresent());
        final QueryJobResponse actualQueryJobResponse = actual.get();
        assertNotNull(actualQueryJobResponse);
    }

    @Test
    public void testGetInstantiateOperationJobStatus_httpServiceProviderReturnsStatusOkWithOutBody_invalidResponse() {

        when(mockedQueryJobResponseResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockedQueryJobResponseResponseEntity.hasBody()).thenReturn(false);

        when(mockedHttpServiceProvider.getHttpResponse(eq(TestConstants.JOB_STATUS_EXPECTED_URL),
                eq(QueryJobResponse.class))).thenReturn(mockedQueryJobResponseResponseEntity);

        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<QueryJobResponse> actual = objUnderTest.getInstantiateOperationJobStatus(DUMMY_JOB_ID);
        assertFalse(actual.isPresent());
    }

    @Test
    public void testGetInstantiateOperationJobStatus_httpServiceProviderReturnsStatusNotOkWithOutBody_invalidResponse() {

        when(mockedQueryJobResponseResponseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        when(mockedHttpServiceProvider.getHttpResponse(eq(TestConstants.JOB_STATUS_EXPECTED_URL),
                eq(QueryJobResponse.class))).thenReturn(mockedQueryJobResponseResponseEntity);

        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<QueryJobResponse> actual = objUnderTest.getInstantiateOperationJobStatus(DUMMY_JOB_ID);
        assertFalse(actual.isPresent());
    }

    @Test(expected = RestProcessingException.class)
    public void testGetInstantiateOperationJobStatus_Exception() {

        when(mockedHttpServiceProvider.getHttpResponse(eq(TestConstants.JOB_STATUS_EXPECTED_URL),
                eq(QueryJobResponse.class))).thenThrow(RestProcessingException.class);

        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        objUnderTest.getInstantiateOperationJobStatus(DUMMY_JOB_ID);
    }

    private QueryJobResponse getQueryJobResponse() {
        return new QueryJobResponse().id(DUMMY_JOB_ID).operationState(OperationStateEnum.COMPLETED);

    }

    private void assertWithJobId(final String jobId) {
        when(mockedHttpServiceProvider.postHttpRequest(eq(CREATE_VNF_REQUEST), anyString(),
                eq(CreateVnfResponse.class))).thenReturn(mockedResponseEntity);
        when(mockedResponseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockedResponseEntity.hasBody()).thenReturn(true);
        final CreateVnfResponse response = getCreateVnfResponse(jobId);
        when(mockedResponseEntity.getBody()).thenReturn(response);


        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<CreateVnfResponse> actual =
                objUnderTest.invokeCreateInstantiationRequest(DUMMY_GENERIC_VND_ID, CREATE_VNF_REQUEST);
        assertFalse(actual.isPresent());
    }

    private void assertWithStatuCode(final HttpStatus status) {
        when(mockedHttpServiceProvider.postHttpRequest(eq(CREATE_VNF_REQUEST), anyString(),
                eq(CreateVnfResponse.class))).thenReturn(mockedResponseEntity);
        when(mockedResponseEntity.getStatusCode()).thenReturn(status);
        when(mockedResponseEntity.hasBody()).thenReturn(false);

        final VnfmAdapterServiceProvider objUnderTest =
                new VnfmAdapterServiceProviderImpl(getVnfmAdapterUrlProvider(), mockedHttpServiceProvider);

        final Optional<CreateVnfResponse> actual =
                objUnderTest.invokeCreateInstantiationRequest(DUMMY_GENERIC_VND_ID, CREATE_VNF_REQUEST);
        assertFalse(actual.isPresent());
    }

    private CreateVnfResponse getCreateVnfResponse(final String jobId) {
        return new CreateVnfResponse().jobId(jobId);
    }

    private DeleteVnfResponse getDeleteVnfResponse(final String jobId) {
        final DeleteVnfResponse response = new DeleteVnfResponse();
        response.setJobId(jobId);
        return response;
    }

    private VnfmAdapterUrlProvider getVnfmAdapterUrlProvider() {
        return new VnfmAdapterUrlProvider(getVnfmBasicHttpConfigProvider());
    }
}
