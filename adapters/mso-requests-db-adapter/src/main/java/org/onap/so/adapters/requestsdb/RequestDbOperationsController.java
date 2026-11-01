package org.onap.so.adapters.requestsdb;

import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.client.RequestDbCommands;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/v1/request-db", produces = MediaType.APPLICATION_JSON_VALUE)
public class RequestDbOperationsController {

    private final RequestDbOperationsService operations;

    public RequestDbOperationsController(RequestDbOperationsService operations) {
        this.operations = operations;
    }

    @PostMapping(value = "/infra-requests/{requestId}/updates", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInfraRequest(@PathVariable String requestId,
            @RequestBody RequestDbCommands.InfraRequestUpdate command) throws MsoRequestsDbException {
        requireArgument(command.getLastModifiedBy());
        RequestStatusType status = parseStatus(command.getRequestStatus());
        operations.updateInfraRequest(requestId, command.getLastModifiedBy(), command.getStatusMessage(),
                command.getResponseBody(), status, command.getProgress(), command.getVnfOutputs(),
                command.getServiceInstanceId(), command.getNetworkId(), command.getVnfId(), command.getVfModuleId(),
                command.getVolumeGroupId(), command.getServiceInstanceName(), command.getConfigurationId(),
                command.getConfigurationName(), command.getVfModuleName());
    }

    @GetMapping("/infra-requests/{requestId}")
    public InfraActiveRequests getInfraRequest(@PathVariable String requestId) throws MsoRequestsDbException {
        return operations.getInfraRequest(requestId);
    }

    @PutMapping(value = "/instance-nfvo-mappings/{instanceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setInstanceNfvoMappingRepository(@PathVariable String instanceId,
            @RequestBody RequestDbCommands.InstanceNfvoMappingUpdate command) {
        operations.setInstanceNfvoMappingRepository(instanceId, command.getNfvoName(), command.getEndpoint(),
                command.getUsername(), command.getPassword(), command.getApiRoot());
    }

    @GetMapping("/instance-nfvo-mappings/{instanceId}")
    public InstanceNfvoMapping getInstanceNfvoMapping(@PathVariable String instanceId) {
        return requireEntity(operations.getInstanceNfvoMapping(instanceId));
    }

    @GetMapping("/sites/{siteName}/status")
    public RequestDbCommands.SiteState getSiteStatus(@PathVariable String siteName) {
        return new RequestDbCommands.SiteState(operations.getSiteStatus(siteName));
    }

    @GetMapping("/service-operations")
    public OperationStatus getServiceOperationStatus(@RequestParam String serviceId,
            @RequestParam(defaultValue = "") String operationId) throws MsoRequestsDbException {
        return requireEntity(operations.getServiceOperationStatus(serviceId, operationId));
    }

    @PostMapping(value = "/service-operations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void initServiceOperationStatus(@RequestBody RequestDbCommands.ServiceOperationWrite command)
            throws MsoRequestsDbException {
        requireArgument(command.getServiceId());
        operations.initServiceOperationStatus(command.getServiceId(), command.getOperationId(),
                command.getOperationType(), command.getUserId(), command.getResult(), command.getOperationContent(),
                command.getProgress(), command.getReason());
    }

    @PutMapping(value = "/service-operations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateServiceOperationStatus(@RequestParam String serviceId,
            @RequestParam(defaultValue = "") String operationId,
            @RequestBody RequestDbCommands.ServiceOperationWrite command) throws MsoRequestsDbException {
        operations.updateServiceOperationStatus(serviceId, operationId, command.getOperationType(), command.getUserId(),
                command.getResult(), command.getOperationContent(), command.getProgress(), command.getReason());
    }

    @PostMapping(value = "/resource-operations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void initResourceOperationStatus(@RequestBody RequestDbCommands.ResourceOperationInit command)
            throws MsoRequestsDbException {
        requireArgument(command.getServiceId());
        requireArgument(command.getOperationId());
        requireArgument(command.getOperationType());
        requireArgument(command.getResourceTemplateUUIDs());
        operations.initResourceOperationStatus(command.getServiceId(), command.getOperationId(),
                command.getOperationType(), command.getResourceTemplateUUIDs());
    }

    @GetMapping("/resource-operations")
    public ResourceOperationStatus getResourceOperationStatus(@RequestParam String serviceId,
            @RequestParam String operationId, @RequestParam String resourceTemplateUUID) throws MsoRequestsDbException {
        return operations.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
    }

    @PutMapping(value = "/resource-operations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResourceOperationStatus(@RequestParam String serviceId, @RequestParam String operationId,
            @RequestParam String resourceTemplateUUID, @RequestBody RequestDbCommands.ResourceOperationUpdate command)
            throws MsoRequestsDbException {
        operations.updateResourceOperationStatus(serviceId, operationId, resourceTemplateUUID, command.getOperType(),
                command.getResourceInstanceID(), command.getJobId(), command.getStatus(), command.getProgress(),
                command.getErrorCode(), command.getStatusDescription());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleFailure(Exception exception, HttpServletRequest request) {
        HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (exception instanceof ErrorResponse) {
            status = ((ErrorResponse) exception).getStatusCode();
        } else if (exception instanceof HttpMessageNotReadableException) {
            status = HttpStatus.BAD_REQUEST;
        }
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof RequestDbOperationsService.MissingEntityException) {
                status = HttpStatus.NOT_FOUND;
                break;
            }
        }
        String requestId = request.getHeader("X-ONAP-RequestID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        String message = status.value() == 404 ? "Entity not found"
                : status.is4xxClientError() ? "Invalid request" : "RequestDB operation failed";
        return ResponseEntity.status(status)
                .body(Map.of("code", "REQUEST_DB_" + status.value(), "message", message, "requestId", requestId));
    }

    private static void requireArgument(Object value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static RequestStatusType parseStatus(String value) {
        if (value == null) {
            return null;
        }
        try {
            return RequestStatusType.valueOf(value);
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static <T> T requireEntity(T entity) {
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return entity;
    }
}
