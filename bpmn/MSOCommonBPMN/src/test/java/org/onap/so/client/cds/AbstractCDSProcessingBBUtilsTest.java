/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

package org.onap.so.client.cds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCDSProcessingBBUtilsTest {
    @InjectMocks
    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils = new AbstractCDSProcessingBBUtils();
    @InjectMocks
    AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();
    @Mock
    ExceptionBuilder exceptionUtil;

    @Before
    public void init() {
        String requestObject =
                "{\"config-assign-request\":{\"resolution-key\":\"resolutionKey\", \"config-assign-properties\":{\"service-instance-id\":\"serviceInstanceId\", \"vnf-id\":\"vnfId\", \"vnf-name\":\"vnfName\", \"service-model-uuid\":\"serviceModelUuid\", \"vnf-customization-uuid\":\"vnfCustomizationUuid\",\"Instance1\":\"Instance1Value\",\"Instance2\":\"Instance2Value\",\"Param3\":\"Param3Value\"}}}";
        String blueprintName = "blueprintName";
        String blueprintVersion = "blueprintVersion";
        String actionName = "actionName";
        String mode = "mode";
        String requestId = "123456";
        String originatorId = "originatorId";
        String subRequestId = UUID.randomUUID().toString();

        abstractCDSPropertiesBean.setActionName(actionName);
        abstractCDSPropertiesBean.setBlueprintName(blueprintName);
        abstractCDSPropertiesBean.setBlueprintVersion(blueprintVersion);
        abstractCDSPropertiesBean.setMode(mode);
        abstractCDSPropertiesBean.setOriginatorId(originatorId);
        abstractCDSPropertiesBean.setRequestId(requestId);
        abstractCDSPropertiesBean.setRequestObject(requestObject);
        abstractCDSPropertiesBean.setSubRequestId(subRequestId);
    }

    @Test
    public void preProcessRequestDETest() {

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("executionObject")).thenReturn(abstractCDSPropertiesBean);

        abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(execution);
        verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(DelegateExecution.class), anyInt(),
                any(Exception.class));
    }

    @Test
    public void sendRequestToCDSClientDETest() {

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("executionServiceInput")).thenReturn(abstractCDSPropertiesBean);
        abstractCDSProcessingBBUtils.sendRequestToCDSClient(execution);
        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(DelegateExecution.class), anyInt(),
                any(Exception.class));

    }

    @Test
    public void preProcessRequestBBTest() {

        BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
        when(execution.getVariable("executionObject")).thenReturn(abstractCDSPropertiesBean);

        abstractCDSProcessingBBUtils.constructExecutionServiceInputObjectBB(execution);
        verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), anyInt(),
                any(Exception.class));
    }

    @Test
    public void sendRequestToCDSClientBBTest() {

        BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
        when(execution.getVariable("executionServiceInput")).thenReturn(abstractCDSPropertiesBean);
        abstractCDSProcessingBBUtils.sendRequestToCDSClientBB(execution);
        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), anyInt(),
                any(Exception.class));

    }

}
