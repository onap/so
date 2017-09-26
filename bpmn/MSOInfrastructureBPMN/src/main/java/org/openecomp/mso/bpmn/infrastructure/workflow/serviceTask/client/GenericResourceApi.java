package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.onap.msb.sdk.httpclient.annotaion.ServiceHttpEndPoint;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by 10112215 on 2017/9/16.
 */
@ServiceHttpEndPoint(serviceName = "sdnc", serviceVersion = "v1")
public interface GenericResourceApi {

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<ResponseBody> postNetworkTopologyOperation(@Body RequestBody input);

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<RpcNetworkTopologyOperationOutputEntity> postNetworkTopologyOperation(@Body RpcNetworkTopologyOperationInputEntity input);

    @POST("/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation")
    Call<RpcServiceTopologyOperationOutputEntity> postServiceTopologyOperation(@Body RpcServiceTopologyOperationInputEntity input);

}

