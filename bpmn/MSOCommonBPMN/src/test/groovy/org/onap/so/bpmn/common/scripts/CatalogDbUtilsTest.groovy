/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Nokia.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.logging.filter.base.ONAPComponents;
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.core.env.Environment
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.mockito.Mockito.verify

class CatalogDbUtilsTest {

    private static final String AUTHORIZATION_HEADER = "AuthHeaderTest"
    private static final String RESPONSE_FROM_CATALOG_DB = "{\"serviceVnfs\": [{\"name\": \"service1\"," +
            "\"vfModules\": [{\"name\": \"module1\", \"isBase\":true, \"initialCount\":1}]}]}"
    private HttpClientFactory httpClientFactoryMock
    private JsonUtils jsonUtilsMock
    private HttpClient httpClientMock
    private DelegateExecutionFake executionFake
    private CatalogDbUtils testedObject


    @Before
    void setUp() {
        httpClientFactoryMock = mock(HttpClientFactory.class)
        jsonUtilsMock = mock(JsonUtils.class)
        httpClientMock = mock(HttpClient.class)
        executionFake = new DelegateExecutionFake()
        testedObject = new CatalogDbUtils(httpClientFactoryMock, jsonUtilsMock)
    }

    @Test
    void getAllVnfsByVnfModelCustomizationUuid_CatVer1_success() {
        // given
        executionFake.setVariable("BasicAuthHeaderValueDB", AUTHORIZATION_HEADER)
        mockGetResponseFromCatalogDb("http://testUrl/v2/serviceVnfs?vnfModelCustomizationUuid=testModel")
        //when
        JSONArray vnfsListResult = testedObject.getAllVnfsByVnfModelCustomizationUuid(executionFake, "testModel", "v1")
        //then
        verifyHeadersInHttpClient()
        JSONAssert.assertEquals("[{\"vfModules\":[{\"initialCount\":1,\"modelInfo\":{\"modelType\":\"vfModule\"},\"isBase\":true}],\"modelInfo\":{\"modelType\":\"vnf\"}}]", vnfsListResult, JSONCompareMode.LENIENT)
    }

    @Test
    void getAllVnfsByVnfModelCustomizationUuid_CatVer2_success() {
        // given
        executionFake.setVariable("BasicAuthHeaderValueDB", AUTHORIZATION_HEADER)
        mockGetResponseFromCatalogDb("http://testUrl/v2/serviceVnfs?vnfModelCustomizationUuid=testModel")
        // when
        JSONArray vnfsListResult = testedObject.getAllVnfsByVnfModelCustomizationUuid(executionFake, "testModel", "v2")
        // then
        verifyHeadersInHttpClient()
        JSONAssert.assertEquals("[{\"vfModules\":[{\"initialCount\":1,\"name\":\"module1\",\"isBase\":true}],\"name\":\"service1\"}]", vnfsListResult, JSONCompareMode.LENIENT)
    }

    @Test
    void getServiceResourcesByServiceModelUuid_success() {
        // given
        executionFake.setVariable("BasicAuthHeaderValueDB", AUTHORIZATION_HEADER)
        mockGetResponseFromCatalogDb("http://testUrl/v2/serviceResources?serviceModelUuid=testModel")
        // when
        JSONObject result = testedObject.getServiceResourcesByServiceModelUuid(executionFake, "testModel", "v2")
        // then
        verifyHeadersInHttpClient()
        JSONAssert.assertEquals("{\"serviceVnfs\": [{\"name\": \"service1\",\"vfModules\": [{\"name\": \"module1\", \"isBase\":true, \"initialCount\":1}]}]}", result, JSONCompareMode.LENIENT)
    }


    @Test
    void getServiceResourcesByServiceModelInvariantUuidString_success() {
        // given
        executionFake.setVariable("BasicAuthHeaderValueDB", AUTHORIZATION_HEADER)
        mockGetResponseFromCatalogDb("http://testUrl/v2/serviceResources?serviceModelInvariantUuid=testModel")
        // when
        String result = testedObject.getServiceResourcesByServiceModelInvariantUuidString(executionFake, "testModel")
        // then
        verifyHeadersInHttpClient()
        assertThat(result).isEqualTo(RESPONSE_FROM_CATALOG_DB)
    }

    private Environment createEnvironmentMock() {
        Environment mockEnvironment = mock(Environment.class)
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
        return mockEnvironment
    }

    private void mockGetResponseFromCatalogDb(String queryEndpoint) {
        Environment environmentMock = createEnvironmentMock()
        when(environmentMock.getProperty("mso.catalog.db.endpoint")).thenReturn("http://testUrl")
        when(httpClientFactoryMock.newJsonClient(new URL(queryEndpoint), ONAPComponents.CATALOG_DB)).thenReturn(httpClientMock)

        Response responseMock = mock(Response.class)
        when(httpClientMock.get()).thenReturn(responseMock)
        when(responseMock.readEntity(String.class)) thenReturn(RESPONSE_FROM_CATALOG_DB)
        when(responseMock.getStatus()).thenReturn(200)
    }

    private void verifyHeadersInHttpClient() {
        verify(httpClientMock).addAdditionalHeader("X-FromAppId", "BPMN")
        verify(httpClientMock).addAdditionalHeader("Accept", MediaType.APPLICATION_JSON)
        verify(httpClientMock).addAdditionalHeader("Authorization", AUTHORIZATION_HEADER)
    }

}
