package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import java.util.Optional;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnassignNetworkBB {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, UnassignNetworkBB.class);
	
	private static String MESSAGE_CANNOT_PERFORM_UNASSIGN = "Cannot perform Unassign Network. Network is still related to ";	
	private static String MESSAGE_ERROR_ROLLBACK = " Rollback is not possible. Please restore data manually.";	
	
	@Autowired
	private ExceptionBuilder exceptionUtil;

	@Autowired
	private NetworkBBUtils networkBBUtils;

	/**
	 * BPMN access method to prepare overall error messages.
	 * 
	 * @param execution - BuildingBlockExecution
	 * @param relatedToValue - String, ex: vf-module
	 * @return void - nothing
	 * @throws Exception
	 */
	
	public void checkRelationshipRelatedTo(BuildingBlockExecution execution, String relatedToValue) throws Exception {
		try {
			AAIResultWrapper aaiResultWrapper = execution.getVariable("l3NetworkAAIResultWrapper");
			Optional<org.onap.aai.domain.yang.L3Network> l3network = aaiResultWrapper.asBean(org.onap.aai.domain.yang.L3Network.class);
			if (networkBBUtils.isRelationshipRelatedToExists(l3network, relatedToValue)) {
				String msg = MESSAGE_CANNOT_PERFORM_UNASSIGN + relatedToValue;
				execution.setVariable("ErrorUnassignNetworkBB", msg);
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}	
	
	/**
	 * BPMN access method to getCloudRegionId
	 * 
	 * @param execution - BuildingBlockExecution
	 * @return void - nothing
	 * @throws Exception
	 */
	
	public void getCloudSdncRegion(BuildingBlockExecution execution) throws Exception {
		try {
			String cloudRegionSdnc = networkBBUtils.getCloudRegion(execution, SourceSystem.SDNC);
			execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}		
	
	/**
	 * BPMN access method to prepare overall error messages.
	 * 
	 * @param execution - BuildingBlockExecution
	 * @return void - nothing
	 */
	public void errorEncountered(BuildingBlockExecution execution) {
		String msg;
		boolean isRollbackNeeded = execution.getVariable("isRollbackNeeded") != null ? execution.getVariable("isRollbackNeeded") : false;
		if (isRollbackNeeded == true) {
			msg = execution.getVariable("ErrorUnassignNetworkBB") + MESSAGE_ERROR_ROLLBACK;
		} else {
			msg = execution.getVariable("ErrorUnassignNetworkBB");
		}
		exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
	}	
	
	
}
