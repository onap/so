/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CatalogDbClient.class, ExceptionBuilder.class, ConfigCheckerDelegate.class})
public class ConfigCheckerDelegateTest {

    private static String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static String NONEXIST_MODEL_UUID = "a55323a2-d34c-4e05-b0c3-426df15294ba";
    private static String TEST_PROCESS_KEY = "processKey1";

    @MockBean
    private CatalogDbClient catalogDbClient;

    @Autowired
    private ConfigCheckerDelegate configCheckerDelegate;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() {
        Service service = buildService();
        given(catalogDbClient.getServiceByID(TEST_MODEL_UUID)).willReturn(service);
        execution.setVariable("testProcessKey", TEST_PROCESS_KEY);
    }

    private Service buildService() {
        Service service = new Service();
        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        pnfResourceCustomization.setSkipPostInstConf(true);
        List<PnfResourceCustomization> pnfResourceCustomizationList = new ArrayList<>();
        pnfResourceCustomizationList.add(pnfResourceCustomization);
        service.setPnfCustomizations(pnfResourceCustomizationList);
        return service;
    }

    @Test
    public void testExecution_validCatalogdb_skipVariableSet() {
        execution.setVariable("modelUuid", TEST_MODEL_UUID);
        try {
            configCheckerDelegate.execute(execution);
            assertThat(execution.getVariable("SkipPostInstantiationConfiguration")).isEqualTo(Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testExecution_validCatalogdbValidServiceNonexistPnf_exceptionThrown(){
        Service service = new Service();
        service.setPnfCustomizations(Collections.EMPTY_LIST);
        given(catalogDbClient.getServiceByID(TEST_MODEL_UUID)).willReturn(service);
        execution.setVariable("modelUuid", TEST_MODEL_UUID);
        assertThatThrownBy(() -> configCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
            .contains("Unable to find the PNF resource customizations of model service UUID").contains(TEST_MODEL_UUID);
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_ValidCatalogdbNonexistVariable_exceptionThrown(){
        assertThatThrownBy(() -> configCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
            .contains("Unable to find parameter modelUuid");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_ValidCatalogdbNonexistPnf_exceptionThrown(){
        execution.setVariable("modelUuid", NONEXIST_MODEL_UUID);
        assertThatThrownBy(() -> configCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
            .contains("Unable to find the service UUID").contains(NONEXIST_MODEL_UUID);
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }
}