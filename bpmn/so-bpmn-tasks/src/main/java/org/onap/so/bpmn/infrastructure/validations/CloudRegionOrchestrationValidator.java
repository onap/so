package org.onap.so.bpmn.infrastructure.validations;

import java.util.Optional;
import java.util.regex.Pattern;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.validation.PreBuildingBlockValidator;
import org.onap.so.bpmn.common.validation.Skip;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Skip
public class CloudRegionOrchestrationValidator implements PreBuildingBlockValidator {

	private static Logger logger = LoggerFactory.getLogger(CloudRegionOrchestrationValidator.class);
	private final Pattern pattern = Pattern.compile("(?:Activate|Assign|Create|Deactivate|Delete|Unassign|Update)(?:Network|Vnf|VfModule|VolumeGroup|FabricConfiguration)BB");
	
	@Autowired
	private ExceptionBuilder exceptionBuilder;

	@Override
	public boolean shouldRunFor(String bbName) {
		return pattern.matcher(bbName).find();
	}
		
	@Override
	public Optional<String> validate(BuildingBlockExecution execution) {
		String msg = null;
		try {
			CloudRegion cloudRegion = execution.getGeneralBuildingBlock().getCloudRegion();
			if (Boolean.TRUE.equals(cloudRegion.getOrchestrationDisabled())) {
				msg = String.format("Error: The request has failed due to orchestration currently disabled for the target cloud region %s for cloud owner %s", 
						cloudRegion.getLcpCloudRegionId(), cloudRegion.getCloudOwner());
				logger.error(msg);
				return Optional.ofNullable(msg);
			}
		}
		catch(Exception e) {
			logger.error("failed to validate", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e);	
		}
		return Optional.empty();
	}

}
