/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.common.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class AutoProcessInstanceMigrationServiceTest {

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private ProcessDefinition outdated;

    @Mock
    private ProcessDefinition newDef;

    @Mock
    private ProcessDefinition key;

    @Mock
    private ProcessDefinition testKey;

    @Mock
    private ProcessDefinition suspendedDef;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private ProcessDefinitionQuery query;

    @Mock
    private ProcessDefinitionQuery keyQuery;

    @Mock
    private Environment env;

    @Spy
    @InjectMocks
    private AutoProcessInstanceMigrationService migrationService;


    @Test
    public void getOldProcessDefinitionsTest() {
        List<ProcessDefinition> expectedList = new ArrayList<>();
        expectedList.add(outdated);

        List<ProcessDefinition> defList = new ArrayList<>();
        defList.add(outdated);
        defList.add(newDef);
        defList.add(suspendedDef);


        doReturn(query).when(repositoryService).createProcessDefinitionQuery();
        doReturn(query).when(query).processDefinitionKey("test");
        doReturn(defList).when(query).list();
        doReturn(3).when(outdated).getVersion();
        doReturn(4).when(newDef).getVersion();
        doReturn(true).when(suspendedDef).isSuspended();
        List<ProcessDefinition> outdatedList = migrationService.getOldProcessDefinitions("test", 4);

        assertEquals(expectedList, outdatedList);
    }

    @Test
    public void getProcessDefinitionsTest() {
        List<ProcessDefinition> expected = new ArrayList<ProcessDefinition>();
        expected.add(testKey);
        expected.add(key);

        List<String> processDefinitionKeys = new ArrayList<String>();
        processDefinitionKeys.add("testKey");
        processDefinitionKeys.add("key");

        doReturn(processDefinitionKeys).when(env).getProperty("migration.processDefinitionKeys", List.class,
                new ArrayList<String>());

        doReturn(query).when(repositoryService).createProcessDefinitionQuery();
        doReturn(query).when(query).processDefinitionKey("testKey");
        doReturn(query).when(query).latestVersion();
        doReturn(testKey).when(query).singleResult();

        doReturn(keyQuery).when(query).processDefinitionKey("key");
        doReturn(keyQuery).when(keyQuery).latestVersion();
        doReturn(key).when(keyQuery).singleResult();

        List<ProcessDefinition> actualProcessDefinitions = migrationService.getProcessDefinitions();

        assertEquals(expected, actualProcessDefinitions);
    }
}
