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


package org.onap.so.bpmn.moi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aai.domain.yang.*;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AAISliceProfileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AAISliceProfileUtil.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private InjectionHelper injectionHelper;

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();


    public Optional<ServiceInstance> getServiceInstance(BuildingBlockExecution execution) {
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();
        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();
        Customer customer = getCustomer(execution);

        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(serviceInstanceId));
        return injectionHelper.getAaiClient().get(ServiceInstance.class, serviceInstanceURI);
    }

    public void deleteSliceProfile(BuildingBlockExecution execution, String profileId) {
        Optional<ServiceInstance> getServiceInstance = getServiceInstance(execution);
        if (getServiceInstance.isPresent()) {
            ServiceInstance serviceInstance = getServiceInstance.get();
            String NssiId = serviceInstance.getServiceInstanceId();
            LOGGER.info("NSSID {}", NssiId);
            List<Relationship> listOfNssiRelationship = serviceInstance.getRelationshipList().getRelationship();

            List<Relationship> listOfNssiRelationshipAR = listOfNssiRelationship.stream()
                    .filter(relationship -> relationship.getRelatedTo().equalsIgnoreCase("allotted-resource"))
                    .collect(Collectors.toList());
            int size = listOfNssiRelationshipAR.size();

            List<SliceProfile> sliceProfileList;
            LOGGER.info("ProfileID from Request: {}", profileId);
            boolean isDeleted = false;
            for (Relationship relationship : listOfNssiRelationshipAR) {
                for (RelationshipData relationshipData : relationship.getRelationshipData()) {
                    if (relationshipData.getRelationshipKey()
                            .equalsIgnoreCase("service-instance.service-instance-id")) {
                        String sliceProfileInstanceId = relationshipData.getRelationshipValue();
                        LOGGER.debug(">>> sliceProfileInstance: {}", sliceProfileInstanceId);
                        Optional<ServiceInstance> sliceProfile1 = aaiRestClient
                                .getServiceInstanceByIdWithDepth(sliceProfileInstanceId, "5G", "5GCustomer");
                        if (sliceProfile1.isPresent()) {
                            sliceProfileList = sliceProfile1.get().getSliceProfiles().getSliceProfile();
                            LOGGER.info("sliceProfileList {}", sliceProfileList);
                            for (SliceProfile slice : sliceProfileList) {
                                if (slice.getProfileId().equalsIgnoreCase(profileId)) {
                                    LOGGER.info("ProfileID matched Deleting slice profile");
                                    deleteSliceProfileFromAAI(sliceProfileInstanceId, size, execution, NssiId);
                                    isDeleted = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isDeleted)
                        break;
                }
                if (isDeleted)
                    break;
            }
        }
    }

    public void updateSliceProfile(BuildingBlockExecution execution, String profileId, SliceProfile updatedSlice) {
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();
        List<Map<String, Object>> sliceProfilesData = gBB.getRequestContext().getRequestParameters().getUserParams();
        LOGGER.info(">>> mapParam: {}", sliceProfilesData);

        Optional<ServiceInstance> getServiceInstance = getServiceInstance(execution);
        if (getServiceInstance.isPresent()) {
            ServiceInstance serviceInstance = getServiceInstance.get();
            List<Relationship> listOfNssiRelationship = serviceInstance.getRelationshipList().getRelationship();

            List<Relationship> listOfNssiRelationshipAR = listOfNssiRelationship.stream()
                    .filter(relationship -> relationship.getRelatedTo().equalsIgnoreCase("allotted-resource"))
                    .collect(Collectors.toList());

            List<SliceProfile> sliceProfileList;
            LOGGER.info("ProfileID : {}", profileId);
            for (Relationship relationship : listOfNssiRelationshipAR) {
                for (RelationshipData relationshipData : relationship.getRelationshipData()) {
                    if (relationshipData.getRelationshipKey()
                            .equalsIgnoreCase("service-instance.service-instance-id")) {
                        String sliceProfileInstanceId = relationshipData.getRelationshipValue();
                        LOGGER.debug(">>> sliceProfileInstance: {}", sliceProfileInstanceId);

                        Optional<ServiceInstance> sliceProfile1 = aaiRestClient
                                .getServiceInstanceByIdWithDepth(sliceProfileInstanceId, "5G", "5GCustomer");

                        Optional<ServiceInstance> sliceProfileInstanceNodepth =
                                aaiRestClient.getServiceInstanceById(sliceProfileInstanceId, "5G", "5GCustomer");

                        if (sliceProfile1.isPresent()) {
                            sliceProfileList = sliceProfile1.get().getSliceProfiles().getSliceProfile();
                            int size = sliceProfileList.size();
                            ServiceInstance updatedSliceInstance = sliceProfileInstanceNodepth.get();

                            for (SliceProfile slice : sliceProfileList) {
                                if (slice.getProfileId().equalsIgnoreCase(profileId)) {
                                    LOGGER.info("Profile ID matched... updating slice profile");
                                    updateSliceProfileInAAI(execution, sliceProfileInstanceId, updatedSlice);

                                    // for update in administrativeState
                                    updatedSliceInstance =
                                            mapUserParamsToServiceInstance(updatedSliceInstance, sliceProfilesData);
                                    LOGGER.info(("Updating Slice-profile Instance"));
                                    updateSliceProfileInstance(execution, updatedSliceInstance);
                                }

                            }
                        }
                    }

                }
            }
        }
    }

    public void updateSliceProfileInAAI(BuildingBlockExecution execution, String sliceProfileInstanceId,
            SliceProfile sliceProfile1) {
        Customer customer = getCustomer(execution);
        AAIResourceUri updateSliceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileInstanceId).sliceProfile(sliceProfile1.getProfileId()));
        try {
            injectionHelper.getAaiClient().update(updateSliceURI, sliceProfile1);
        } catch (Exception e) {
            LOGGER.info("Error in updating Slice Profile {}", e);
        }
    }

    public void updateSliceProfileInstance(BuildingBlockExecution execution, ServiceInstance sliceProfileinstance) {
        Customer customer = getCustomer(execution);
        AAIResourceUri updateSliceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileinstance.getServiceInstanceId()));
        try {
            injectionHelper.getAaiClient().update(updateSliceURI, sliceProfileinstance);
        } catch (Exception e) {
            LOGGER.info("Error in updating Slice Profile instance {}", e);
        }
    }

    public void deleteSliceProfileFromAAI(String serviceInstanceID, int size, BuildingBlockExecution execution,
            String NssiId) {
        Customer customer = getCustomer(execution);

        AAIResourceUri deleteInstanceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(serviceInstanceID));
        try {
            injectionHelper.getAaiClient().delete(deleteInstanceURI);
            LOGGER.info("Slice Profile Instance with ID {} deleted", serviceInstanceID);
        } catch (Exception e) {
            LOGGER.info("Error in deleting Slice Profile instace {}", e);
        }
        LOGGER.info(">>>> Size : {}", size);
        if (size == 1) {
            AAIResourceUri serviceInstanceURI = AAIUriFactory
                    .createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                            .serviceSubscription(
                                    customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                            .serviceInstance(NssiId));
            // Delete NSSI
            try {
                injectionHelper.getAaiClient().delete(serviceInstanceURI);
                LOGGER.info("deleted Slice Prfile as well ass NSSI {}", NssiId);
            } catch (Exception e) {
                LOGGER.info("Error in deleting NSSI {}", e);
            }

        }

    }

    private Customer getCustomer(BuildingBlockExecution execution) {

        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String serviceType = gBB.getCustomer().getServiceSubscription().getServiceType();

        String globalCustomerId = gBB.getCustomer().getGlobalCustomerId();

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType(serviceType);

        ServiceSubscriptions serviceSubscriptions = new ServiceSubscriptions();
        serviceSubscriptions.getServiceSubscription().add(serviceSubscription);

        Customer customer = new Customer();
        customer.setGlobalCustomerId(globalCustomerId);
        customer.setServiceSubscriptions(serviceSubscriptions);

        return customer;

    }

    private ServiceInstance mapUserParamsToServiceInstance(ServiceInstance sliceProfileServiceInstanceObj,
            List<Map<String, Object>> sliceProfilesData) {
        Map<String, Object> mapParam = (Map<String, Object>) sliceProfilesData.get(0).get("nssi");
        LOGGER.info(">>> mapParam: {}", mapParam);

        // update administrative State
        String administrativeState = (String) mapParam.get("administrativeState");
        if (administrativeState != null) {
            LOGGER.info(">>> administrativeState: {}", administrativeState);
            sliceProfileServiceInstanceObj.setOperationalStatus(administrativeState);
        }

        String operationalState = (String) mapParam.get("operationalState");
        if (operationalState != null) {
            LOGGER.info(">>> operationalState: {}", operationalState);
            sliceProfileServiceInstanceObj.setOrchestrationStatus(operationalState);
        }
        List<Object> list = (ArrayList<Object>) mapParam.get("sliceProfileList");
        LOGGER.info(">>> sliceProfile List: {}", list);
        Map<String, Object> idMap = (Map<String, Object>) list.get(0);
        LOGGER.info("Keys of Id Map {} ", idMap.keySet());

        return sliceProfileServiceInstanceObj;
    }
}
