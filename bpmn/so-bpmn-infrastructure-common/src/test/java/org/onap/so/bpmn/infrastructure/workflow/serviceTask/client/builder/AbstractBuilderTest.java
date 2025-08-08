/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder;

import static org.junit.Assert.assertEquals;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder.RequestAction;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder.SvcAction;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.ParamEntity;
import org.onap.so.requestsdb.RequestsDbConstant;

public class AbstractBuilderTest {

    AbstractBuilder<?, ?> abstractBuilder = new AbstractBuilder<Object, Object>() {
        @Override
        Object build(DelegateExecution execution, Object input) throws Exception {
            return null;
        }
    };

    DelegateExecution delegateExecution = new DelegateExecution() {
        private String operType;
        private String resourceType;
        private String requestId;

        @Override
        public String getProcessInstanceId() {
            return null;
        }

        @Override
        public String getProcessBusinessKey() {
            return null;
        }

        @Override
        public String getProcessDefinitionId() {
            return null;
        }

        @Override
        public String getParentId() {
            return null;
        }

        @Override
        public String getCurrentActivityId() {
            return null;
        }

        @Override
        public String getCurrentActivityName() {
            return null;
        }

        @Override
        public String getActivityInstanceId() {
            return null;
        }

        @Override
        public String getParentActivityInstanceId() {
            return null;
        }

        @Override
        public String getCurrentTransitionId() {
            return null;
        }

        @Override
        public DelegateExecution getProcessInstance() {
            return null;
        }

        @Override
        public DelegateExecution getSuperExecution() {
            return null;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public String getTenantId() {
            return null;
        }

        @Override
        public void setVariable(String s, Object o, String s1) {

        }

        @Override
        public Incident createIncident(String s, String s1) {
            return null;
        }

        @Override
        public Incident createIncident(String s, String s1, String s2) {
            return null;
        }

        @Override
        public void resolveIncident(String s) {

        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getEventName() {
            return null;
        }

        @Override
        public String getBusinessKey() {
            return null;
        }

        @Override
        public BpmnModelInstance getBpmnModelInstance() {
            return null;
        }

        @Override
        public FlowElement getBpmnModelElementInstance() {
            return null;
        }

        @Override
        public ProcessEngineServices getProcessEngineServices() {
            return null;
        }

        @Override
        public String getVariableScopeKey() {
            return null;
        }

        @Override
        public Map<String, Object> getVariables() {
            return null;
        }

        @Override
        public VariableMap getVariablesTyped() {
            return null;
        }

        @Override
        public VariableMap getVariablesTyped(boolean b) {
            return null;
        }

        @Override
        public Map<String, Object> getVariablesLocal() {
            return null;
        }

        @Override
        public VariableMap getVariablesLocalTyped() {
            return null;
        }

        @Override
        public VariableMap getVariablesLocalTyped(boolean b) {
            return null;
        }

        @Override
        public Object getVariable(String s) {
            if (AbstractBuilder.OPERATION_TYPE.equals(s)) {
                return operType;
            } else if (AbstractBuilder.RESOURCE_TYPE.equals(s)) {
                return resourceType;
            } else if ("msoRequestId".equals(s)) {
                return requestId;
            }
            return null;
        }

        @Override
        public Object getVariableLocal(String s) {
            return null;
        }

        @Override
        public <T extends TypedValue> T getVariableTyped(String s) {
            return null;
        }

        @Override
        public <T extends TypedValue> T getVariableTyped(String s, boolean b) {
            return null;
        }

        @Override
        public <T extends TypedValue> T getVariableLocalTyped(String s) {
            return null;
        }

        @Override
        public <T extends TypedValue> T getVariableLocalTyped(String s, boolean b) {
            return null;
        }

        @Override
        public Set<String> getVariableNames() {
            return null;
        }

        @Override
        public Set<String> getVariableNamesLocal() {
            return null;
        }

        @Override
        public void setVariable(String s, Object o) {
            if (AbstractBuilder.OPERATION_TYPE.equals(s)) {
                operType = (String) o;
            } else if (AbstractBuilder.RESOURCE_TYPE.equals(s)) {
                resourceType = (String) o;
            } else if ("msoRequestId".equals(s)) {
                requestId = (String) o;
            }
        }

        @Override
        public void setVariableLocal(String s, Object o) {

        }

        @Override
        public void setVariables(Map<String, ?> map) {

        }

        @Override
        public void setVariablesLocal(Map<String, ?> map) {

        }

        @Override
        public boolean hasVariables() {
            return false;
        }

        @Override
        public boolean hasVariablesLocal() {
            return false;
        }

        @Override
        public boolean hasVariable(String s) {
            return false;
        }

        @Override
        public boolean hasVariableLocal(String s) {
            return false;
        }

        @Override
        public void removeVariable(String s) {

        }

        @Override
        public void removeVariableLocal(String s) {

        }

        @Override
        public void removeVariables(Collection<String> collection) {

        }

        @Override
        public void removeVariablesLocal(Collection<String> collection) {

        }

        @Override
        public void removeVariables() {

        }

        @Override
        public void removeVariablesLocal() {

        }

        @Override
        public ProcessEngine getProcessEngine() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setProcessBusinessKey(String arg0) {
            // TODO Auto-generated method stub

        }
    };

    @Test
    public void requestActionGetIntValueTest() {
        assertEquals(0, RequestAction.CREATE_NETWORK_INSTANCE.getIntValue());
    }

    @Test
    public void svcActionGetIntValueTest() {
        assertEquals(0, SvcAction.RESERVE.getIntValue());
    }

    @Test
    public void buildTest() throws Exception {
        abstractBuilder.build(null, null);
    }

    @Test
    public void getRequestActionBlankOperationTypeTest() {
        assertEquals(AbstractBuilder.RequestAction.CREATE_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionDeleteOperationTypeBlankResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "");
        assertEquals(AbstractBuilder.RequestAction.DELETE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionDeleteOperationTypeBadResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "bad");
        assertEquals(AbstractBuilder.RequestAction.DELETE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionDeleteOperationTypeOverlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "overlay");
        assertEquals(AbstractBuilder.RequestAction.DEACTIVATE_DCI_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionDeleteOperationTypeUnderlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "underlay");
        assertEquals(AbstractBuilder.RequestAction.DELETE_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionDeleteOperationTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        assertEquals(AbstractBuilder.RequestAction.DELETE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionCreateOperationTypeBlankResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "");
        assertEquals(AbstractBuilder.RequestAction.CREATE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionCreateOperationTypeBadResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "bad");
        assertEquals(AbstractBuilder.RequestAction.CREATE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionCreateOperationTypeOverlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "overlay");
        assertEquals(AbstractBuilder.RequestAction.ACTIVATE_DCI_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionCreateOperationTypeUnderlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "underlay");
        assertEquals(AbstractBuilder.RequestAction.CREATE_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionCreateOperationTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        assertEquals(AbstractBuilder.RequestAction.CREATE_SERVICE_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getRequestActionBadOperationType() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, "bad");
        assertEquals(AbstractBuilder.RequestAction.CREATE_NETWORK_INSTANCE.getName(),
                abstractBuilder.getRequestAction(delegateExecution));
    }

    @Test
    public void getSvcActionBlankOperationTypeTest() {
        assertEquals(AbstractBuilder.SvcAction.CREATE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionDeleteOperationTypeBlankResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "");
        assertEquals(AbstractBuilder.SvcAction.UNASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionDeleteOperationTypeBadResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "bad");
        assertEquals(AbstractBuilder.SvcAction.UNASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionDeleteOperationTypeOverlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "overlay");
        assertEquals(AbstractBuilder.SvcAction.DEACTIVATE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionDeleteOperationTypeUnderlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "underlay");
        assertEquals(AbstractBuilder.SvcAction.DELETE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionDeleteOperationTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.DELETE);
        assertEquals(AbstractBuilder.SvcAction.UNASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionCreateOperationTypeBlankResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "");
        assertEquals(AbstractBuilder.SvcAction.ASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionCreateOperationTypeBadResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "bad");
        assertEquals(AbstractBuilder.SvcAction.ASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionCreateOperationTypeOverlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "overlay");
        assertEquals(AbstractBuilder.SvcAction.ACTIVATE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionCreateOperationTypeUnderlayResourceTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        delegateExecution.setVariable(AbstractBuilder.RESOURCE_TYPE, "underlay");
        assertEquals(AbstractBuilder.SvcAction.CREATE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionCreateOperationTypeTest() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, RequestsDbConstant.OperationType.CREATE);
        assertEquals(AbstractBuilder.SvcAction.ASSIGN.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getSvcActionBadOperationType() {
        delegateExecution.setVariable(AbstractBuilder.OPERATION_TYPE, "bad");
        assertEquals(AbstractBuilder.SvcAction.CREATE.getName(), abstractBuilder.getSvcAction(delegateExecution));
    }

    @Test
    public void getRequestIdBlankNotOnExecutionTest() {
        abstractBuilder.getRequestId(delegateExecution);
    }

    @Test
    public void getRequestIdBlankOnExecutionTest() {
        String expected = "requestId";
        delegateExecution.setVariable("msoRequestId", expected);
        assertEquals(expected, abstractBuilder.getRequestId(delegateExecution));
    }

    @Test
    public void getRequestIdTest() {
        String expected = "requestId";
        abstractBuilder.requestId = expected;
        assertEquals(expected, abstractBuilder.getRequestId(delegateExecution));
    }

    @Test
    public void getOnapServiceModelInformationEntityTest() {
        abstractBuilder.getOnapServiceModelInformationEntity(delegateExecution);
    }

    @Test
    public void getOnapNetworkModelInformationEntityTest() {
        abstractBuilder.getOnapNetworkModelInformationEntity(delegateExecution);
    }

    @Test
    public void getParamEntitiesTest() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("foo", "bar");
        List<ParamEntity> list = abstractBuilder.getParamEntities(inputs);
        assertEquals(1, list.size());
        assertEquals("foo", list.get(0).getName());
        assertEquals("bar", list.get(0).getValue());
    }

    @Test
    public void getParamEntitiesNullInputsTest() {
        List<ParamEntity> list = abstractBuilder.getParamEntities(null);
        assertEquals(0, list.size());
    }

    @Test
    public void getParamEntitiesEmptyInputsTest() {
        List<ParamEntity> list = abstractBuilder.getParamEntities(new HashMap<>());
        assertEquals(0, list.size());
    }

    @Test
    public void getRequestInformationEntityTest() {
        abstractBuilder.getRequestInformationEntity(delegateExecution);
    }

    @Test
    public void getServiceInformationEntityTest() {
        abstractBuilder.getServiceInformationEntity(delegateExecution);
    }

    @Test
    public void getServiceInstanceNameTest() {
        abstractBuilder.getServiceInstanceName(delegateExecution);
    }

}
