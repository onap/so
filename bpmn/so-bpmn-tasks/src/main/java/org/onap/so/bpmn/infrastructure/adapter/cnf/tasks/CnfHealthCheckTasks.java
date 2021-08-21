package org.onap.so.bpmn.infrastructure.adapter.cnf.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.client.adapter.cnf.entities.HealthCheckErrorResponse;
import org.onap.so.client.adapter.cnf.entities.HealthcheckInstance;
import org.onap.so.client.adapter.cnf.entities.HealthcheckInstanceRequest;
import org.onap.so.client.adapter.cnf.entities.HealthcheckInstanceResponse;
import org.onap.so.client.adapter.cnf.entities.HealthcheckResponse;
import org.onap.so.client.adapter.cnf.entities.HealthcheckResult;
import org.onap.so.client.adapter.cnf.entities.StatusCheckInstanceResponse;
import org.onap.so.client.adapter.cnf.entities.StatusCheckResponse;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CnfHealthCheckTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(CnfHealthCheckTasks.class);
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String HEALTH_CHECK_SCOPE = "health-check";
    private static final String STATUS_CHECK_SCOPE = "status-check";
    private static final String CNF_ADAPTER_MESSAGE_TYPE = "CNFCallback";
    private static final String CNF_ADAPTER_CALLBACK_TIMEOUT = "PT30M";

    @Autowired
    private ExceptionBuilder exceptionUtil;

    private ObjectMapper mapper = new ObjectMapper();

    public void prepareCnfAdaperRequest(BuildingBlockExecution execution) {
        GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
        ServiceInstance serviceInstance = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0);
        GenericVnf genericVnf = serviceInstance.getVnfs().get(0);
        List<VfModule> listOfVfModules = genericVnf.getVfModules();
        List<String> listOfHeatStackIds =
                listOfVfModules.stream().map(VfModule::getHeatStackId).collect(Collectors.toList());
        LOGGER.debug("listOfHeatStackIds: {}", listOfHeatStackIds);

        // Prepare values to pass in execution variable for CNF Adapter async Handling
        String requestId = execution.getVariable("mso-request-id");
        execution.setVariable("messageType", CNF_ADAPTER_MESSAGE_TYPE);
        execution.setVariable("correlator", requestId);
        execution.setVariable("timeout", CNF_ADAPTER_CALLBACK_TIMEOUT);
        // Replace with environment values
        String callBackUrl =
                "http://so-bpmn-infra.onap:8081/mso/WorkflowMessage/" + CNF_ADAPTER_MESSAGE_TYPE + "/" + requestId;
        HealthcheckInstanceRequest request = new HealthcheckInstanceRequest();
        try {
            request = createStatusCheckRequest(listOfHeatStackIds, callBackUrl);
        } catch (JsonProcessingException e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 6822, e);
        }
        LOGGER.debug("request: {}", request);

        String requestPayload = "";
        try {
            requestPayload = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error in JSON");
        }
        execution.setVariable("cnfRequestPayload", requestPayload);

        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();
        String action = Optional.ofNullable(buildingBlock.getBpmnAction()).orElseThrow(
                () -> new NullPointerException("BPMN Action is NULL in the orchestration_flow_reference table "));

        // Replace values with environment values
        String uri = "http://so-cnf-adapter:8090";
        String apiPath = "";

        if (STATUS_CHECK_SCOPE.equals(action)) {
            apiPath = uri + "/api/cnf-adapter/v1/statuscheck/";
        } else if (HEALTH_CHECK_SCOPE.equals(action)) {
            apiPath = uri + "/api/cnf-adapter/v1/healthcheck/";
        }

        LOGGER.debug("apiPath: {}", apiPath);

        execution.setVariable("apiPath", apiPath);
    }

    public void processAsyncResponse(BuildingBlockExecution execution) {
        // Value from CNF Async Handler activity
        String asyncResponse = execution.getVariable("asyncCallbackResponse");

        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();
        String action = Optional.ofNullable(buildingBlock.getBpmnAction()).orElseThrow(
                () -> new NullPointerException("BPMN Action is NULL in the orchestration_flow_reference table "));

        if (isAnErrorResponse(asyncResponse)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, asyncResponse, ONAPComponents.EXTERNAL);
        }

        if ((STATUS_CHECK_SCOPE.equals(action) && !isStatusCheckPositive(asyncResponse))
                || (HEALTH_CHECK_SCOPE.equals(action) && !isHealthCheckPositive(asyncResponse))) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, asyncResponse, ONAPComponents.SO);
        }
        execution.setVariable("StatusMessage", asyncResponse);
    }

    protected HealthcheckInstanceRequest createStatusCheckRequest(List<String> listOfHeatStackIds, String callBackUrl)
            throws JsonProcessingException {
        HealthcheckInstanceRequest healthcheckInstanceRequest = new HealthcheckInstanceRequest();
        List<HealthcheckInstance> listOfHealthcheckInstance = new ArrayList<>();

        listOfHeatStackIds.stream().forEach(x -> listOfHealthcheckInstance.add(new HealthcheckInstance(x)));
        LOGGER.debug("listOfHealthcheckInstance: {}", listOfHealthcheckInstance);

        healthcheckInstanceRequest.setInstances(listOfHealthcheckInstance);
        healthcheckInstanceRequest.setCallbackUrl(callBackUrl);
        LOGGER.debug("healthcheckInstanceRequest: {}", healthcheckInstanceRequest);

        return healthcheckInstanceRequest;
    }

    protected boolean isAnErrorResponse(String jsonStr) {
        try {
            mapper.readValue(jsonStr, HealthCheckErrorResponse.class);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    protected boolean isStatusCheckPositive(String asyncResponse) {
        StatusCheckResponse statusCheckResponse = new StatusCheckResponse();
        try {
            statusCheckResponse = mapper.readValue(asyncResponse, StatusCheckResponse.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error in parsing JSON response: {}", e);
        }

        List<StatusCheckInstanceResponse> listOfStatusInstanceResponse = statusCheckResponse.getInstanceResponse();

        for (StatusCheckInstanceResponse statusCheckInstanceResponse : listOfStatusInstanceResponse) {
            if (!statusCheckInstanceResponse.isStatus()) {
                return false;
            }
        }
        return true;
    }

    protected boolean isHealthCheckPositive(String asyncResponse) {
        HealthcheckResponse healthCheckResponse = new HealthcheckResponse();
        try {
            healthCheckResponse = mapper.readValue(asyncResponse, HealthcheckResponse.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error in parsing JSON: {}", e);
        }

        List<HealthcheckInstanceResponse> listOfHealthcheckInstanceResponses =
                healthCheckResponse.getInstanceResponse();

        for (HealthcheckInstanceResponse healthcheckInstanceResponse : listOfHealthcheckInstanceResponses) {
            String status = healthcheckInstanceResponse.getStatus();

            if (HealthcheckResult.FAILED == HealthcheckResult.fromString(status)
                    || HealthcheckResult.UNKNOWN == HealthcheckResult.fromString(status)) {
                return false;
            }
        }
        return true;
    }

}
