/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vnf.vmfm;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.Map;
import javax.xml.ws.Holder;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.VnfAdapterRestUtils;
import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;

/**
 * Implements the VNF adapter interface for the VNFM. The main responsibility is
 * to clean the interface of the MsoVnfAdapter from misnamed, misused or Heat
 * specific attributes.
 */
public class MsoVnfmAdapterImpl implements MsoVnfAdapter {
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    private VnfmVnfManager vnfmVnfManager = new VnfmVnfManager();
    private VnfmRollbackManager vnfmRollbackManager = new VnfmRollbackManager();
    private VnfmVfModuleManager vnfmVfModuleManager = new VnfmVfModuleManager();

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck() {
        LOGGER.debug("Health check call in VNFM Adapter");
    }

    @Override
    public void createVnf(String cloudSiteId, String tenantId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, String> inputs, Boolean failIfExists, Boolean backout, MsoRequest msoRequest, Holder<String> heatStackId, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException, VnfAlreadyExists {
        String vnfId = VnfAdapterRestUtils.getVnfByName(vnfName, msoRequest.getRequestId()).get().getVnfId();
        String csarId = vnfVersion;
        String cloudOwner = Lists.newArrayList(Splitter.on("_").split(cloudSiteId)).get(0);
        String regionName = Lists.newArrayList(Splitter.on("_").split(cloudSiteId)).get(1);
        prepareRollback(cloudSiteId, cloudSiteId, tenantId, msoRequest, rollback);
        vnfmVnfManager.createVnf(cloudOwner, regionName, tenantId, vnfId, vnfName, inputs, msoRequest, csarId, heatStackId, failIfExists, backout, new Rollback(rollback.value));
    }

    @Override
    public void updateVnf(String cloudSiteId, String tenantId, String vnfId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, String> inputs, MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        prepareRollback(cloudSiteId, cloudSiteId, tenantId, msoRequest, rollback);
        vnfmVnfManager.updateVnf(vnfId, vnfVersion, inputs, msoRequest, new Rollback(rollback.value));
    }

    @Override
    public void deleteVnf(String cloudSiteId, String tenantId, String vnfName, MsoRequest msoRequest) throws VnfException {
        String vnfId = VnfAdapterRestUtils.getVnfByName(vnfName, msoRequest.getRequestId()).get().getVnfId();
        vnfmVnfManager.deleteVnf(vnfId, msoRequest);
    }

    @Override
    public void queryVnf(String cloudSiteId, String tenantId, String vnfName, MsoRequest msoRequest, Holder<Boolean> vnfExists, Holder<String> vnfId, Holder<VnfStatus> status, Holder<Map<String, String>> outputs) throws VnfException {
        vnfmVnfManager.query(vnfExists, vnfId, status);
    }

    @Override
    public void rollbackVnf(VnfRollback rollback) throws VnfException {
        vnfmRollbackManager.rollback(new Rollback(rollback));
    }

    @Override
    public void createVfModule(String cloudSiteId, String tenantId, String vnfCompositType, String vnfVersion, String vnfModuleName, String vfModuleId, String requestType, String volumeGroupHeatStackId, String baseVfHeatStackId, String modelCustomizationUuid, Map<String, String> inputs, Boolean failIfExists, Boolean backout, MsoRequest msoRequest, Holder<String> heatStackId, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException, VnfAlreadyExists {
        String vnfId = VnfAdapterRestUtils.getVnfByName(vnfModuleName, msoRequest.getRequestId()).get().getVnfId();
        prepareRollback(cloudSiteId, tenantId, vnfId, msoRequest, rollback);
        try (CatalogDatabase db = CatalogDatabase.getInstance()) {
            VfModuleCustomization vfModuleCustomization = db.getVfModuleCustomizationByModelCustomizationId(modelCustomizationUuid);
            VfModule module = vfModuleCustomization.getVfModule();
            heatStackId.value = vnfmVfModuleManager.createVfModule(vnfId, vfModuleId, inputs, msoRequest, new Rollback(rollback.value));
        }
    }

    @Override
    public void deleteVfModule(String cloudSiteId, String tenantId, String vnfId, String vfModuleId, String vfModuleHeatStackId, MsoRequest msoRequest, Holder<Map<String, String>> vfModuleOutputs) throws VnfException {
        vnfmVfModuleManager.deleteVfModule(vnfId, vfModuleId, msoRequest);
    }

    @Override
    public void updateVfModule(String cloudSiteId, String tenantId, String vnfId, String vfModuleId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, String baseVfHeatStackId, String vfModuleStackId, String modelCustomizationUuid, Map<String, String> inputs, MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        prepareRollback(cloudSiteId, tenantId, vnfId, msoRequest, rollback);
        vnfmVfModuleManager.updateVfModule(vnfId, vfModuleId, inputs, msoRequest, new Rollback(rollback.value));
    }

    private void prepareRollback(String cloudSiteId, String tenantId, String vnfId, MsoRequest request, Holder<VnfRollback> rollback) {
        rollback.value.setCloudSiteId(cloudSiteId);
        rollback.value.setTenantId(tenantId);
        rollback.value.setMsoRequest(request);
        rollback.value.setTenantCreated(true);
        rollback.value.setVnfId(vnfId);
    }
}