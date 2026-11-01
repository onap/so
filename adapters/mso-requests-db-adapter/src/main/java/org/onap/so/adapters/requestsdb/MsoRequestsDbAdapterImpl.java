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

import jakarta.jws.WebService;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
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

    @Autowired
    private RequestDbOperationsService operations;

    @Override
    public void setInstanceNfvoMappingRepository(String instanceId, String nfvoName, String endpoint, String username,
            String password, String apiRoot) {
        operations.setInstanceNfvoMappingRepository(instanceId, nfvoName, endpoint, username, password, apiRoot);
    }

    @Override
    public InstanceNfvoMapping getInstanceNfvoMapping(String instanceId) {
        return operations.getInstanceNfvoMapping(instanceId);
    }

    @Override
    public void updateInfraRequest(String requestId, String lastModifiedBy, String statusMessage, String responseBody,
            RequestStatusType requestStatus, String progress, String vnfOutputs, String serviceInstanceId,
            String networkId, String vnfId, String vfModuleId, String volumeGroupId, String serviceInstanceName,
            String configurationId, String configurationName, String vfModuleName) throws MsoRequestsDbException {
        operations.updateInfraRequest(requestId, lastModifiedBy, statusMessage, responseBody, requestStatus, progress,
                vnfOutputs, serviceInstanceId, networkId, vnfId, vfModuleId, volumeGroupId, serviceInstanceName,
                configurationId, configurationName, vfModuleName);
    }

    @Override
    public InfraActiveRequests getInfraRequest(String requestId) throws MsoRequestsDbException {
        return operations.getInfraRequest(requestId);
    }

    @Override
    public boolean getSiteStatus(String siteName) {
        return operations.getSiteStatus(siteName);
    }

    @Override
    public OperationStatus getServiceOperationStatus(String serviceId, String operationId)
            throws MsoRequestsDbException {
        return operations.getServiceOperationStatus(serviceId, operationId);
    }

    @Override
    public void updateServiceOperationStatus(String serviceId, String operationId, String operationType, String userId,
            String result, String operationContent, String progress, String reason) throws MsoRequestsDbException {
        operations.updateServiceOperationStatus(serviceId, operationId, operationType, userId, result, operationContent,
                progress, reason);
    }

    @Override
    public void initServiceOperationStatus(String serviceId, String operationId, String operationType, String userId,
            String result, String operationContent, String progress, String reason) throws MsoRequestsDbException {
        operations.initServiceOperationStatus(serviceId, operationId, operationType, userId, result, operationContent,
                progress, reason);
    }

    @Override
    public void initResourceOperationStatus(String serviceId, String operationId, String operationType,
            String resourceTemplateUUIDs) throws MsoRequestsDbException {
        operations.initResourceOperationStatus(serviceId, operationId, operationType, resourceTemplateUUIDs);
    }

    @Override
    public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId,
            String resourceTemplateUUID) throws MsoRequestsDbException {
        return operations.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
    }

    @Override
    public void updateResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID,
            String operationType, String resourceInstanceID, String jobId, String status, String progress,
            String errorCode, String statusDescription) throws MsoRequestsDbException {
        operations.updateResourceOperationStatus(serviceId, operationId, resourceTemplateUUID, operationType,
                resourceInstanceID, jobId, status, progress, errorCode, statusDescription);
    }
}
