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
import org.onap.so.apihandlerinfra.infra.rest.exception.NoRecipeException;
import org.onap.so.apihandlerinfra.infra.rest.exception.RequestConflictedException;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.ServiceRecipe;
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
public class ServiceInstanceRestHandler extends AbstractRestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceRestHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DEFAULT_VF_MODULE_UUID = "d88da85c-d9e8-4f73-b837-3a72a431622b";


    public InfraActiveRequests mapInfraActiveRequestForDelete(String requestId, String serviceInstanceId,
            String requestorId, String source, String requestURL) {
        Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
        InfraActiveRequests deleteRequest = new InfraActiveRequests();
        deleteRequest.setRequestAction(Action.deleteInstance.toString());
        deleteRequest.setStartTime(startTimeStamp);
        deleteRequest.setServiceInstanceId(serviceInstanceId);
        deleteRequest.setRequestId(requestId);
        deleteRequest.setRequestUrl(MDC.get(LogConstants.HTTP_URL));
        deleteRequest.setRequestorId(requestorId);
        deleteRequest.setSource(source);
        deleteRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        deleteRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        deleteRequest.setRequestUrl(requestURL);
        deleteRequest.setRequestScope(ModelType.service.toString());
        return deleteRequest;
    }

    public InfraActiveRequests createInfraActiveRequestForDelete(String requestId, String serviceInstanceId,
            String requestorId, String source, String requestURL) {
        InfraActiveRequests request =
                mapInfraActiveRequestForDelete(requestId, serviceInstanceId, requestorId, source, requestURL);
        infraActiveRequestsClient.save(request);
        return request;
    }

    public RequestClientParameter buildRequestParams(ServiceInstancesRequest request, String requestURI,
            String requestId, String serviceInstanceId) throws JsonProcessingException {
        return new RequestClientParameter.Builder().setRequestId(requestId).setServiceInstanceId(serviceInstanceId)
                .setALaCarte(true).setRequestDetails(mapper.writeValueAsString(request))
                .setRequestAction(Action.deleteInstance.toString()).setRequestUri(requestURI).setApiVersion("v8")
                .build();
    }

    public void saveInstanceName(ServiceInstancesRequest request, InfraActiveRequests currentRequest) {
        try {
            currentRequest.setServiceInstanceName(request.getRequestDetails().getRequestInfo().getInstanceName());
            infraActiveRequestsClient.updateInfraActiveRequests(currentRequest);
        } catch (Exception e) {
            logger.warn("Could not update instance name", e);
        }
    }

    public void checkDuplicateRequest(String serviceInstanceId, String instanceName, String requestId)
            throws RequestConflictedException {
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        checkDuplicateRequest(instanceIdMap, ModelType.service, instanceName, requestId);
    }

    public Recipe findServiceRecipe(String modelUUID, String action) throws NoRecipeException {
        ServiceRecipe recipe = catalogDbClient.findServiceRecipeByActionAndServiceModelUUID(action, modelUUID);
        if (recipe == null) {
            recipe = catalogDbClient.findServiceRecipeByActionAndServiceModelUUID(action, DEFAULT_VF_MODULE_UUID);
        }
        if (recipe == null) {
            throw new NoRecipeException(String.format(
                    "Unable to locate custom or default recipe for, Action: %s, Model UUID: %s", action, modelUUID));
        }
        return recipe;
    }

}
