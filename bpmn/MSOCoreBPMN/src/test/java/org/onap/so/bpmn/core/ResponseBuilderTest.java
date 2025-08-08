/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Before;
import org.junit.Test;

public class ResponseBuilderTest {

    private String prefix = "PRE_";
    private String processKey = "AnyProcessKey";
    private String definitionId = "100";
    private int errorCode_200 = 200;
    private int errorCode_404 = 404;
    private String errorMessage = "any error message!";
    private String errorMessageXML = "<ErrorMessage>any error message!</ErrorMessage><ErrorCode>200</ErrorCode>";
    private String response = "<WorkflowResponse>bad</WorkflowResponse>";

    private DelegateExecution mockExecution;
    private ResponseBuilder responseBuilder;
    private WorkflowException workflowException;
    private Object obj;

    @Before
    public void before() {
        responseBuilder = new ResponseBuilder();
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class);
        when(mockProcessDefinition.getKey()).thenReturn(processKey);
        RepositoryService mockRepositoryService = mock(RepositoryService.class);
        when(mockRepositoryService.getProcessDefinition(definitionId)).thenReturn(mockProcessDefinition);
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class);
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService);
        mockExecution = mock(DelegateExecution.class);
        when(mockExecution.getId()).thenReturn(definitionId);
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices);
        when(mockExecution.getProcessEngineServices().getRepositoryService()
                .getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition);
        when(mockExecution.getVariable("prefix")).thenReturn(prefix);
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true");
    }

    @Test
    public void buildWorkflowException_WorkflowException_2000_Test() {
        when(mockExecution.getVariable(prefix + "ErrorResponse")).thenReturn(errorMessage);
        when(mockExecution.getVariable(prefix + "ResponseCode")).thenReturn(errorCode_200);
        workflowException = responseBuilder.buildWorkflowException(mockExecution);
        assertEquals(2000, workflowException.getErrorCode());
        assertEquals("any error message!", workflowException.getErrorMessage());
    }

    @Test
    public void buildWorkflowException_WorkflowException_XML_2000_Test() {
        when(mockExecution.getVariable(prefix + "ErrorResponse")).thenReturn(errorMessageXML);
        when(mockExecution.getVariable(prefix + "ResponseCode")).thenReturn(errorCode_200);
        workflowException = responseBuilder.buildWorkflowException(mockExecution);
        assertEquals(2000, workflowException.getErrorCode());
        assertEquals("any error message!", workflowException.getErrorMessage());
    }

    @Test
    public void buildWorkflowException_WorkflowException_NULL_Test() {
        when(mockExecution.getVariable(prefix + "ErrorResponse")).thenReturn(null);
        when(mockExecution.getVariable(prefix + "ResponseCode")).thenReturn(null);
        workflowException = responseBuilder.buildWorkflowException(mockExecution);
        assertEquals(null, workflowException);
    }

    @Test
    public void buildWorkflowException_Response_1002_Test() {
        when(mockExecution.getVariable(processKey + "Response")).thenReturn(response);
        when(mockExecution.getVariable(prefix + "ResponseCode")).thenReturn(errorCode_404);
        workflowException = responseBuilder.buildWorkflowException(mockExecution);
        assertEquals(response, workflowException.getErrorMessage());
        assertEquals(1002, workflowException.getErrorCode());
    }

    @Test
    public void buildWorkflowResponse_Object_Test() {
        String workflowResponse = "<WorkflowResponse>good</WorkflowResponse>";
        when(mockExecution.getVariable("WorkflowResponse")).thenReturn(workflowResponse);
        obj = responseBuilder.buildWorkflowResponse(mockExecution);
        assertEquals(workflowResponse, obj);
    }

}
