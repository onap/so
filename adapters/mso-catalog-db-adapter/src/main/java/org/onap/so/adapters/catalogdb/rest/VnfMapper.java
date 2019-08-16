package org.onap.so.adapters.catalogdb.rest;

import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.rest.catalog.beans.Vnf;
import org.springframework.stereotype.Component;
import com.google.common.base.Strings;

@Component
public class VnfMapper {

    public VnfResourceCustomization mapVnf(VnfResourceCustomization vnfCust, Vnf vnf) {

        vnfCust.setAvailabilityZoneMaxCount(vnf.getAvailabilityZoneMaxCount());
        vnfCust.setMaxInstances(vnf.getMaxInstances());
        vnfCust.setMinInstances(vnf.getMinInstances());
        vnfCust.setModelCustomizationUUID(vnf.getModelCustomizationId());
        vnfCust.setModelInstanceName(vnf.getModelInstanceName());
        vnfCust.setMultiStageDesign(vnf.getMultiStageDesign());
        vnfCust.setNfDataValid(vnf.getNfDataValid());
        vnfCust.setNfFunction(Strings.nullToEmpty(vnf.getNfFunction()));
        vnfCust.setNfNamingCode(Strings.nullToEmpty(vnf.getNfNamingCode()));
        vnfCust.setNfRole(Strings.nullToEmpty(vnf.getNfRole()));
        vnfCust.setNfType(Strings.nullToEmpty(vnf.getNfType()));

        VnfResource vnfRes = vnfCust.getVnfResources();
        vnfRes.setOrchestrationMode(Strings.nullToEmpty(vnfRes.getOrchestrationMode()));
        vnfRes.setSubCategory(Strings.nullToEmpty(vnfRes.getSubCategory()));
        vnfRes.setToscaNodeType(Strings.nullToEmpty(vnfRes.getToscaNodeType()));
        vnfRes.setModelInvariantUUID(vnfRes.getModelInvariantId());
        vnfRes.setModelName(vnfRes.getModelName());
        vnfRes.setModelUUID(vnfRes.getModelUUID());
        vnfRes.setModelVersion(vnfRes.getModelVersion());
        return vnfCust;
    }

}
