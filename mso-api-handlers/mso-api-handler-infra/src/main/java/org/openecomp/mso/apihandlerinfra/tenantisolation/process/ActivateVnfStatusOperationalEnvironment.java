/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.AsdcClientCallFailed;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.TenantIsolationException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AsdcClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Distribution;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatus;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatusDb;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatus;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatusDb;


public class ActivateVnfStatusOperationalEnvironment extends OperationalEnvironmentProcess {

	private static final String SERVICE_NAME = "ActivateVnfStatusOperationalEnvironment"; 
	private AsdcClientHelper asdcClientHelper = null;
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	private String className = this.getClass().getSimpleName();
	private String methodName = "";
	private String classMethodMessage = "";
	private String errorMessage = "";
	
	private String operationalEnvironmentId = "";
	private boolean successIndicator = false;
	
	MsoJavaProperties properties; 
	OperationalEnvDistributionStatusDb activateDistributionDb = null;
	OperationalEnvDistributionStatus queryDistributionDbResponse = null;
	OperationalEnvServiceModelStatusDb activateServiceModelDb = null; 
	OperationalEnvServiceModelStatus queryServiceModelResponse = null;
	
	/**
	 * The class constructor with loadProperties()
	 * @param CloudOrchestrationRequest - object   
	 * @param requestId - string 	  
	 */		
	public ActivateVnfStatusOperationalEnvironment(CloudOrchestrationRequest request, String requestId) {
		super(request, requestId);
		MsoLogger.setServiceName (getRequestId());
        MsoLogger.setLogContext(getRequestId(), getRequest().getOperationalEnvironmentId());		
		this.properties = MsoPropertiesUtils.loadMsoProperties();
		asdcClientHelper = new AsdcClientHelper(properties);
	}
	
	@Override
	protected String getServiceName() {
		return ActivateVnfStatusOperationalEnvironment.SERVICE_NAME;
	}	
	

	/**
	 * The Point-Of-Entry from APIH with activate status from ASDC
	 * @return void - nothing 
	 */
	@Override
	public void execute() {
	
		methodName = "execute() method. ";
		classMethodMessage = className + " " + methodName;
		msoLogger.debug("Begin of " + classMethodMessage);	

		activateDistributionDb = getOperationalEnvDistributionStatusDb();		
		activateServiceModelDb = getOperationalEnvServiceModelStatusDb();
		
		try {
			
			String asdcDistributionId = request.getDistributionId();
			Distribution distributionObject = request.getDistribution();
			msoLogger.debug(" ** asdcDistributionId: " + asdcDistributionId + ";" +  " status: " +  request.getDistribution().getStatus());
			
			// Distribution, Query for operationalEnvironmentId, serviceModelVersionId
			queryDistributionDbResponse = activateDistributionDb.getOperationalEnvDistributionStatus(asdcDistributionId);
			
			if(queryDistributionDbResponse == null) {
				throw new TenantIsolationException("DistributionId doesn't exist in the DB: " + asdcDistributionId);
			}
			
			String operationalEnvironmentId = queryDistributionDbResponse.getOperationalEnvId();
			this.operationalEnvironmentId = operationalEnvironmentId;
			String serviceModelVersionId = queryDistributionDbResponse.getServiceModelVersionId();

			// ServiceModel, Query for dbRequestId, recoveryAction, retryCountString
			queryServiceModelResponse =  activateServiceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId);
			String origRequestId = queryServiceModelResponse.getRequestId();		
			this.requestId = origRequestId;
			
			msoLogger.debug("Start of processing activation status.");
			processActivateASDCStatus(asdcDistributionId, distributionObject);
			msoLogger.debug("End of processing activation status.");
			
			// After EVERY status processed, need to query the status of all service modelId 
			//  to determine the OVERALL status if "COMPLETE" or "FAILURE":
			checkOrUpdateOverallStatus(origRequestId, operationalEnvironmentId);			

			msoLogger.debug("End of " + classMethodMessage);
			
		} catch (Exception ex) {
			errorMessage = "** OVERALL status of flow: " + methodName + ex.getMessage();
			msoLogger.debug(errorMessage);
			getRequestDb().updateInfraFailureCompletion(errorMessage, requestId, operationalEnvironmentId);

		}
		
	}
	
	/**
	 * The Method to process the Activation Status from ASDC
	 * @param asdcDistributionId - string
	 * @param Distribution - object    
	 * @return void - nothing 
	 */			
	public void processActivateASDCStatus(String asdcDistributionId, Distribution asdcStatus) throws TenantIsolationException { 
		
		String operationalEnvironmentId = queryDistributionDbResponse.getOperationalEnvId();
		String serviceModelVersionId = queryDistributionDbResponse.getServiceModelVersionId();		
		
		String origRequestId = queryServiceModelResponse.getRequestId();		
		String recoveryAction = queryServiceModelResponse.getRecoveryAction();
		int retryCount = queryServiceModelResponse.getRetryCount();
		String workloadContext  = queryServiceModelResponse.getWorkloadContext();

		// Validate/process status
		if (asdcStatus.getStatus().toString().equals(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString())) {
			// should update 1 row, update status to "DISTRIBUTION_COMPLETE_OK"
			activateDistributionDb.updateOperationalEnvDistributionStatus(asdcStatus.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId);
			// should update 1 row, update status and retryCount = 0 (ie, serviceModelVersionId is DONE!)
			activateServiceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, asdcStatus.getStatus().toString(), 0);
		
		} else {
			
			  // "DISTRIBUTION_COMPLETE_ERROR", Check if recoveryAction is "RETRY" 
			  if (recoveryAction.equals("RETRY") & retryCount > 0) {
					// RESEND / RETRY serviceModelVersionId to ASDC    
					JSONObject jsonResponse = null;
					String newDistributionId = "";
					try {
						jsonResponse = asdcClientHelper.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext);
						String statusCode = jsonResponse.get("statusCode").toString();
						if (statusCode.equals("202")) {
							newDistributionId = jsonResponse.get("distributionId").toString();
							
							// should insert 1 row, NEW distributionId for old serviceModelServiceId
							activateDistributionDb.insertOperationalEnvDistributionStatus(newDistributionId, operationalEnvironmentId, serviceModelVersionId, "SENT", origRequestId);  					
									
							// update retryCount (less 1) for the serviceModelServiceId
							retryCount = retryCount - 1;
							// should update 1 row, original insert
							activateServiceModelDb.updateOperationalEnvRetryCountStatusPerReqId(operationalEnvironmentId, serviceModelVersionId, asdcStatus.getStatus().toString(), retryCount, origRequestId);
				
							// should update 1 row, OLD distributionId set to status error (ie, old distributionId is DONE!).  
							activateDistributionDb.updateOperationalEnvDistributionStatus(DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId);
							
						} else {					
							errorMessage = " Failure calling ASDC: statusCode: " + statusCode + 
		                                                         "; messageId: " + jsonResponse.get("messageId") +
		                                                         "; message: " + jsonResponse.get("message"); 
							msoLogger.debug(errorMessage);
							throw new AsdcClientCallFailed(errorMessage);
							
						} 
						
					} catch (Exception ex) {
						errorMessage = " Encountered Exception in " + methodName + " Exception: " + ex.getMessage();
						msoLogger.debug(errorMessage);
						throw new TenantIsolationException(errorMessage);	
					}
					
		
			 } else { // either RETRY & Count = 0, or 'ABORT', or 'SKIP' 

				 	if (recoveryAction.equals("SKIP") || recoveryAction.equals("ABORT")) {
					 	String modifiedStatus = "";
				 		if (recoveryAction.equals("SKIP")) {  // considered SUCCESS
				 			modifiedStatus = DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString();
				 		} else { 
				 			if (recoveryAction.equals("ABORT")) {
				 				modifiedStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();  // ABORT, error
				 			}
				 		}	
				 		// should update 1 row, modified status & retryCount set 0
				 		activateServiceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, modifiedStatus, 0);
				 		// should update 1 row, modified status
				 		activateDistributionDb.updateOperationalEnvDistributionStatus(modifiedStatus, asdcDistributionId, operationalEnvironmentId, serviceModelVersionId);
				 		
			 		} else {
			 			// RETRY & Count = 0 (do nothing!)
			 		}	
			  }		
		
		} 

	}
	
	/**
	 * The Method to check the overall status of the Activation for an operationalEnvironmentId
	 * @param origRequestId - string
	 * @param operationalEnvironmentId - string   
	 * @return void - nothing 
	 * @throws Exception 
	 */		
	public void checkOrUpdateOverallStatus(String origRequestId, String operationalEnvironmentId) throws Exception {
		
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = activateServiceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, origRequestId);
		msoLogger.debug(" **** queryServiceModelResponseList.size(): " + queryServiceModelResponseList.size());
		
		String status = "Waiting";
		int count = 0;
		// loop through the statuses of the service model
		for (OperationalEnvServiceModelStatus  queryServiceModelResponse : queryServiceModelResponseList) {
				status = queryServiceModelResponse.getServiceModelVersionDistrStatus();
				// all should be OK to be completed.
				if ((status.equals(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString()) &&
					(queryServiceModelResponse.getRetryCount() == 0))) {
					status = "Completed";
					count ++;					
				} 
				// one error with zero retry, means all are failures.
				if ((status.equals(DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString()) &&
					(queryServiceModelResponse.getRetryCount() == 0))) {
					status = "Failure";
					count = queryServiceModelResponseList.size();
					break;
				} 
				
		}
		
		//  "DISTRIBUTION_COMPLETE_OK"    : Completed / Successful
		if (status == "Completed" && queryServiceModelResponseList.size() == count) {
			executeAAIPatch(operationalEnvironmentId);
			String messageStatus = "Overall Activation process is complete. " + status;
			successIndicator = true;
			msoLogger.debug(messageStatus);
			//	Update DB to COMPLETION
			getRequestDb().updateInfraSuccessCompletion(messageStatus, origRequestId, operationalEnvironmentId);
		} else {	
			//  "DISTRIBUTION_COMPLETE_ERROR"  : Failure
			if (status == "Failure" && queryServiceModelResponseList.size() == count) {
				errorMessage = "Overall Activation process is a Failure. " + status;
				msoLogger.debug(errorMessage);
				getRequestDb().updateInfraFailureCompletion(errorMessage, requestId, operationalEnvironmentId);
			} else {	
			  msoLogger.debug(" **** Still waiting for more distribution status!"); // 1+ rows
			} 
		}	

	}	
	
	private void executeAAIPatch(String operationalEnvironmentId) throws Exception {
		msoLogger.debug("Start of AA&I UPDATE client call in ActivateVnfStatusOperationalEnvironment");
		
		Map<String, String> payload = new HashMap<>();
		payload.put("operational-environment-status", "ACTIVE");
		getAaiHelper().updateAaiOperationalEnvironment(operationalEnvironmentId, payload);
		
		msoLogger.debug("End of AA&I UPDATE client call in ActivateVnfStatusOperationalEnvironment");
	}
	
	/**
	 * Overall Success indicator 
	 * @return true or false
	 */	
	public boolean isSuccess() {
		return successIndicator;
	}
	
	/**
	 * Set to new OperationalEnvDistributionStatusDb 
	 * @return void
	 */	
	public void setOperationalEnvDistributionStatusDb (OperationalEnvDistributionStatusDb activateDistributionDb) {
		this.activateDistributionDb = activateDistributionDb;
	}
	
	/**
	 * Set to new OperationalEnvServiceModelStatusDb 
	 * @return void
	 */	
	public void setOperationalEnvServiceModelStatusDb (OperationalEnvServiceModelStatusDb activateServiceModelDb) {
		this.activateServiceModelDb = activateServiceModelDb;
	}

	
	/**
	 * Set to new AsdcClientHelper 
	 * @return void
	 */	
	public void setAsdcClientHelper (AsdcClientHelper asdcClientHelper) {
		this.asdcClientHelper = asdcClientHelper;
	}		
	
	/**
	 * get OperationalEnvDistributionStatusDb instance 
	 */	
	public OperationalEnvDistributionStatusDb getOperationalEnvDistributionStatusDb() {
		if(this.activateDistributionDb == null) {
			this.activateDistributionDb = OperationalEnvDistributionStatusDb.getInstance();
		}
		return this.activateDistributionDb;
	}	
	
	/**
	 * get OperationalEnvServiceModelStatusDb instance 
	 */	
	public OperationalEnvServiceModelStatusDb getOperationalEnvServiceModelStatusDb() {
		if(this.activateServiceModelDb == null) {
			this.activateServiceModelDb = OperationalEnvServiceModelStatusDb.getInstance();
		}
		return this.activateServiceModelDb;
	}			
	
}
