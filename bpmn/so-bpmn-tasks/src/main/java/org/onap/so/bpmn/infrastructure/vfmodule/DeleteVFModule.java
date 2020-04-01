package org.onap.so.bpmn.infrastructure.vfmodule;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteVFModule {

    private static final Logger logger = LoggerFactory.getLogger(DeleteVFModule.class);

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    public void createInventoryVariable(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            CloudInformation cloudInformation = new CloudInformation();
            cloudInformation.setOwner(gBBInput.getCloudRegion().getCloudOwner());
            cloudInformation.setRegionId(gBBInput.getCloudRegion().getLcpCloudRegionId());
            cloudInformation.setTenantId(gBBInput.getTenant().getTenantId());
            cloudInformation.setTenantName(gBBInput.getTenant().getTenantName());
            cloudInformation.setTenantContext(gBBInput.getTenant().getTenantContext());
            cloudInformation.setTemplateInstanceId(vfModule.getHeatStackId());
            cloudInformation.setVnfName(vnf.getVnfName());
            cloudInformation.setVnfId(vnf.getVnfId());
            cloudInformation.setVfModuleId(vfModule.getVfModuleId());

            execution.setVariable("cloudInformation", cloudInformation);
        } catch (Exception e) {
            logger.error("Error building CloudInformation Object for NC Inventory", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }


}
