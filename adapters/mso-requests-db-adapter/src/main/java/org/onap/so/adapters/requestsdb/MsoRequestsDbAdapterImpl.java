/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * Modifications Copyright (c) 2019 Samsung
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
import java.util.List;
import jakarta.jws.WebService;
import jakarta.transaction.Transactional;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.ResourceOperationStatusId;
import org.onap.so.db.request.beans.SiteStatus;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.onap.so.db.request.data.repository.SiteStatusRepository;
import org.onap.so.db.request.data.repository.InstanceNfvoMappingRepository;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Deprecated
@WebService(serviceName = "RequestsDbAdapter",
        endpointInterface = "org.onap.so.adapters.requestsdb.MsoRequestsDbAdapter",
        targetNamespace = "http://org.onap.so/requestsdb")
@Component
@Primary
public class MsoRequestsDbAdapterImpl implements MsoRequestsDbAdapter {

    private static Logger logger = LoggerFactory.getLogger(MsoRequestsDbAdapterImpl.class);

    @Autowired
    private InfraActiveRequestsRepository infraActive;

    @Autowired
    private InstanceNfvoMappingRepository instanceNfvoMappingRepository;

    @Autowired
    private SiteStatusRepository siteRepo;

    @Autowired
    private OperationStatusRepository operationStatusRepository;

    @Autowired
    private ResourceOperationStatusRepository resourceOperationStatusRepository;

    @Transactional
    @Override
    public void setInstanceNfvoMappingRepository(String instanceId, String nfvoName, String endpoint, String username,
            String password, String apiRoot) {
        InstanceNfvoMapping instanceNfvoMapping = new InstanceNfvoMapping();
        if (apiRoot != null) {
            instanceNfvoMapping.setApiRoot(apiRoot);
        }
        if (endpoint != null) {
            instanceNfvoMapping.setEndpoint(endpoint);
        }
        if (instanceId != null) {
            instanceNfvoMapping.setInstanceId(instanceId);
        }
        if (nfvoName != null) {
            instanceNfvoMapping.setNfvoName(nfvoName);
        }
        if (username != null) {
            instanceNfvoMapping.setUsername(username);
        }
        if (password != null) {
            instanceNfvoMapping.setPassword(password);
        }

        instanceNfvoMappingRepository.save(instanceNfvoMapping);
    }

    @Transactional
    @Override
    public InstanceNfvoMapping getInstanceNfvoMapping(String instanceId) {
        InstanceNfvoMapping instanceNfvoMapping = instanceNfvoMappingRepository.findOneByInstanceId(instanceId);
        return instanceNfvoMapping;
    }

    @Transactional
    @Override
    public void updateInfraRequest(String requestId, String lastModifiedBy, String statusMessage, String responseBody,
            RequestStatusType requestStatus, String progress, String vnfOutputs, String serviceInstanceId,
            String networkId, String vnfId, String vfModuleId, String volumeGroupId, String serviceInstanceName,
            String configurationId, String configurationName, String vfModuleName) throws MsoRequestsDbException {
        try {
            InfraActiveRequests request = infraActive.findOneByRequestId(requestId);
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
            throw new MsoRequestsDbException(error, ErrorCode.BusinessProcessError, e);
        }
    }

    private void setProgress(String progress, InfraActiveRequests request) {
        try {
            request.setProgress(Long.parseLong(progress));
        } catch (NumberFormatException e) {
            logger.warn("UpdateInfraRequest", "Invalid value sent for progress");
        }
    }

    @Override
    @Transactional
    public InfraActiveRequests getInfraRequest(String requestId) throws MsoRequestsDbException {
        logger.debug("Call to MSO Infra RequestsDb adapter get method with request Id: {}", requestId);
        InfraActiveRequests request = null;
        try {
            request = infraActive.findOneByRequestId(requestId);
            if (request == null) {
                String error = "Entity not found. Unable to retrieve MSO Infra Requests DB for Request ID " + requestId;
                throw new MsoRequestsDbException(error);
            }
        } catch (Exception e) {
            String error = "Error retrieving MSO Infra Requests DB for Request ID " + requestId;
            logger.error(error, e);
            throw new MsoRequestsDbException(error, ErrorCode.BusinessProcessError, e);
        }
        return request;
    }

    /**
     * Get SiteStatus by SiteName.
     *
     * @param siteName The unique name of the site
     * @return Status of that site
     */
    @Override
    @Transactional
    public boolean getSiteStatus(String siteName) {
        SiteStatus siteStatus;
        logger.debug("Request database - get Site Status with Site name: {}", siteName);
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
     * get the operation status
     *
     * @param serviceId
     * @param operationId
     * @return operationStatus
     * @throws MsoRequestsDbException
     */
    @Override
    @Transactional
    public OperationStatus getServiceOperationStatus(String serviceId, String operationId)
            throws MsoRequestsDbException {
        OperationStatus operationStatus;
        if (operationId.isEmpty()) {
            operationStatus = operationStatusRepository.findOneByServiceId(serviceId);
        } else {
            operationStatus = operationStatusRepository.findOneByServiceIdAndOperationId(serviceId, operationId);
        }
        return operationStatus;
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
            String error = "Entity not found. Unable to retrieve OperationStatus Object ServiceId: " + serviceId
                    + " operationId: " + operationId;
            logger.error(error);
            operStatus = new OperationStatus();
            operStatus.setOperationId(operationId);
            operStatus.setServiceId(serviceId);
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
     * Init operation status <br>
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
     * @since ONAP Casablanca Release
     */
    @Override
    @Transactional
    public void initServiceOperationStatus(String serviceId, String operationId, String operationType, String userId,
            String result, String operationContent, String progress, String reason) throws MsoRequestsDbException {
        OperationStatus operStatus = new OperationStatus();

        operStatus.setOperationId(operationId);
        operStatus.setServiceId(serviceId);
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
     * @param serviceId the service Id
     * @param operationId the operation Id
     * @param operationType the operationType
     * @param resourceTemplateUUIDs the resources, the UUID is split by ":"
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
                .findById(new ResourceOperationStatusId(serviceId, operationId, resourceTemplateUUID))
                .orElseThrow(() -> new MsoRequestsDbException("Operation not found:" + operationId));

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

        updateOperationStatusBasedOnResourceStatus(resStatus);
    }

    /**
     * update service operation status when a operation resource status updated <br>
     *
     * @param operStatus the resource operation status
     * @since ONAP Amsterdam Release
     */
    private void updateOperationStatusBasedOnResourceStatus(ResourceOperationStatus operStatus) {
        String serviceId = operStatus.getServiceId();
        String operationId = operStatus.getOperationId();

        logger.debug("Request database - update Operation Status Based On Resource Operation Status with service Id: "
                + "{}, operationId: {}", serviceId, operationId);

        List<ResourceOperationStatus> lstResourceStatus =
                resourceOperationStatusRepository.findByServiceIdAndOperationId(serviceId, operationId);
        if (lstResourceStatus == null) {
            logger.error("Unable to retrieve resourceOperStatus Object by ServiceId: {} operationId: {}", serviceId,
                    operationId);
            return;
        }

        // count the total progress
        int resourceCount = lstResourceStatus.size();
        int progress = 0;
        boolean isFinished = true;
        for (ResourceOperationStatus lstResourceStatu : lstResourceStatus) {
            progress = progress + Integer.valueOf(lstResourceStatu.getProgress()) / resourceCount;
            if (RequestsDbConstant.Status.PROCESSING.equals(lstResourceStatu.getStatus())) {
                isFinished = false;
            }
        }

        OperationStatus serviceOperStatus =
                operationStatusRepository.findOneByServiceIdAndOperationId(serviceId, operationId);
        if (serviceOperStatus == null) {
            String error = "Entity not found. Unable to retrieve OperationStatus Object ServiceId: " + serviceId
                    + " operationId: " + operationId;
            logger.error(error);

            serviceOperStatus = new OperationStatus();
            serviceOperStatus.setOperationId(operationId);
            serviceOperStatus.setServiceId(serviceId);
        }

        progress = progress > 100 ? 100 : progress;
        serviceOperStatus.setProgress(String.valueOf(progress));
        serviceOperStatus.setOperationContent(operStatus.getStatusDescription());
        // if current resource failed. service failed.
        if (RequestsDbConstant.Status.ERROR.equals(operStatus.getStatus())) {
            serviceOperStatus.setResult(RequestsDbConstant.Status.ERROR);
            serviceOperStatus.setReason(operStatus.getStatusDescription());
        } else if (isFinished) {
            // if finished
            serviceOperStatus.setResult(RequestsDbConstant.Status.FINISHED);
            serviceOperStatus.setProgress(RequestsDbConstant.Progress.ONE_HUNDRED);
        }

        operationStatusRepository.save(serviceOperStatus);
    }
}
