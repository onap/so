package org.onap.so.bpmn.infrastructure.workflow.tasks.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.validation.PreWorkflowValidator;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.stereotype.Component;

@Component
public class UpgradePreWorkflowValidator implements PreWorkflowValidator {

    private static final String ERR_MSG_INVARIANT_MISMATCH =
            "Request service modelInvariantId: %s does not match AAI service modelInvariantId: %s";
    private static final String ERR_MSG_EXISTING_VNFS_NOT_SUPPORTED =
            "Existing vnfs in AAI are not supported by service model. Unsupported vnfCustomizationIds: %s";

    private final CatalogDbClient catalogDbClient;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Function<WorkflowType, Predicate<Resource>> resourceType =
            workflowType -> resource -> resource.getResourceType() == workflowType;

    public UpgradePreWorkflowValidator(CatalogDbClient catalogDbClient) {
        this.catalogDbClient = catalogDbClient;
    }

    @Override
    public boolean shouldRunFor(String requestAction) {
        return "upgradeInstance".equalsIgnoreCase(requestAction);
    }

    @Override
    public Optional<String> validate(BuildingBlockExecution execution) {
        final String bpmnRequest = execution.getVariable(BBConstants.G_BPMN_REQUEST);
        List<Resource> resources = execution.getVariable("resources");

        Optional<ServiceInstancesRequest> sir = parseBpmnRequest(bpmnRequest);
        if (sir.isEmpty()) {
            return Optional.of("Failed to parse bpmnRequest");
        }
        var requestDetails = sir.get().getRequestDetails();
        String requestModelInvariantId = requestDetails.getModelInfo().getModelInvariantId();

        Optional<String> modelInvariantMismatch = validateInvariantId(resources, requestModelInvariantId);
        if (modelInvariantMismatch.isPresent()) {
            return modelInvariantMismatch;
        }

        List<Resource> aaiVnfResources = getVnfResources(resources);
        if (aaiVnfResources.isEmpty()) {
            return Optional.empty();
        }

        String serviceModelUuid = requestDetails.getModelInfo().getModelUuid();
        Optional<List<VnfResourceCustomization>> vnfResourceCustomizations =
                getVnfResourceCustomizations(serviceModelUuid);
        if (vnfResourceCustomizations.isEmpty()) {
            return Optional.of(String.format("Service model: %s does not exist in catalog db.", serviceModelUuid));
        }

        return validateExistingVnfsSupported(aaiVnfResources, vnfResourceCustomizations.get());
    }

    private Optional<ServiceInstancesRequest> parseBpmnRequest(String bpmnRequest) {
        try {
            return Optional.of(mapper.readValue(bpmnRequest, ServiceInstancesRequest.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<String> validateInvariantId(List<Resource> resources, String requestModelInvariantId) {
        return resources.stream().filter(resourceType.apply(WorkflowType.SERVICE)).findFirst()
                .filter(r -> !r.getModelInvariantId().equals(requestModelInvariantId))
                .map(r -> String.format(ERR_MSG_INVARIANT_MISMATCH, requestModelInvariantId, r.getModelInvariantId()));
    }

    private Optional<List<VnfResourceCustomization>> getVnfResourceCustomizations(String serviceModelUuid) {
        return Optional.ofNullable(catalogDbClient.getServiceByModelUUID(serviceModelUuid))
                .map(Service::getVnfCustomizations);
    }

    private List<Resource> getVnfResources(List<Resource> resources) {
        return resources.stream().filter(resourceType.apply(WorkflowType.VNF)).collect(Collectors.toList());
    }

    private Optional<String> validateExistingVnfsSupported(List<Resource> vnfResources,
            List<VnfResourceCustomization> vnfResourceCustomizations) {
        Set<String> modeledVnfCustomizationIds = vnfResourceCustomizations.stream()
                .map(VnfResourceCustomization::getModelCustomizationUUID).collect(Collectors.toSet());

        String unsupportedVnfCustomizationIds = vnfResources.stream().map(Resource::getVnfCustomizationId)
                .filter(id -> !modeledVnfCustomizationIds.contains(id)).collect(Collectors.joining(","));

        if (unsupportedVnfCustomizationIds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.format(ERR_MSG_EXISTING_VNFS_NOT_SUPPORTED, unsupportedVnfCustomizationIds));
    }

}
