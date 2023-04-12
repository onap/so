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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aai.domain.yang.*;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.moi.util.AAISliceProfileUtil;
import org.onap.so.bpmn.moi.util.SliceProfileAaiToMoiMapperUtil;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.moi.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AssignRANNssiBBTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignRANNssiBBTasks.class);

    @Autowired
    private InjectionHelper injectionHelper;

    private ObjectMapper mapper = new ObjectMapper();

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    @Autowired
    AAISliceProfileUtil aaiSliceProfileUtil;

    @Autowired
    private SliceProfileAaiToMoiMapperUtil mapperUtil;

    public void createNssi(BuildingBlockExecution execution) throws Exception {


        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();

        ModelInfoServiceInstance modelInfoServiceInstance = gBB.getServiceInstance().getModelInfoServiceInstance();

        // for NON-SHARED check if its Already present
        if (checkNSSI(execution)) {
            if (aaiSliceProfileUtil.getServiceInstance(execution).isPresent()) {
                throw new RuntimeException("Invalid NSSI, Slice subnet already exists");
            }
        }
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setServiceInstanceName("ran_" + serviceInstanceId);
        serviceInstance.setOrchestrationStatus("Assigned");
        serviceInstance.setServiceType("nssi");
        serviceInstance.setModelInvariantId(modelInfoServiceInstance.getModelInvariantUuid());
        serviceInstance.setModelVersionId(modelInfoServiceInstance.getModelUuid());
        serviceInstance.setOperationalStatus("LOCKED");

        Customer customer = getCustomer(execution);

        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(serviceInstance.getServiceInstanceId()));

        injectionHelper.getAaiClient().createIfNotExists(serviceInstanceURI, Optional.of(serviceInstance));

    }

    private boolean checkNSSI(BuildingBlockExecution execution) {

        Optional<ServiceInstance> serviceInstance = aaiSliceProfileUtil.getServiceInstance(execution);


        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();

        List<Map<String, Object>> mapUserParams = gBB.getRequestContext().getRequestParameters().getUserParams();


        Attributes attributes = null;

        for (Map<String, Object> userParamData : mapUserParams) {
            if (userParamData.get("nssi") != null) {
                Map<String, Object> mapParam = (Map<String, Object>) userParamData.get("nssi");
                attributes = mapper.convertValue(mapParam, Attributes.class);
            }
        }
        if (attributes.getSliceProfileList().get(0).getRANSliceSubnetProfile().getResourceSharingLevel()
                .equalsIgnoreCase("NON-SHARED"))
            return true;
        else
            return false;
    }


    public void createSliceProfileInstance(BuildingBlockExecution execution) {
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();

        Customer customer = getCustomer(execution);

        List<Map<String, Object>> mapUserParams = gBB.getRequestContext().getRequestParameters().getUserParams();


        Attributes attributes = null;

        for (Map<String, Object> userParamData : mapUserParams) {
            if (userParamData.get("nssi") != null) {
                Map<String, Object> mapParam = (Map<String, Object>) userParamData.get("nssi");
                attributes = mapper.convertValue(mapParam, Attributes.class);
            }
        }
        // Create SliceProfile Instance
        ServiceInstance sliceProfileServiceInstance = new ServiceInstance();
        String sliceProfileInstanceId = UUID.randomUUID().toString();
        sliceProfileServiceInstance.setServiceInstanceId(sliceProfileInstanceId);
        sliceProfileServiceInstance.setServiceInstanceName("slice-profile-" + serviceInstanceId);
        sliceProfileServiceInstance.setServiceRole("slice-profile");

        sliceProfileServiceInstance =
                mapperUtil.fillSliceProfileInstanceFromMoiRequest(attributes, sliceProfileServiceInstance);

        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileServiceInstance.getServiceInstanceId()));

        injectionHelper.getAaiClient().createIfNotExists(serviceInstanceURI, Optional.of(sliceProfileServiceInstance));

        List<Map<String, Object>> sliceProfilesData = gBB.getRequestContext().getRequestParameters().getUserParams();

        // sliceProfile
        SliceProfile sliceProfile = mapperUtil.extractAaiSliceProfileFromMoiRequest(attributes);
        String sliceProfileId = UUID.randomUUID().toString();
        sliceProfile.setProfileId(sliceProfileId);

        AAIResourceUri sliceProfileURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileInstanceId).sliceProfile(sliceProfile.getProfileId()));

        injectionHelper.getAaiClient().createIfNotExists(sliceProfileURI, Optional.of(sliceProfile));

        execution.setVariable("sliceProfileServiceInstanceId", sliceProfileServiceInstance.getServiceInstanceId());
    }

    public void allotResources(BuildingBlockExecution execution) {

        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();

        String sliceProfileServiceInstanceId = execution.getVariable("sliceProfileServiceInstanceId");

        LOGGER.debug("sliceProfileServiceInstanceId: {}", sliceProfileServiceInstanceId);

        Customer customer = getCustomer(execution);

        org.onap.aai.domain.yang.v23.AllottedResource allottedResource =
                new org.onap.aai.domain.yang.v23.AllottedResource();

        UUID allottedResourceUuid = UUID.randomUUID();
        allottedResource.setId(allottedResourceUuid.toString());

        AAIResourceUri allotedResourceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileServiceInstanceId).allottedResource(allottedResource.getId()));

        injectionHelper.getAaiClient().createIfNotExists(allotedResourceURI, Optional.of(allottedResource));

        execution.setVariable("allottedResourceUuid", allottedResource.getId());

    }


    public void addSliceProfileToNssi(BuildingBlockExecution execution) {
        LOGGER.info("Entering into addSliceProfileToNssi");

        String sliceProfileServiceInstanceId = execution.getVariable("sliceProfileServiceInstanceId");
        String allottedResourceUuid = execution.getVariable("allottedResourceUuid");
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();
        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();

        Customer customer = getCustomer(execution);


        AAIResourceUri nssiUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(serviceInstanceId));

        AAIResourceUri allotedResourceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(sliceProfileServiceInstanceId).allottedResource(allottedResourceUuid));

        try {
            injectionHelper.getAaiClient().connect(allotedResourceURI, nssiUri);
        } catch (Exception e) {
            LOGGER.error(">>>>> Error in creating Relationship: {} ", e);
        }
    }

    public void activateNssi(BuildingBlockExecution execution) {
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();
        String serviceInstanceId = gBB.getServiceInstance().getServiceInstanceId();

        Customer customer = getCustomer(execution);

        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(
                                customer.getServiceSubscriptions().getServiceSubscription().get(0).getServiceType())
                        .serviceInstance(serviceInstanceId));

        Optional<ServiceInstance> serviceInstanceReturned =
                injectionHelper.getAaiClient().get(ServiceInstance.class, serviceInstanceURI);

        ServiceInstance serviceInstance = null;
        if (serviceInstanceReturned.isPresent()) {
            serviceInstance = serviceInstanceReturned.get();
            serviceInstance.setOperationalStatus("UNLOCKED");
            serviceInstance.setOrchestrationStatus("Active");

            try {
                injectionHelper.getAaiClient().update(serviceInstanceURI, serviceInstance);
            } catch (Exception e) {
                LOGGER.error("Nssi  couldnot be activated: {}", e);
            }
        } else {
            LOGGER.debug("Service Instance not present with Id: {}", serviceInstanceId);
        }

        // SliceProfile
        List<org.onap.so.moi.SliceProfile> sliceProfileList = new ArrayList<org.onap.so.moi.SliceProfile>();
        ServiceInstance serviceInstanceObj;
        List<Relationship> listOfNssiRelationship = serviceInstance.getRelationshipList().getRelationship();

        List<Relationship> listOfNssiRelationshipAR = listOfNssiRelationship.stream()
                .filter(relationship -> relationship.getRelatedTo().equalsIgnoreCase("allotted-resource"))
                .collect(Collectors.toList());

        for (Relationship relationship : listOfNssiRelationshipAR) {
            org.onap.so.moi.SliceProfile sliceProfile = new org.onap.so.moi.SliceProfile();
            for (RelationshipData relationshipData : relationship.getRelationshipData()) {
                if (relationshipData.getRelationshipKey().equalsIgnoreCase("service-instance.service-instance-id")) {
                    String sliceProfileInstanceId = relationshipData.getRelationshipValue();

                    Optional<ServiceInstance> sliceProfileServiceInstance =
                            aaiRestClient.getServiceInstanceById(sliceProfileInstanceId, "5G", "5GCustomer");
                    if (sliceProfileServiceInstance.isPresent()) {
                        ServiceInstance sliceProflieInstance = sliceProfileServiceInstance.get();
                        sliceProflieInstance.setOperationalStatus("UNLOCKED");
                        sliceProflieInstance.setOrchestrationStatus("ACTIVE");

                        AAIResourceUri sliceProfileInstanceURI = AAIUriFactory.createResourceUri(
                                AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                                        .serviceSubscription(customer.getServiceSubscriptions().getServiceSubscription()
                                                .get(0).getServiceType())
                                        .serviceInstance(sliceProflieInstance.getServiceInstanceId()));
                        try {
                            injectionHelper.getAaiClient().update(sliceProfileInstanceURI, sliceProflieInstance);
                        } catch (Exception e) {
                            LOGGER.error("SliceProfile couldnot be activated: {}", e);
                        }
                    } else {
                        LOGGER.debug("Slice Profile Instance not present with Id: {}", serviceInstanceId);
                    }


                }
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

}
