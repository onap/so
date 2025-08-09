/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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

package org.onap.so.client.cds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aaiclient.client.aai.entities.CustomQuery;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.NodesSingleUri;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.Format;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.moi.Attributes;
import org.onap.so.moi.Snssai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.SEPARATOR;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NssiCDSRequestProvider implements CDSRequestProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NssiCDSRequestProvider.class);

    private String blueprintName;
    private String blueprintVersion;
    private BuildingBlockExecution execution;

    private static final String DELETE_SLICE_PROFILE_ACTION = "delete-sliceprofile";
    private static final String CREATE_SLICE_PROFILE_ACTION = "create-sliceprofile";
    private static final String MODIFY_SLICE_PROFILE_ACTION = "modify-sliceprofile";
    private static final String SERVICE_INSTANCE_KEY = "service-instance";
    private static final String SERVICE_INSTANCE_ID_KEY = "service-instance-id";
    private static final String NSSI = "nssi";
    private static final String NSSI_ID = "nssiId";
    private static final String NSSI_NAME = "nssiName";
    private static final String NSI_NAME = "nsiName";
    private static final String NSI_ID = "nsiId";
    private static final String SLICE_PROFILE_INSTANCE_ID = "sliceProfileInstanceId";
    private static final String SLICE_INSTANCE_FROM_PROFILE_ID_CUSTOM_QUERY =
            "related-to?startingNodeType=slice-profile&relatedToNodeType=service-instance";
    private static final String ENVIRONMENT_CONTEXT_QUERY_PARAM = "environment-context";
    private static final String AAI_SUPPORTED_SLICE_PROFILE =
            "latency|maxNumberofUEs|coverageAreaTAList|areaTrafficCapDL|resourceSharingLevel|serviceType|uEMobilityLevel|expDataRateUL|expDataRateDL";
    private static final ObjectMapper mapper = new ObjectMapper();

    JsonUtils jsonUtil = new JsonUtils();

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private InjectionHelper injectionHelper;

    @Override
    public String getBlueprintName() {
        return blueprintName;
    }

    @Override
    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (BuildingBlockExecution) executionObject;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        JsonObject cdsPropertyObject = null;

        final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
        List<Map<String, Object>> userParamsFromRequest =
                buildingBlock.getRequestContext().getRequestParameters().getUserParams();

        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            // get values from service Instance
            blueprintName = serviceInstance.getModelInfoServiceInstance().getBlueprintName();
            blueprintVersion = serviceInstance.getModelInfoServiceInstance().getBlueprintVersion();

        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObjectForNssi", e);
        }

        cdsPropertyObject = setCreateSliceProfileRequestValues(action, buildingBlock);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

    private JsonObject setCreateSliceProfileRequestValues(String action, GeneralBuildingBlock buildingBlock) {

        JsonObject cdsRequestObject = new JsonObject();

        ServiceInstance serviceInstance = null;
        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> userParamsFromRequest =
                buildingBlock.getRequestContext().getRequestParameters().getUserParams();

        Attributes attributes = null;

        for (Map<String, Object> userParamData : userParamsFromRequest) {
            if (userParamData.get(NSSI) != null) {
                attributes = mapper.convertValue(userParamData.get(NSSI), Attributes.class);
            }
        }
        // Value to come as a request == Hardcoded for now
        String nssiId = serviceInstance.getServiceInstanceId();
        String nssiName = getNssiName(nssiId);
        String nsiId = "nssi-id";
        String nsiName = "nssi-name";

        cdsRequestObject.addProperty(NSSI_ID, nssiId);

        if (!DELETE_SLICE_PROFILE_ACTION.equals(action)) {
            cdsRequestObject.addProperty(NSSI_NAME, nssiName);
            cdsRequestObject.addProperty(NSI_NAME, nsiName);
        }

        String sliceProfileInstanceId = null;
        if (CREATE_SLICE_PROFILE_ACTION.equalsIgnoreCase(action)) {
            Snssai snssai = attributes.getSliceProfileList().get(0).getPlmnInfoList().get(0).getSnssai();
            String sNssaiString = snssai.getSst() + SEPARATOR + snssai.getSd();
            sliceProfileInstanceId = getSliceProfileInstanceIdForNssi(sNssaiString);
        } else {
            String sliceProfileId = attributes.getSliceProfileList().get(0).getSliceProfileId();
            sliceProfileInstanceId = getSliceProfileInstanceFromSliceProfileId(sliceProfileId);
        }

        cdsRequestObject.addProperty(SLICE_PROFILE_INSTANCE_ID, sliceProfileInstanceId);

        cdsRequestObject.addProperty(NSI_ID, nsiId);
        /*
         * JsonObject nssiPropertyObject = setSliceProfileProperties(getSliceProfilesFromUserParams(buildingBlock));
         * cdsRequestObject.add(action + SEPARATOR + PROPERTIES, nssiPropertyObject);
         */
        return cdsRequestObject;
    }

    private Map<String, Object> getSliceProfilesFromUserParams(GeneralBuildingBlock gBB) {


        List<Map<String, Object>> mapUserParams = gBB.getRequestContext().getRequestParameters().getUserParams();

        Map<String, Object> sliceProfileMap = null;
        try {
            String userParamsJson = mapper.writeValueAsString(mapUserParams.get(0));
            String rANSliceSubnetProfile =
                    jsonUtil.getJsonParamValue(userParamsJson, "nssi.sliceProfileList", "RANSliceSubnetProfile");

            if (rANSliceSubnetProfile != null) {
                sliceProfileMap = mapper.readValue(rANSliceSubnetProfile, Map.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return sliceProfileMap;
    }

    // May be needed later

    /*
     * private JsonObject setSliceProfileProperties(Map<String, Object> userParamsMap) { JsonObject
     * sliceProfilePropertiesNotPresentInAai = new JsonObject();
     *
     * if (userParamsMap != null) { userParamsMap.forEach((k, v) -> { if (!AAI_SUPPORTED_SLICE_PROFILE.contains((String)
     * k)) { sliceProfilePropertiesNotPresentInAai.addProperty(k, v.toString()); } }); }
     *
     * return sliceProfilePropertiesNotPresentInAai; }
     */

    private String getSliceProfileInstanceFromSliceProfileId(String sliceProfileId) {

        List<AAIResourceUri> startNodes = new ArrayList<>();
        startNodes.add(
                AAIUriFactory.createNodesUri(AAIFluentTypeBuilder.Types.SLICE_PROFILE.getFragment(sliceProfileId)));

        CustomQuery customQuery = new CustomQuery(startNodes, SLICE_INSTANCE_FROM_PROFILE_ID_CUSTOM_QUERY);

        String results = injectionHelper.getAaiQueryClient().query(Format.RESOURCE, customQuery);

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Slice Profile Instance Not found");
        }

        Map<String, List<Map<String, Object>>> serviceInstancesMap = null;
        try {
            serviceInstancesMap = mapper.readValue(results, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, Object> serviceInstance =
                (Map<String, Object>) serviceInstancesMap.get("results").get(0).get(SERVICE_INSTANCE_KEY);

        return (String) serviceInstance.get(SERVICE_INSTANCE_ID_KEY);
    }

    private String getSliceProfileInstanceIdForNssi(String sNssai) {

        String sliceProfileInstanceId = null;

        try {
            AAIPluralResourceUri uriSI =
                    AAIUriFactory.createNodesUri(AAIFluentTypeBuilder.Types.SERVICE_INSTANCES.getFragment())
                            .queryParam(ENVIRONMENT_CONTEXT_QUERY_PARAM, sNssai);
            Optional<ServiceInstances> sliceProfileInstancesOptional =
                    injectionHelper.getAaiClient().get(ServiceInstances.class, uriSI);

            if (sliceProfileInstancesOptional.isPresent()) {
                sliceProfileInstanceId =
                        sliceProfileInstancesOptional.get().getServiceInstance().get(0).getServiceInstanceId();
            }
        } catch (Exception e) {
            LOGGER.error("Error in getting sliceProfile Instance {}", e.getMessage());
        }
        return sliceProfileInstanceId;
    }

    private String getNssiName(String nssiId) {

        String nssiName = null;

        try {
            NodesSingleUri uriSI =
                    AAIUriFactory.createNodesUri(AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(nssiId));
            Optional<org.onap.aai.domain.yang.ServiceInstance> sliceProfileInstancesOptional =
                    injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.ServiceInstance.class, uriSI);

            if (sliceProfileInstancesOptional.isPresent()) {
                nssiName = sliceProfileInstancesOptional.get().getServiceInstanceName();
            }
        } catch (Exception e) {
            LOGGER.error("Error in getting Nssi Instance{}", e.getMessage());
        }
        return nssiName;
    }
}
