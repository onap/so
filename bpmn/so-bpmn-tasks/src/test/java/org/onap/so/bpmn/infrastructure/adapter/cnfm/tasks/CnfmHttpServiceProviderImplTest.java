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
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.rest.service.HttpRestServiceProvider;
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
    private HttpRestServiceProvider httpRestServiceProvider;
    @Mock
    private ResponseEntity<AsInstance> responseEntity;

    private final String createURL = "http://so-cnfm-lcm.onap:9888/so/so-cnfm/v1/api/aslcm/v1/as_instances";
    private final String instantiateURL =
            "http://so-cnfm-lcm.onap:9888/so/so-cnfm/v1/api/aslcm/v1/as_instances" + getAsInstance().getAsInstanceid()
            + "/instantiate";
    private CnfmHttpServiceProviderImpl cnfmHttpServiceProviderImpl;
    private final URI uri = URI.create("sample");
    private final CreateAsRequest createAsRequest = new CreateAsRequest();
    private final InstantiateAsRequest instantiateAsRequest = new InstantiateAsRequest();

    @Before
    public void setup() {
        cnfmHttpServiceProviderImpl = new CnfmHttpServiceProviderImpl(cnfmUrlProvider, httpRestServiceProvider);
        when(httpRestServiceProvider.postHttpRequest(Mockito.any(), Mockito.anyString(), eq(AsInstance.class))).
                thenReturn(responseEntity);
    }
    @Test
    public void invokeCreateAsRequest_withStatuscodeSuccess_hasBody() {

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
    public void invokeCreateAsRequest_withStatuscodeSuccess_hasBody_emptyInstanceID() {

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
    public void invokeCreateAsRequest_withStatuscodeNotSuccess_hasBody_emptyInstanceID() {

        AsInstance emptyInstanceID = getAsInstance();
        emptyInstanceID.setAsInstanceid(null);
        when(cnfmUrlProvider.getCreateAsRequestUrl()).thenReturn(createURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        final Optional<AsInstance> returnedContent = cnfmHttpServiceProviderImpl.invokeCreateAsRequest(createAsRequest);
        assertFalse(returnedContent.isPresent());
    }


    @Test
    public void InstantiateAsRequest_withStatuscodeSuccess() {
        final String asinstanceId = getAsInstance().getAsInstanceid();
        when(cnfmUrlProvider.getInstantiateAsRequestUrl(asinstanceId)).thenReturn(instantiateURL);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(responseEntity.getHeaders()).thenReturn(getHttpHeaders());
        final Optional<URI> returnedContent = cnfmHttpServiceProviderImpl
                .invokeInstantiateAsRequest(instantiateAsRequest, asinstanceId);
        assertEquals(uri.toString(), returnedContent.orElseThrow().toString());
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
}
