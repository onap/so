/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.ActivitySequence;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.ArtifactInfo;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.Validation;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowInputParameter;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowSpecification;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowSpecificationList;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowSpecifications;
import org.onap.so.db.catalog.beans.ActivitySpec;
import org.onap.so.db.catalog.beans.ActivitySpecUserParameters;
import org.onap.so.db.catalog.beans.UserParameters;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.WorkflowActivitySpecSequence;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("onap/so/infra/workflowSpecifications")
@Api(value="onap/so/infra/workflowSpecifications",description="Queries of Workflow Specifications")
@Component
public class WorkflowSpecificationsHandler {

    @Autowired
    private ResponseBuilder builder;
    
    @Autowired
    private CatalogDbClient catalogDbClient;
    
    private static final String ARTIFACT_TYPE_WORKFLOW = "workflow";
    
    @Path("/{version:[vV]1}/workflows")
    @GET
    @ApiOperation(value="Finds Workflow Specifications",response=Response.class)
    @Transactional
    
    public Response queryWorkflowSpecifications (@QueryParam("vnfModelVersionId") String vnfModelVersionId,
        @PathParam("version") String version) throws Exception {
                
        String apiVersion = version.substring(1);
        
        ObjectMapper mapper1 = new ObjectMapper();        
        mapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        List<Workflow> workflows = catalogDbClient.findWorkflowByModelUUID(vnfModelVersionId);        
        WorkflowSpecifications workflowSpecifications = mapWorkflowsToWorkflowSpecifications(workflows);
        
        String jsonResponse = null;
        try {
            ObjectMapper mapper = new ObjectMapper();            
            jsonResponse = mapper.writeValueAsString(workflowSpecifications);
        }
        catch (JsonProcessingException e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
            ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            throw validateException;
        }
        
        return builder.buildResponse(HttpStatus.SC_OK, "", jsonResponse, apiVersion);
    }
    
    protected WorkflowSpecifications mapWorkflowsToWorkflowSpecifications (List<Workflow> workflows) {
        if (workflows == null || workflows.size() == 0) {
            return null;
        }
        WorkflowSpecifications workflowSpecifications = new WorkflowSpecifications();
        List<WorkflowSpecificationList> workflowSpecificationList = new ArrayList<WorkflowSpecificationList>();      
        
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
        if (workflowActivitySpecSequences == null || workflowActivitySpecSequences.size() == 0) {
            return null;
        }        
        List<ActivitySequence> activitySequences = new ArrayList<ActivitySequence>();
        for (WorkflowActivitySpecSequence workflowActivitySpecSequence : workflowActivitySpecSequences) {
            if (workflowActivitySpecSequence != null) {
                ActivitySpec activitySpec = workflowActivitySpecSequence.getActivitySpec();
                if (activitySpec != null) {
                    ActivitySequence activitySequence = new ActivitySequence();
                    activitySequence.setName(activitySpec.getName());
                    activitySequence.setDescription(activitySpec.getDescription());                    
                    activitySequences.add(activitySequence);
                }
            }
        }        
        return activitySequences;
    }
    
    private List<WorkflowInputParameter> buildWorkflowInputParameters(Workflow workflow) {
        List<WorkflowActivitySpecSequence> workflowActivitySpecSequences = workflow.getWorkflowActivitySpecSequence();
        if (workflowActivitySpecSequences == null || workflowActivitySpecSequences.size() == 0) {
            return null;
        }
        Map<String, WorkflowInputParameter> workflowInputParameterMap = new HashMap<String, WorkflowInputParameter>();
        for (WorkflowActivitySpecSequence workflowActivitySpecSequence : workflowActivitySpecSequences) {
            if (workflowActivitySpecSequence != null) {
                ActivitySpec activitySpec = workflowActivitySpecSequence.getActivitySpec();
                if (activitySpec != null) {
                    List<ActivitySpecUserParameters> activitySpecUserParameters = activitySpec.getActivitySpecUserParameters();
                    if (activitySpecUserParameters != null && activitySpecUserParameters.size() != 0) {
                        for (ActivitySpecUserParameters activitySpecUserParameter : activitySpecUserParameters) {
                            UserParameters userParameter = activitySpecUserParameter.getUserParameters();
                            if (userParameter != null) {
                                WorkflowInputParameter workflowInputParameter = buildWorkflowInputParameter(userParameter);
                                workflowInputParameterMap.put(userParameter.getName(), workflowInputParameter);
                            }
                        }                   
                    }                   
                }
            }
        }
        
        if (workflowInputParameterMap.size() == 0) {
            return null;
        }
        List<WorkflowInputParameter> workflowInputParameterList = workflowInputParameterMap.values().stream()
                .collect(Collectors.toList());
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
            validationList = new ArrayList<Validation>();
            Validation validation = new Validation();
            if (userParameter.getMaxLength() != null) {
                validation.setMaxLength(userParameter.getMaxLength().toString());
            }
            validation.setAllowableChars(userParameter.getAllowableChars());
            validationList.add(validation);
          }
          return validationList;
    }
}
