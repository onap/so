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

package org.onap.so.bpmn.core;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.env.Environment;
import static org.mockito.Mockito.*;

public class UrnPropertiesReaderTest {

    @Test
    public void testGetVariableFromExecution() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class);
        when(mockExecution.getVariable("testKey")).thenReturn("testValue");
        String value = UrnPropertiesReader.getVariable("testKey", mockExecution);
        Assert.assertEquals("testValue", value);
        verify(mockExecution).getVariable("testKey");
        verify(mockExecution, never()).setVariable("testKey", value);
    }

    @Test
    public void testGetVariableFromEnvironment() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class);
        Environment mockEnvironment = mock(Environment.class);
        when(mockEnvironment.getProperty("testKey")).thenReturn("testValue");
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader();
        urnPropertiesReader.setEnvironment(mockEnvironment);
        String value = UrnPropertiesReader.getVariable("testKey", mockExecution);
        Assert.assertEquals("testValue", value);
        verify(mockExecution).getVariable("testKey");
        verify(mockExecution).setVariable("testKey", value);
    }

    @Test
    public void testGetVariableNotExist() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class);
        String value = UrnPropertiesReader.getVariable("notExist", mockExecution);
        Assert.assertEquals(null, value);
        verify(mockExecution).getVariable("notExist");
        verify(mockExecution, never()).setVariable("notExist", value);
    }
}
