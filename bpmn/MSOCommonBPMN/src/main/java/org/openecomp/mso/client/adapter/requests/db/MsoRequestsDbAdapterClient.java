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

package org.openecomp.mso.client.adapter.requests.db;

import java.sql.Timestamp;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openecomp.mso.client.adapter.requests.db.entities.MsoRequestsDbException;
import org.openecomp.mso.client.adapter.requests.db.entities.RequestStatusType;
import org.openecomp.mso.client.adapter.requests.db.entities.UpdateInfraRequest;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.requestsdb.SiteStatus;
import org.openecomp.mso.utils.UUIDChecker;

public class MsoRequestsDbAdapterClient implements MsoRequestsDbAdapter {

	protected AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager();

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

	@Override
	public void updateInfraRequest(UpdateInfraRequest request) throws MsoRequestsDbException {
		Session session = requestsDbSessionFactoryManager.getSessionFactory().openSession();
		int result = 0;
		long startTime = System.currentTimeMillis();
		if (request.getRequestId() != null && request.getLastModifiedBy() != null) {
			MsoLogger.setLogContext(request.getRequestId(), null);
			try {
				session.beginTransaction();
				String queryString = "update InfraActiveRequests set ";
				String statusMessage = null;
				String responseBody = null;
				RequestStatusType requestStatus = null;
				String progress = null;
				String vnfOutputs = null;
				String serviceInstanceId = null;
				String networkId = null;
				String vnfId = null;
				String vfModuleId = null;
				String volumeGroupId = null;
				String serviceInstanceName = null;
				String vfModuleName = null;
				String configurationId = null;
				String configurationName = null;
				if (request.getStatusMessage() != null) {
					queryString += "statusMessage = :statusMessage, ";
					statusMessage = request.getStatusMessage();
				}
				if (request.getResponseBody() != null) {
					queryString += "responseBody = :responseBody, ";
					responseBody = request.getResponseBody();
				}
				if (request.getRequestStatus() != null) {
					queryString += "requestStatus = :requestStatus, ";
					requestStatus = request.getRequestStatus();
				}
				if (request.getProgress() != null) {
					queryString += "progress = :progress, ";
					progress = request.getProgress();
				}
				if (request.getVnfOutputs() != null) {
					queryString += "vnfOutputs = :vnfOutputs, ";
					vnfOutputs = request.getVnfOutputs();
				}
				if (request.getServiceInstanceId() != null) {
					queryString += "serviceInstanceId = :serviceInstanceId, ";
					serviceInstanceId = request.getServiceInstanceId();
				}
				if (request.getNetworkId() != null) {
					queryString += "networkId = :networkId, ";
					networkId = request.getNetworkId();
				}
				if (request.getVnfId() != null) {
					queryString += "vnfId = :vnfId, ";
					vnfId = request.getVnfId();
				}
				if (request.getVfModuleId() != null) {
					queryString += "vfModuleId = :vfModuleId, ";
					vfModuleId = request.getVfModuleId();
				}
				if (request.getVolumeGroupId() != null) {
					queryString += "volumeGroupId = :volumeGroupId, ";
					volumeGroupId = request.getVolumeGroupId();
				}
				if (request.getServiceInstanceName() != null) {
					queryString += "serviceInstanceName = :serviceInstanceName, ";
					serviceInstanceName = request.getServiceInstanceName();
				}
				if (request.getVfModuleName() != null) {
					queryString += "vfModuleName = :vfModuleName, ";
					vfModuleName = request.getVfModuleName();
				}
				if (request.getConfigurationId() != null) {
					queryString += "configurationId = :configurationId, ";
					configurationId = request.getConfigurationId();
				}
				if (request.getConfigurationName() != null) {
					queryString += "configurationName = :configurationName, ";
					configurationName = request.getConfigurationName();
				}
				if (request.getRequestStatus() == RequestStatusType.COMPLETE
						|| request.getRequestStatus() == RequestStatusType.FAILED) {
					queryString += "endTime = :endTime, ";
				} else {
					queryString += "modifyTime = :modifyTime, ";
				}
				queryString += "lastModifiedBy = :lastModifiedBy where requestId = :requestId OR clientRequestId = :requestId";

				LOGGER.debug("Executing update: " + queryString);

				Query query = session.createQuery(queryString);
				query.setParameter("requestId", request.getRequestId());
				if (statusMessage != null) {
					query.setParameter("statusMessage", statusMessage);
					LOGGER.debug("StatusMessage in updateInfraRequest is set to: " + statusMessage);
				}
				if (responseBody != null) {
					query.setParameter("responseBody", responseBody);
					LOGGER.debug("ResponseBody in updateInfraRequest is set to: " + responseBody);
				}
				if (requestStatus != null) {
					query.setParameter("requestStatus", requestStatus.toString());
					LOGGER.debug("RequestStatus in updateInfraRequest is set to: " + requestStatus.toString());
				}

				if (progress != null) {
					query.setParameter("progress", Long.parseLong(progress));
					LOGGER.debug("Progress in updateInfraRequest is set to: " + progress);
				}
				if (vnfOutputs != null) {
					query.setParameter("vnfOutputs", vnfOutputs);
					LOGGER.debug("VnfOutputs in updateInfraRequest is set to: " + vnfOutputs);
				}
				if (serviceInstanceId != null) {
					query.setParameter("serviceInstanceId", serviceInstanceId);
					LOGGER.debug("ServiceInstanceId in updateInfraRequest is set to: " + serviceInstanceId);
				}
				if (networkId != null) {
					query.setParameter("networkId", networkId);
					LOGGER.debug("NetworkId in updateInfraRequest is set to: " + networkId);
				}
				if (vnfId != null) {
					query.setParameter("vnfId", vnfId);
					LOGGER.debug("VnfId in updateInfraRequest is set to: " + vnfId);
				}
				if (vfModuleId != null) {
					query.setParameter("vfModuleId", vfModuleId);
					LOGGER.debug("vfModuleId in updateInfraRequest is set to: " + vfModuleId);
				}
				if (volumeGroupId != null) {
					query.setParameter("volumeGroupId", volumeGroupId);
					LOGGER.debug("VolumeGroupId in updateInfraRequest is set to: " + volumeGroupId);
				}
				if (serviceInstanceName != null) {
					query.setParameter("serviceInstanceName", serviceInstanceName);
					LOGGER.debug("ServiceInstanceName in updateInfraRequest is set to: " + serviceInstanceName);
				}
				if (configurationId != null) {
					query.setParameter("configurationId", configurationId);
					LOGGER.debug("configurationId in updateInfraRequest is set to: " + configurationId);
				}
				if (configurationName != null) {
					query.setParameter("configurationName", configurationName);
					LOGGER.debug("configurationName in updateInfraRequest is set to: " + configurationName);
				}
				if (vfModuleName != null) {
					query.setParameter("vfModuleName", vfModuleName);
					LOGGER.debug("vfModuleName in updateInfraRequest is set to: " + vfModuleName);
				}
				Timestamp nowTimeStamp = new Timestamp(System.currentTimeMillis());
				if (request.getRequestStatus() == RequestStatusType.COMPLETE
						|| request.getRequestStatus() == RequestStatusType.FAILED) {
					query.setParameter("endTime", nowTimeStamp);
					LOGGER.debug("EndTime in updateInfraRequest is set to: " + nowTimeStamp);
				} else {
					query.setParameter("modifyTime", nowTimeStamp);
					LOGGER.debug("ModifyTime in updateInfraRequest is set to: " + nowTimeStamp);
				}
				query.setParameter("lastModifiedBy", request.getLastModifiedBy());
				LOGGER.debug("LastModifiedBy in updateInfraRequest is set to: " + request.getLastModifiedBy());
				result = query.executeUpdate();
				checkIfExists(result, request.getRequestId(), startTime);
				session.getTransaction().commit();
			} catch (HibernateException e) {
				String error = "Unable to update MSO Requests DB: " + e.getMessage();
				LOGGER.error(MessageEnum.RA_CANT_UPDATE_REQUEST, "infra request parameters", request.getRequestId(), "",
						"", MsoLogger.ErrorCode.BusinessProcesssError, "HibernateException - " + error, e);
				LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
						error);
				throw new MsoRequestsDbException(error, e);
			} finally {
				if (session != null && session.isOpen()) {
					session.close();
				}
			}
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
		} else {
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest,
					"Required fields: requestId and lastModifiedBy");
		}
	}

	private void checkIfExists(int result, String requestId, long startTime) throws MsoRequestsDbException {
		if (result == 0) {
			String error = "Request ID does not exist in MSO Requests DB: " + requestId;
			LOGGER.error(MessageEnum.RA_DB_REQUEST_NOT_EXIST, requestId, "", "", MsoLogger.ErrorCode.DataError, error);
			throw new MsoRequestsDbException(error);
		}
	}

	@Override
	public InfraActiveRequests getInfraRequest(String requestId) throws MsoRequestsDbException {
		long startTime = System.currentTimeMillis();
		MsoLogger.setLogContext(requestId, null);
		Session session = requestsDbSessionFactoryManager.getSessionFactory().openSession();

		LOGGER.debug("Call to MSO Infra RequestsDb adapter get method with request Id: " + requestId);

		InfraActiveRequests request = null;
		try {
			session.beginTransaction();
			Query query = session.createQuery(
					"FROM InfraActiveRequests where requestId = :requestId OR clientRequestId = :requestId");
			query.setParameter("requestId", requestId);
			request = (InfraActiveRequests) query.uniqueResult();
		} catch (HibernateException e) {
			String error = "Unable to retrieve MSO Infra Requests DB for Request ID " + requestId;
			LOGGER.error(MessageEnum.RA_DB_REQUEST_NOT_EXIST, "Get Infra request", requestId, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError, "HibernateException - " + error, e);
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, error);
			throw new MsoRequestsDbException(error, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
		return request;
	}

	/**
	 * Get SiteStatus by SiteName.
	 *
	 * @param siteName
	 *            The unique name of the site
	 * @return Status of that site
	 */
	public boolean getSiteStatus(String siteName) {
		UUIDChecker.generateUUID(LOGGER);
		Session session = requestsDbSessionFactoryManager.getSessionFactory().openSession();

		long startTime = System.currentTimeMillis();
		SiteStatus siteStatus = null;
		LOGGER.debug("Request database - get Site Status with Site name:" + siteName);
		try {
			String hql = "FROM SiteStatus WHERE siteName = :site_name";
			Query query = session.createQuery(hql);
			query.setParameter("site_name", siteName);

			siteStatus = (SiteStatus) query.uniqueResult();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		if (siteStatus == null) {
			// if not exist in DB, it means the site is not disabled, thus
			// return true
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
			return true;
		} else {
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
			return siteStatus.getStatus();
		}
	}
}
