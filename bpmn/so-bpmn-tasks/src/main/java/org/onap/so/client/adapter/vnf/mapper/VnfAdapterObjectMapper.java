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

package org.onap.so.client.adapter.vnf.mapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleTopology;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduletopologyVfModuleTopology;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.entity.MsoRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VnfAdapterObjectMapper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String ENABLE_BRIDGE = "mso.bridgeEnabled";

    @PostConstruct
    public void init() {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    public CreateVolumeGroupRequest createVolumeGroupRequestMapper(RequestContext requestContext,
            CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance,
            GenericVnf genericVnf, VolumeGroup volumeGroup, String sdncVfModuleQueryResponse)
            throws JsonParseException, JsonMappingException, IOException {
        CreateVolumeGroupRequest createVolumeGroupRequest = new CreateVolumeGroupRequest();

        createVolumeGroupRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        createVolumeGroupRequest.setTenantId(cloudRegion.getTenantId());
        createVolumeGroupRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());
        createVolumeGroupRequest.setVolumeGroupName(volumeGroup.getVolumeGroupName());
        createVolumeGroupRequest.setVnfType(genericVnf.getVnfType());
        createVolumeGroupRequest.setVnfVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        createVolumeGroupRequest.setVfModuleType(volumeGroup.getModelInfoVfModule().getModelName());
        createVolumeGroupRequest
                .setModelCustomizationUuid(volumeGroup.getModelInfoVfModule().getModelCustomizationUUID());
        createVolumeGroupRequest.setVolumeGroupParams(
                createVolumeGroupParams(requestContext, genericVnf, volumeGroup, sdncVfModuleQueryResponse));

        createVolumeGroupRequest.setSkipAAI(true);
        createVolumeGroupRequest.setSuppressBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        createVolumeGroupRequest.setFailIfExists(false);

        createVolumeGroupRequest.setMsoRequest(createMsoRequest(requestContext, serviceInstance));

        String messageId = getRandomUuid();
        createVolumeGroupRequest.setMessageId(messageId);
        createVolumeGroupRequest.setNotificationUrl(createCallbackUrl("VNFAResponse", messageId));

        String enableBridge = getProperty(ENABLE_BRIDGE);
        if (enableBridge == null || Boolean.valueOf(enableBridge)) {
            createVolumeGroupRequest.setEnableBridge(true);
        }
        return createVolumeGroupRequest;
    }

    public DeleteVolumeGroupRequest deleteVolumeGroupRequestMapper(RequestContext requestContext,
            CloudRegion cloudRegion, ServiceInstance serviceInstance, VolumeGroup volumeGroup) throws IOException {
        DeleteVolumeGroupRequest deleteVolumeGroupRequest = new DeleteVolumeGroupRequest();

        deleteVolumeGroupRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        deleteVolumeGroupRequest.setTenantId(cloudRegion.getTenantId());
        deleteVolumeGroupRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());
        if (!StringUtils.isEmpty(volumeGroup.getHeatStackId())) {
            deleteVolumeGroupRequest.setVolumeGroupStackId(volumeGroup.getHeatStackId());
        } else {
            deleteVolumeGroupRequest.setVolumeGroupStackId(volumeGroup.getVolumeGroupName());
        }

        deleteVolumeGroupRequest.setSkipAAI(true);
        deleteVolumeGroupRequest.setMsoRequest(createMsoRequest(requestContext, serviceInstance));

        String messageId = getRandomUuid();
        deleteVolumeGroupRequest.setMessageId(messageId);
        deleteVolumeGroupRequest.setNotificationUrl(createCallbackUrl("VNFAResponse", messageId));

        return deleteVolumeGroupRequest;
    }

    public Map<String, Object> createVolumeGroupParams(RequestContext requestContext, GenericVnf genericVnf,
            VolumeGroup volumeGroup, String sdncVfModuleQueryResponse)
            throws JsonParseException, JsonMappingException, IOException {
        Map<String, Object> volumeGroupParams = new HashMap<>();
        final String USER_PARAM_NAME_KEY = "name";
        final String USER_PARAM_VALUE_KEY = "value";
        // sdncVfModuleQueryResponse will not be available in aLaCarte case
        if (sdncVfModuleQueryResponse != null) {
            GenericResourceApiVfModuleTopology vfModuleTop =
                    mapper.readValue(sdncVfModuleQueryResponse, GenericResourceApiVfModuleTopology.class);
            GenericResourceApiVfmoduletopologyVfModuleTopology vfModuleTopology = vfModuleTop.getVfModuleTopology();
            buildParamsMapFromSdncParams(volumeGroupParams, vfModuleTopology.getVfModuleParameters());
        }

        if (null != requestContext.getRequestParameters()
                && null != requestContext.getRequestParameters().getUserParams()) {
            List<Map<String, Object>> userParams = requestContext.getRequestParameters().getUserParams();
            for (Map<String, Object> userParamsMap : userParams) {
                if (userParamsMap.containsKey(USER_PARAM_NAME_KEY)
                        && (userParamsMap.get(USER_PARAM_NAME_KEY) instanceof String)
                        && userParamsMap.containsKey(USER_PARAM_VALUE_KEY)
                        && (userParamsMap.get(USER_PARAM_VALUE_KEY) instanceof String)) {
                    volumeGroupParams.put((String) userParamsMap.get(USER_PARAM_NAME_KEY),
                            (String) userParamsMap.get(USER_PARAM_VALUE_KEY));
                }
            }
        }
        volumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        volumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        volumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        volumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());

        return volumeGroupParams;
    }

    public MsoRequest createMsoRequest(RequestContext requestContext, ServiceInstance serviceInstance) {
        MsoRequest msoRequest = new MsoRequest();

        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());

        return msoRequest;
    }

    private void buildParamsMapFromSdncParams(Map<String, Object> volumeGroupParams,
            GenericResourceApiParam sdncParameters) {
        if (sdncParameters != null) {
            List<GenericResourceApiParamParam> sdncParametersList = sdncParameters.getParam();
            if (sdncParametersList != null) {
                for (int i = 0; i < sdncParametersList.size(); i++) {
                    GenericResourceApiParamParam param = sdncParametersList.get(i);
                    String parameterName = param.getName();
                    String parameterValue = param.getValue();
                    volumeGroupParams.put(parameterName, parameterValue);
                }
            }
        }
    }

    protected String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

    protected String createCallbackUrl(String messageType, String correlator) throws UnsupportedEncodingException {
        String endpoint = getProperty("mso.workflow.message.endpoint");

        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        return endpoint + "/" + UriUtils.encodePathSegment(messageType, "UTF-8") + "/"
                + UriUtils.encodePathSegment(correlator, "UTF-8");
    }

    protected String getProperty(String key) {
        return UrnPropertiesReader.getVariable(key);
    }

}
