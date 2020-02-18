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

package org.onap.so.adapters.requestsdb;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import java.util.List;
import org.onap.so.db.request.beans.OperationStatus;

/**
 * MSO Request DB Adapter Web Service
 */
@Deprecated
@WebService(name = "RequestsDbAdapter", targetNamespace = "http://org.onap.so/requestsdb")
public interface MsoRequestsDbAdapter {

    @WebMethod
    public void updateInfraRequest(@WebParam(name = "requestId") @XmlElement(required = true) String requestId,
            @WebParam(name = "lastModifiedBy") @XmlElement(required = true) String lastModifiedBy,
            @WebParam(name = "statusMessage") @XmlElement(required = false) String statusMessage,
            @WebParam(name = "responseBody") @XmlElement(required = false) String responseBody,
            @WebParam(name = "requestStatus") @XmlElement(required = false) RequestStatusType requestStatus,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "vnfOutputs") @XmlElement(required = false) String vnfOutputs,
            @WebParam(name = "serviceInstanceId") @XmlElement(required = false) String serviceInstanceId,
            @WebParam(name = "networkId") @XmlElement(required = false) String networkId,
            @WebParam(name = "vnfId") @XmlElement(required = false) String vnfId,
            @WebParam(name = "vfModuleId") @XmlElement(required = false) String vfModuleId,
            @WebParam(name = "volumeGroupId") @XmlElement(required = false) String volumeGroupId,
            @WebParam(name = "serviceInstanceName") @XmlElement(required = false) String serviceInstanceName,
            @WebParam(name = "configurationId") @XmlElement(required = false) String configurationId,
            @WebParam(name = "configurationName") @XmlElement(required = false) String configurationName,
            @WebParam(name = "vfModuleName") @XmlElement(required = false) String vfModuleName)
            throws MsoRequestsDbException;

    @WebMethod
    public InfraActiveRequests getInfraRequest(
            @WebParam(name = "requestId") @XmlElement(required = true) String requestId) throws MsoRequestsDbException;

    @WebMethod
    public boolean getSiteStatus(@WebParam(name = "siteName") @XmlElement(required = true) String siteName);

    @WebMethod
    public void updateServiceOperationStatus(
            @WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = false) String operationId,
            @WebParam(name = "operationType") @XmlElement(required = false) String operationType,
            @WebParam(name = "userId") @XmlElement(required = false) String userId,
            @WebParam(name = "result") @XmlElement(required = false) String result,
            @WebParam(name = "operationContent") @XmlElement(required = false) String operationContent,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "reason") @XmlElement(required = false) String reason) throws MsoRequestsDbException;

    @WebMethod
    public void initServiceOperationStatus(@WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = false) String operationId,
            @WebParam(name = "operationType") @XmlElement(required = false) String operationType,
            @WebParam(name = "userId") @XmlElement(required = false) String userId,
            @WebParam(name = "result") @XmlElement(required = false) String result,
            @WebParam(name = "operationContent") @XmlElement(required = false) String operationContent,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "reason") @XmlElement(required = false) String reason) throws MsoRequestsDbException;

    @WebMethod
    public void initResourceOperationStatus(@WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = true) String operationId,
            @WebParam(name = "operationType") @XmlElement(required = true) String operationType,
            @WebParam(name = "resourceTemplateUUIDs") @XmlElement(required = true) String resourceTemplateUUIDs)
            throws MsoRequestsDbException;

    @WebMethod
    public ResourceOperationStatus getResourceOperationStatus(
            @WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = true) String operationId,
            @WebParam(name = "resourceTemplateUUID") @XmlElement(required = true) String resourceTemplateUUID)
            throws MsoRequestsDbException;

    @WebMethod
    public void updateResourceOperationStatus(
            @WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = true) String operationId,
            @WebParam(name = "resourceTemplateUUID") @XmlElement(required = true) String resourceTemplateUUID,
            @WebParam(name = "operType") @XmlElement(required = false) String operType,
            @WebParam(name = "resourceInstanceID") @XmlElement(required = false) String resourceInstanceID,
            @WebParam(name = "jobId") @XmlElement(required = false) String jobId,
            @WebParam(name = "status") @XmlElement(required = false) String status,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "errorCode") @XmlElement(required = false) String errorCode,
            @WebParam(name = "statusDescription") @XmlElement(required = false) String statusDescription)
            throws MsoRequestsDbException;

    @WebMethod
    public void initAccessServiceOperationStatus(
            @WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = false) String operationId,
            @WebParam(name = "serviceName") @XmlElement(required = false) String serviceName,
            @WebParam(name = "operationType") @XmlElement(required = false) String operationType,
            @WebParam(name = "userId") @XmlElement(required = false) String userId,
            @WebParam(name = "result") @XmlElement(required = false) String result,
            @WebParam(name = "operationContent") @XmlElement(required = false) String operationContent,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "reason") @XmlElement(required = false) String reason) throws MsoRequestsDbException;

    @WebMethod
    public void initServiceOperationStatusWithAccessService(

            @WebParam(name = "serviceId") @XmlElement(required = true) String serviceId,
            @WebParam(name = "operationId") @XmlElement(required = false) String operationId,
            @WebParam(name = "serviceName") @XmlElement(required = false) String serviceName,
            @WebParam(name = "operationType") @XmlElement(required = false) String operationType,
            @WebParam(name = "userId") @XmlElement(required = false) String userId,
            @WebParam(name = "result") @XmlElement(required = false) String result,
            @WebParam(name = "operationContent") @XmlElement(required = false) String operationContent,
            @WebParam(name = "progress") @XmlElement(required = false) String progress,
            @WebParam(name = "reason") @XmlElement(required = false) String reason,
            @WebParam(name = "accessServiceId") @XmlElement(required = false) String accessServiceId)
            throws MsoRequestsDbException;

    @WebMethod
    public List<OperationStatus> getControllerServiceOperationStatus(

            @WebParam(name = "accessServiceId") @XmlElement(required = true) String accessServiceId)
            throws MsoRequestsDbException;

    @WebMethod
    public OperationStatus getOperationStatusByOperationId(

            @WebParam(name = "operationId") @XmlElement(required = true) String operationId)
            throws MsoRequestsDbException;
}
