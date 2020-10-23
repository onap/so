/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowActionExtractResourcesAAITest {

    private static final String CONFIGURATION_ID = "configTestId";
    private static final String VPN_ID = "vpnTestId";

    @Mock
    private BBInputSetupUtils bbInputSetupUtils;
    @InjectMocks
    private WorkflowActionExtractResourcesAAI testedObject;

    @Test
    public void extractRelationshipsConfigurationSuccess() {
        // given
        Relationships relationships = mock(Relationships.class);
        when(relationships.getByType(Types.CONFIGURATION)).thenReturn(getConfigurationList());
        // when
        Optional<Configuration> resultOpt = testedObject.extractRelationshipsConfiguration(relationships);
        // then
        assertThat(resultOpt).isNotEmpty();
        assertThat(resultOpt.get().getConfigurationId()).isEqualTo(CONFIGURATION_ID);
    }

    @Test
    public void extractRelationshipsConfiguration_notFound() {
        // given
        Relationships relationships = mock(Relationships.class);
        when(relationships.getByType(Types.CONFIGURATION)).thenReturn(Collections.emptyList());
        // when
        Optional<Configuration> resultOpt = testedObject.extractRelationshipsConfiguration(relationships);
        // then
        assertThat(resultOpt).isEmpty();
    }

    @Test
    public void extractRelationshipsVpnBindingSuccess() {
        // given
        Relationships relationships = mock(Relationships.class);
        AAIResourceUri aaiResourceUri = mock(AAISimpleUri.class);
        List<AAIResourceUri> aaiResourceUriList = new ArrayList<>();
        aaiResourceUriList.add(aaiResourceUri);
        when(relationships.getRelatedUris(Types.VPN_BINDING)).thenReturn(aaiResourceUriList);
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper("{\"vpn-id\" : \"" + VPN_ID + "\"}");
        when(bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri)).thenReturn(aaiResultWrapper);
        // when
        Optional<VpnBinding> resultOpt = testedObject.extractRelationshipsVpnBinding(relationships);
        // then
        assertThat(resultOpt).isNotEmpty();
        assertThat(resultOpt.get().getVpnId()).isEqualTo(VPN_ID);
    }

    private List<AAIResultWrapper> getConfigurationList() {
        List<AAIResultWrapper> configurations = new ArrayList<>();
        AAIResultWrapper aaiResultWrapper =
                new AAIResultWrapper("{\"configuration-id\" : \"" + CONFIGURATION_ID + "\"}");
        configurations.add(aaiResultWrapper);
        return configurations;
    }
}
