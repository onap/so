/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest.handler;


import java.sql.Timestamp;
import java.util.HashMap;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.LogConstants;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeRestHandler extends AbstractRestHandler {

    private static final Logger logger = LoggerFactory.getLogger(VolumeRestHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public InfraActiveRequests mapInfraActiveRequestForDelete(String requestId, String volumeGroupId,
            String serviceInstanceId, String vnfId, String requestorId, String source, String requestURL) {
        Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
        InfraActiveRequests deleteRequest = new InfraActiveRequests();
        deleteRequest.setRequestAction(Action.deleteInstance.toString());
        deleteRequest.setStartTime(startTimeStamp);
        deleteRequest.setServiceInstanceId(serviceInstanceId);
        deleteRequest.setVnfId(vnfId);
        deleteRequest.setVolumeGroupId(volumeGroupId);
        deleteRequest.setRequestId(requestId);
        deleteRequest.setRequestUrl(MDC.get(LogConstants.HTTP_URL));
        deleteRequest.setRequestorId(requestorId);
        deleteRequest.setSource(source);
        deleteRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        deleteRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        deleteRequest.setRequestScope(ModelType.volumeGroup.toString());
        deleteRequest.setRequestUrl(requestURL);
        return deleteRequest;
    }

    public InfraActiveRequests createInfraActiveRequestForDelete(String requestId, String volumeGroupId,
            String serviceInstanceId, String vnfId, String requestorId, String source, String requestURL) {
        InfraActiveRequests request = mapInfraActiveRequestForDelete(requestId, volumeGroupId, serviceInstanceId, vnfId,
                requestorId, source, requestURL);
        infraActiveRequestsClient.save(request);
        return request;
    }

    public RequestClientParameter buildRequestParams(ServiceInstancesRequest request, String requestURI,
            String requestId, String serviceInstanceId, String vnfId, String volumeGroupId)
            throws JsonProcessingException {
        return new RequestClientParameter.Builder().setRequestId(requestId).setServiceInstanceId(serviceInstanceId)
                .setVnfId(vnfId).setVolumeGroupId(volumeGroupId).setALaCarte(true)
                .setRequestDetails(mapper.writeValueAsString(request))
                .setRequestAction(Action.deleteInstance.toString()).setRequestUri(requestURI).setApiVersion("v8")
                .build();
    }

    public void saveInstanceName(ServiceInstancesRequest request, InfraActiveRequests currentRequest) {
        try {
            currentRequest.setVolumeGroupName(request.getRequestDetails().getRequestInfo().getInstanceName());
            infraActiveRequestsClient.updateInfraActiveRequests(currentRequest);
        } catch (Exception e) {
            logger.warn("Could not update instance name", e);
        }
    }

    public void checkDuplicateRequest(String serviceInstanceId, String vnfInstanceId, String volumeGroupId,
            String instanceName, String requestId) {
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("volumeGroupInstanceId", volumeGroupId);
        checkDuplicateRequest(instanceIdMap, ModelType.volumeGroup, instanceName, requestId);
    }

}
