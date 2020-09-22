/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nokia
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

package org.onap.so.bpmn.infrastructure.vfmodule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.cloud.resource.beans.CloudInformation;

@RunWith(MockitoJUnitRunner.class)
public class DeleteVfModuleTest {

    private static final String VF_MODULE_ID = "vfModuleIdTest";
    private static final String HEAT_STACK_ID = "hsId";
    private static final String VNF_ID = "vnfId";
    private static final String VNF_NAME = "vnfName";

    private static final String CLOUD_OWNER = "cloudOwTest";
    private static final String LCP_CLOUD_REGION_ID = "lcpClRegTest";
    private static final String TENANT_ID = "tenantIdTest";
    private static final String TENANT_NAME = "tenantNameTest";
    private static final String TENANT_CONTEXT = "tenantContext";

    @Mock
    private ExtractPojosForBB extractPojosForBB;

    @InjectMocks
    private DeleteVFModule testedObject;

    @Test
    public void createInventoryVariable_Success() throws BBObjectNotFoundException {
        // given
        BuildingBlockExecution buildingBlockExecution = prepareBuildingBlockExecution();
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.VF_MODULE_ID))
                .thenReturn(prepareVmModule());
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(prepareGenericVnf());
        // when
        testedObject.createInventoryVariable(buildingBlockExecution);
        // then
        verifyExecution(buildingBlockExecution);
    }

    private void verifyExecution(BuildingBlockExecution buildingBlockExecution) {
        CloudInformation cloudInformation = buildingBlockExecution.getVariable("cloudInformation");
        assertThat(cloudInformation.getOwner()).isEqualTo(CLOUD_OWNER);
        assertThat(cloudInformation.getRegionId()).isEqualTo(LCP_CLOUD_REGION_ID);
        assertThat(cloudInformation.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(cloudInformation.getTenantName()).isEqualTo(TENANT_NAME);
        assertThat(cloudInformation.getTenantContext()).isEqualTo(TENANT_CONTEXT);
        assertThat(cloudInformation.getTemplateInstanceId()).isEqualTo(HEAT_STACK_ID);

        assertThat(cloudInformation.getVnfId()).isEqualTo(VNF_ID);
        assertThat(cloudInformation.getVnfName()).isEqualTo(VNF_NAME);
        assertThat(cloudInformation.getVfModuleId()).isEqualTo(VF_MODULE_ID);
    }

    private BuildingBlockExecution prepareBuildingBlockExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable("gBBInput", prepareGeneralBuildingBlock());
        return new DelegateExecutionImpl(execution);
    }

    private GeneralBuildingBlock prepareGeneralBuildingBlock() {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setCloudOwner(CLOUD_OWNER);
        cloudRegion.setLcpCloudRegionId(LCP_CLOUD_REGION_ID);
        generalBuildingBlock.setCloudRegion(cloudRegion);
        Tenant tenant = new Tenant();
        tenant.setTenantId(TENANT_ID);
        tenant.setTenantName(TENANT_NAME);
        tenant.setTenantContext(TENANT_CONTEXT);
        generalBuildingBlock.setTenant(tenant);
        return generalBuildingBlock;
    }

    private VfModule prepareVmModule() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId(VF_MODULE_ID);
        vfModule.setHeatStackId(HEAT_STACK_ID);
        return vfModule;
    }

    private GenericVnf prepareGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(VNF_ID);
        genericVnf.setVnfName(VNF_NAME);
        return genericVnf;
    }
}
