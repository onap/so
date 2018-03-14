package org.openecomp.mso.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.client.orchestration.AAIOrchestrator
import org.openecomp.mso.client.orchestration.SDNCOrchestrator

public class DoCreateServiceInstanceRollbackV2 extends AbstractServiceTaskProcessor{

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		
	}
	
	public void aaiServiceInstanceRollback (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")	
		def aaiServiceInstanceRollback = execution.getVariable("aaiServiceInstanceRollback")
		if(aaiServiceInstanceRollback){
			utils.log("DEBUG"," ***** Started aaiServiceInstanceRollback *****",  isDebugEnabled)
			try{
				ServiceDecomposition serviceDecomp = execution.getVariable("ServiceDecomposition")
				AAIOrchestrator aaiO = new AAIOrchestrator()
				aaiO.deleteServiceInstance(serviceDecomp)
			}catch (Exception ex) {
				String msg =  "Error Response from AAI for aaiServiceInstanceRollback"
				execution.setVariable("rollbackError", msg)
				utils.log("DEBUG", msg, isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
			utils.log("DEBUG"," ***** Completed aaiServiceInstanceRollback *****",  isDebugEnabled)
		}else{
			utils.log("DEBUG", "***** SKIPPING A&AI ROLLBACK *****", isDebugEnabled)
		}
	}
	
	public void rollbackError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** rollbackError ***** ", isDebugEnabled)
		try{
			utils.log("DEBUG", "Caught an Exception in DoCreateServiceInstanceRollbackV2", isDebugEnabled)
		}catch(BpmnError b){
			utils.log("DEBUG", "BPMN Error during rollbackError: " + b.getMessage(), isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during rollbackError: " + e.getMessage(), isDebugEnabled)
		}
		utils.log("DEBUG", " Exit processRollbackException", isDebugEnabled)
	}
	
}
