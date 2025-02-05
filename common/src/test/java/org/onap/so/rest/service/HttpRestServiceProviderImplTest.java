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

package org.onap.so.rest.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


/**
 * @author waqas.ikram@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRestServiceProviderImplTest {

    private static final String BODY = "{}";
    private static final String DUMMY_URL = "http://localhost:9000/dummy/url";

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private ResponseEntity<String> mockEntity;

    @Mock
    private HttpStatusCodeException httpStatusCodeException;

    @Test
    public void test_get_returnOptionalPresentIfResponseIsOKAndHasBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(true);
        when(mockEntity.getBody()).thenReturn(BODY);

        final Optional<String> actual = objUnderTest.get(DUMMY_URL, String.class);

        assertTrue(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void test_get_returnOptionalPresentIfResponseIsNotOK() {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        final Optional<String> actual = objUnderTest.get(DUMMY_URL, String.class);

        assertFalse(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void test_get_returnOptionalPresentIfResponseIsOKAndNoBody() {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(false);

        final Optional<String> actual = objUnderTest.get(DUMMY_URL, String.class);

        assertFalse(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(String.class));
    }

    @Test(expected = InvalidRestRequestException.class)
    public void test_get_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionWithHttpStatusBadRequest() {
        assertGetErrorScenario(HttpStatus.BAD_REQUEST);

    }

    @Test(expected = HttpResouceNotFoundException.class)
    public void test_get_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionWithHttpStatusNotFoundHttpStatus() {
        assertGetErrorScenario(HttpStatus.NOT_FOUND);
    }

    @Test(expected = RestProcessingException.class)
    public void test_get_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionOccured() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.CONFLICT);
        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpClientErrorException);

        objUnderTest.get(DUMMY_URL, String.class);
    }

    @Test(expected = RestProcessingException.class)
    public void test_get_ThrowsInvalidRestRequestExceptionifRestProcessingExceptionOccured() {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(RestClientException.class);

        objUnderTest.get(DUMMY_URL, String.class);
    }

    @Test
    public void test_post_returnOptionalPresentIfResponseIsOKAndHasBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(true);
        when(mockEntity.getBody()).thenReturn(BODY);

        final Optional<String> actual = objUnderTest.post(BODY, DUMMY_URL, String.class);

        assertTrue(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void test_post_returnOptionalPresentIfResponseIsOKAndHasNoBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(false);

        final Optional<String> actual = objUnderTest.post(BODY, DUMMY_URL, String.class);

        assertFalse(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void test_put_returnOptionalPresentIfResponseIsOKAndHasBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(true);
        when(mockEntity.getBody()).thenReturn(BODY);

        final Optional<String> actual = objUnderTest.put(BODY, DUMMY_URL, String.class);

        assertTrue(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.PUT), any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void test_put_returnOptionalPresentIfResponseIsOKAndHasNoBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockEntity.hasBody()).thenReturn(false);

        final Optional<String> actual = objUnderTest.put(BODY, DUMMY_URL, String.class);

        assertFalse(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.PUT), any(HttpEntity.class),
                eq(String.class));
    }


    @Test
    public void test_post_returnOptionalPresentIfResponseIsNotOKAndHasBody() {

        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockEntity);

        when(mockEntity.getStatusCode()).thenReturn(HttpStatus.PARTIAL_CONTENT);

        final Optional<String> actual = objUnderTest.post(BODY, DUMMY_URL, String.class);

        assertFalse(actual.isPresent());
        verify(mockRestTemplate, atLeastOnce()).exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(String.class));
    }

    @Test(expected = InvalidRestRequestException.class)
    public void test_post_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionWithHttpStatusBadRequest() {
        assertPostErrorScenario(HttpStatus.BAD_REQUEST);

    }

    @Test(expected = HttpResouceNotFoundException.class)
    public void test_post_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionWithHttpStatusNotFoundHttpStatus() {
        assertPostErrorScenario(HttpStatus.NOT_FOUND);
    }

    @Test(expected = RestProcessingException.class)
    public void test_post_ThrowsInvalidRestRequestExceptionifHttpClientErrorExceptionOccured() {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.CONFLICT);
        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpClientErrorException);

        objUnderTest.post(BODY, DUMMY_URL, String.class);
    }

    @Test(expected = RestProcessingException.class)
    public void test_post_ThrowsInvalidRestRequestExceptionifRestProcessingExceptionOccured() {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(RestClientException.class);

        objUnderTest.post(BODY, DUMMY_URL, String.class);
    }

    private void assertPostErrorScenario(final HttpStatus status) {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        final HttpClientErrorException errorException = new HttpClientErrorException(status);
        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(errorException);

        objUnderTest.post(BODY, DUMMY_URL, String.class);
    }

    private void assertGetErrorScenario(final HttpStatus status) {
        final HttpRestServiceProvider objUnderTest = new HttpRestServiceProviderImpl(mockRestTemplate);

        final HttpClientErrorException errorException = new HttpClientErrorException(status);
        when(mockRestTemplate.exchange(eq(DUMMY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(errorException);

        objUnderTest.get(DUMMY_URL, String.class);
    }

}
