/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.mso.adapters.vnf.vmfm;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.ws.Holder;
import org.onap.vnfmadapter.so.model.SoMsoRequest;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.yaml.snakeyaml.Yaml;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.openecomp.mso.adapters.vnf.VnfAdapterRestUtils.getVnfByName;
import static org.openecomp.mso.adapters.vnf.vmfm.VnfmRollbackManager.RequestType.CREATE_VF_MODULE;
import static org.openecomp.mso.adapters.vnf.vmfm.VnfmRollbackManager.RequestType.UPDATE_VF_MODULE;
import static org.openecomp.mso.adapters.vnf.vmfm.VnfmRollbackManager.prepare;

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

    static SoMsoRequest buildMsoRequest(MsoRequest msoRequest) {
        SoMsoRequest soMsoRequest = new SoMsoRequest();
        soMsoRequest.setRequestId(msoRequest.getRequestId());
        soMsoRequest.setServiceInstanceId(msoRequest.getServiceInstanceId());
        return soMsoRequest;
    }

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck() {
        LOGGER.debug("Health check call in VNFM Adapter");
    }

    @Override
    public void createVnf(String cloudSiteId, String tenantId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, String> inputs, Boolean failIfExists, Boolean backout, MsoRequest msoRequest, Holder<String> heatStackId, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException, VnfAlreadyExists {
        String vnfId = getVnfByName(vnfName, msoRequest.getRequestId()).get().getVnfId();
        String csarId = vnfVersion;
        ArrayList<String> cloudSiteParts = newArrayList(on("_").split(cloudSiteId));
        String cloudOwner = cloudSiteParts.get(0);
        String regionName = cloudSiteParts.get(1);
        prepare(rollback, vnfId, cloudSiteId, msoRequest);
        rollback.value.setRequestType(VnfmRollbackManager.RequestType.CREATE_VNF.name());
        rollback.value.setIsBase(true);
        vnfmVnfManager.createVnf(cloudOwner, regionName, tenantId, vnfId, vnfName, inputs, msoRequest, csarId, failIfExists, backout);
    }

    @Override
    public void updateVnf(String cloudSiteId, String tenantId, String vnfId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, String> inputs, MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        prepare(rollback, vnfId, cloudSiteId, msoRequest);
        vnfmVnfManager.updateVnf(vnfId, vnfVersion, inputs, msoRequest, rollback);
    }

    @Override
    public void deleteVnf(String cloudSiteId, String tenantId, String vnfName, MsoRequest msoRequest) {
        vnfmVnfManager.deleteVnf(getVnfByName(vnfName, msoRequest.getRequestId()).get().getVnfId(), msoRequest);
    }

    @Override
    public void queryVnf(String cloudSiteId, String tenantId, String vnfName, MsoRequest msoRequest, Holder<Boolean> vnfExists, Holder<String> heatStackId, Holder<VnfStatus> status, Holder<Map<String, String>> outputs) throws VnfException {
        vnfmVnfManager.query(getVnfByName(vnfName, msoRequest.getRequestId()).get().getVnfId(), msoRequest, vnfExists, heatStackId, status);
    }

    @Override
    public void rollbackVnf(VnfRollback rollback) {
        vnfmRollbackManager.rollback(rollback);
    }

    @Override
    public void createVfModule(String cloudSiteId, String tenantId, String vnfType, String vnfVersion, String vnfModuleName, String vfModuleId, String requestType, String volumeGroupHeatStackId, String baseVfHeatStackId, String modelCustomizationUuid, Map<String, String> inputs, Boolean failIfExists, Boolean backout, MsoRequest msoRequest, Holder<String> heatStackId, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException, VnfAlreadyExists {
        String vnfId = getVnfByName(vnfModuleName, msoRequest.getRequestId()).get().getVnfId();
        prepare(rollback, vnfId, cloudSiteId, msoRequest);
        rollback.value.setRequestType(CREATE_VF_MODULE.name());
        rollback.value.setBaseGroupHeatStackId(vfModuleId);
        rollback.value.setIsBase(false);
        try (CatalogDatabase db = CatalogDatabase.getInstance()) {
            VfModuleCustomization vfModuleCustomization = db.getVfModuleCustomizationByModelCustomizationId(modelCustomizationUuid);
            HeatEnvironment heatEnvironmentByArtifactUuid = db.getHeatEnvironmentByArtifactUuid(vfModuleCustomization.getHeatEnvironmentArtifactUuid());
            JsonObject root = new Gson().toJsonTree(new Yaml().load(heatEnvironmentByArtifactUuid.getEnvironment())).getAsJsonObject();
            String scalingAspectId = root.get("parameters").getAsJsonObject().get("etsi.scalingAspectId").getAsString();
            vnfmVfModuleManager.createVfModule(vnfId, vfModuleId, scalingAspectId, inputs, msoRequest, failIfExists, backout);
        }
    }

    @Override
    public void deleteVfModule(String cloudSiteId, String tenantId, String vnfId, String vfModuleId, String vfModuleHeatStackId, MsoRequest msoRequest, Holder<Map<String, String>> vfModuleOutputs) throws VnfException {
        vnfmVfModuleManager.deleteVfModule(vnfId, vfModuleId, msoRequest);
    }

    @Override
    public void updateVfModule(String cloudSiteId, String tenantId, String vnfId, String vfModuleId, String vnfType, String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId, String baseVfHeatStackId, String vfModuleStackId, String modelCustomizationUuid, Map<String, String> inputs, MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        prepare(rollback, vnfId, cloudSiteId, msoRequest);
        rollback.value.setRequestType(UPDATE_VF_MODULE.name());
        rollback.value.setBaseGroupHeatStackId(vfModuleId);
        rollback.value.setIsBase(false);
        vnfmVfModuleManager.updateVfModule(vnfId, vfModuleId, inputs, msoRequest, rollback);
    }
}