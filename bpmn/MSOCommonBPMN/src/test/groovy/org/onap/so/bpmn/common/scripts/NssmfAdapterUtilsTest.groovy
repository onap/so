/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory

import javax.ws.rs.core.Response

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class NssmfAdapterUtilsTest extends MsoGroovyTest {

    @Mock
    HttpClientFactory httpClientFactory

    @Mock
    HttpClient httpClient

    NssmfAdapterUtils nssmfAdapterUtils

    @Before
    void init() throws IOException {
        super.init("NssmfAdapterUtils")
        MockitoAnnotations.initMocks(this)
        nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, new JsonUtils())
    }

    @Test(expected = Exception.class)
    void testSendPostRequestNSSMF_missingEndpoint() {
        ExecutionEntity mockExecution = setupMock("NssmfAdapterUtils")
        when(mockExecution.getVariable("BasicAuthHeaderValue")).thenReturn("Basic dGVzdDp0ZXN0")

        // No NSSMF endpoint configured - will throw exception
        nssmfAdapterUtils.sendPostRequestNSSMF(mockExecution, "/api/test", '{"test": "data"}')
    }
}
