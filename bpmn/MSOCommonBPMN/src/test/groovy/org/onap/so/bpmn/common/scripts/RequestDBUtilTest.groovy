/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.db.request.beans.OperationStatus
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class RequestDBUtilTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeleteSliceServiceTest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void prepareUpdateOperationStatus(){
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://sdc-wfd-be:8080")
        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId("testServiceId")
        operationStatus.setOperationId("testOperationId")
        operationStatus.setUserId("testUserId")
        operationStatus.setOperation("testOpertation")
        operationStatus.setResult("testResult")
        operationStatus.setProgress("testProgress")
        operationStatus.setOperationContent("testOperationContent")
        operationStatus.setReason("testReason")

        RequestDBUtil requestDBUtil = new RequestDBUtil()
        requestDBUtil.prepareUpdateOperationStatus(mockExecution, operationStatus)

        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus = captor.getAllValues()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testGetOperationStatus(){
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://sdc-wfd-be:8080")

        RequestDBUtil requestDBUtil = new RequestDBUtil()
        requestDBUtil.getOperationStatus(mockExecution, "testServiceId","testOperationId")

        Mockito.verify(mockExecution,times(1)).setVariable(eq("getOperationStatus"), captor.capture())
        String getOperationStatus = captor.getAllValues()
        assertNotNull(getOperationStatus)

    }
}
