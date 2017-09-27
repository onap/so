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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.NetworkTopologyOperationInput;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.NetworkTopologyOperationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.network.request.input.NetworkRequestInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.network.request.input.network.request.input.NetworkInputParametersBuilder;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.param.Param;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.param.ParamBuilder;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.param.ParamKey;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.request.information.RequestInformation;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.request.information.RequestInformationBuilder;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.sdnc.request.header.SdncRequestHeader;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.sdnc.request.header.SdncRequestHeaderBuilder;
import org.openecomp.mso.yangDecoder.transform.api.ITransformJava2StringService;
import org.openecomp.mso.yangDecoder.transform.impl.TransfromJava2StringFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkTopologyRequestBodyBuilder implements AbstractBuilder<Map<String, String>, RequestBody> {

    public static final String URI_PATH = "GENERIC-RESOURCE-API:network-topology-operation";
    public static final SdncRequestHeader.SvcAction SVC_DEFAULT_ACTION = SdncRequestHeader.SvcAction.Create;
    public static final String SVC_REQUEST_ID = "MSO";
    public static final String SDC_ACTION = "SDC_ACTION";
    public static final RequestInformation.RequestAction REQUEST_ACTION = RequestInformation.RequestAction.CreateNetworkInstance;
    protected static ITransformJava2StringService java2jsonService;

    static
    {
        try {
            java2jsonService = TransfromJava2StringFactory.getJava2jsonService();
        } catch (Exception e) {
            e.printStackTrace();
            java2jsonService = null;
        }
    }

    @Override
    public RequestBody build(Map<String, String> input) throws Exception {
        NetworkTopologyOperationInput sdncInput = getSdncInput(input);
        RequestBody body = getRequestBody(sdncInput);
        return body;
    }

    protected String getJsonInput(NetworkTopologyOperationInput sdncInput) throws Exception {
        return java2jsonService.transformRpcDataObjectToString(URI_PATH, sdncInput);
    }

    private RequestBody getRequestBody(NetworkTopologyOperationInput sdncInput) throws Exception {
        String jsonString = getJsonInput(sdncInput);
        String json = (new JSONObject(jsonString)).toString();
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),json);
    }

    protected NetworkTopologyOperationInput getSdncInput(Map<String, String> inputs) {
        NetworkTopologyOperationInputBuilder networkTopologyOperationInputBuilder = new NetworkTopologyOperationInputBuilder();
        {
            loadSdncRequestHeader(inputs, networkTopologyOperationInputBuilder);
            loadRequestInformation(networkTopologyOperationInputBuilder);
            loadNetworkInputParameters(inputs, networkTopologyOperationInputBuilder);
        }
        return networkTopologyOperationInputBuilder.build();
    }

    private void loadNetworkInputParameters(Map<String, String> inputs, NetworkTopologyOperationInputBuilder networkTopologyOperationInputBuilder) {
        NetworkRequestInputBuilder networkRequestInputBuilder = new NetworkRequestInputBuilder();
        {
            NetworkInputParametersBuilder networkInputParametersBuilder = new NetworkInputParametersBuilder();
            {
                List<Param> paramList = getParamList(inputs);
                networkInputParametersBuilder.setParam(paramList);
            }
            networkRequestInputBuilder.setNetworkInputParameters(networkInputParametersBuilder.build());
        }
        networkTopologyOperationInputBuilder.setNetworkRequestInput(networkRequestInputBuilder.build());
    }

    private void loadRequestInformation(NetworkTopologyOperationInputBuilder networkTopologyOperationInputBuilder) {
        RequestInformationBuilder requestInformationBuilder = new RequestInformationBuilder();
        {
            requestInformationBuilder.setRequestId(SVC_REQUEST_ID);
            requestInformationBuilder.setRequestAction(REQUEST_ACTION);
        }
        networkTopologyOperationInputBuilder.setRequestInformation(requestInformationBuilder.build());
    }

    private void loadSdncRequestHeader(Map<String, String> inputs, NetworkTopologyOperationInputBuilder networkTopologyOperationInputBuilder) {
        SdncRequestHeaderBuilder sdncRequestHeaderBuilder = new SdncRequestHeaderBuilder();
        {
            sdncRequestHeaderBuilder.setSvcRequestId(SVC_REQUEST_ID);
            SdncRequestHeader.SvcAction svcAction = SVC_DEFAULT_ACTION;
            String action = inputs.get(SDC_ACTION);
            if (!StringUtils.isBlank(action)) {
                if (action.toLowerCase().contains("delete")) {
                    svcAction = SdncRequestHeader.SvcAction.Delete;
                } else if (action.toLowerCase().contains("create")) {
                    svcAction = SdncRequestHeader.SvcAction.Create;
                }
            }
            sdncRequestHeaderBuilder.setSvcAction(svcAction);
        }
        networkTopologyOperationInputBuilder.setSdncRequestHeader(sdncRequestHeaderBuilder.build());
    }

    private List<Param> getParamList(Map<String, String> inputs) {
        List<Param> paramList = new ArrayList<>();
        if (inputs != null && !inputs.isEmpty()) {
            inputs.keySet().forEach(key -> {
                ParamBuilder paramBuilder = new ParamBuilder();
                paramBuilder.setName(key);
                paramBuilder.setValue(inputs.get(key));
                paramBuilder.setKey(new ParamKey(key));
                paramList.add(paramBuilder.build());
            });
        }
        return paramList;
    }
}
