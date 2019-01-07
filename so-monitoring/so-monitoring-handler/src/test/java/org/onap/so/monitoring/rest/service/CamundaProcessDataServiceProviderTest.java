/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.onap.so.monitoring.camunda.model.ActivityInstance;
import org.onap.so.monitoring.camunda.model.ProcessDefinition;
import org.onap.so.monitoring.camunda.model.ProcessInstance;
import org.onap.so.monitoring.camunda.model.ProcessInstanceVariable;
import org.onap.so.monitoring.configuration.camunda.CamundaRestUrlProvider;
import org.onap.so.monitoring.model.ActivityInstanceDetail;
import org.onap.so.monitoring.model.ProcessDefinitionDetail;
import org.onap.so.monitoring.model.ProcessInstanceIdDetail;
import org.onap.so.monitoring.model.ProcessInstanceVariableDetail;

import com.google.common.base.Optional;


/**
 * @author waqas.ikram@ericsson.com
 */
public class CamundaProcessDataServiceProviderTest {
    private static final String DURATION = "1";
    private static final String FLOW_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String NAME = "Test";
    private static final String DEFAULT = "default";
    private static final String CAMUNDA_REST_API_URL = "http://localhost:9080/engine-rest/engine/";

    private static final String ID = UUID.randomUUID().toString();
    private static final String PROCESS_ID = UUID.randomUUID().toString();
    private static final String DEF_ID = UUID.randomUUID().toString();
    private static final String SUPER_PROCESS_ID = UUID.randomUUID().toString();
    private final HttpRestServiceProvider httpRestServiceProvider = mock(HttpRestServiceProvider.class);
    private final CamundaRestUrlProvider camundaRestUrlProvider =
            new CamundaRestUrlProvider(CAMUNDA_REST_API_URL, DEFAULT);


    @Test
    public void test_GetProcessInstanceDetail_EmptyResponse() {
        final Optional<ProcessInstance[]> response = Optional.<ProcessInstance[]>absent();
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/history/process-instance?variables=requestId_eq_" + ID;
        when(httpRestServiceProvider.getHttpResponse(url, ProcessInstance[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final Optional<ProcessInstanceIdDetail> actualResponse = objUnderTest.getProcessInstanceIdDetail(ID);
        assertFalse(actualResponse.isPresent());
    }

    @Test
    public void test_GetProcessInstanceDetail_NonEmptyResponseWithSuperProcessIdNull() {
        final Optional<ProcessInstance[]> response = Optional.of(getProcessInstance());
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/history/process-instance?variables=requestId_eq_" + ID;
        when(httpRestServiceProvider.getHttpResponse(url, ProcessInstance[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final Optional<ProcessInstanceIdDetail> actualResponse = objUnderTest.getProcessInstanceIdDetail(ID);
        assertTrue(actualResponse.isPresent());

        final ProcessInstanceIdDetail actualProcessInstanceDetail = actualResponse.get();
        assertEquals(PROCESS_ID, actualProcessInstanceDetail.getProcessInstanceId());
    }

    @Test
    public void test_GetProcessInstanceDetail_NonEmptyResponseWithSuperProcessIdNotNull() {
        final Optional<ProcessInstance[]> response = Optional.of(getProcessInstance(SUPER_PROCESS_ID));
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/history/process-instance?variables=requestId_eq_" + ID;
        when(httpRestServiceProvider.getHttpResponse(url, ProcessInstance[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final Optional<ProcessInstanceIdDetail> actualResponse = objUnderTest.getProcessInstanceIdDetail(ID);
        assertFalse(actualResponse.isPresent());

    }

    @Test
    public void test_GetProcessDefinition_EmptyResponse() {
        final Optional<ProcessDefinition> response = Optional.<ProcessDefinition>absent();
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/process-definition/" + ID + "/xml";
        when(httpRestServiceProvider.getHttpResponse(url, ProcessDefinition.class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final Optional<ProcessDefinitionDetail> actualResponse = objUnderTest.getProcessDefinition(ID);
        assertFalse(actualResponse.isPresent());
    }

    @Test
    public void test_GetProcessDefinition_NonEmptyResponse() {
        final Optional<ProcessDefinition> response = getProcessDefinition();
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/process-definition/" + PROCESS_ID + "/xml";
        when(httpRestServiceProvider.getHttpResponse(url, ProcessDefinition.class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final Optional<ProcessDefinitionDetail> actualResponse = objUnderTest.getProcessDefinition(PROCESS_ID);
        assertTrue(actualResponse.isPresent());
        assertEquals(PROCESS_ID, actualResponse.get().getProcessDefinitionId());
        assertEquals(FLOW_XML, actualResponse.get().getProcessDefinitionXml());
    }

    @Test
    public void test_GetActivityInstance_EmptyResponse() {
        final Optional<ActivityInstance[]> response = Optional.<ActivityInstance[]>absent();
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/history/activity-instance?processInstanceId=" + PROCESS_ID
                + "&sortBy=startTime&sortOrder=asc";
        when(httpRestServiceProvider.getHttpResponse(url, ActivityInstance[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final List<ActivityInstanceDetail> actualResponse = objUnderTest.getActivityInstance(PROCESS_ID);
        assertTrue(actualResponse.isEmpty());

    }

    @Test
    public void test_GetActivityInstance_NonEmptyResponse() {
        final Optional<ActivityInstance[]> response = getActivityInstance();
        final String url = CAMUNDA_REST_API_URL + DEFAULT + "/history/activity-instance?processInstanceId=" + PROCESS_ID
                + "&sortBy=startTime&sortOrder=asc";
        when(httpRestServiceProvider.getHttpResponse(url, ActivityInstance[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final List<ActivityInstanceDetail> actualResponse = objUnderTest.getActivityInstance(PROCESS_ID);
        assertFalse(actualResponse.isEmpty());
        final ActivityInstanceDetail actualActivityInstanceDetail = actualResponse.get(0);
        assertEquals(ID, actualActivityInstanceDetail.getActivityId());
        assertEquals(NAME, actualActivityInstanceDetail.getActivityName());
        assertEquals(NAME, actualActivityInstanceDetail.getActivityType());

    }

    @Test
    public void test_GetProcessInstanceVariable_EmptyResponse() {
        final Optional<ProcessInstanceVariable[]> response = Optional.<ProcessInstanceVariable[]>absent();
        final String url =
                CAMUNDA_REST_API_URL + DEFAULT + "/history/variable-instance?processInstanceId=" + PROCESS_ID;
        when(httpRestServiceProvider.getHttpResponse(url, ProcessInstanceVariable[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final List<ProcessInstanceVariableDetail> actualResponse = objUnderTest.getProcessInstanceVariable(PROCESS_ID);
        assertTrue(actualResponse.isEmpty());

    }

    @Test
    public void test_GetProcessInstanceVariable_NonEmptyResponse() {
        final Optional<ProcessInstanceVariable[]> response = getProcessInstanceVariable();
        final String url =
                CAMUNDA_REST_API_URL + DEFAULT + "/history/variable-instance?processInstanceId=" + PROCESS_ID;
        when(httpRestServiceProvider.getHttpResponse(url, ProcessInstanceVariable[].class)).thenReturn(response);
        final CamundaProcessDataServiceProvider objUnderTest =
                new CamundaProcessDataServiceProviderImpl(camundaRestUrlProvider, httpRestServiceProvider);

        final List<ProcessInstanceVariableDetail> actualResponse = objUnderTest.getProcessInstanceVariable(PROCESS_ID);
        assertFalse(actualResponse.isEmpty());
        final ProcessInstanceVariableDetail variableDetail = actualResponse.get(0);
        assertEquals(NAME, variableDetail.getName());
        assertEquals(NAME, variableDetail.getType());
        assertEquals(NAME, variableDetail.getValue());

    }

    private Optional<ProcessInstanceVariable[]> getProcessInstanceVariable() {
        final ProcessInstanceVariable instanceVariable = new ProcessInstanceVariable();
        instanceVariable.setName(NAME);
        instanceVariable.setType(NAME);
        instanceVariable.setValue(NAME);
        return Optional.of(new ProcessInstanceVariable[] {instanceVariable});
    }

    private Optional<ActivityInstance[]> getActivityInstance() {
        final ActivityInstance activityInstance = new ActivityInstance();
        activityInstance.setActivityId(ID);
        activityInstance.setActivityName(NAME);
        activityInstance.setActivityType(NAME);
        activityInstance.setDurationInMillis(DURATION);
        return Optional.of(new ActivityInstance[] {activityInstance});
    }

    private Optional<ProcessDefinition> getProcessDefinition() {
        final ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(PROCESS_ID);
        processDefinition.setBpmn20Xml(FLOW_XML);
        return Optional.of(processDefinition);
    }

    private ProcessInstance[] getProcessInstance() {
        return getProcessInstance(null);
    }

    private ProcessInstance[] getProcessInstance(final String superProcessInstanceId) {
        final ProcessInstance instance = new ProcessInstance();
        instance.setId(PROCESS_ID);
        instance.setProcessDefinitionId(DEF_ID);
        instance.setProcessDefinitionName(NAME);
        instance.setSuperProcessInstanceId(superProcessInstanceId);
        return new ProcessInstance[] {instance};
    }


}
