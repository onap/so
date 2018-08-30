/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.requestsdb;

import java.sql.Timestamp;
import javax.jws.WebService;
import javax.transaction.Transactional;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatusId;
import org.onap.so.db.request.beans.SiteStatus;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.onap.so.db.request.data.repository.SiteStatusRepository;
import org.onap.so.logger.MsoLogger;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@WebService(serviceName = "RequestsDbAdapter", endpointInterface = "org.onap.so.adapters.requestsdb.MsoRequestsDbAdapter", targetNamespace = "http://org.onap.so/requestsdb")
@Component
@Primary
public class MsoRequestsDbAdapterImpl implements MsoRequestsDbAdapter {	

	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, MsoRequestsDbAdapterImpl.class);
	
	@Autowired
	private InfraActiveRequestsRepository infraActive;

	@Autowired
	private SiteStatusRepository siteRepo;

	@Autowired
	private OperationStatusRepository operationStatusRepository;

	@Autowired
	private ResourceOperationStatusRepository resourceOperationStatusRepository;

	@Transactional
	@Override
	public void updateInfraRequest(String requestId, String lastModifiedBy, String statusMessage, String responseBody,
			RequestStatusType requestStatus, String progress, String vnfOutputs, String serviceInstanceId,
			String networkId, String vnfId, String vfModuleId, String volumeGroupId, String serviceInstanceName,
			String configurationId, String configurationName, String vfModuleName) throws MsoRequestsDbException {
		try {
			InfraActiveRequests request = infraActive.findOneByRequestIdOrClientRequestId(requestId, requestId);
			if (request == null) {
				String error = "Entity not found. Unable to retrieve MSO Infra Requests DB for Request ID " + requestId;
				throw new MsoRequestsDbException(error);
			}
			if (statusMessage != null) {
				request.setStatusMessage(statusMessage);
			}
			if (responseBody != null) {
				request.setResponseBody(responseBody);
			}
			if (requestStatus != null) {
				request.setRequestStatus(requestStatus.toString());
			}
			if (progress != null) {
				setProgress(progress, request);
			}
			if (vnfOutputs != null) {
				request.setVnfOutputs(vnfOutputs);
			}
			if (serviceInstanceId != null) {
				request.setServiceInstanceId(serviceInstanceId);
			}
			if (networkId != null) {
				request.setNetworkId(networkId);
			}
			if (vnfId != null) {
				request.setVnfId(vnfId);
			}
			if (vfModuleId != null) {
				request.setVfModuleId(vfModuleId);
			}
			if (volumeGroupId != null) {
				request.setVolumeGroupId(volumeGroupId);
			}
			if (serviceInstanceName != null) {
				request.setServiceInstanceName(serviceInstanceName);
			}
			if (vfModuleName != null) {
				request.setVfModuleName(vfModuleName);
			}
			if (configurationId != null) {
				request.setConfigurationId(configurationId);
			}
			if (configurationName != null) {
				request.setConfigurationName(configurationName);
			}
			if (requestStatus == RequestStatusType.COMPLETE || requestStatus == RequestStatusType.FAILED) {
				Timestamp nowTimeStamp = new Timestamp(System.currentTimeMillis());
				request.setEndTime(nowTimeStamp);
			}
			request.setLastModifiedBy(lastModifiedBy);
			infraActive.save(request);
		} catch (Exception e) {
			String error = "Error retrieving MSO Infra Requests DB for Request ID " + requestId;
			logger.error(error, e);
			throw new MsoRequestsDbException(error, MsoLogger.ErrorCode.BusinessProcesssError, e);
		}
	}

	private void setProgress(String progress, InfraActiveRequests request) {
		try {
			request.setProgress(Long.parseLong(progress));
		} catch (NumberFormatException e) {
			logger.warnSimple("UpdateInfraRequest", "Invalid value sent for progress");
		}
	}

	@Override
	@Transactional
	public InfraActiveRequests getInfraRequest(String requestId) throws MsoRequestsDbException {		
		logger.debug("Call to MSO Infra RequestsDb adapter get method with request Id: " + requestId);
		InfraActiveRequests request = null;
		try {
			request = infraActive.findOneByRequestIdOrClientRequestId(requestId, requestId);
			if (request == null) {
				String error = "Entity not found. Unable to retrieve MSO Infra Requests DB for Request ID " + requestId;
				throw new MsoRequestsDbException(error);
			}
		} catch (Exception e) {
			String error = "Error retrieving MSO Infra Requests DB for Request ID " + requestId;
			logger.error(error,e);
			throw new MsoRequestsDbException(error,MsoLogger.ErrorCode.BusinessProcesssError , e);
		}
		return request;
	}

	/**
	 * Get SiteStatus by SiteName.
	 *
	 * @param siteName
	 *            The unique name of the site
	 * @return Status of that site
	 */
	@Override
	@Transactional
	public boolean getSiteStatus(String siteName) {
		UUIDChecker.generateUUID(logger);
		SiteStatus siteStatus;
		logger.debug("Request database - get Site Status with Site name:" + siteName);
		siteStatus = siteRepo.findOneBySiteName(siteName);
		if (siteStatus == null) {
			// if not exist in DB, it means the site is not disabled, thus
			// return true
			return true;
		} else {			
			return siteStatus.getStatus();
		}
	}

	/**
	 * update operation status <br>
	 * 
	 * @param serviceId
	 * @param operationId
	 * @param operationType
	 * @param userId
	 * @param result
	 * @param operationContent
	 * @param progress
	 * @param reason
	 * @throws MsoRequestsDbException
	 * @since ONAP Amsterdam Release
	 */
	@Override
	@Transactional
	public void updateServiceOperationStatus(String serviceId, String operationId, String operationType, String userId,
			String result, String operationContent, String progress, String reason) throws MsoRequestsDbException {
		OperationStatus operStatus = operationStatusRepository.findOneByServiceIdAndOperationId(serviceId, operationId);
		if (operStatus == null) {
			String error = "Entity not found. Unable to retrieve OperationStatus Object ServiceId: " + serviceId + " operationId: "
					+ operationId;
			logger.error(error);
			throw new MsoRequestsDbException(error,MsoLogger.ErrorCode.BusinessProcesssError);
		}

		operStatus.setUserId(userId);
		operStatus.setOperation(operationType);
		operStatus.setReason(reason);
		operStatus.setProgress(progress);
		operStatus.setResult(result);
		operStatus.setOperationContent(operationContent);
		operStatus.setResult(result);
		operationStatusRepository.save(operStatus);
	}

	/**
	 * init the operation status of all the resources <br>
	 * 
	 * @param serviceId
	 *            the service Id
	 * @param operationId
	 *            the operation Id
	 * @param operationType
	 *            the operationType
	 * @param resourceTemplateUUIDs
	 *            the resources, the UUID is split by ":"
	 * @throws MsoRequestsDbException
	 * @since ONAP Amsterdam Release
	 */
	@Override
	@Transactional
	public void initResourceOperationStatus(String serviceId, String operationId, String operationType,
			String resourceTemplateUUIDs) throws MsoRequestsDbException {
		String[] resourceLst = resourceTemplateUUIDs.split(":");
		for (String resource : resourceLst) {
			if ("".equals(resource)) {
				continue;
			}
			ResourceOperationStatus resourceStatus = new ResourceOperationStatus();
			resourceStatus.setOperationId(operationId);
			resourceStatus.setServiceId(serviceId);
			resourceStatus.setResourceTemplateUUID(resource);
			resourceStatus.setOperType(operationType);
			resourceStatus.setStatus(RequestsDbConstant.Status.PROCESSING);
			resourceStatus.setStatusDescription("Waiting for start");
			resourceOperationStatusRepository.save(resourceStatus);

		}
	}

	/**
	 * get resource operation status <br>
	 * 
	 * @param serviceId
	 * @param operationId
	 * @param resourceTemplateUUID
	 * @return
	 * @throws MsoRequestsDbException
	 * @since ONAP Amsterdam Release
	 */
	@Override
	public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId,
			String resourceTemplateUUID) throws MsoRequestsDbException {

		return resourceOperationStatusRepository
				.findOne(new ResourceOperationStatusId(serviceId, operationId, resourceTemplateUUID));
	}

	/**
	 * update resource operation status <br>
	 * 
	 * @param serviceId
	 * @param operationId
	 * @param resourceTemplateUUID
	 * @param operationType
	 * @param resourceInstanceID
	 * @param jobId
	 * @param status
	 * @param progress
	 * @param errorCode
	 * @param statusDescription
	 * @throws MsoRequestsDbException
	 * @since ONAP Amsterdam Release
	 */
	@Override
	public void updateResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID,
			String operationType, String resourceInstanceID, String jobId, String status, String progress,
			String errorCode, String statusDescription) throws MsoRequestsDbException {
		ResourceOperationStatus resStatus = new ResourceOperationStatus();
		resStatus.setServiceId(serviceId);
		resStatus.setOperationId(operationId);
		resStatus.setResourceTemplateUUID(resourceTemplateUUID);
		resStatus.setOperType(operationType);
		resStatus.setResourceInstanceID(resourceInstanceID);
		resStatus.setJobId(jobId);
		resStatus.setStatus(status);
		resStatus.setProgress(progress);
		resStatus.setErrorCode(errorCode);
		resStatus.setStatusDescription(statusDescription);
		resourceOperationStatusRepository.save(resStatus);
	}
}