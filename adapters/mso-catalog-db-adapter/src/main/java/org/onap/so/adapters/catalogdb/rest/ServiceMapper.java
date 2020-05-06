/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.rest;

import java.util.ArrayList;
import java.util.List;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.rest.catalog.beans.Cvnfc;
import org.onap.so.rest.catalog.beans.Service;
import org.onap.so.rest.catalog.beans.VfModule;
import org.onap.so.rest.catalog.beans.Vnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMapper.class);

    public Service mapService(org.onap.so.db.catalog.beans.Service service, int depth) {
        Service restService = new Service();
        if (service.getCategory() != null) {
            restService.setCategory(service.getCategory());
        }
        restService.setCreated(service.getCreated());
        restService.setDescription(service.getDescription());

        if (service.getDistrobutionStatus() != null) {
            restService.setDistrobutionStatus(service.getDistrobutionStatus());
        }
        restService.setEnvironmentContext(service.getEnvironmentContext());
        restService.setModelInvariantId(service.getModelInvariantUUID());
        restService.setModelName(service.getModelName());
        restService.setModelVersionId(service.getModelUUID());
        restService.setModelVersion(service.getModelVersion());
        if (service.getServiceRole() != null) {
            restService.setServiceRole(service.getServiceRole());
        }
        restService.setServiceType(service.getServiceType());
        restService.setWorkloadContext(service.getWorkloadContext());
        if (depth > 0)
            restService.setVnf(mapVnfs(service, depth));
        return restService;
    }

    private List<Vnf> mapVnfs(org.onap.so.db.catalog.beans.Service service, int depth) {
        List<Vnf> vnfs = new ArrayList<>();
        logger.info("Vnf Count : {}", service.getVnfCustomizations().size());
        service.getVnfCustomizations().stream().forEach(vnf -> vnfs.add(mapVnf(vnf, depth)));
        return vnfs;
    }

    protected Vnf mapVnf(org.onap.so.db.catalog.beans.VnfResourceCustomization vnfResourceCustomization, int depth) {
        Vnf vnf = new Vnf();
        vnf.setAvailabilityZoneMaxCount(vnfResourceCustomization.getAvailabilityZoneMaxCount());
        vnf.setCategory(vnfResourceCustomization.getVnfResources().getCategory());
        vnf.setCloudVersionMax(vnfResourceCustomization.getVnfResources().getAicVersionMax());
        vnf.setCloudVersionMin(vnfResourceCustomization.getVnfResources().getAicVersionMin());
        vnf.setMaxInstances(vnfResourceCustomization.getMaxInstances());
        vnf.setMinInstances(vnfResourceCustomization.getMinInstances());
        vnf.setModelCustomizationId(vnfResourceCustomization.getModelCustomizationUUID());
        vnf.setModelInstanceName(vnfResourceCustomization.getModelInstanceName());
        vnf.setModelInvariantId(vnfResourceCustomization.getVnfResources().getModelInvariantId());
        vnf.setModelName(vnfResourceCustomization.getVnfResources().getModelName());
        vnf.setModelVersionId(vnfResourceCustomization.getVnfResources().getModelUUID());
        vnf.setModelVersion(vnfResourceCustomization.getVnfResources().getModelVersion());
        vnf.setMultiStageDesign(vnfResourceCustomization.getMultiStageDesign());
        vnf.setNfFunction(vnfResourceCustomization.getNfFunction());
        vnf.setNfNamingCode(vnfResourceCustomization.getNfNamingCode());
        vnf.setNfRole(vnfResourceCustomization.getNfRole());
        vnf.setNfType(vnfResourceCustomization.getNfType());
        vnf.setNfDataValid(vnfResourceCustomization.getNfDataValid());
        vnf.setOrchestrationMode(vnfResourceCustomization.getVnfResources().getOrchestrationMode());
        vnf.setSubCategory(vnfResourceCustomization.getVnfResources().getSubCategory());
        vnf.setToscaNodeType(vnfResourceCustomization.getVnfResources().getToscaNodeType());

        if (depth > 1) {
            vnf.setVfModule(mapVfModules(vnfResourceCustomization, depth));
        }
        return vnf;
    }

    private List<VfModule> mapVfModules(VnfResourceCustomization vnfResourceCustomization, int depth) {
        List<VfModule> vfModules = new ArrayList<>();
        vnfResourceCustomization.getVfModuleCustomizations().stream()
                .forEach(vfModule -> vfModules.add(mapVfModule(vfModule, depth)));
        return vfModules;
    }

    private VfModule mapVfModule(VfModuleCustomization vfModuleCust, int depth) {
        VfModule vfModule = new VfModule();
        vfModule.setAvailabilityZoneCount(vfModuleCust.getAvailabilityZoneCount());
        vfModule.setCreated(vfModuleCust.getCreated());
        vfModule.setDescription(vfModuleCust.getVfModule().getDescription());
        vfModule.setInitialCount(vfModuleCust.getInitialCount());
        vfModule.setIsBase(vfModuleCust.getVfModule().getIsBase());
        vfModule.setIsVolumeGroup(getIsVolumeGroup(vfModuleCust));
        vfModule.setMaxInstances(vfModuleCust.getMaxInstances());
        vfModule.setMinInstances(vfModuleCust.getMinInstances());
        vfModule.setLabel(vfModuleCust.getLabel());
        vfModule.setModelCustomizationId(vfModuleCust.getModelCustomizationUUID());
        vfModule.setModelInvariantId(vfModuleCust.getVfModule().getModelInvariantUUID());
        vfModule.setModelName(vfModuleCust.getVfModule().getModelName());
        vfModule.setModelVersionId(vfModuleCust.getVfModule().getModelUUID());
        vfModule.setModelVersion(vfModuleCust.getVfModule().getModelVersion());
        if (depth > 3) {
            vfModule.setVnfc(mapCvnfcs(vfModuleCust));
        }
        return vfModule;
    }

    private List<Cvnfc> mapCvnfcs(VfModuleCustomization vfModuleCustomization) {
        List<Cvnfc> cvnfcs = new ArrayList<>();
        vfModuleCustomization.getCvnfcCustomization().stream().forEach(cvnfcCust -> cvnfcs.add(mapCvnfcCus(cvnfcCust)));
        return cvnfcs;
    }

    private Cvnfc mapCvnfcCus(CvnfcCustomization cvnfcCust) {
        Cvnfc cvnfc = new Cvnfc();
        cvnfc.setCreated(cvnfcCust.getCreated());
        cvnfc.setDescription(cvnfcCust.getDescription());
        cvnfc.setModelCustomizationId(cvnfcCust.getModelCustomizationUUID());
        cvnfc.setModelInstanceName(cvnfcCust.getModelInstanceName());
        cvnfc.setModelInvariantId(cvnfcCust.getModelInvariantUUID());
        cvnfc.setModelName(cvnfcCust.getModelName());
        cvnfc.setModelVersion(cvnfcCust.getModelVersion());
        cvnfc.setModelVersionId(cvnfcCust.getModelUUID());
        cvnfc.setNfcFunction(cvnfcCust.getNfcFunction());
        cvnfc.setNfcNamingCode(cvnfcCust.getNfcNamingCode());
        return cvnfc;
    }

    private boolean getIsVolumeGroup(VfModuleCustomization vfModuleCust) {
        boolean isVolumeGroup = false;
        HeatEnvironment envt = vfModuleCust.getVolumeHeatEnv();
        if (envt != null) {
            isVolumeGroup = true;
        }
        return isVolumeGroup;
    }

}
