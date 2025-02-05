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


package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.moi.util.AAISliceProfileUtil;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.moi.PlmnInfo;
import org.onap.so.moi.RANSliceSubnetProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class ModifyRANNssiBBTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyRANNssiBBTask.class);

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAISliceProfileUtil aaiSliceProfileUtil;

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    private static final ObjectMapper mapper = new ObjectMapper();


    public void modifyNssi(BuildingBlockExecution execution) throws JsonProcessingException {
        LOGGER.info("Modify NSSI");
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();
        List<Map<String, Object>> sliceProfilesData = gBB.getRequestContext().getRequestParameters().getUserParams();

        SliceProfile updatedSliceProfile = mapUserParamsToSliceProfile(sliceProfilesData);
        String sliceProfileIdFromRequest = getSliceProfileIdFromReq(sliceProfilesData);
        aaiSliceProfileUtil.updateSliceProfile(execution, sliceProfileIdFromRequest, updatedSliceProfile);



    }

    private ServiceInstance mapUserParamsToServiceInstance(ServiceInstance sliceProfileServiceInstanceObj,
            List<Map<String, Object>> sliceProfilesData) {
        Map<String, Object> mapParam = (Map<String, Object>) sliceProfilesData.get(0).get("nssi");
        LOGGER.info(">>> mapParam: {}", mapParam);

        // update administrative State
        String administrativeState = (String) mapParam.get("administrativeState");
        LOGGER.info(">>> administrativeState: {}", administrativeState);
        sliceProfileServiceInstanceObj.setOperationalStatus(administrativeState);

        List<Object> list = (ArrayList<Object>) mapParam.get("sliceProfileList");
        LOGGER.info(">>> sliceProfile List: {}", list);
        Map<String, Object> idMap = (Map<String, Object>) list.get(0);
        LOGGER.info("Keys of Id Map {} ", idMap.keySet());

        // PlmnInfoList
        for (String key : idMap.keySet()) {
            if (key.equalsIgnoreCase("plmnInfoList")) {
                PlmnInfo plmnInfo = mapper.convertValue(mapParam, PlmnInfo.class);
                LOGGER.info("PlmnInfo {}", plmnInfo.getPlmnId().getMcc() + "-" + plmnInfo.getPlmnId().getMnc());
                LOGGER.info("Snssai {} ", plmnInfo.getSnssai().getSst() + "-" + plmnInfo.getSnssai().getSd());
                sliceProfileServiceInstanceObj.setServiceInstanceLocationId(
                        plmnInfo.getPlmnId().getMcc() + "-" + plmnInfo.getPlmnId().getMnc());
                sliceProfileServiceInstanceObj
                        .setEnvironmentContext(plmnInfo.getSnssai().getSst() + "-" + plmnInfo.getSnssai().getSd());
            }
        }
        return sliceProfileServiceInstanceObj;
    }

    SliceProfile mapUserParamsToSliceProfile(List<Map<String, Object>> sliceProfilesData)
            throws JsonProcessingException {
        SliceProfile sliceProfile = new SliceProfile();
        Map<String, Object> mapParam = (Map<String, Object>) sliceProfilesData.get(0).get("nssi");
        LOGGER.info(">>> mapParam in map: {}", mapParam);

        List<Object> list = (ArrayList<Object>) mapParam.get("sliceProfileList");

        Map<String, Object> idMap = (Map<String, Object>) list.get(0);

        String sliceProfileId = (String) idMap.get("sliceProfileId");

        sliceProfile.setProfileId(sliceProfileId);
        RANSliceSubnetProfile ranSliceSubnetProfile = mapper.convertValue(mapParam, RANSliceSubnetProfile.class);


        for (String key : idMap.keySet()) {
            if (key.equalsIgnoreCase("RANSliceSubnetProfile")) {
                RANSliceSubnetProfile RANSliceSubnetProfile =
                        mapper.convertValue(mapParam, RANSliceSubnetProfile.class);
                LOGGER.info("RANSliceSubnetProfile inside {}", RANSliceSubnetProfile);
                Map<String, Object> ranMap = (Map<String, Object>) idMap.get(key);
                ranMap.forEach((k, v) -> {
                    LOGGER.info("Key : {}", k);
                    switch (k) {
                        case "coverageAreaTAList":
                            Integer coverageAreaTAList = (Integer) ranMap.get(k);
                            LOGGER.info("coverageAreaTAList {}", coverageAreaTAList);
                            sliceProfile.setCoverageAreaTAList(coverageAreaTAList.toString());
                            break;

                        case "latency":
                            Integer latency = (Integer) ranMap.get(k);
                            LOGGER.info("latency {}", latency);
                            sliceProfile.setLatency(latency);
                            break;

                        case "dLLatency":
                            Integer dLLatency = (Integer) ranMap.get(k);
                            LOGGER.info("dLLatency {}", dLLatency);
                            sliceProfile.setLatency(dLLatency);
                            break;

                        case "areaTrafficCapDL":
                            Integer areaTrafficCapDL = (Integer) ranMap.get(k);
                            LOGGER.info("areaTrafficCapDL {}", areaTrafficCapDL);
                            sliceProfile.setAreaTrafficCapDL(areaTrafficCapDL);
                            break;

                        case "resourceSharingLevel":
                            String resourceSharingLevel = (String) ranMap.get(k);
                            LOGGER.info("resourceSharingLevel {}", resourceSharingLevel);
                            sliceProfile.setResourceSharingLevel(resourceSharingLevel);
                            break;

                        case "maxNumberofUEs":
                            Integer maxNumberofUEs = (Integer) ranMap.get(k);
                            LOGGER.info("maxNumberofUEs {}", maxNumberofUEs);
                            sliceProfile.setMaxNumberOfUEs(maxNumberofUEs);
                            break;

                    }
                });
            }
        }
        return sliceProfile;
    }

    private String getSliceProfileIdFromReq(List<Map<String, Object>> sliceProfilesData)
            throws JsonProcessingException {
        Map<String, Object> mapParam = (Map<String, Object>) sliceProfilesData.get(0).get("nssi");

        List<Object> list = (ArrayList<Object>) mapParam.get("sliceProfileList");

        Map<String, Object> idMap = (Map<String, Object>) list.get(0);
        String sliceProfileId = (String) idMap.get("sliceProfileId");

        return sliceProfileId;
    }


}
