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

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;
import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.AsdcClientCallFailed;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.TenantIsolationException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AsdcClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatus;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatusDb;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatus;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatusDb;
import org.openecomp.mso.requestsdb.RequestsDBHelper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


public class ActivateVnfOperationalEnvironment extends OperationalEnvironmentProcess {

	private static final String SERVICE_NAME = "ActivateVnfOperationalEnvironment"; 
	private AsdcClientHelper asdcClientHelper = null;
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	private String className = this.getClass().getSimpleName();
	private String methodName = "";
	private String classMethodMessage = "";
	private String errorMessage = "";
	
	private String operationalEnvironmentId = "";
	private int DEFAULT_ACTIVATE_RETRY_COUNT = 3;
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
	public ActivateVnfOperationalEnvironment(CloudOrchestrationRequest request, String requestId) {
		super(request, requestId);
		MsoLogger.setServiceName (getRequestId());
        MsoLogger.setLogContext(getRequestId(), getRequest().getOperationalEnvironmentId());		
		this.properties = MsoPropertiesUtils.loadMsoProperties();
		asdcClientHelper = new AsdcClientHelper(properties);
	}
	
	@Override
	protected String getServiceName() {
		return ActivateVnfOperationalEnvironment.SERVICE_NAME;
	}	
	
	/**
	 * The Point-Of-Entry from APIH with VID request to send activate request
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

			msoLogger.debug("Start of extracting variables from Input.");
			msoLogger.debug("  requestId: " + requestId);
			msoLogger.debug("  cloudOrchestrationRequest: " + request.toString());
			String operationalEnvironmentId = request.getOperationalEnvironmentId();
			this.operationalEnvironmentId = operationalEnvironmentId;
			msoLogger.debug("  operationalEnvironmentId: " + this.operationalEnvironmentId);			
			String vidWorkloadContext = request.getRequestDetails().getRequestParameters().getWorkloadContext();
			List<ServiceModelList> serviceModelVersionIdList = request.getRequestDetails().getRequestParameters().getManifest().getServiceModelList();
			msoLogger.debug("  serviceModelVersionIdList size(): " + serviceModelVersionIdList.size());			
		    msoLogger.debug("End of extracting variables from Input.");
			
			msoLogger.debug("Start of getting AAIOperationalEnvironment Object.");
			AAIOperationalEnvironment operationalEnv = getAAIOperationalEnvironment(operationalEnvironmentId);
			String workloadContext = operationalEnv.getWorkloadContext();
			msoLogger.debug("  aai workloadContext: " + workloadContext);
			if (vidWorkloadContext.equals(workloadContext)) {
				msoLogger.debug("  vid workloadContext matched with aai record, continue!");
			} else {
				errorMessage = " The vid workloadContext did not match from aai record. " + " vid workloadContext:" + vidWorkloadContext + " aai workloadContext:" + workloadContext;
				msoLogger.debug(errorMessage);
				throw new TenantIsolationException(errorMessage);
			}
			msoLogger.debug("End of getting AAIOperationalEnvironment Object.");					

			msoLogger.debug("Start of sending activation request to ASDC.");
			processActivateASDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList, workloadContext);
			msoLogger.debug("End of sending activation request to ASDC.");
			
			msoLogger.debug("** OVERALL status of flow: Processed ALL " + serviceModelVersionIdList.size() + " activation requests are SUCCESSFUL!");
			successIndicator = true;
			msoLogger.debug("End of " + classMethodMessage);			
			
		} catch (Exception ex) {
			errorMessage = "** OVERALL status of flow: " + methodName + ex.getMessage();
			msoLogger.debug(errorMessage);
			getRequestDb().updateInfraFailureCompletion(errorMessage, requestId, operationalEnvironmentId);

		}	
		
	}	
	
	
	/**
	 * The Method to send the Activation Requests to ASDC
	 * @param requestId - string
	 * @param operationalEnvironmentId - string   
	 * @param List<ServiceModelList> serviceModelVersionIdList - list
	 * @param workloadContext - string	  
	 * @return void - nothing 
	 */		
	public void processActivateASDCRequest(String requestId, String operationalEnvironmentId, 
									    List<ServiceModelList> serviceModelVersionIdList, String workloadContext) throws TenantIsolationException, AsdcClientCallFailed {
		
		int retryCount = 0;
		String retryCountString = properties.getProperty("mso.tenant.isolation.retry.count", null);
		try {
			retryCount = Integer.parseInt(retryCountString);
			msoLogger.debug(" ** Used Properties File retryCount: " + retryCount);				
		} catch (NumberFormatException e) {
			retryCount = DEFAULT_ACTIVATE_RETRY_COUNT;
			msoLogger.debug(" ** Used Default retryCount: " + retryCount + " Exception: " + e.getMessage());			
		}			

		msoLogger.debug(" ** serviceModelVersionIdList: " + serviceModelVersionIdList.size());
		
		// loop through the serviceModelVersionId, and send request ASDC
		for(ServiceModelList serviceModelList : serviceModelVersionIdList){
			String serviceModelVersionId = serviceModelList.getServiceModelVersionId();
			String recoveryAction = serviceModelList.getRecoveryAction().toString().toUpperCase();
			msoLogger.debug(" ** serviceModelVersionId: " + serviceModelVersionId + "; recoveryAction: " + recoveryAction);
			// should insert 1 row
			activateServiceModelDb.insertOperationalEnvServiceModelStatus(requestId, operationalEnvironmentId, serviceModelVersionId, "SENT", recoveryAction, retryCount, workloadContext);  
			
			JSONObject jsonResponse = null;
			String distributionId = "";
			try {
				jsonResponse = asdcClientHelper.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext);
				msoLogger.debug("  JSONObject jsonResponse:" + jsonResponse.toString());	
				String statusCode = jsonResponse.get("statusCode").toString();
				if (statusCode.equals("202")) {
					distributionId = jsonResponse.get("distributionId").toString();
					
					// should insert 1 row
					activateDistributionDb.insertOperationalEnvDistributionStatus(distributionId, operationalEnvironmentId, serviceModelVersionId, "SENT", requestId);
					
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
			
		}
			
	}

	/**
	 * Get AAIOperationalEnvironment object
	 * @param  String operationalEnvironmentId
	 * @return object AAIOperationalEnvironment
	 */
	public AAIOperationalEnvironment getAAIOperationalEnvironment(String operationalEnvironmentId) {
		
		AAIOperationalEnvironment operationalEnv = null;
		getAaiHelper();		
		
		try {
			AAIResultWrapper aaiResult = aaiHelper.getAaiOperationalEnvironment(operationalEnvironmentId);
			operationalEnv = aaiResult.asBean(AAIOperationalEnvironment.class).get();
		} catch (JsonParseException e) {
			msoLogger.debug(" **** JsonParseException: " + e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			msoLogger.debug(" **** JsonMappingException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			msoLogger.debug(" **** IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			msoLogger.debug(" **** Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		return operationalEnv;
		
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
