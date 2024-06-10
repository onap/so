/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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
package org.onap.so.bpmn.common.scripts

import org.assertj.core.api.AbstractAssert
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Test
import org.onap.logging.ref.slf4j.ONAPLogConstants
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ONAPComponents;
import org.springframework.http.HttpStatus

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.catchThrowableOfType
import static org.mockito.BDDMockito.given
import static org.mockito.BDDMockito.then
import static org.mockito.BDDMockito.willThrow
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times

class ExternalAPIUtilTest {

    private static final String URL = "http://someUrl"
    private static final String UUID_STR = UUID.nameUUIDFromBytes("deterministic_uuid".getBytes())
    private static final String BODY_PAYLOAD = "payload"

    @Test
    void executeExternalAPIGetCall_shouldPerformRestGetCall_withAuthorizationHeaderSet() {
        // GIVEN
        Response expectedResponse = createExpectedResponse(HttpStatus.ACCEPTED, BODY_PAYLOAD)
        HttpClient httpClient = mock(HttpClient.class)
        given(httpClient.get()).willReturn(expectedResponse)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.newJsonClient(new URL(URL), ONAPComponents.EXTERNAL)).willReturn(httpClient)

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID_STR), new ExceptionUtil())
        Response apiResponse = externalAPIUtil.executeExternalAPIGetCall(createDelegateExecution(), URL)

        // THEN
        then(httpClient).should(times(1)).addBasicAuthHeader("value_externalapi_auth", "value_mso_msoKey")
        then(httpClient).should(times(1)).addAdditionalHeader("X-FromAppId", "MSO")
        then(httpClient).should(times(1)).addAdditionalHeader(ONAPLogConstants.Headers.REQUEST_ID, UUID_STR)
        then(httpClient).should(times(1)).addAdditionalHeader("Accept", MediaType.APPLICATION_JSON)
        ResponseAssert.assertThat(apiResponse)
                .hasStatusCode(HttpStatus.ACCEPTED)
                .hasBody(BODY_PAYLOAD)
    }

    @Test
    void executeExternalAPIGetCall_shouldHandleExceptionsThrownByGetCall_andRethrowAsBpmnError() {
        // GIVEN
        HttpClient httpClient = mock(HttpClient.class)
        willThrow(new RuntimeException("error occurred")).given(httpClient).get()
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.newJsonClient(new URL(URL), ONAPComponents.EXTERNAL)).willReturn(httpClient)
        DelegateExecution delegateExecution = createDelegateExecution()
        DummyExceptionUtil exceptionUtil = new DummyExceptionUtil()

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID_STR), exceptionUtil)
        BpmnError bpmnError = catchThrowableOfType({ ->
            externalAPIUtil.executeExternalAPIGetCall(delegateExecution, URL)
        }, BpmnError.class)

        // THEN
        assertThat(exceptionUtil.getDelegateExecution()).isSameAs(delegateExecution)
        assertThat(bpmnError.getMessage()).isEqualTo("error occurred")
        assertThat(bpmnError.getErrorCode()).isEqualTo("9999")
    }

    @Test
    void executeExternalAPIPostCall_shouldHandleExceptionsThrownByPostCall_andRethrowAsBpmnError() {
        // GIVEN
        HttpClient httpClient = mock(HttpClient.class)
        willThrow(new RuntimeException("error occurred")).given(httpClient).post(BODY_PAYLOAD)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.newJsonClient(new URL(URL), ONAPComponents.AAI)).willReturn(httpClient)
        DelegateExecution delegateExecution = createDelegateExecution()
        DummyExceptionUtil exceptionUtil = new DummyExceptionUtil()

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID_STR), exceptionUtil)
        BpmnError bpmnError = catchThrowableOfType({ ->
            externalAPIUtil.executeExternalAPIPostCall(delegateExecution, URL, BODY_PAYLOAD)
        }, BpmnError.class)

        // THEN
        assertThat(exceptionUtil.getDelegateExecution()).isSameAs(delegateExecution)
        assertThat(bpmnError.getMessage()).isEqualTo("error occurred")
        assertThat(bpmnError.getErrorCode()).isEqualTo("9999")
    }

    @Test
    void executeExternalAPIPostCall_shouldPerformRestPostCall_withPayloadAndAuthorizationHeaderSet() {
        // GIVEN
        Response expectedResponse = createExpectedResponse(HttpStatus.ACCEPTED, BODY_PAYLOAD)
        HttpClient httpClient = mock(HttpClient.class)
        given(httpClient.post(BODY_PAYLOAD)).willReturn(expectedResponse)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.newJsonClient(new URL(URL), ONAPComponents.AAI)).willReturn(httpClient)

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID_STR), new ExceptionUtil())
        Response apiResponse = externalAPIUtil.executeExternalAPIPostCall(createDelegateExecution(), URL, BODY_PAYLOAD)

        // THEN
        then(httpClient).should(times(1)).addBasicAuthHeader("value_externalapi_auth", "value_mso_msoKey")
        then(httpClient).should(times(1)).addAdditionalHeader("X-FromAppId", "MSO")
        ResponseAssert.assertThat(apiResponse)
                .hasStatusCode(HttpStatus.ACCEPTED)
                .hasBody(BODY_PAYLOAD)
    }

    private Response createExpectedResponse(HttpStatus httpStatus, String body) {
        Response expectedResponse = mock(Response.class)
        given(expectedResponse.getStatus()).willReturn(httpStatus.value())
        given(expectedResponse.getEntity()).willReturn(body)
        return expectedResponse
    }

    private DelegateExecution createDelegateExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class)
        given(delegateExecution.getVariable("URN_externalapi_auth")).willReturn("value_externalapi_auth")
        given(delegateExecution.getVariable("URN_mso_msoKey")).willReturn("value_mso_msoKey")
		given(delegateExecution.getVariable("SPPartnerUrl")).willReturn("http://LocalExtAPIURL:8080")
        return delegateExecution
    }

    private static class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

        ResponseAssert(Response response) {
            super(response, ResponseAssert.class)
        }

        static ResponseAssert assertThat(Response response) {
            return new ResponseAssert(response)
        }

        ResponseAssert hasStatusCode(HttpStatus httpStatus) {
            assertThat(actual.getStatus()).isEqualTo(httpStatus.value())
            return this
        }

        ResponseAssert hasBody(String responseBody) {
            assertThat(actual.getEntity()).isEqualTo(responseBody)
            return this
        }
    }

    private static class DummyMsoUtils extends MsoUtils {

        private final String uuid

        DummyMsoUtils(String uuid) {
            this.uuid = uuid
        }

        String getRequestID() {
            return uuid
        }
    }

    private static class DummyExceptionUtil extends ExceptionUtil {

        private DelegateExecution delegateExecution

        @Override
        void buildAndThrowWorkflowException(DelegateExecution delegateExecution, int errorCode, String errorMessage) {
            this.delegateExecution = delegateExecution
            throw new BpmnError(String.valueOf(errorCode), errorMessage)
        }

        DelegateExecution getDelegateExecution() {
            return delegateExecution
        }
    }
}