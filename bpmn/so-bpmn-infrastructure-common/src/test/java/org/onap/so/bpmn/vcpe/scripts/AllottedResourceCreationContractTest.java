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
package org.onap.so.bpmn.vcpe.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.onap.aai.domain.yang.AllottedResource;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;

@RunWith(Parameterized.class)
public class AllottedResourceCreationContractTest {
    private static final String SERVICE_ROOT = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/";
    private final String family;
    private final String prefix;
    private final boolean brg;
    private AbstractServiceTaskProcessor script;
    private final AAIResourcesClient client = mock(AAIResourcesClient.class);
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> families() {
        return Arrays.asList(new Object[][] {{"BRG", "DCARBRG_", true}, {"TXC", "DCARTXC_", false}});
    }

    public AllottedResourceCreationContractTest(String family, String prefix, boolean brg) {
        this.family = family;
        this.prefix = prefix;
        this.brg = brg;
    }

    @Before
    public void configureExecution() {
        script = brg ? spy(new DoCreateAllottedResourceBRG()) : spy(new DoCreateAllottedResourceTXC());
        doReturn(client).when(script).getAAIClient();
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        variables.put("testProcessKey", "DoCreateAllottedResource" + family);
        variables.put("PSI_resourceLink", AAIUriFactory.createResourceFromExistingURI(Types.SERVICE_INSTANCE,
                URI.create(SERVICE_ROOT + "parent-1")));
        variables.put("CSI_resourceLink", SERVICE_ROOT + "child-1");
        variables.put("serviceInstanceId", "child-1");
        variables.put("parentServiceInstanceId", "parent-1");
        variables.put("allottedResourceId", "ar-1");
        variables.put("allottedResourceType", "type " + family);
        variables.put("allottedResourceRole", "role Gr\u00fc\u00dfe & SO");
        variables.put("allottedResourceModelInfo",
                "{\"modelInvariantUuid\":\"invariant-1\",\"modelUuid\":\"model-1\"}");
        variables.put("disableRollback", Boolean.FALSE);
    }

    @Test
    public void creationPreservesIdentityAndPayloadAndRegistersTypedRollbackState() {
        create();

        assertCreatedAndConnected("ar-1");
        assertRollback("ar-1", "false");
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void missingIdentityGeneratesOneUuidUsedForBothActionsAndRollback() {
        variables.remove("allottedResourceId");

        create();

        String id = (String) variables.get("allottedResourceId");
        assertEquals(id, UUID.fromString(id).toString());
        assertCreatedAndConnected(id);
        assertRollback(id, "false");
    }

    @Test
    public void blankIdentityIsReplacedBeforeCreatingTheResource() {
        variables.put("allottedResourceId", " \t ");

        create();

        String id = (String) variables.get("allottedResourceId");
        assertEquals(id, UUID.fromString(id).toString());
        assertCreatedAndConnected(id);
        assertRollback(id, "false");
    }

    @Test
    public void explicitRollbackOptOutRetainsTheStringFlagInRollbackData() {
        variables.put("disableRollback", Boolean.TRUE);

        create();

        assertCreatedAndConnected("ar-1");
        assertRollback("ar-1", "true");
        assertSame(Boolean.TRUE, variables.get("disableRollback"));
    }

    @Test
    public void failedCreationReportsItsErrorWithoutAttemptingTheRelationship() {
        doThrow(new IllegalStateException("AAI create rejected")).when(client).create(any(AAIResourceUri.class),
                any(AllottedResource.class));

        assertCreateFailure("Exception in createAaiAR AAI create rejected");

        verify(client).create(any(AAIResourceUri.class), any(AllottedResource.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void failedRelationshipReportsItsErrorAfterExactlyOneCreation() {
        rejectRelationship();

        assertCreateFailure("Exception in createAaiAR AAI relationship rejected");

        assertCreatedAndConnected("ar-1");
    }

    @Test
    public void malformedModelFailsBeforeAnyAaiMutation() {
        Assume.assumeTrue("SO-4446: malformed model JSON must be rejected before AAI mutations",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("allottedResourceModelInfo", "{broken");

        try {
            create();
            fail("Expected malformed model input to fail");
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(7000, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().startsWith("Exception in createAaiAR "));
        verifyNoInteractions(client);
    }

    @Test
    public void createdResourceRemainsEligibleForRollbackWhenRelationshipCreationFails() {
        Assume.assumeTrue("SO-4446: successful AAI creation must remain compensable when relationship creation fails",
                Boolean.getBoolean("groovy.migration.verification"));
        rejectRelationship();

        assertCreateFailure("Exception in createAaiAR AAI relationship rejected");

        assertCreatedAndConnected("ar-1");
        assertRollback("ar-1", "false");
    }

    @Test
    public void createdStateSuppliesTheIdentityAndPathRequiredByItsRollbackConsumer() {
        Assume.assumeTrue("SO-4446: creation state must supply the identity and AAI path used by its rollback consumer",
                Boolean.getBoolean("groovy.migration.verification"));
        create();
        String createdPath = (String) variables.get("aaiARPath");
        // Reusing parent variables would hide state missing from the production call activity's inputs.
        Map<String, Object> child = new HashMap<>();
        child.put("msoRequestId", "request-1");
        child.put("rollbackData", variables.get("rollbackData"));
        child.put("disableRollback", variables.get("disableRollback"));
        child.put("testProcessKey", "DoCreateAllottedResource" + family + "Rollback");
        DelegateExecution rollbackExecution = mock(DelegateExecution.class);
        when(rollbackExecution.getVariable(anyString())).thenAnswer(call -> child.get(call.getArgument(0)));
        doAnswer(call -> {
            child.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(rollbackExecution).setVariable(anyString(), any());

        if (brg) {
            new DoCreateAllottedResourceBRGRollback().preProcessRequest(rollbackExecution);
        } else {
            new DoCreateAllottedResourceTXCRollback().preProcessRequest(rollbackExecution);
        }

        assertSame(Boolean.TRUE, child.get("rollbackAAI"));
        assertFalse(Boolean.TRUE.equals(child.get("skipRollback")));
        assertEquals(Arrays.asList("ar-1", createdPath),
                Arrays.asList(child.get("allottedResourceId"), child.get("aaiARPath")));
        assertCreatedAndConnected("ar-1");
    }

    private void rejectRelationship() {
        doThrow(new IllegalStateException("AAI relationship rejected")).when(client).connect(any(AAIResourceUri.class),
                any(AAIResourceUri.class));
    }

    private void create() {
        if (brg) {
            ((DoCreateAllottedResourceBRG) script).createAaiAR(execution);
        } else {
            ((DoCreateAllottedResourceTXC) script).createAaiAR(execution);
        }
    }

    private void assertCreatedAndConnected(String id) {
        ArgumentCaptor<AAIResourceUri> createdUri = ArgumentCaptor.forClass(AAIResourceUri.class);
        ArgumentCaptor<AllottedResource> resource = ArgumentCaptor.forClass(AllottedResource.class);
        ArgumentCaptor<AAIResourceUri> target = ArgumentCaptor.forClass(AAIResourceUri.class);
        InOrder order = inOrder(client);
        order.verify(client).create(createdUri.capture(), resource.capture());
        order.verify(client).connect(org.mockito.ArgumentMatchers.eq(createdUri.getValue()), target.capture());
        verifyNoMoreInteractions(client);
        assertEquals(SERVICE_ROOT + "parent-1/allotted-resources/allotted-resource/" + id,
                createdUri.getValue().build().getPath());
        assertEquals(createdUri.getValue().build().toString(), variables.get("aaiARPath"));
        assertEquals(SERVICE_ROOT + "child-1", target.getValue().build().getPath());
        assertEquals(id, resource.getValue().getId());
        assertEquals("type " + family, resource.getValue().getType());
        assertEquals("role Gr\u00fc\u00dfe & SO", resource.getValue().getRole());
        assertEquals("invariant-1", resource.getValue().getModelInvariantId());
        assertEquals("model-1", resource.getValue().getModelVersionId());
    }

    private void assertRollback(String id, String disabled) {
        RollbackData rollback = (RollbackData) variables.get("rollbackData");
        assertNotNull("A created AAI resource must have compensation state", rollback);
        assertEquals("true", rollback.get(prefix, "rollbackAAI"));
        assertEquals(disabled, rollback.get(prefix, "disableRollback"));
        assertEquals(id, rollback.get(prefix, "allottedResourceId"));
        assertEquals("child-1", rollback.get(prefix, "serviceInstanceId"));
        assertEquals("parent-1", rollback.get(prefix, "parentServiceInstanceId"));
    }

    private void assertCreateFailure(String message) {
        try {
            create();
            fail("Expected a BPMN error");
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("DoCreateAllottedResource" + family, error.getProcessKey());
        assertEquals(7000, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
    }
}
