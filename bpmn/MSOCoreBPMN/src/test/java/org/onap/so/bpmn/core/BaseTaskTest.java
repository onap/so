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
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BaseTaskTest {

    private String prefix = "PRE_";
    private String processKey = "AnyProcessKey";
    private String definitionId = "100";
    private String anyVariable = "anyVariable";
    private String anyValueString = "anyValue";
    private String badValueString = "123abc";
    private int anyValueInt = 123;
    private Integer anyValueInteger = new Integer(anyValueInt);
    private long anyValuelong = 123L;
    private Long anyValueLong = new Long(anyValuelong);

    private DelegateExecution mockExecution;
    private Expression mockExpression;
    private BaseTask baseTask;
    private Object obj1;
    private Object obj2;
    private Object objectString;
    private Object objectInteger;
    private Object objectLong;
    private Object objectBoolean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        baseTask = new BaseTask();
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
        mockExpression = mock(Expression.class);
    }

    @Test
    public void testExecution() throws Exception {
        baseTask.execute(mockExecution);
        assertEquals("BaseTask", baseTask.getTaskName());
    }

    @Test
    public void testGetFieldAndMissingInjectedException() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());

        expectedException.expect(MissingInjectedFieldException.class);
        obj2 = baseTask.getField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        obj1 = baseTask.getField(mockExpression, mockExecution, null);
    }

    @Test
    public void testGetOptionalField() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getOptionalField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());
    }

    @Test
    public void testGetStringFieldAndMissingInjectedFieldException() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getStringField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());

        expectedException.expect(MissingInjectedFieldException.class);
        Object objectBoolean = new Boolean(true); // bad data
        when(mockExpression.getValue(mockExecution)).thenReturn(objectBoolean);
        obj2 = baseTask.getStringField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetStringFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        obj1 = baseTask.getStringField(mockExpression, mockExecution, null);
    }

    @Test
    public void testGetOptionalStringField() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getOptionalStringField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());
    }

    @Test
    public void testGetIntegerFieldAndMissingInjectedFieldException() throws Exception {
        objectInteger = new Integer(anyValueInt);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectInteger);
        obj1 = baseTask.getIntegerField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueInteger, (Integer) obj1);

        expectedException.expect(MissingInjectedFieldException.class);
        objectString = new String(badValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj2 = baseTask.getIntegerField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetIntegerFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        obj1 = baseTask.getIntegerField(mockExpression, mockExecution, null);
    }


    @Test
    public void testGetOptionalIntegerField() throws Exception {
        objectInteger = new Integer(anyValueInt);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectInteger);
        obj1 = baseTask.getOptionalIntegerField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueInteger, (Integer) obj1);
    }

    @Test
    public void testGetOptionalIntegerFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        objectBoolean = new Boolean(true);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectBoolean);
        obj1 = baseTask.getOptionalIntegerField(mockExpression, mockExecution, anyVariable);
    }

    @Test
    public void testGetLongFieldAndMissingInjectedFieldException() throws Exception {
        objectLong = new Long(anyValuelong);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectLong);
        obj1 = baseTask.getLongField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueLong, (Long) obj1);

        expectedException.expect(MissingInjectedFieldException.class);
        objectString = new String(badValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj2 = baseTask.getLongField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetLongFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        obj2 = baseTask.getLongField(mockExpression, mockExecution, null);
    }

    @Test
    public void testGetOptionalLongField() throws Exception {
        objectLong = new Long(anyValuelong);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectLong);
        obj1 = baseTask.getOptionalLongField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueLong, (Long) obj1);
    }

    @Test
    public void testGetOptionalLongFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        objectBoolean = new Boolean(true);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectBoolean);
        obj1 = baseTask.getOptionalLongField(mockExpression, mockExecution, anyVariable);
    }

    @Test
    public void testGetOutputAndMissingInjectedFieldException() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getOutputField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());

        expectedException.expect(MissingInjectedFieldException.class);
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj2 = baseTask.getOutputField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetOutputAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        obj2 = baseTask.getOutputField(null, mockExecution, anyVariable);
    }

    @Test
    public void testGetOptionalOutputField() throws Exception {
        objectString = new String(anyValueString);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectString);
        obj1 = baseTask.getOptionalOutputField(mockExpression, mockExecution, anyVariable);
        assertEquals(anyValueString, obj1.toString());
    }

    @Test
    public void testGetOptionalOutputFieldAndBadInjectedFieldException() throws Exception {
        expectedException.expect(BadInjectedFieldException.class);
        objectBoolean = new Boolean(true);
        when(mockExpression.getValue(mockExecution)).thenReturn(objectBoolean);
        obj1 = baseTask.getOptionalOutputField(mockExpression, mockExecution, anyVariable);
    }

}
