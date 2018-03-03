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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openecomp.mso.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.OperationStatus;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;
import org.openecomp.mso.requestsdb.SiteStatus;
import org.openecomp.mso.utils.UUIDChecker;

@WebService(serviceName = "RequestsDbAdapter", endpointInterface = "org.openecomp.mso.adapters.requestsdb.MsoRequestsDbAdapter", targetNamespace = "http://org.openecomp.mso/requestsdb")
public class MsoRequestsDbAdapterImpl implements MsoRequestsDbAdapter {

    protected AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager ();
    
    private static MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    @Override
    public void updateInfraRequest (String requestId,
                                    String lastModifiedBy,
                                    String statusMessage,
                                    String responseBody,
                                    RequestStatusType requestStatus,
                                    String progress,
                                    String vnfOutputs,
                                    String serviceInstanceId,
                                    String networkId,
                                    String vnfId,
                                    String vfModuleId,
                                    String volumeGroupId,
                                    String serviceInstanceName,
                                    String vfModuleName) throws MsoRequestsDbException {
        MsoLogger.setLogContext (requestId, null);
        Session session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();
        int result = 0;
        long startTime = System.currentTimeMillis ();
        try {
           	session.beginTransaction ();
            String queryString = "update InfraActiveRequests set ";
            if (statusMessage != null) {
                queryString += "statusMessage = :statusMessage, ";
            }
            if (responseBody != null) {
                queryString += "responseBody = :responseBody, ";
            }
            if (requestStatus != null) {
                queryString += "requestStatus = :requestStatus, ";
            }
            if (progress != null) {
                queryString += "progress = :progress, ";
            }
            if (vnfOutputs != null) {
                queryString += "vnfOutputs = :vnfOutputs, ";
            }
            if (serviceInstanceId != null) {
                queryString += "serviceInstanceId = :serviceInstanceId, ";
            }
            if (networkId != null) {
                queryString += "networkId = :networkId, ";
            }
            if (vnfId != null) {
                queryString += "vnfId = :vnfId, ";
            }
            if (vfModuleId != null) {
                queryString += "vfModuleId = :vfModuleId, ";
            }
            if (volumeGroupId != null) {
                queryString += "volumeGroupId = :volumeGroupId, ";
            }
            if (serviceInstanceName != null) {
                queryString += "serviceInstanceName = :serviceInstanceName, ";
            }
            if (vfModuleName != null) {
                queryString += "vfModuleName = :vfModuleName, ";
            }
            if (requestStatus == RequestStatusType.COMPLETE || requestStatus == RequestStatusType.FAILED) {
                queryString += "endTime = :endTime, ";
            } else {
                queryString += "modifyTime = :modifyTime, ";
            }
            queryString += "lastModifiedBy = :lastModifiedBy where requestId = :requestId OR clientRequestId = :requestId";

            logger.debug("Executing update: " + queryString);

            Query query = session.createQuery (queryString);
            query.setParameter ("requestId", requestId);
            if (statusMessage != null) {
                query.setParameter ("statusMessage", statusMessage);
                logger.debug ("StatusMessage in updateInfraRequest is set to: " + statusMessage);
            }
            if (responseBody != null) {
            	query.setParameter ("responseBody", responseBody);
                logger.debug ("ResponseBody in updateInfraRequest is set to: " + responseBody);
            }
            if (requestStatus != null) {
                query.setParameter ("requestStatus", requestStatus.toString ());
                logger.debug ("RequestStatus in updateInfraRequest is set to: " + requestStatus.toString());
            }

            if (progress != null) {
                query.setParameter ("progress", Long.parseLong (progress));
                logger.debug ("Progress in updateInfraRequest is set to: " + progress);
            }
            if (vnfOutputs != null) {
                query.setParameter ("vnfOutputs", vnfOutputs);
                logger.debug ("VnfOutputs in updateInfraRequest is set to: " + vnfOutputs);
            }
            if (serviceInstanceId != null) {
                query.setParameter ("serviceInstanceId", serviceInstanceId);
                logger.debug ("ServiceInstanceId in updateInfraRequest is set to: " + serviceInstanceId);
            }
            if (networkId != null) {
                query.setParameter ("networkId", networkId);
                logger.debug ("NetworkId in updateInfraRequest is set to: " + networkId);
            }
            if (vnfId != null) {
                query.setParameter ("vnfId", vnfId);
                logger.debug ("VnfId in updateInfraRequest is set to: " + vnfId);
            }
            if (vfModuleId != null) {
                query.setParameter ("vfModuleId", vfModuleId);
                logger.debug ("vfModuleId in updateInfraRequest is set to: " + vfModuleId);
            }
            if (volumeGroupId != null) {
                query.setParameter ("volumeGroupId", volumeGroupId);
                logger.debug ("VolumeGroupId in updateInfraRequest is set to: " + volumeGroupId);
            }
            if (serviceInstanceName != null) {
                query.setParameter ("serviceInstanceName", serviceInstanceName);
                logger.debug ("ServiceInstanceName in updateInfraRequest is set to: " + serviceInstanceName);
            }
            if (vfModuleName != null) {
                query.setParameter ("vfModuleName", vfModuleName);
                logger.debug ("vfModuleName in updateInfraRequest is set to: " + vfModuleName);
            }
            Timestamp nowTimeStamp = new Timestamp (System.currentTimeMillis ());
            if (requestStatus == RequestStatusType.COMPLETE || requestStatus == RequestStatusType.FAILED) {
                query.setParameter ("endTime", nowTimeStamp);
                logger.debug ("EndTime in updateInfraRequest is set to: " + nowTimeStamp);
            } else {
                query.setParameter ("modifyTime", nowTimeStamp);
                logger.debug ("ModifyTime in updateInfraRequest is set to: " + nowTimeStamp);
            }
            query.setParameter ("lastModifiedBy", lastModifiedBy);
            logger.debug ("LastModifiedBy in updateInfraRequest is set to: " + lastModifiedBy);
            result = query.executeUpdate ();
            checkIfExists (result, requestId);
            session.getTransaction ().commit ();
        } catch (HibernateException e) {
            String error = "Unable to update MSO Requests DB: " + e.getMessage ();
            logger.error (MessageEnum.RA_CANT_UPDATE_REQUEST, "infra request parameters", requestId, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "HibernateException - " + error, e);
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, error);
            throw new MsoRequestsDbException (error, e);
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
        }
        logger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
    }


    private void checkIfExists (int result, String requestId) throws MsoRequestsDbException {
        if (result == 0) {
            String error = "Request ID does not exist in MSO Requests DB: " + requestId;
            logger.error (MessageEnum.RA_DB_REQUEST_NOT_EXIST, requestId, "", "", MsoLogger.ErrorCode.DataError, error);
            throw new MsoRequestsDbException (error);
        }
    }


    @Override
    public InfraActiveRequests getInfraRequest (String requestId) throws MsoRequestsDbException {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setLogContext (requestId, null);
        Session session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();

        logger.debug ("Call to MSO Infra RequestsDb adapter get method with request Id: " + requestId);

        InfraActiveRequests request = null;
        try {
            session.beginTransaction ();
            Query query = session.createQuery ("FROM InfraActiveRequests where requestId = :requestId OR clientRequestId = :requestId");
            query.setParameter ("requestId", requestId);
            request = (InfraActiveRequests) query.uniqueResult();
        } catch (HibernateException e) {
            String error = "Unable to retrieve MSO Infra Requests DB for Request ID "
                           + requestId;
            logger.error (MessageEnum.RA_DB_REQUEST_NOT_EXIST, "Get Infra request", requestId, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "HibernateException - " + error, e);
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, error);
            throw new MsoRequestsDbException (error, e);
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
        }
        logger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        return request;
    }


    /**
     * Get SiteStatus by SiteName.
     *
     * @param siteName The unique name of the site
     * @return Status of that site
     */
    @Override
    public boolean getSiteStatus (String siteName) {
        UUIDChecker.generateUUID (logger);
        Session session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();

        long startTime = System.currentTimeMillis ();
        SiteStatus siteStatus = null;
        logger.debug ("Request database - get Site Status with Site name:" + siteName);
        try {
            String hql = "FROM SiteStatus WHERE siteName = :site_name";
            Query query = session.createQuery (hql);
            query.setParameter ("site_name", siteName);

            siteStatus = (SiteStatus) query.uniqueResult ();
        } finally {
            if (session != null && session.isOpen ()) {
                session.close ();
            }
        }
        if (siteStatus == null) {
            // if not exist in DB, it means the site is not disabled, thus return true
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
            return true;
        } else {
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
            return siteStatus.getStatus();
        }
    }
    
    /**
     * update operation status
     * <br>
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
     * @since   ONAP Amsterdam Release
     */
    @Override
    public void updateServiceOperationStatus(String serviceId, String operationId, String serviceName,String operationType, String userId,
            String result, String operationContent, String progress, String reason) throws MsoRequestsDbException {
        OperationStatus operStatus = new OperationStatus();
        operStatus.setResult(RequestsDbConstant.Status.PROCESSING);
        operStatus.setServiceId(serviceId);
        operStatus.setOperationId(operationId);
        operStatus.setServiceName(serviceName);
        operStatus.setUserId(userId);
        operStatus.setOperation(operationType);
        operStatus.setReason(reason);
        operStatus.setProgress(progress);
        operStatus.setOperationContent(operationContent);
        RequestsDatabase.getInstance().updateOperationStatus(operStatus);
    }
    
    /**
     * init the operation status of  all the resources 
     * <br>
     * 
     * @param serviceId the service Id
     * @param operationId the operation Id
     * @param operationType the operationType
     * @param resourceTemplateUUIDs the resources, the UUID is split by ":"
     * @throws MsoRequestsDbException
     * @since   ONAP Amsterdam Release
     */
    @Override
    public void initResourceOperationStatus(String serviceId, String operationId, String operationType,
            String resourceTemplateUUIDs) throws MsoRequestsDbException{
        String[] resourceLst = resourceTemplateUUIDs.split(":");
        for(String resource: resourceLst){
            if("".equals(resource)){
                continue;
            }
            ResourceOperationStatus resourceStatus = new ResourceOperationStatus();
            resourceStatus.setOperationId(operationId);
            resourceStatus.setServiceId(serviceId);
            resourceStatus.setResourceTemplateUUID(resource);
            resourceStatus.setOperType(operationType);
            resourceStatus.setStatus(RequestsDbConstant.Status.PROCESSING);
            resourceStatus.setStatusDescription("Waiting for start");
            RequestsDatabase.getInstance().updateResOperStatus(resourceStatus);
        }     
    }
    
    /**
     * get resource operation status
     * <br>
     * 
     * @param serviceId
     * @param operationId
     * @param resourceTemplateUUID
     * @return
     * @throws MsoRequestsDbException
     * @since   ONAP Amsterdam Release
     */
    @Override
    public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID)
            throws MsoRequestsDbException {
        return RequestsDatabase.getInstance().getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
    }
    
    /**
     * update resource operation status
     * <br>
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
     * @since   ONAP Amsterdam Release
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
         RequestsDatabase.getInstance().updateResOperStatus(resStatus);
    }
}
