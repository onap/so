package org.onap.so.bpmn.infrastructure.workflow.tasks.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;

public class UpgradePreWorkflowValidatorTest {

    @Mock
    private CatalogDbClient catalogDbClient;

    private UpgradePreWorkflowValidator validator;

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        validator = new UpgradePreWorkflowValidator(catalogDbClient);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldRunFor() {
        assertTrue(validator.shouldRunFor("upgradeInstance"));
        assertFalse(validator.shouldRunFor("createInstance"));
    }

    private BuildingBlockExecution createExecution(ServiceInstancesRequest sir, List<Resource> resourceList)
            throws JsonProcessingException {
        BuildingBlockExecution mock = Mockito.mock(BuildingBlockExecution.class);
        String jsonSir = objectMapper.writer().writeValueAsString(sir);
        when(mock.getVariable(BBConstants.G_BPMN_REQUEST)).thenReturn(jsonSir);
        when(mock.getVariable("resources")).thenReturn(resourceList);
        return mock;
    }

    @Test
    public void validateModelInvariantMismatch() throws JsonProcessingException {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setRequestDetails(new RequestDetails());
        sir.getRequestDetails().setModelInfo(new ModelInfo());
        sir.getRequestDetails().getModelInfo().setModelInvariantId(UUID.randomUUID().toString());

        Resource serviceResource = new Resource(WorkflowType.SERVICE, "", false, null);
        String aaiModelInvariantId = UUID.randomUUID().toString();
        serviceResource.setModelInvariantId(aaiModelInvariantId);

        BuildingBlockExecution execution = createExecution(sir, Arrays.asList(serviceResource));

        Optional<String> message = validator.validate(execution);

        assertTrue(message.isPresent());
        assertTrue(message.get().startsWith("Request service modelInvariantId"));
    }

    @Test
    public void validateNoVnfsInAAI() throws JsonProcessingException {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setRequestDetails(new RequestDetails());
        sir.getRequestDetails().setModelInfo(new ModelInfo());
        String modelInvariantId = UUID.randomUUID().toString();
        sir.getRequestDetails().getModelInfo().setModelInvariantId(modelInvariantId);

        Resource serviceResource = new Resource(WorkflowType.SERVICE, "", false, null);
        serviceResource.setModelInvariantId(modelInvariantId);

        BuildingBlockExecution execution = createExecution(sir, Arrays.asList(serviceResource));

        Optional<String> message = validator.validate(execution);

        assertThat(message).isEmpty();
    }

    @Test
    public void validateAAIVnfsNotSupported() throws JsonProcessingException {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setRequestDetails(new RequestDetails());
        sir.getRequestDetails().setModelInfo(new ModelInfo());
        sir.getRequestDetails().getModelInfo().setModelUuid(UUID.randomUUID().toString());
        String modelInvariantId = UUID.randomUUID().toString();
        sir.getRequestDetails().getModelInfo().setModelInvariantId(modelInvariantId);

        Resource serviceResource = new Resource(WorkflowType.SERVICE, "", false, null);
        serviceResource.setModelInvariantId(modelInvariantId);
        Resource vnfResource = new Resource(WorkflowType.VNF, "", false, serviceResource);
        vnfResource.setVnfCustomizationId(UUID.randomUUID().toString());

        Service service = new Service();
        VnfResourceCustomization vnfCustomization = new VnfResourceCustomization();
        vnfCustomization.setModelCustomizationUUID(UUID.randomUUID().toString());
        service.setVnfCustomizations(Arrays.asList(vnfCustomization));

        when(catalogDbClient.getServiceByModelUUID(anyString())).thenReturn(service);

        BuildingBlockExecution execution = createExecution(sir, Arrays.asList(serviceResource, vnfResource));

        Optional<String> message = validator.validate(execution);

        assertTrue(message.isPresent());
        assertTrue(message.get().startsWith("Existing vnfs in AAI are not supported by service model"));
    }

    @Test
    public void validateHappyCase() throws JsonProcessingException {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setRequestDetails(new RequestDetails());
        sir.getRequestDetails().setModelInfo(new ModelInfo());
        sir.getRequestDetails().getModelInfo().setModelUuid(UUID.randomUUID().toString());
        String modelInvariantId = UUID.randomUUID().toString();
        sir.getRequestDetails().getModelInfo().setModelInvariantId(modelInvariantId);

        Resource serviceResource = new Resource(WorkflowType.SERVICE, "", false, null);
        serviceResource.setModelInvariantId(modelInvariantId);
        Resource vnfResource = new Resource(WorkflowType.VNF, "", false, serviceResource);
        String vnfCustomiationId = UUID.randomUUID().toString();
        vnfResource.setVnfCustomizationId(vnfCustomiationId);

        Service service = new Service();
        VnfResourceCustomization vnfCustomization = new VnfResourceCustomization();
        vnfCustomization.setModelCustomizationUUID(vnfCustomiationId);
        service.setVnfCustomizations(Arrays.asList(vnfCustomization));

        when(catalogDbClient.getServiceByModelUUID(anyString())).thenReturn(service);

        BuildingBlockExecution execution = createExecution(sir, Arrays.asList(serviceResource, vnfResource));

        Optional<String> message = validator.validate(execution);

        assertFalse(message.isPresent());
    }

}
