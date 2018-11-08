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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Test
import org.onap.logging.ref.slf4j.ONAPLogConstants
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.utils.TargetEntity
import org.springframework.http.HttpStatus

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.BDDMockito.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times

class ExternalAPIUtilTest {

    private static final String URL = "http://someUrl"
    private static final String UUID = "UUID666"
    private static final HttpStatus HTTP_STATUS = HttpStatus.ACCEPTED
    private static final String BODY_PAYLOAD = "payload"

    @Test
    void executeExternalAPIGetCall_shouldPerformRestGetCall_withAuthorizationHeaderSet() {
        // GIVEN
        HttpClient httpClient = givenHttpClientWith({ HttpClient httpClient -> httpClient.get() },
                HTTP_STATUS, BODY_PAYLOAD)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.create(new URL(URL), MediaType.APPLICATION_JSON, TargetEntity.EXTERNAL)).willReturn(httpClient)

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID), new ExceptionUtil())
        Response apiResponse = externalAPIUtil.executeExternalAPIGetCall(createDelegateExecution(), URL)

        // THEN
        then(httpClient).should(times(1)).addBasicAuthHeader("value_externalapi_auth", "value_mso_msoKey")
        then(httpClient).should(times(1)).addAdditionalHeader("X-FromAppId", "MSO")
        then(httpClient).should(times(1)).addAdditionalHeader(ONAPLogConstants.Headers.REQUEST_ID, UUID)
        then(httpClient).should(times(1)).addAdditionalHeader("Accept", MediaType.APPLICATION_JSON)
        ResponseAssert.assertThat(apiResponse)
                .hasStatusCode(HTTP_STATUS)
                .hasBody(BODY_PAYLOAD)
    }

    @Test
    void executeExternalAPIGetCall_shouldHandleExceptionsThrownByGetCall() {
        // GIVEN
        HttpClient httpClient = mock(HttpClient.class)
        willThrow(new RuntimeException("error occurred")).given(httpClient).get()
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.create(new URL(URL), MediaType.APPLICATION_JSON, TargetEntity.EXTERNAL)).willReturn(httpClient)
        DelegateExecution execution = createDelegateExecution()
        DummyExceptionUtil exceptionUtil = new DummyExceptionUtil(execution, 9999, "error occurred")

        // WHEN // THEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID), exceptionUtil)
        externalAPIUtil.executeExternalAPIGetCall(execution, URL)
    }

    @Test
    void executeExternalAPIPostCall_shouldHandleExceptionsThrownByPostCall() {
        // GIVEN
        HttpClient httpClient = mock(HttpClient.class)
        willThrow(new RuntimeException("error occurred")).given(httpClient).post(BODY_PAYLOAD)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.create(new URL(URL), MediaType.APPLICATION_JSON, TargetEntity.AAI)).willReturn(httpClient)
        DelegateExecution execution = createDelegateExecution()
        DummyExceptionUtil exceptionUtil = new DummyExceptionUtil(execution, 9999, "error occurred")

        // WHEN // THEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID), exceptionUtil)
        externalAPIUtil.executeExternalAPIPostCall(execution, URL, BODY_PAYLOAD)
    }

    @Test
    void executeExternalAPIPostCall_shouldPerformRestPostCall_withPayloadAndAuthorizationHeaderSet() {
        // GIVEN
        HttpClient httpClient = givenHttpClientWith({ HttpClient httpClient -> httpClient.post(BODY_PAYLOAD) },
                HTTP_STATUS, BODY_PAYLOAD)
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class)
        given(httpClientFactory.create(new URL(URL), MediaType.APPLICATION_JSON, TargetEntity.AAI)).willReturn(httpClient)

        // WHEN
        ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(httpClientFactory, new DummyMsoUtils(UUID), new ExceptionUtil())
        Response apiResponse = externalAPIUtil.executeExternalAPIPostCall(createDelegateExecution(), URL, BODY_PAYLOAD)

        // THEN
        then(httpClient).should(times(1)).addBasicAuthHeader("value_externalapi_auth", "value_mso_msoKey")
        then(httpClient).should(times(1)).addAdditionalHeader("X-FromAppId", "MSO")
        then(httpClient).should(times(1)).addAdditionalHeader("X-TransactionId", UUID)
        ResponseAssert.assertThat(apiResponse)
                .hasStatusCode(HTTP_STATUS)
                .hasBody(BODY_PAYLOAD)
    }

    private HttpClient givenHttpClientWith(Closure<Response> method, HttpStatus httpStatus, String body) {
        Response expectedAPIResponse = mock(Response.class)
        given(expectedAPIResponse.getStatus()).willReturn(httpStatus.value())
        given(expectedAPIResponse.getEntity()).willReturn(body)
        HttpClient httpClient = mock(HttpClient.class)
        given(method.call(httpClient)).willReturn(expectedAPIResponse)
        return httpClient
    }

    private DelegateExecution createDelegateExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class)
        given(delegateExecution.getVariable("URN_externalapi_auth")).willReturn("value_externalapi_auth")
        given(delegateExecution.getVariable("URN_mso_msoKey")).willReturn("value_mso_msoKey")
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

        private final DelegateExecution execution
        private final int errorCode
        private final String errorMessage

        DummyExceptionUtil(DelegateExecution execution, int errorCode, String errorMessage) {
            this.execution = execution
            this.errorCode = errorCode
            this.errorMessage = errorMessage
        }

        @Override
        void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
            assertThat(execution).isSameAs(this.execution)
            assertThat(errorCode).isEqualTo(this.errorCode)
            assertThat(errorMessage).isEqualTo(this.errorMessage)
        }
    }
}