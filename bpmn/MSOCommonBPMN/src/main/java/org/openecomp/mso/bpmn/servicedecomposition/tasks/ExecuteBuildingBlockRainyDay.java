package org.openecomp.mso.bpmn.servicedecomposition.tasks;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecuteBuildingBlockRainyDay {
	
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ExecuteBuildingBlockRainyDay.class);
	
	@Autowired
	private CatalogDbClient catalogDbClient;
	private static final String ASTERISK = "ASTERISK";

	public void setRetryTimer(DelegateExecution execution) {
		try {
			int retryCount = (int) execution.getVariable("retryCount");
			int retryTimeToWait = (int) Math.pow(5, retryCount);
			String RetryDuration = "PT" + retryTimeToWait + "M";
			execution.setVariable("RetryDuration", RetryDuration);
		} catch (Exception e) {
			msoLogger.error(e);
			throw new BpmnError("Unknown error incrementing retry counter");
		}
	}
	
	public void queryRainyDayTable(DelegateExecution execution) {
		try {
			ExecuteBuildingBlock ebb = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
			int sequenceNo = ebb.getBuildingBlock().getSequenceNumber();
			String bbName = ebb.getBuildingBlock().getBpmnFlowName();
			GeneralBuildingBlock gBBInput = (GeneralBuildingBlock) execution.getVariable("gBBInput");
			String serviceType = ASTERISK;
			try {
				serviceType = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0).getModelInfoServiceInstance().getServiceType();
			} catch (Exception ex) {
				// keep default serviceType value
			}
			String vnfType = ASTERISK;
			try {
				vnfType = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs().get(sequenceNo).getVnfType();
			} catch (Exception ex) {
				// keep default vnfType value
			}
			WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
			String errorCode = ASTERISK;
			try {
				errorCode = "" + workflowException.getErrorCode();
			} catch (Exception ex) {
				// keep default errorCode value
			}
			String workStep = ASTERISK;
			try {
				workStep = workflowException.getWorkStep();
			} catch (Exception ex) {
				// keep default workStep value
			}
			RainyDayHandlerStatus rainyDayHandlerStatus;
			String handlingCode = "";
			rainyDayHandlerStatus = catalogDbClient.getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(bbName,serviceType,vnfType,errorCode,workStep);
			if(rainyDayHandlerStatus==null){
				rainyDayHandlerStatus = catalogDbClient.getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(bbName,ASTERISK,ASTERISK,ASTERISK,ASTERISK);
				if(rainyDayHandlerStatus==null){
					handlingCode = "Abort";
				}else{
					handlingCode = rainyDayHandlerStatus.getPolicy();
				}
			}else{
				handlingCode = rainyDayHandlerStatus.getPolicy();
			}
			msoLogger.debug("RainyDayHandler Status Code is: " + handlingCode);
			execution.setVariable("handlingCode", handlingCode);
		} catch (Exception e) {
			msoLogger.debug("RainyDayHandler Status Code is: Abort");
			execution.setVariable("handlingCode", "Abort");
		}
	}
}
