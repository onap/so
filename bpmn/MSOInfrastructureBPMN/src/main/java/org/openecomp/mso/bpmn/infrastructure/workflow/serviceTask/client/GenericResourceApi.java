package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.onap.msb.sdk.httpclient.annotaion.ServiceHttpEndPoint;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcOutputEntity;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by 10112215 on 2017/9/16.
 */
@ServiceHttpEndPoint(serviceName = "sdnc", serviceVersion = "v1")
public interface GenericResourceApi {

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<ResponseBody> postNetworkTopologyPeration(@Body RequestBody input);

    @POST("/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation")
    Call<NetworkRpcOutputEntity> postNetworkTopologyPeration(@Body NetworkRpcInputEntity input);

}

