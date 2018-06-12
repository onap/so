package org.openecomp.mso.bpmn.infrastructure.workflow.tasks;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.exception.OrchestrationStatusValidationException;
import org.openecomp.mso.db.catalog.beans.BuildingBlockDetail;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrchestrationStatusValidator {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, OrchestrationStatusValidator.class);
	
	private static final String BUILDING_BLOCK_DETAIL_NOT_FOUND = "Building Block (%s) not set up in Orchestration_Status_Validation table in CatalogDB.";
	private static final String UNKNOWN_RESOURCE_TYPE = "Building Block (%s) not set up correctly in Orchestration_Status_Validation table in CatalogDB. ResourceType=(%s), TargetAction=(%s)";
	private static final String ORCHESTRATION_VALIDATION_FAIL = "Orchestration Status Validation failed. ResourceType=(%s), TargetAction=(%s), OrchestrationStatus=(%s)";
	private static final String ORCHESTRATION_STATUS_VALIDATION_RESULT = "orchestrationStatusValidationResult";
	
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	@Autowired
	private CatalogDbClient catalogDbClient;
	
	public void validateOrchestrationStatus(BuildingBlockExecution execution) {
		try {
			execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT, null);
			
			String buildingBlockFlowName = (String) execution.getVariable("flowToBeCalled");
			
			BuildingBlockDetail buildingBlockDetail = catalogDbClient.getBuildingBlockDetail(buildingBlockFlowName);
			
			if (buildingBlockDetail == null) {
				throw new OrchestrationStatusValidationException(String.format(BUILDING_BLOCK_DETAIL_NOT_FOUND, buildingBlockFlowName));
			}
			
			OrchestrationStatus orchestrationStatus = null;
			
			switch(buildingBlockDetail.getResourceType()) {
			case SERVICE:
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
				orchestrationStatus = serviceInstance.getOrchestrationStatus();
				break;
			case VNF:
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
				orchestrationStatus = genericVnf.getOrchestrationStatus();
				break;
			case VF_MODULE:
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
				orchestrationStatus = vfModule.getOrchestrationStatus();
				break;
			case VOLUME_GROUP:
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
				orchestrationStatus = volumeGroup.getOrchestrationStatus();
				break;
			case NETWORK:
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
				orchestrationStatus = network.getOrchestrationStatus();
				break;
			default:
				// can't currently get here, so not tested. Added in case enum is expanded without a change to this code
				throw new OrchestrationStatusValidationException(String.format(UNKNOWN_RESOURCE_TYPE, buildingBlockFlowName, buildingBlockDetail.getResourceType(), buildingBlockDetail.getTargetAction()));
			}
			
			OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = catalogDbClient.getOrchestrationStatusStateTransitionDirective(buildingBlockDetail.getResourceType(), orchestrationStatus, buildingBlockDetail.getTargetAction());
			
			if (orchestrationStatusStateTransitionDirective.getFlowDirective() == OrchestrationStatusValidationDirective.FAIL) {
				throw new OrchestrationStatusValidationException(String.format(ORCHESTRATION_VALIDATION_FAIL, buildingBlockDetail.getResourceType(), buildingBlockDetail.getTargetAction(), orchestrationStatus));
			}
			
			execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT, orchestrationStatusStateTransitionDirective.getFlowDirective());
		} catch (Exception e) {
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e);
		}
	}
}
