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

package org.onap.so.bpmn.common.scripts

import static com.shazam.shazamcrest.MatcherAssert.assertThat
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipData
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.constants.Defaults

class ConfirmVolumeGroupTenantTest extends MsoGroovyTest {

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    public void init(){
        super.init("ConfirmVolumeGroupTenant")
    }
    @Test
    void testpreProcessRequest() {
        VolumeGroup expectedVolumeGroup = new VolumeGroup();
        expectedVolumeGroup.setVolumeGroupId("VolumeGroupId")
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("tenant")
        RelationshipData data = new RelationshipData();
        data.setRelationshipKey("tenant.tenant-id")
        data.setRelationshipValue("tenantId")
        relationship.setRelatedLink("/cloud-infrastructure/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER.toString() + "/cloudRegionId/tenants/tenant/tenantId")
        relationship.getRelationshipData().add(data)
        relationshipList.getRelationship().add(relationship)
        expectedVolumeGroup.setRelationshipList(relationshipList)
        
        
        expectedVolumeGroup.setRelationshipList(relationshipList)
        ConfirmVolumeGroupTenant confirmVolumeGroupTenant = spy(ConfirmVolumeGroupTenant.class)
        when(confirmVolumeGroupTenant.getAAIClient()).thenReturn(client)
        when(mockExecution.getVariable("aicCloudRegion")).thenReturn("aicCloudRegionId");
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("volumeGroupId");
        when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
        when(mockExecution.getVariable("volumeGroupName")).thenReturn('testVolumeGroupName')
        when(mockExecution.getVariable("tenantId")).thenReturn('tenantId')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn('/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        AAIResourceUri arURI = AAIUriFactory. createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), "aicCloudRegionId").volumeGroup("volumeGroupId"))
        AAIResultWrapper wrapper = new AAIResultWrapper(expectedVolumeGroup)
        when(client.get(arURI)).thenReturn(wrapper)
        confirmVolumeGroupTenant.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
        List<ExecutionEntity> executionEntities = captor.getAllValues()

        assertThat(executionEntities.get(3), sameBeanAs(expectedVolumeGroup))
    }
}
