/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2024 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.adapters.vnf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.xml.ws.Holder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.adapters.vdu.mapper.VfModuleCustomizationToVduMapper;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoKeystoneUtils;
import org.onap.so.openstack.utils.MsoMulticloudUtils;
import org.springframework.core.env.Environment;

/**
 * Pure-Mockito unit tests for {@link MsoVnfPluginAdapterImpl}, which previously had zero coverage.
 *
 * The class under test is a ~1,150 LOC adapter with two "god methods" (createVfModule/updateVfModule) that mix catalog
 * lookups, parameter filtering, environment merging and VDU plugin dispatch. This test intentionally covers only the
 * tractable surface: the trivial/unsupported endpoints, the read-only queryVnf path, the deleteVfModule path, the
 * rollbackVnf branches, and the early validation branches of createVfModule. Deep, end-to-end coverage of the
 * createVfModule/updateVfModule happy paths is deliberately deferred to the structural refactor tracked under Issue-ID
 * SO-4402 - exercising those fully here would require reconstructing large HEAT template/environment fixtures and is
 * better done once the methods are decomposed.
 *
 * getVduPlugin(cloudSiteId, cloudOwner) selects the VDU plugin from the CloudSite orchestrator: a CloudSite with
 * orchestrator "HEAT" routes to MsoHeatUtils, so these tests stub cloudConfig + heatUtils to drive the plugin calls.
 */
@RunWith(MockitoJUnitRunner.class)
public class MsoVnfPluginAdapterImplTest {

    @Mock
    private CloudConfig cloudConfig;

    @Mock
    private VFModuleCustomizationRepository vfModuleCustomRepo;

    @Mock
    private Environment environment;

    @Mock
    private MsoKeystoneUtils keystoneUtils;

    @Mock
    private MsoHeatUtils heatUtils;

    @Mock
    private MsoMulticloudUtils multicloudUtils;

    @Mock
    private VfModuleCustomizationToVduMapper vduMapper;

    @InjectMocks
    private MsoVnfPluginAdapterImpl adapter;

    private static final String CLOUD_SITE_ID = "mtn13";
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String TENANT_ID = "88a6ca3ee0394ade9403f075db23167e";

    private MsoRequest msoRequest() {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");
        return msoRequest;
    }

    private CloudSite heatCloudSite() {
        CloudSite cloudSite = new CloudSite();
        cloudSite.setId(CLOUD_SITE_ID);
        cloudSite.setOrchestrator("HEAT");
        return cloudSite;
    }

    private VduInstance vduInstance(String id, VduStateType state, Map<String, Object> outputs) {
        VduInstance vduInstance = new VduInstance();
        vduInstance.setVduInstanceId(id);
        vduInstance.setStatus(new VduStatus(state));
        if (outputs != null) {
            vduInstance.setOutputs(outputs);
        }
        return vduInstance;
    }

    @Test
    public void healthCheck_doesNotTouchCollaborators() {
        adapter.healthCheck();

        // healthCheck only logs; it must not reach out to any plugin, catalog or cloud collaborator.
        verifyNoInteractions(cloudConfig, vfModuleCustomRepo, heatUtils, multicloudUtils, keystoneUtils, vduMapper);
    }

    @Test
    public void queryVnf_existingStack_populatesHolders() throws Exception {
        Map<String, Object> stackOutputs = new HashMap<>();
        stackOutputs.put("output1", "value1");
        VduInstance found = vduInstance("stack-id-123", VduStateType.INSTANTIATED, stackOutputs);

        when(cloudConfig.getCloudSite(CLOUD_SITE_ID)).thenReturn(Optional.of(heatCloudSite()));
        when(heatUtils.queryVdu(any(CloudInfo.class), eq("vnf-name"))).thenReturn(found);

        Holder<Boolean> vnfExists = new Holder<>();
        Holder<String> vnfId = new Holder<>();
        Holder<VnfStatus> status = new Holder<>();
        Holder<Map<String, String>> outputs = new Holder<>();

        adapter.queryVnf(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "vnf-name", msoRequest(), vnfExists, vnfId, status,
                outputs);

        assertEquals(Boolean.TRUE, vnfExists.value);
        assertEquals(VnfStatus.ACTIVE, status.value);
        assertEquals("stack-id-123", vnfId.value);
        assertNotNull(outputs.value);
        assertEquals("value1", outputs.value.get("output1"));
    }

    @Test
    public void queryVnf_notFound_populatesHoldersAsAbsent() throws Exception {
        VduInstance notFound = vduInstance(null, VduStateType.NOTFOUND, null);

        when(cloudConfig.getCloudSite(CLOUD_SITE_ID)).thenReturn(Optional.of(heatCloudSite()));
        when(heatUtils.queryVdu(any(CloudInfo.class), eq("missing-vnf"))).thenReturn(notFound);

        Holder<Boolean> vnfExists = new Holder<>();
        Holder<String> vnfId = new Holder<>();
        Holder<VnfStatus> status = new Holder<>();
        Holder<Map<String, String>> outputs = new Holder<>();

        adapter.queryVnf(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "missing-vnf", msoRequest(), vnfExists, vnfId, status,
                outputs);

        assertEquals(Boolean.FALSE, vnfExists.value);
        assertEquals(VnfStatus.NOTFOUND, status.value);
        assertEquals(null, vnfId.value);
        assertNotNull(outputs.value);
        assertTrue(outputs.value.isEmpty());
    }

    @Test
    public void deleteVfModule_existingStack_queriesOutputsAndDeletes() throws Exception {
        Map<String, Object> stackOutputs = new HashMap<>();
        stackOutputs.put("server_name", "vm-1");
        VduInstance found = vduInstance("stack-id-456", VduStateType.INSTANTIATED, stackOutputs);

        when(cloudConfig.getCloudSite(CLOUD_SITE_ID)).thenReturn(Optional.of(heatCloudSite()));
        when(heatUtils.queryVdu(any(CloudInfo.class), eq("vf-module-id"))).thenReturn(found);

        Holder<Map<String, String>> outputs = new Holder<>();

        adapter.deleteVfModule(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "vf-module-id", msoRequest(), outputs);

        assertNotNull(outputs.value);
        assertEquals("vm-1", outputs.value.get("server_name"));
        verify(heatUtils).deleteVdu(any(CloudInfo.class), eq("vf-module-id"), anyInt());
    }

    @Test
    public void deleteVfModule_notFound_skipsDelete() throws Exception {
        VduInstance notFound = vduInstance(null, VduStateType.NOTFOUND, null);

        when(cloudConfig.getCloudSite(CLOUD_SITE_ID)).thenReturn(Optional.of(heatCloudSite()));
        when(heatUtils.queryVdu(any(CloudInfo.class), eq("gone"))).thenReturn(notFound);

        Holder<Map<String, String>> outputs = new Holder<>();

        adapter.deleteVfModule(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "gone", msoRequest(), outputs);

        assertNotNull(outputs.value);
        assertTrue(outputs.value.isEmpty());
        // NOTFOUND is treated as an already-deleted success, so no deleteVdu call should be made.
        verify(heatUtils, never()).deleteVdu(any(CloudInfo.class), eq("gone"), anyInt());
    }

    @Test
    public void rollbackVnf_nullRollback_isNoOp() throws Exception {
        adapter.rollbackVnf(null);

        // A null rollback object (e.g. stack already existed at create time) must short-circuit before any plugin call.
        verifyNoInteractions(cloudConfig, heatUtils, multicloudUtils);
    }

    @Test
    public void rollbackVnf_vnfNotCreated_isNoOp() throws Exception {
        VnfRollback rollback = new VnfRollback();
        rollback.setVnfCreated(false);
        rollback.setCloudSiteId(CLOUD_SITE_ID);

        adapter.rollbackVnf(rollback);

        // Nothing was created originally, so rollback must not attempt to delete anything.
        verifyNoInteractions(cloudConfig, heatUtils, multicloudUtils);
    }

    @Test
    public void rollbackVnf_populatedRollback_deletesVfModule() throws Exception {
        VnfRollback rollback = new VnfRollback();
        rollback.setVnfCreated(true);
        rollback.setCloudSiteId(CLOUD_SITE_ID);
        rollback.setCloudOwner(CLOUD_OWNER);
        rollback.setTenantId(TENANT_ID);
        rollback.setVfModuleStackId("rollback-stack-id");

        when(cloudConfig.getCloudSite(CLOUD_SITE_ID)).thenReturn(Optional.of(heatCloudSite()));
        // deleteVdu's return value is dereferenced for logging, so it must be non-null.
        when(heatUtils.deleteVdu(any(CloudInfo.class), eq("rollback-stack-id"), anyInt()))
                .thenReturn(vduInstance("rollback-stack-id", VduStateType.DELETED, null));

        adapter.rollbackVnf(rollback);

        verify(heatUtils).deleteVdu(any(CloudInfo.class), eq("rollback-stack-id"), anyInt());
    }

    @Test
    public void createVfModule_nullModelCustomizationUuid_throwsUserDataException() {
        VnfException ex = assertThrows(VnfException.class,
                () -> adapter.createVfModule(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "vnfType", "1", "genericVnfId",
                        "vfModuleName", "vfModuleId", "VFMOD", null, null, null, new HashMap<>(), Boolean.FALSE,
                        Boolean.TRUE, Boolean.FALSE, msoRequest(), new Holder<>()));

        assertTrue(ex.getMessage().contains("modelCustomizationUuid"));
        // Validation must fail before touching the catalog repository or any cloud plugin.
        verifyNoInteractions(vfModuleCustomRepo, heatUtils, multicloudUtils);
    }

    @Test
    public void createVfModule_vfModuleCustNotFound_throwsUserDataException() {
        when(vfModuleCustomRepo.findFirstByModelCustomizationUUIDOrderByCreatedDesc("unknown-uuid")).thenReturn(null);

        VnfException ex = assertThrows(VnfException.class,
                () -> adapter.createVfModule(CLOUD_SITE_ID, CLOUD_OWNER, TENANT_ID, "vnfType", "1", "genericVnfId",
                        "vfModuleName", "vfModuleId", "VFMOD", null, null, "unknown-uuid", new HashMap<>(),
                        Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest(), new Holder<>()));

        assertTrue(ex.getMessage().contains("Unable to find vfModuleCust"));
        // The cloud site lookup / plugin dispatch is downstream of the failed catalog lookup and must not run.
        verifyNoInteractions(cloudConfig, heatUtils, multicloudUtils);
    }
}
