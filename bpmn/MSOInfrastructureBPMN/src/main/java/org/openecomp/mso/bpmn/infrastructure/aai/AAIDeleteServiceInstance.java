package org.openecomp.mso.bpmn.infrastructure.aai;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

public class AAIDeleteServiceInstance implements JavaDelegate{

	ExceptionUtil exceptionUtil = new ExceptionUtil();
	public void execute(DelegateExecution execution) throws Exception {
		try{
			String serviceInstanceId = (String) execution.getVariable("serviceInstanceId");
			AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
					serviceInstanceId);
			AAIResourcesClient aaiRC = new AAIResourcesClient();
			aaiRC.delete(serviceInstanceURI);
			execution.setVariable("GENDS_SuccessIndicator",true);
		} catch(Exception ex){
			String msg = "Exception in Delete Serivce Instance. Service Instance could not be deleted in AAI." + ex.getMessage();
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		
	}
	
}
