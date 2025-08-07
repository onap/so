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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask.client;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.onap.msb.sdk.httpclient.annotaion.ServiceHttpEndPoint;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationOutputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationOutputEntity;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

@ServiceHttpEndPoint(serviceName = "sdnc", serviceVersion = "v1")
public interface GenericResourceApi {

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<ResponseBody> postNetworkTopologyOperation(@Header("Authorization") String authorization,
            @Body RequestBody input);

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<RpcNetworkTopologyOperationOutputEntity> postNetworkTopologyOperation(
            @Header("Authorization") String authorization, @Body RpcNetworkTopologyOperationInputEntity input);

    @POST("/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation")
    Call<RpcServiceTopologyOperationOutputEntity> postServiceTopologyOperation(
            @Header("Authorization") String authorization, @Body RpcServiceTopologyOperationInputEntity input);

}

