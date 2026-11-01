/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

public class VnfCreationRequestContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final DoCreateVnf script = new DoCreateVnf();
    private MockedConstruction<CatalogDbClient> catalogs;
    private Environment previousEnvironment;
    private HomingInstance homing;
    private boolean rejectHoming;

    @Before
    public void configureStatefulExecutionAndTheExternalCatalogBoundary() {
        previousEnvironment = (Environment) ReflectionTestUtils.getField(UrnPropertiesReader.class, "environment");
        new UrnPropertiesReader().setEnvironment(new MockEnvironment());
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getKey()).thenReturn("DoCreateVnf");
        RepositoryService repository = mock(RepositoryService.class);
        when(repository.getProcessDefinition("definition-1")).thenReturn(definition);
        ProcessEngineServices services = mock(ProcessEngineServices.class);
        when(services.getRepositoryService()).thenReturn(repository);
        when(execution.getProcessDefinitionId()).thenReturn("definition-1");
        when(execution.getProcessEngineServices()).thenReturn(services);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        variables.put("msoRequestId", "request & 東京");
        variables.put("serviceInstanceId", "service-1");
        variables.put("vnfType", "type-1");
        variables.put("vnfName", "name & 東京");
        variables.put("vnfId", "vnf-1");
        variables.put("productFamilyId", "family-1");
        variables.put("disableRollback", "false");
        variables.put("lcpCloudRegionId", "region-1");
        variables.put("tenantId", "tenant-1");
        variables.put("vnfModelInfo", "{\"modelInvariantUuid\":\"invariant-1\"}");
        variables.put("serviceModelInfo", "{}");
        variables.put("mso.catalog.db.spring.endpoint", "http://catalog.invalid");
        variables.put("mso.workflow.sdncadapter.callback", "http://callback.invalid/sdnc");
        VnfResource resource = new VnfResource();
        resource.setNfRole("role-1");
        resource.setNfType("nf-type-1");
        resource.setNfFunction("function-1");
        resource.setNfNamingCode("naming-1");
        variables.put("vnfResourceDecomposition", resource);
        catalogs = mockConstruction(CatalogDbClient.class,
                (client, context) -> when(
                        client.getHomingInstance("service-1", "http://catalog.invalid/homingInstance/"))
                                .thenAnswer(call -> {
                                    if (rejectHoming) {
                                        throw new IllegalStateException("catalog unavailable");
                                    }
                                    return homing;
                                }));
    }

    @After
    public void restoreCatalogAndEnvironment() {
        try {
            if (catalogs != null) {
                catalogs.close();
            }
        } finally {
            new UrnPropertiesReader().setEnvironment(previousEnvironment);
        }
    }

    @Test
    public void preprocessingPublishesNamedTypedIdentityAndRollbackContracts() throws Exception {
        script.preProcessRequest(execution);
        assertEquals("DoCVNF_", variables.get("prefix"));
        for (String key : new String[] {"requestId", "serviceInstanceId", "vnfType", "vnfName", "vnfId", "tenantId"}) {
            String input = key.equals("requestId") ? "msoRequestId" : key;
            assertEquals(variables.get(input), variables.get("DoCVNF_" + key));
            assertEquals(String.class, variables.get("DoCVNF_" + key).getClass());
        }
        assertEquals("request & 東京", variables.get("mso-request-id"));
        assertEquals(Boolean.FALSE, variables.get("SDNCInteractionEnabled"));
        assertEquals("http://callback.invalid/sdnc", variables.get("DoCVNF_sdncCallbackUrl"));
        assertEquals("Created", variables.get("DoCVNF_orchStatus"));
        assertEquals("generic-vnf", variables.get("DoCVNF_type"));
        assertEquals("service-instance", variables.get("GENGS_type"));
        assertEquals("role-1", variables.get("DoCVNF_nfRole"));
        assertEquals("nf-type-1", variables.get("DoCVNF_nfType"));
        assertEquals("function-1", variables.get("DoCVNF_nfFunction"));
        assertEquals("naming-1", variables.get("DoCVNF_nfNamingCode"));
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        var request = factory.newDocumentBuilder().parse(new ByteArrayInputStream(
                ((String) variables.get("DoCVNF_requestInfo")).getBytes(StandardCharsets.UTF_8)));
        assertEquals("http://org.onap/so/infra/vnf-request/v1", request.getDocumentElement().getNamespaceURI());
        assertEquals("request & 東京", request.getElementsByTagNameNS("*", "request-id").item(0).getTextContent());
        assertEquals("CREATE", request.getElementsByTagNameNS("*", "action").item(0).getTextContent());
        RollbackData rollback = rollback();
        assertEquals("service-1", rollback.get("VNF", "serviceInstanceId"));
        assertEquals("tenant-1", rollback.get("VNF", "tenantId"));
        assertEquals("http://callback.invalid/sdnc", rollback.get("VNF", "sdncCallbackUrl"));
        for (String stage : new String[] {"rollbackSDNCAssign", "rollbackSDNCActivate", "rollbackVnfCreate"}) {
            assertEquals("false", rollback.get("VNF", stage));
        }
        assertNull(variables.get("WorkflowException"));
        verify(catalogs.constructed().get(0)).getHomingInstance("service-1", "http://catalog.invalid/homingInstance/");
    }

    @Test
    public void suppliedModelMetadataAndControllerVersionRemainStrings() {
        variables.put("vnfModelInfo", "{\"modelInvariantUuid\":\"invariant-1\",\"modelUuid\":\"version-id\","
                + "\"modelVersion\":\"0\",\"modelName\":\"model 東京\",\"modelCustomizationUuid\":\"custom-id\"}");
        variables.put("globalSubscriberId", "subscriber-1");
        variables.put("sdncVersion", "1802");
        script.preProcessRequest(execution);
        assertEquals("invariant-1", variables.get("DoCVNF_modelInvariantId"));
        assertEquals("version-id", variables.get("DoCVNF_modelVersionId"));
        assertEquals("0", variables.get("DoCVNF_modelVersion"));
        assertEquals("model 東京", variables.get("DoCVNF_modelName"));
        assertEquals("custom-id", variables.get("DoCVNF_modelCustomizationId"));
        assertEquals("subscriber-1", variables.get("DoCVNF_globalSubscriberId"));
        assertEquals("1802", variables.get("DoCVNF_sdncVersion"));
    }

    @Test
    public void absentOptionalModelIdsSubscriberAndControllerVersionUseExistingDefaults() {
        script.preProcessRequest(execution);
        assertEquals("", variables.get("DoCVNF_modelVersionId"));
        assertEquals("", variables.get("DoCVNF_modelCustomizationId"));
        assertEquals("", variables.get("DoCVNF_globalSubscriberId"));
        assertEquals("1702", variables.get("DoCVNF_sdncVersion"));
    }

    @Test
    public void generatedVnfIdentityIsAUuidStoredAsAString() {
        variables.remove("vnfId");
        script.preProcessRequest(execution);
        Object id = variables.get("DoCVNF_vnfId");
        assertEquals(String.class, id.getClass());
        assertEquals(id, UUID.fromString((String) id).toString());
    }

    @Test
    public void suppliedVnfIdentityIsRetainedWithoutGeneration() {
        script.preProcessRequest(execution);
        assertEquals("vnf-1", variables.get("DoCVNF_vnfId"));
        assertEquals("vnf-1", variables.get("vnfId"));
    }

    @Test
    public void homingOverridesWorkflowAndRollbackCloudRegionsTogether() {
        homing = new HomingInstance();
        homing.setCloudRegionId("homed-region");
        script.preProcessRequest(execution);
        assertEquals("homed-region", variables.get("DoCVNF_cloudSiteId"));
        assertEquals("homed-region", rollback().get("VNF", "cloudSiteId"));
        assertEquals("region-1", variables.get("lcpCloudRegionId"));
    }

    @Test
    public void missingHomingKeepsTheIncomingRegionInBothConsumers() {
        script.preProcessRequest(execution);
        assertEquals("region-1", variables.get("DoCVNF_cloudSiteId"));
        assertEquals("region-1", rollback().get("VNF", "cloudSiteId"));
    }

    @Test
    public void unavailableHomingRetainsTheCallerRegionAndFreshRollbackState() {
        rejectHoming = true;
        script.preProcessRequest(execution);
        assertEquals("region-1", variables.get("DoCVNF_cloudSiteId"));
        assertEquals("region-1", rollback().get("VNF", "cloudSiteId"));
        assertEquals("false", rollback().get("VNF", "rollbackVnfCreate"));
        assertNull(variables.get("WorkflowException"));
    }

    @Test
    public void absentAndBlankCallbacksRetainTheRequiredFieldBusinessError() {
        for (String value : new String[] {null, "", "  "}) {
            variables.put("mso.workflow.sdncadapter.callback", value);
            BpmnError error = assertThrows(BpmnError.class, () -> script.preProcessRequest(execution));
            assertEquals("2000", error.getErrorCode());
            Object failure = variables.get("WorkflowException");
            assertEquals(WorkflowException.class, failure.getClass());
            assertEquals(2000, ((WorkflowException) failure).getErrorCode());
            assertTrue(((WorkflowException) failure).getErrorMessage().contains("mso.workflow.sdncadapter.callback"));
            assertTrue(catalogs.constructed().isEmpty());
        }
    }

    @Test
    public void existingRollbackDataPreservesOtherResourceFamilies() {
        RollbackData prior = new RollbackData();
        prior.put("SERVICEINSTANCE", "serviceInstanceId", "outer-service");
        prior.put("SERVICEINSTANCE", "rollbackAAI", "true");
        variables.put("rollbackData", prior);
        script.preProcessRequest(execution);
        assertSame(prior, rollback());
        assertEquals("outer-service", prior.get("SERVICEINSTANCE", "serviceInstanceId"));
        assertEquals("true", prior.get("SERVICEINSTANCE", "rollbackAAI"));
        assertEquals("false", prior.get("VNF", "rollbackVnfCreate"));
    }

    private RollbackData rollback() {
        Object value = variables.get("rollbackData");
        assertEquals(RollbackData.class, value.getClass());
        return (RollbackData) value;
    }
}
