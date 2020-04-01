package org.onap.so.bpmn.infrastructure.vfmodule;

import java.util.Optional;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.cloud.resource.beans.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CreateVFModule {

    private static final Logger logger = LoggerFactory.getLogger(CreateVFModule.class);

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected ExtractPojosForBB extractPojosForBB;

    public void createInventoryVariable(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            CloudInformation cloudInformation = new CloudInformation();
            cloudInformation.setOwner(gBBInput.getCloudRegion().getCloudOwner());
            cloudInformation.setRegionId(gBBInput.getCloudRegion().getLcpCloudRegionId());
            cloudInformation.setTenantId(gBBInput.getTenant().getTenantId());
            cloudInformation.setTenantName(gBBInput.getTenant().getTenantName());
            cloudInformation.setTenantContext(gBBInput.getTenant().getTenantContext());
            cloudInformation.setTemplateInstanceId(execution.getVariable("heatStackId"));
            cloudInformation.setNodeType(getNodeType(gBBInput.getCloudRegion()));
            cloudInformation.setVnfName(vnf.getVnfName());
            cloudInformation.setVnfId(vnf.getVnfId());
            cloudInformation.setVfModuleId(vfModule.getVfModuleId());
            execution.setVariable("cloudInformation", cloudInformation);
        } catch (Exception e) {
            logger.error("Error building CloudInformation Object for NC Inventory", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }

    protected NodeType getNodeType(CloudRegion cloudRegion) {
        AAIResourceUri cloudRegionUri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId());
        AAIResourcesClient client = getAAIClient();
        Optional<Relationships> relationships = client.get(cloudRegionUri).getRelationships();
        if (relationships.isPresent()) {
            AAIPluralResourceUri networkTechsGreenfieldUri = AAIUriFactory
                    .createResourceUri(AAIObjectType.CLOUD_REGION, cloudRegion.getCloudOwner(),
                            cloudRegion.getLcpCloudRegionId())
                    .relatedTo(AAIObjectPlurals.NETWORK_TECHNOLOGY)
                    .queryParam("network-technology-name", NodeType.GREENFIELD.getNetworkTechnologyName());

            AAIResultWrapper networkTechsGreenfield = client.get(networkTechsGreenfieldUri);
            if (networkTechsGreenfield != null && !networkTechsGreenfield.isEmpty()) {
                return NodeType.GREENFIELD;
            }
        }
        return NodeType.BROWNFIELD;
    }

    protected AAIResourcesClient getAAIClient() {
        return new AAIResourcesClient();
    }

}
