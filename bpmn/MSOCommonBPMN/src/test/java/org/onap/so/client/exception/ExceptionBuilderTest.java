/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.BaseTest;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.mock.FileUtil;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ExceptionBuilderTest extends BaseTest {

    private static final String RESOURCE_PATH = "__files/";
    private static final String VALID_ERROR_MESSAGE = "{test error message}";

    @Mock
    protected ExtractPojosForBB extractPojosForBB;


    @Spy
    @InjectMocks
    private ExceptionBuilder exceptionBuilder = new ExceptionBuilder();

    GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();

    @Before
    public void before() throws BBObjectNotFoundException, JsonProcessingException {
        setCloudRegion();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID)))
                .thenReturn(buildVfModule());
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        auditList.setAuditType("create");
        auditList.setHeatStackName("testStackName");
        AAIObjectAudit vserver = new AAIObjectAudit();
        vserver.setAaiObjectType(AAIObjectType.VSERVER.typeName());
        vserver.setDoesObjectExist(false);
        Vserver vs = new Vserver();
        vs.setVserverId("testVServerId");
        Vserver vServerShallow = new Vserver();
        BeanUtils.copyProperties(vs, vServerShallow, "LInterfaces");
        vserver.setAaiObject(vServerShallow);
        auditList.getAuditList().add(vserver);

        execution.setVariable("auditInventoryResult", objectMapper.getMapper().writeValueAsString(auditList));
    }


    @Test
    public void buildAndThrowWorkflowExceptionTest() {
        try {
            ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    new NullPointerException(VALID_ERROR_MESSAGE));
        } catch (BpmnError bpmnException) {
            assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
        }
    }

    @Test
    public void buildAndThrowWorkflowExceptionInvalidMessageTest() {
        try {
            ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
            String invalidErrorMessage = FileUtil.readResourceFile(RESOURCE_PATH + "invalidErrorMessage.txt");
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    new NullPointerException(invalidErrorMessage));
        } catch (BpmnError bpmnException) {
            assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
        }
    }

    @Test
    public void buildAndThrowWorkflowExceptionInvalidMessageFlagTest() {
        try {
            ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
            String invalidErrorMessage = FileUtil.readResourceFile(RESOURCE_PATH + "invalidErrorMessageFlag.txt");
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    new NullPointerException(invalidErrorMessage));
        } catch (BpmnError bpmnException) {
            assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
        }
    }

    @Test
    public void buildAndThrowWorkflowExceptionNullMessageTest() {
        try {
            ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, new NullPointerException());
        } catch (BpmnError bpmnException) {
            assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
        }
    }

    @Test
    public void processAuditExceptionTest() {
        try {
            Mockito.doReturn(extractPojosForBB).when(exceptionBuilder).getExtractPojosForBB();
            exceptionBuilder.processAuditException((DelegateExecutionImpl) execution, false);
        } catch (BpmnError bpmnException) {
            assertEquals("AAIInventoryFailure", bpmnException.getErrorCode());
            WorkflowException we = execution.getVariable("WorkflowException");
            assertNotNull(we);
            assertEquals(
                    "create VF-Module testVfModuleId1 failed due to incomplete AAI vserver inventory population after stack testStackName was successfully created in cloud region testLcpCloudRegionId. MSO Audit indicates that the following was not created in AAI: vserver testVServerId.",
                    we.getErrorMessage());
        }
    }

    @Test
    public void processAuditExceptionContinueTest() {
        try {
            Mockito.doReturn(extractPojosForBB).when(exceptionBuilder).getExtractPojosForBB();
            exceptionBuilder.processAuditException((DelegateExecutionImpl) execution, true);
            String sm = execution.getVariable("StatusMessage");
            assertNotNull(sm);
            assertEquals(
                    "create VF-Module testVfModuleId1 failed due to incomplete AAI vserver inventory population after stack testStackName was successfully created in cloud region testLcpCloudRegionId. MSO Audit indicates that the following was not created in AAI: vserver testVServerId.",
                    sm);
        } catch (BpmnError bpmnException) {
            fail();
        }
    }

}
