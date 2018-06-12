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

package org.openecomp.mso.adapters.requestsdb;

import java.sql.Timestamp;

import javax.jws.WebService;
import javax.transaction.Transactional;

import org.openecomp.mso.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.beans.OperationStatus;
import org.openecomp.mso.db.request.beans.ResourceOperationStatus;
import org.openecomp.mso.db.request.beans.ResourceOperationStatusId;
import org.openecomp.mso.db.request.beans.SiteStatus;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.openecomp.mso.db.request.data.repository.OperationStatusRepository;
import org.openecomp.mso.db.request.data.repository.ResourceOperationStatusRepository;
import org.openecomp.mso.db.request.data.repository.SiteStatusRepository;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@WebService(serviceName = "RequestsDbAdapter", endpointInterface = "org.openecomp.mso.adapters.requestsdb.MsoRequestsDbAdapter", targetNamespace = "http://org.openecomp.mso/requestsdb")
@Component
@Primary
public class MsoRequestsDbAdapterImpl implements MsoRequestsDbAdapter {

	private static final String SUCCESSFUL = "Successful";

	private static final String GET_INFRA_REQUEST = "Get Infra request";

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
		MsoLogger.setLogContext(requestId, serviceInstanceId);
		long startTime = System.currentTimeMillis();
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
			logger.error("Error " + MsoLogger.ErrorCode.BusinessProcesssError + " for " + GET_INFRA_REQUEST + " - " + MessageEnum.RA_DB_REQUEST_NOT_EXIST + " - " + error, e);
			logger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, error);
			throw new MsoRequestsDbException(error, e);
		}
		logger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, SUCCESSFUL);

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
		long startTime = System.currentTimeMillis();
		MsoLogger.setLogContext(requestId, null);

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
			logger.error("Error " + MsoLogger.ErrorCode.BusinessProcesssError + " for " + GET_INFRA_REQUEST + " - " + MessageEnum.RA_DB_REQUEST_NOT_EXIST + " - " + error, e);
			logger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, error);
			throw new MsoRequestsDbException(error, e);
		}
		logger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, SUCCESSFUL);
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
		long startTime = System.currentTimeMillis();
		SiteStatus siteStatus;
		logger.debug("Request database - get Site Status with Site name:" + siteName);

		siteStatus = siteRepo.findOneBySiteName(siteName);
		if (siteStatus == null) {
			// if not exist in DB, it means the site is not disabled, thus
			// return true
			logger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, SUCCESSFUL);
			return true;
		} else {
			logger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, SUCCESSFUL);
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
			MsoRequestsDbException e = new MsoRequestsDbException(error);
			logger.error("Error "+ MsoLogger.ErrorCode.BusinessProcesssError + " - " + MessageEnum.RA_DB_REQUEST_NOT_EXIST + " - " + error, e);
			throw e;
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