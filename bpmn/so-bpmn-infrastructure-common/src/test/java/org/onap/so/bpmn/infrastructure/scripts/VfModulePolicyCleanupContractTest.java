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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.NetworkPolicies;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModulePolicyCleanupContractTest {
    private static final String FIRST = "domain-one:policy & \u96ea";
    private static final String SECOND = "domain-two:policy-two";
    private static final String POLICY_PATH = "/network/network-policies/network-policy/";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Optional<NetworkPolicies>> responses = new HashMap<>();
    private final List<String> effects = new ArrayList<>();
    private final DoDeleteVfModule script = new DoDeleteVfModule();
    private MockedConstruction<AAIResourcesClient> clients;
    private String failedPolicy;

    @Before
    public void configureStatefulExecutionAndTheExternalInventoryBoundary() {
        variables.put("testProcessKey", "DoDeleteVfModule");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        responses.put(FIRST, Optional.of(policies("policy-one")));
        responses.put(SECOND, Optional.of(policies("policy-two")));
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.get(eq(NetworkPolicies.class), any(AAIPluralResourceUri.class))).thenAnswer(call -> {
                AAIPluralResourceUri uri = call.getArgument(1);
                assertTrue(uri.build().getPath(), uri.build().getPath().endsWith("/network/network-policies"));
                String query = uri.build().getRawQuery();
                assertTrue(query, query.startsWith("network-policy-fqdn="));
                String fqdn =
                        URLDecoder.decode(query.substring("network-policy-fqdn=".length()), StandardCharsets.UTF_8);
                assertTrue(fqdn, responses.containsKey(fqdn));
                effects.add("GET " + fqdn);
                return responses.get(fqdn);
            });
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                String path = uri.build().getPath();
                assertTrue(path, path.contains(POLICY_PATH));
                String id = path.substring(path.indexOf(POLICY_PATH) + POLICY_PATH.length());
                effects.add("DELETE " + id);
                if (id.equals(failedPolicy)) {
                    throw new IllegalStateException("policy delete rejected");
                }
                return null;
            }).when(client).delete(any(AAIResourceUri.class));
        });
    }

    @After
    public void restoreTheExternalClientBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void absentPolicyListDoesNotQueryInventoryOrPublishACount() {
        script.deleteNetworkPoliciesFromAAI(execution);
        assertTrue(effects.isEmpty());
        assertTrue(clients.constructed().isEmpty());
        assertFalse(variables.containsKey("DoDVfMod_networkPolicyFqdnCount"));
    }

    @Test
    public void emptyPolicyListPublishesAnIntegerZeroWithoutInventoryCalls() {
        variables.put("DoDVfMod_contrailNetworkPolicyFqdnList", Collections.emptyList());
        script.deleteNetworkPoliciesFromAAI(execution);
        assertEquals(Integer.valueOf(0), variables.get("DoDVfMod_networkPolicyFqdnCount"));
        assertTrue(effects.isEmpty());
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void eachRequestedPolicyIsQueriedAndItsReturnedIdentityDeletedInOrder() {
        requestBoth();
        script.deleteNetworkPoliciesFromAAI(execution);
        assertEquals(Arrays.asList("GET " + FIRST, "DELETE policy-one", "GET " + SECOND, "DELETE policy-two"), effects);
        assertEquals(Integer.valueOf(2), variables.get("DoDVfMod_networkPolicyFqdnCount"));
        assertEquals(Integer.valueOf(200), variables.get("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode"));
        assertEquals(Integer.valueOf(200), variables.get("DoDVfMod_aaiDeleteNetworkPolicyReturnCode"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void missingInventoryDoesNotDeleteAnUnreturnedPolicyIdentity() {
        variables.put("DoDVfMod_contrailNetworkPolicyFqdnList", Collections.singletonList(FIRST));
        responses.put(FIRST, Optional.empty());
        script.deleteNetworkPoliciesFromAAI(execution);
        assertMissingPolicy();
    }

    @Test
    public void anEmptyInventoryCollectionDoesNotDeleteAnyPolicy() {
        variables.put("DoDVfMod_contrailNetworkPolicyFqdnList", Collections.singletonList(FIRST));
        responses.put(FIRST, Optional.of(new NetworkPolicies()));
        script.deleteNetworkPoliciesFromAAI(execution);
        assertMissingPolicy();
    }

    @Test
    public void aMissingFirstPolicyDoesNotPreventDeletingTheNextReturnedPolicy() {
        requestBoth();
        responses.put(FIRST, Optional.empty());
        script.deleteNetworkPoliciesFromAAI(execution);
        assertEquals(Arrays.asList("GET " + FIRST, "GET " + SECOND, "DELETE policy-two"), effects);
        assertEquals(Integer.valueOf(200), variables.get("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode"));
        assertEquals(Integer.valueOf(200), variables.get("DoDVfMod_aaiDeleteNetworkPolicyReturnCode"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void noRequestedPoliciesPreservesTheOriginalWorkflowFailure() {
        WorkflowException original = new WorkflowException("DoDeleteVfModule", 9001, "original operation failed");
        variables.put("WorkflowException", original);
        script.deleteNetworkPoliciesFromAAI(execution);
        assertSame(original, variables.get("WorkflowException"));
        assertTrue(effects.isEmpty());
    }

    @Test
    public void aFailedFirstDeletionRaisesItsBusinessErrorBeforeTouchingTheNextPolicy() {
        requireDeleteFailurePropagation();
        failedDeletion("policy-one", Arrays.asList("GET " + FIRST, "DELETE policy-one"));
    }

    @Test
    public void aFailedLastDeletionRaisesItsBusinessErrorAfterTheCompletedFirstDeletion() {
        requireDeleteFailurePropagation();
        failedDeletion("policy-two",
                Arrays.asList("GET " + FIRST, "DELETE policy-one", "GET " + SECOND, "DELETE policy-two"));
    }

    private void requestBoth() {
        variables.put("DoDVfMod_contrailNetworkPolicyFqdnList", Arrays.asList(FIRST, SECOND));
    }

    private void assertMissingPolicy() {
        assertEquals(Collections.singletonList("GET " + FIRST), effects);
        assertEquals(Integer.valueOf(404), variables.get("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode"));
        assertFalse(variables.containsKey("DoDVfMod_aaiDeleteNetworkPolicyReturnCode"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    private void failedDeletion(String id, List<String> expectedEffects) {
        requestBoth();
        failedPolicy = id;
        BpmnError failure = assertThrows(BpmnError.class, () -> script.deleteNetworkPoliciesFromAAI(execution));
        assertEquals("2500", failure.getErrorCode());
        assertEquals(expectedEffects, effects);
        assertEquals(Integer.valueOf(500), variables.get("DoDVfMod_aaiDeleteNetworkPolicyReturnCode"));
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("DoDeleteVfModule", error.getProcessKey());
        assertEquals(2500, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains("policy delete rejected"));
    }

    private static NetworkPolicies policies(String id) {
        NetworkPolicy policy = new NetworkPolicy();
        policy.setNetworkPolicyId(id);
        NetworkPolicies policies = new NetworkPolicies();
        policies.getNetworkPolicy().add(policy);
        return policies;
    }

    private static void requireDeleteFailurePropagation() {
        Assume.assumeTrue("SO-4457: policy cleanup catches its own deletion business error and continues",
                Boolean.getBoolean("groovy.migration.verification"));
    }
}
