package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.util.Map;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.DuplicateNameException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.MultipleObjectsFoundException;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceIdValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceIdValidator.class);

    private static final String SERVICE_INSTANCE = "serviceInstance";
    private static final String NAME_EXISTS_WITH_DIFF_VERSION_ID = "(%s) and different version id (%s)";
    private static final String WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI =
            "WorkflowAction was unable to verify if the instance name already exist in AAI.";
    private static final String NAME_EXISTS_MULTIPLE =
            "(%s) and multiple combination of model-version-id + service-type + global-customer-id";
    private static final String NAME_EXISTS_WITH_DIFF_COMBINATION =
            "(%s) and global-customer-id (%s), service-type (%s), model-version-id (%s)";
    private static final String NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID =
            "(%s), same parent and different customization id (%s)";
    private static final String NAME_EXISTS_WITH_DIFF_PARENT = "(%s) id (%s) and different parent relationship";


    private final BBInputSetupUtils bbInputSetupUtils;

    public ResourceIdValidator(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    protected String validateResourceIdInAAI(String generatedResourceId, WorkflowType type, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws Exception {
        try {
            if ("SERVICE".equalsIgnoreCase(type.toString())) {
                return validateServiceResourceIdInAAI(generatedResourceId, instanceName, reqDetails);
            } else if ("NETWORK".equalsIgnoreCase(type.toString())) {
                return validateNetworkResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("VNF".equalsIgnoreCase(type.toString())) {
                return validateVnfResourceIdInAAI(generatedResourceId, instanceName, reqDetails, workflowResourceIds);
            } else if ("VFMODULE".equalsIgnoreCase(type.toString())) {
                return validateVfModuleResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("VOLUMEGROUP".equalsIgnoreCase(type.toString())) {
                return validateVolumeGroupResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("CONFIGURATION".equalsIgnoreCase(type.toString())) {
                return validateConfigurationResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            }
            return generatedResourceId;
        } catch (DuplicateNameException dne) {
            throw dne;
        } catch (Exception ex) {
            LOGGER.error(WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI, ex);
            throw new IllegalStateException(
                    WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI);
        }
    }

    protected String validateServiceResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails) throws DuplicateNameException {
        String globalCustomerId = reqDetails.getSubscriberInfo().getGlobalSubscriberId();
        String serviceType = reqDetails.getRequestParameters().getSubscriptionServiceType();
        if (instanceName != null) {
            Optional<ServiceInstance> serviceInstanceAAI =
                    bbInputSetupUtils.getAAIServiceInstanceByName(globalCustomerId, serviceType, instanceName);
            if (serviceInstanceAAI.isPresent()) {
                if (serviceInstanceAAI.get().getModelVersionId()
                        .equalsIgnoreCase(reqDetails.getModelInfo().getModelVersionId())) {
                    return serviceInstanceAAI.get().getServiceInstanceId();
                } else {
                    throw new DuplicateNameException(SERVICE_INSTANCE, String.format(NAME_EXISTS_WITH_DIFF_VERSION_ID,
                            instanceName, reqDetails.getModelInfo().getModelVersionId()));
                }
            } else {
                ServiceInstances aaiServiceInstances =
                        bbInputSetupUtils.getAAIServiceInstancesGloballyByName(instanceName);
                if (aaiServiceInstances != null) {
                    if (aaiServiceInstances.getServiceInstance() != null
                            && !aaiServiceInstances.getServiceInstance().isEmpty()) {
                        if (aaiServiceInstances.getServiceInstance().size() > 1) {
                            throw new DuplicateNameException(SERVICE_INSTANCE,
                                    String.format(NAME_EXISTS_MULTIPLE, instanceName));
                        } else {
                            ServiceInstance si = aaiServiceInstances.getServiceInstance().stream().findFirst().get();
                            Map<String, String> keys =
                                    bbInputSetupUtils.getURIKeysFromServiceInstance(si.getServiceInstanceId());

                            throw new DuplicateNameException(SERVICE_INSTANCE, String.format(
                                    NAME_EXISTS_WITH_DIFF_COMBINATION, instanceName,
                                    keys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId),
                                    keys.get(
                                            AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType),
                                    si.getModelVersionId()));
                        }
                    }
                }
            }
        }
        return generatedResourceId;
    }

    protected String validateNetworkResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds)
            throws DuplicateNameException, MultipleObjectsFoundException {
        Optional<L3Network> network = bbInputSetupUtils
                .getRelatedNetworkByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
        if (network.isPresent()) {
            if (network.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return network.get().getNetworkId();
            } else {
                throw new DuplicateNameException("l3Network", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, network.get().getModelCustomizationId()));
            }
        }
        if (bbInputSetupUtils.existsAAINetworksGloballyByName(instanceName)) {
            throw new DuplicateNameException("l3Network", String.format(NAME_EXISTS_WITH_DIFF_PARENT, instanceName,
                    workflowResourceIds.getServiceInstanceId()));
        }
        return generatedResourceId;
    }

    protected String validateVnfResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<GenericVnf> vnf = bbInputSetupUtils
                .getRelatedVnfByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
        if (vnf.isPresent()) {
            if (vnf.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return vnf.get().getVnfId();
            } else {
                throw new DuplicateNameException("generic-vnf", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, vnf.get().getModelCustomizationId()));
            }
        }
        GenericVnfs vnfs = bbInputSetupUtils.getAAIVnfsGloballyByName(instanceName);
        if (vnfs != null) {
            throw new DuplicateNameException("generic-vnf",
                    String.format(NAME_EXISTS_WITH_DIFF_PARENT, instanceName, vnfs.getGenericVnf().get(0).getVnfId()));
        }
        return generatedResourceId;
    }

    protected String validateVfModuleResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
        if (vnf != null && vnf.getVfModules() != null) {
            for (org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()) {
                if (vfModule.getVfModuleName().equalsIgnoreCase(instanceName)) {
                    if (vfModule.getModelCustomizationId()
                            .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                        return vfModule.getVfModuleId();
                    } else {
                        throw new DuplicateNameException("vfModule",
                                String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID, instanceName,
                                        reqDetails.getModelInfo().getModelCustomizationId()));
                    }
                }
            }
        }
        if (bbInputSetupUtils.existsAAIVfModuleGloballyByName(instanceName)) {
            throw new DuplicateNameException("vfModule", instanceName);
        }
        return generatedResourceId;
    }

    protected String validateVolumeGroupResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<VolumeGroup> volumeGroup =
                bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(workflowResourceIds.getVnfId(), instanceName);
        if (volumeGroup.isPresent()) {
            if (volumeGroup.get().getVfModuleModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return volumeGroup.get().getVolumeGroupId();
            } else {
                throw new DuplicateNameException("volumeGroup", volumeGroup.get().getVolumeGroupName());
            }
        }
        if (bbInputSetupUtils.existsAAIVolumeGroupGloballyByName(instanceName)) {
            throw new DuplicateNameException("volumeGroup", instanceName);
        }
        return generatedResourceId;
    }

    protected String validateConfigurationResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<org.onap.aai.domain.yang.Configuration> configuration =
                bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance(
                        workflowResourceIds.getServiceInstanceId(), instanceName);
        if (configuration.isPresent()) {
            if (configuration.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return configuration.get().getConfigurationId();
            } else {
                throw new DuplicateNameException("configuration", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, configuration.get().getConfigurationId()));
            }
        }
        if (bbInputSetupUtils.existsAAIConfigurationGloballyByName(instanceName)) {
            throw new DuplicateNameException("configuration", instanceName);
        }
        return generatedResourceId;
    }
}
