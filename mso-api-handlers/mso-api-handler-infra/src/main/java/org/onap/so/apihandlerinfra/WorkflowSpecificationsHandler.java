/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nordix
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
package org.onap.so.apihandlerinfra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.*;
import org.onap.so.db.catalog.beans.*;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;


@Path("onap/so/infra/workflowSpecifications")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/workflowSpecifications",
        description = "Queries of Workflow Specifications"))
@Component
public class WorkflowSpecificationsHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CatalogDbClient catalogDbClient;

    private static Logger logger = LoggerFactory.getLogger(WorkflowSpecificationsHandler.class);
    private static final String ARTIFACT_TYPE_WORKFLOW = "workflow";
    private static final String NATIVE_WORKFLOW = "native";
    private static final String EMPTY_BODY = "";

    @Path("/{version:[vV]1}/workflows")
    @GET
    @Operation(description = "Finds Workflow Specifications", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional

    public Response queryWorkflowSpecifications(@QueryParam("vnfModelVersionId") String vnfModelVersionId,
            @QueryParam("pnfModelVersionId") String pnfModelVersionId,
            @QueryParam("resourceTarget") String resourceTarget, @PathParam("version") String version)
            throws Exception {
        String apiVersion = version.substring(1);

        List<Workflow> workflows = new ArrayList<>();
        if (vnfModelVersionId == null && pnfModelVersionId == null && resourceTarget == null) {
            workflows.addAll(queryWorkflowSpecificationsForAll());
        } else {
            // 1. query workflow specifications for given vnfModelVersionId if need.
            if (vnfModelVersionId != null) {
                List<Workflow> vnfWorkflows = queryWorkflowSpecificationsForVnf(vnfModelVersionId);
                logger.debug("Retrieved " + vnfWorkflows.size() + " workflows for given vnfModelVersionId.");
                if (vnfWorkflows.size() > 0) {
                    workflows.addAll(vnfWorkflows);
                }
            }

            // 2. query workflow specifications for given pnfModelVersionId if need.
            if (pnfModelVersionId != null) {
                List<Workflow> pnfWorkflows = queryWorkflowSpecificationsForPnf(pnfModelVersionId);
                logger.debug("Retrieved " + pnfWorkflows.size() + " workflows for given pnfModelVerionId.");
                if (pnfWorkflows.size() > 0) {
                    workflows.addAll(pnfWorkflows);
                }
            }

            // 3. query workflow specifications for given resourceTarget
            if (resourceTarget != null) {
                List<Workflow> workflowsForResourceTarget = queryWorkflowsForResourceTarget(resourceTarget);
                logger.debug(
                        "Retrieved " + workflowsForResourceTarget.size() + " workflows for given resource target.");
                if (workflowsForResourceTarget.size() > 0) {
                    workflows.addAll(workflowsForResourceTarget);
                }
            }
        }

        // Deduplication
        List<Workflow> retWorkflows = workflows.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Workflow::getArtifactUUID))),
                        ArrayList::new));

        Optional<String> optional = getResponseByWorkflowSpec(retWorkflows);
        return builder.buildResponse(HttpStatus.SC_OK, "", optional.isPresent() ? optional.get() : EMPTY_BODY,
                apiVersion);
    }

    /**
     * @deprecated As of G release, workflows for all resource types (pnf,vnf,service) can be fetched using
     *             /workflowSpecifications/{version:[vV]1}/workflows?resourceTarget={resourceType} API
     */
    @Path("/{version:[vV]1}/pnfWorkflows")
    @GET
    @Operation(description = "Finds pnf workflow specifications", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    @Deprecated
    public Response getWorkflowsSpecForPnf(@PathParam("version") String version) throws Exception {

        final String pnf_resource = "pnf";
        String apiVersion = version.substring(1);

        List<Workflow> workflows = queryWorkflowsForResourceTarget(pnf_resource);

        Optional<String> optional = getResponseByWorkflowSpec(workflows);
        return builder.buildResponse(HttpStatus.SC_OK, "", optional.isPresent() ? optional.get() : EMPTY_BODY,
                apiVersion);
    }

    protected WorkflowSpecifications mapWorkflowsToWorkflowSpecifications(List<Workflow> workflows) {
        if (workflows == null || workflows.isEmpty()) {
            return null;
        }
        WorkflowSpecifications workflowSpecifications = new WorkflowSpecifications();
        List<WorkflowSpecificationList> workflowSpecificationList = new ArrayList<>();

        for (Workflow workflow : workflows) {
            WorkflowSpecificationList workflowSpecificationListItem = new WorkflowSpecificationList();
            WorkflowSpecification workflowSpecification = new WorkflowSpecification();
            workflowSpecification.setArtifactInfo(buildArtifactInfo(workflow));
            workflowSpecification.setActivitySequence(buildActivitySequence(workflow));
            workflowSpecification.setWorkflowInputParameters(buildWorkflowInputParameters(workflow));
            workflowSpecificationListItem.setWorkflowSpecification(workflowSpecification);
            workflowSpecificationList.add(workflowSpecificationListItem);
        }
        workflowSpecifications.setWorkflowSpecificationList(workflowSpecificationList);
        return workflowSpecifications;
    }

    private Optional<String> getResponseByWorkflowSpec(List<Workflow> workflows) throws ValidateException {
        WorkflowSpecifications workflowSpecifications = mapWorkflowsToWorkflowSpecifications(workflows);

        try {
            return Optional.of(mapper.writeValueAsString(workflowSpecifications));
        } catch (JsonProcessingException e) {
            catchAndThrowValidationEx(e);
        }
        return Optional.empty();
    }

    private Response catchAndThrowValidationEx(JsonProcessingException e) throws ValidateException {
        ErrorLoggerInfo errorLoggerInfo =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
        ValidateException validateException =
                new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                        HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo)
                                .build();
        throw validateException;
    }

    private ArtifactInfo buildArtifactInfo(Workflow workflow) {
        ArtifactInfo artifactInfo = new ArtifactInfo();
        artifactInfo.setArtifactType(ARTIFACT_TYPE_WORKFLOW);
        artifactInfo.setArtifactUuid(workflow.getArtifactUUID());
        artifactInfo.setArtifactName(workflow.getArtifactName());
        if (workflow.getVersion() != null) {
            artifactInfo.setArtifactVersion(workflow.getVersion().toString());
        }
        artifactInfo.setArtifactDescription(workflow.getDescription());
        artifactInfo.setWorkflowName(workflow.getName());
        artifactInfo.setOperationName(workflow.getOperationName());
        artifactInfo.setWorkflowSource(workflow.getSource());
        artifactInfo.setWorkflowResourceTarget(workflow.getResourceTarget());
        return artifactInfo;
    }

    private List<ActivitySequence> buildActivitySequence(Workflow workflow) {
        List<WorkflowActivitySpecSequence> workflowActivitySpecSequences = workflow.getWorkflowActivitySpecSequence();
        if (workflowActivitySpecSequences == null || workflowActivitySpecSequences.isEmpty()) {
            return null;
        }
        List<ActivitySequence> activitySequences = new ArrayList<>();
        for (WorkflowActivitySpecSequence workflowActivitySpecSequence : workflowActivitySpecSequences) {
            if (workflowActivitySpecSequence != null) {
                ActivitySpec activitySpec = workflowActivitySpecSequence.getActivitySpec();
                if (activitySpec != null) {
                    ActivitySequence activitySequence = new ActivitySequence();
                    activitySequence.setName(activitySpec.getName());
                    logger.debug("Adding activity: " + activitySpec.getName());
                    activitySequence.setDescription(activitySpec.getDescription());
                    activitySequences.add(activitySequence);
                }
            }
        }
        return activitySequences;
    }

    private List<WorkflowInputParameter> buildWorkflowInputParameters(Workflow workflow) {
        List<WorkflowActivitySpecSequence> workflowActivitySpecSequences = workflow.getWorkflowActivitySpecSequence();
        if (workflowActivitySpecSequences == null || workflowActivitySpecSequences.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, WorkflowInputParameter> workflowInputParameterMap = new HashMap<>();
        for (WorkflowActivitySpecSequence workflowActivitySpecSequence : workflowActivitySpecSequences) {
            if (workflowActivitySpecSequence != null) {
                ActivitySpec activitySpec = workflowActivitySpecSequence.getActivitySpec();
                if (activitySpec != null) {
                    List<ActivitySpecUserParameters> activitySpecUserParameters =
                            activitySpec.getActivitySpecUserParameters();
                    if (activitySpecUserParameters != null && !activitySpecUserParameters.isEmpty()) {
                        for (ActivitySpecUserParameters activitySpecUserParameter : activitySpecUserParameters) {
                            UserParameters userParameter = activitySpecUserParameter.getUserParameters();
                            if (userParameter != null) {
                                WorkflowInputParameter workflowInputParameter =
                                        buildWorkflowInputParameter(userParameter);
                                workflowInputParameterMap.put(userParameter.getName(), workflowInputParameter);
                            }
                        }
                    }
                }
            }
        }

        if (workflowInputParameterMap.size() == 0) {
            return new ArrayList<>();
        }
        List<WorkflowInputParameter> workflowInputParameterList =
                workflowInputParameterMap.values().stream().collect(Collectors.toList());
        return workflowInputParameterList;
    }

    private WorkflowInputParameter buildWorkflowInputParameter(UserParameters userParameter) {
        WorkflowInputParameter workflowInputParameter = new WorkflowInputParameter();
        workflowInputParameter.setLabel(userParameter.getLabel());
        workflowInputParameter.setInputType(userParameter.getType());
        workflowInputParameter.setRequired(userParameter.getIsRequried());
        workflowInputParameter.setSoFieldName(userParameter.getName());
        workflowInputParameter.setSoPayloadLocation(userParameter.getPayloadLocation());
        workflowInputParameter.setValidation(buildValidationList(userParameter));
        return workflowInputParameter;
    }

    private List<Validation> buildValidationList(UserParameters userParameter) {
        List<Validation> validationList = null;
        if (userParameter.getMaxLength() != null || userParameter.getAllowableChars() != null) {
            validationList = new ArrayList<>();
            Validation validation = new Validation();
            if (userParameter.getMaxLength() != null) {
                validation.setMaxLength(userParameter.getMaxLength().toString());
            }
            validation.setAllowableChars(userParameter.getAllowableChars());
            validationList.add(validation);
        }
        return validationList;
    }

    private List<Workflow> queryWorkflowSpecificationsForAll() {
        List<Workflow> workflows = catalogDbClient.findWorkflowBySource(NATIVE_WORKFLOW);
        return workflows;
    }

    private List<Workflow> queryWorkflowSpecificationsForVnf(String vnfModelVersionId) {
        List<Workflow> workflows = catalogDbClient.findWorkflowByVnfModelUUID(vnfModelVersionId);

        List<Workflow> nativeWorkflows = catalogDbClient.findWorkflowBySource(NATIVE_WORKFLOW);
        if (!nativeWorkflows.isEmpty()) {
            workflows.addAll(nativeWorkflows);
        }
        return workflows;
    }

    private List<Workflow> queryWorkflowSpecificationsForPnf(String pnfModelVersionId) {
        List<Workflow> workflows = catalogDbClient.findWorkflowByPnfModelUUID(pnfModelVersionId);
        return workflows;
    }

    private List<Workflow> queryWorkflowsForResourceTarget(String resourceTarget) {
        List<Workflow> workflows = catalogDbClient.findWorkflowByResourceTarget(resourceTarget);
        return workflows;
    }


}
