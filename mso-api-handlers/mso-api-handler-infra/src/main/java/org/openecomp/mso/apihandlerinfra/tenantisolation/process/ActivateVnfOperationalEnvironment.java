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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.exceptions.ValidateException;
import org.openecomp.mso.apihandlerinfra.logging.ErrorLoggerInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.ActivateVnfDBHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.SDCClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.db.request.beans.OperationalEnvDistributionStatus;
import org.openecomp.mso.db.request.beans.OperationalEnvServiceModelStatus;
import org.openecomp.mso.db.request.data.repository.OperationalEnvDistributionStatusRepository;
import org.openecomp.mso.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ActivateVnfOperationalEnvironment {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, ActivateVnfOperationalEnvironment.class);
	private static final int DEFAULT_ACTIVATE_RETRY_COUNT = 3;
	private static final String DISTRIBUTION_STATUS_SENT = "SENT";	
	
	@Autowired
	private ActivateVnfDBHelper dbHelper;	
	@Autowired
	private AAIClientHelper aaiHelper;
	@Autowired
	private RequestsDBHelper requestDb;
	@Autowired 
	private SDCClientHelper sdcClientHelper;
	
	@Value("${mso.tenant.isolation.retry.count}")
	private String sdcRetryCount;
	
	/**
	 * The Point-Of-Entry from APIH with VID request to send activate request
	 * @param requestId - String
	 * @param request - CloudOrchestrationRequest object
	 * @param distributionStatusRepository - OperationalEnvDistributionStatusRepository object
	 * @param modelStatusRepository - OperationalEnvServiceModelStatusRepository object
	 * @return void - nothing
	 */		
	public void execute(String requestId, CloudOrchestrationRequest request, OperationalEnvDistributionStatusRepository distributionStatusRepository, 
							OperationalEnvServiceModelStatusRepository modelStatusRepository) throws ApiException{
		String operationalEnvironmentId = request.getOperationalEnvironmentId();

		String vidWorkloadContext = request.getRequestDetails().getRequestParameters().getWorkloadContext();
		List<ServiceModelList> serviceModelVersionIdList = request.getRequestDetails().getRequestParameters().getManifest().getServiceModelList();
			

		AAIOperationalEnvironment operationalEnv = getAAIOperationalEnvironment(operationalEnvironmentId);
		String workloadContext = operationalEnv.getWorkloadContext();
		msoLogger.debug("  aai workloadContext: " + workloadContext);
		if (!vidWorkloadContext.equals(workloadContext)) {


			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.BusinessProcesssError).build();
			throw new ValidateException.Builder(" The vid workloadContext did not match from aai record. " + " vid workloadContext:" + vidWorkloadContext + " aai workloadContext:" + workloadContext,
					HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
		}

			processActivateSDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList, workloadContext, distributionStatusRepository, modelStatusRepository);

	}	
	
	
	/**
	 * The Method to send the Activation Requests to SDC
	 * @param requestId - String
	 * @param operationalEnvironmentId - String   
	 * @param serviceModelVersionIdList - List<ServiceModelList> list
	 * @param workloadContext - String	  
	 * @param distributionStatusRepository - OperationalEnvDistributionStatusRepository object
	 * @param modelStatusRepository - OperationalEnvServiceModelStatusRepository object
	 * @return jsonResponse - JSONObject object  
	 */		
	public void processActivateSDCRequest(String requestId, String operationalEnvironmentId, List<ServiceModelList> serviceModelVersionIdList, 
											String workloadContext, OperationalEnvDistributionStatusRepository distributionStatusRepository, 
											OperationalEnvServiceModelStatusRepository modelStatusRepository) throws ApiException {
		
		JSONObject jsonResponse = null;		
		int retryCount = 0;
		try {
			retryCount = Integer.parseInt(sdcRetryCount);
		} catch (NumberFormatException e) {
			retryCount = DEFAULT_ACTIVATE_RETRY_COUNT;
		}

		// loop through the serviceModelVersionId, and send request SDC
		for(ServiceModelList serviceModelList : serviceModelVersionIdList){
			String serviceModelVersionId = serviceModelList.getServiceModelVersionId();
			String recoveryAction = serviceModelList.getRecoveryAction().toString().toUpperCase();

			// should insert 1 row
			OperationalEnvServiceModelStatus serviceModelStatus = 
				dbHelper.insertRecordToOperationalEnvServiceModelStatus(requestId,
																	    operationalEnvironmentId,
																	    serviceModelVersionId,
																	    DISTRIBUTION_STATUS_SENT,
																	    recoveryAction, 
																	    retryCount,
				 													    workloadContext); 					
			modelStatusRepository.save(serviceModelStatus);
			
			String distributionId = "";

			jsonResponse = sdcClientHelper.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext);

			String statusCode = jsonResponse.get("statusCode").toString();
			if (statusCode.equals(String.valueOf(Response.Status.ACCEPTED.getStatusCode()))) {
				distributionId = jsonResponse.get("distributionId").toString();
				// should insert 1 row
				OperationalEnvDistributionStatus distStatus =
						dbHelper.insertRecordToOperationalEnvDistributionStatus(distributionId,
								operationalEnvironmentId,
								serviceModelVersionId,
								requestId,
								DISTRIBUTION_STATUS_SENT,
								"");
				distributionStatusRepository.save(distStatus);

			} else {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.BusinessProcesssError).build();
                String dbErrorMessage = " Failure calling SDC: statusCode: " + statusCode +
                        "; messageId: " + jsonResponse.get("messageId") +
                        "; message: " + jsonResponse.get("message");

                requestDb.updateInfraFailureCompletion(dbErrorMessage, requestId, operationalEnvironmentId);
				throw new ValidateException.Builder(dbErrorMessage,
						HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
			}
		}

	}

	/**
	 * Get AAIOperationalEnvironment object
	 * @param  operationalEnvironmentId - String 
	 * @return operationalEnv - AAIOperationalEnvironment object
	 */
	public AAIOperationalEnvironment getAAIOperationalEnvironment(String operationalEnvironmentId) {
		AAIResultWrapper aaiResult = aaiHelper.getAaiOperationalEnvironment(operationalEnvironmentId);
		return aaiResult.asBean(AAIOperationalEnvironment.class).orElse(new AAIOperationalEnvironment());		
	}

}
