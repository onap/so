/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.openstack.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.PluginAction;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduPlugin;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.cloud.authentication.KeystoneAuthHolder;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.CloudApiRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.CreateStackRequest;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.exceptions.MsoStackAlreadyExists;
import org.onap.so.openstack.exceptions.MsoTenantNotFound;
import org.onap.so.openstack.mappers.StackInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Events;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.heat.model.Stack.Output;
import com.woorea.openstack.heat.model.Stacks;


@Primary
@Component
public class MsoHeatUtils extends MsoCommonUtils implements VduPlugin {

    private static final String CREATE_FAILED = "CREATE_FAILED";

    private static final String DELETE_FAILED = "DELETE_FAILED";

    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";

    private static final String DELETE_COMPLETE = "DELETE_COMPLETE";

    private static final String DELETE_IN_PROGRESS = "DELETE_IN_PROGRESS";

    private static final String CREATE_IN_PROGRESS = "CREATE_IN_PROGRESS";

    protected static final String HEAT_ERROR = "HeatError";

    protected static final String CREATE_STACK = "CreateStack";
    public static final String FOUND = "Found: {}";
    public static final String EXCEPTION_ROLLING_BACK_STACK =
            "{} Create Stack: Nested exception rolling back stack: {} ";
    public static final String IN_PROGRESS = "in_progress";
    private static final int DEFAULT_POLLING_TIMEOUT = 118;

    @Autowired
    private Environment environment;

    @Autowired
    StackStatusHandler statusHandler;

    @Autowired
    NovaClientImpl novaClient;

    @Autowired
    RequestsDbClient requestDBClient;

    @Autowired
    private CatalogDbClient catalogClient;

    private static final Logger logger = LoggerFactory.getLogger(MsoHeatUtils.class);

    // Properties names and variables (with default values)
    protected String createPollIntervalProp = "org.onap.so.adapters.po.pollInterval";

    protected static final String CREATE_POLL_INTERVAL_DEFAULT = "15";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();


    /**
     * Create a new Stack in the specified cloud location and tenant. The Heat template and parameter map are passed in
     * as arguments, along with the cloud access credentials. It is expected that parameters have been validated and
     * contain at minimum the required parameters for the given template with no extra (undefined) parameters..
     *
     * The Stack name supplied by the caller must be unique in the scope of this tenant. However, it should also be
     * globally unique, as it will be the identifier for the resource going forward in Inventory. This latter is managed
     * by the higher levels invoking this function.
     *
     * The caller may choose to let this function poll Openstack for completion of the stack creation, or may handle
     * polling itself via separate calls to query the status. In either case, a StackInfo object will be returned
     * containing the current status. When polling is enabled, a status of CREATED is expected. When not polling, a
     * status of BUILDING is expected.
     *
     * An error will be thrown if the requested Stack already exists in the specified Tenant and Cloud.
     *
     * For 1510 - add "environment", "files" (nested templates), and "heatFiles" (get_files) as parameters for
     * createStack. If environment is non-null, it will be added to the stack. The nested templates and get_file entries
     * both end up being added to the "files" on the stack. We must combine them before we add them to the stack if
     * they're both non-null.
     *
     * @param cloudSiteId The cloud (may be a region) in which to create the stack.
     * @param cloudOwner the cloud owner of the cloud site in which to create the stack
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param stackName The name of the stack to create
     * @param vduModel contains information about the vdu model (added for plugin adapter)
     * @param heatTemplate The Heat template
     * @param stackInputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param environment An optional yaml-format string to specify environmental parameters
     * @param files a Map<String, Object> that lists the child template IDs (file is the string, object is an int of
     *        Template id)
     * @param heatFiles a Map<String, Object> that lists the get_file entries (fileName, fileBody)
     * @param backout Donot delete stack on create Failure - defaulted to True
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */

    public StackInfo createStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName,
            VduModelInfo vduModel, String heatTemplate, Map<String, ?> stackInputs, boolean pollForCompletion,
            int timeoutMinutes, String environment, Map<String, Object> nestedTemplates, Map<String, Object> heatFiles,
            boolean backout, boolean failIfExists) throws MsoException {
        stripMultiCloudInputs(stackInputs);
        CreateStackParam createStack = createStackParam(stackName, heatTemplate, stackInputs, timeoutMinutes,
                environment, nestedTemplates, heatFiles);
        Stack currentStack = queryHeatStack(stackName, cloudSiteId, tenantId);
        boolean operationPerformed = false;
        if (currentStack != null) {
            logger.debug("Existing Stack found with Status: {} ", currentStack.getStackStatus());
            if (CREATE_COMPLETE.equals(currentStack.getStackStatus())) {
                new StackInfoMapper(currentStack).map();
            } else if (CREATE_IN_PROGRESS.equals(currentStack.getStackStatus())) {
                // TODO should check poll for completion right here
                currentStack = processCreateStack(cloudSiteId, tenantId, timeoutMinutes, backout, currentStack,
                        createStack, true);
            } else if (CREATE_FAILED.equals(currentStack.getStackStatus())
                    || DELETE_FAILED.equals(currentStack.getStackStatus())) {
                try {
                    if (pollForCompletion) {
                        processCreateStack(cloudSiteId, tenantId, timeoutMinutes, backout, currentStack, createStack,
                                true);
                    }
                } catch (MsoException e) {
                    if (e instanceof StackCreationException) {
                        logger.warn("Error during Stack will attempt to recreate stack");
                        currentStack = createStack(createStack, cloudSiteId, tenantId);
                        currentStack.setStackName(stackName);
                        if (pollForCompletion) {
                            currentStack = processCreateStack(cloudSiteId, tenantId, timeoutMinutes, backout,
                                    currentStack, createStack, true);
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            currentStack = createStack(createStack, cloudSiteId, tenantId);
            currentStack.setStackName(stackName);
            if (pollForCompletion) {
                currentStack = processCreateStack(cloudSiteId, tenantId, timeoutMinutes, backout, currentStack,
                        createStack, true);
            }
            operationPerformed = true;
        }
        StackInfo stackInfo = new StackInfoMapper(currentStack).map();
        stackInfo.setOperationPerformed(operationPerformed);
        return stackInfo;
    }

    /**
     * @param stackInputs
     */
    protected void stripMultiCloudInputs(Map<String, ?> stackInputs) {
        // Take out the multicloud inputs, if present.
        for (String key : MsoMulticloudUtils.MULTICLOUD_INPUTS) {
            if (stackInputs.containsKey(key)) {
                stackInputs.remove(key);
                if (stackInputs.isEmpty()) {
                    break;
                }
            }
        }
    }

    protected Stack createStack(CreateStackParam stack, String cloudSiteId, String tenantId) throws MsoException {
        try {
            OpenStackRequest<Stack> request = getHeatClient(cloudSiteId, tenantId).getStacks().create(stack);
            saveStackRequest(stack, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID), stack.getStackName());
            return executeAndRecordOpenstackRequest(request);
        } catch (OpenStackResponseException e) {
            if (e.getStatus() == 409) {
                MsoStackAlreadyExists me = new MsoStackAlreadyExists(stack.getStackName(), tenantId, cloudSiteId);
                me.addContext(CREATE_STACK);
                throw me;
            } else {
                logger.error("ERROR STATUS = {},\n{}\n{}", e.getStatus(), e.getMessage(), e.getLocalizedMessage());
                throw heatExceptionToMsoException(e, CREATE_STACK);
            }
        } catch (OpenStackConnectException e) {
            throw heatExceptionToMsoException(e, CREATE_STACK);
        } catch (RuntimeException e) {
            throw runtimeExceptionToMsoException(e, CREATE_STACK);
        }
    }


    protected Stack processCreateStack(String cloudSiteId, String tenantId, int timeoutMinutes, boolean backout,
            Stack heatStack, CreateStackParam stackCreate, boolean keyPairCleanUp) throws MsoException {
        Stack latestStack = null;
        try {

            latestStack =
                    pollStackForStatus(timeoutMinutes, heatStack, CREATE_IN_PROGRESS, cloudSiteId, tenantId, false);
        } catch (MsoException me) {
            logger.error("Exception in Create Stack", me);
        }
        return postProcessStackCreate(latestStack, backout, timeoutMinutes, keyPairCleanUp, cloudSiteId, tenantId,
                stackCreate);
    }

    public Stack postProcessStackCreate(Stack stack, boolean backout, int timeoutMinutes, boolean cleanUpKeyPair,
            String cloudSiteId, String tenantId, CreateStackParam stackCreate) throws MsoException {
        boolean stackCreationFailed = false;
        boolean stackRollbackFailed = false;
        if (stack == null) {
            throw new StackCreationException("Unknown Error in Stack Creation");
        } else {
            logger.info("Performing post processing backout: {} cleanUpKeyPair: {}, stack {}", backout, cleanUpKeyPair,
                    stack);
            if (!CREATE_COMPLETE.equals(stack.getStackStatus())) {
                if (cleanUpKeyPair && !Strings.isNullOrEmpty(stack.getStackStatusReason())
                        && isKeyPairFailure(stack.getStackStatusReason())) {
                    return handleKeyPairConflict(cloudSiteId, tenantId, stackCreate, timeoutMinutes, backout, stack);
                }
                if (!backout) {
                    logger.info("Status is not CREATE_COMPLETE, stack deletion suppressed");
                    stackCreationFailed = true;
                    String errorMessage =
                            "Stack Creation Failed Openstack Status: " + stack.getStackStatus() + " Status Reason: "
                                    + stack.getStackStatusReason() + ". Stack rollback suppressed, stack not deleted";
                    throw new StackCreationException(errorMessage, stackCreationFailed, stackRollbackFailed);
                } else {
                    logger.info("Status is not CREATE_COMPLETE, stack deletion will be executed");
                    stackCreationFailed = true;
                    String errorMessage = "Stack Creation Failed Openstack Status: " + stack.getStackStatus()
                            + " Status Reason: " + stack.getStackStatusReason();
                    try {
                        Stack deletedStack =
                                handleUnknownCreateStackFailure(stack, timeoutMinutes, cloudSiteId, tenantId);
                        errorMessage = errorMessage + " , Rollback of Stack Creation completed with status: "
                                + deletedStack.getStackStatus() + " Status Reason: "
                                + deletedStack.getStackStatusReason();
                    } catch (MsoException e) {
                        stackRollbackFailed = true;
                        logger.error("Sync Error Deleting Stack during rollback", e);
                        if (e instanceof StackRollbackException) {
                            errorMessage = errorMessage + e.getMessage();
                        } else {
                            errorMessage = errorMessage + " , Rollback of Stack Creation failed with sync error: "
                                    + e.getMessage();
                        }
                    }
                    throw new StackCreationException(errorMessage, stackCreationFailed, stackRollbackFailed);
                }
            } else {
                return stack;
            }
        }
    }

    public Stack postProcessStackUpdate(Stack updateStack) throws MsoOpenstackException {
        if (!"UPDATE_COMPLETE".equals(updateStack.getStackStatus())) {
            logger.error("{} Stack status: {} Stack status reason: {} {} Update Stack error",
                    MessageEnum.RA_UPDATE_STACK_ERR, updateStack.getStackStatus(), updateStack.getStackStatusReason(),
                    ErrorCode.DataError.getValue());

            MsoOpenstackException me = null;
            if ("UPDATE_IN_PROGRESS".equals(updateStack.getStackStatus())) {
                me = new MsoOpenstackException(0, "", "Stack Update Timeout");
            } else {
                String error =
                        "Stack error (" + updateStack.getStackStatus() + "): " + updateStack.getStackStatusReason();
                me = new MsoOpenstackException(0, "", error);
            }
            me.addContext("UpdateStack");
            throw me;
        } else {
            return updateStack;
        }
    }

    public Stack pollStackForStatus(int timeoutMinutes, Stack stack, String stackStatus, String cloudSiteId,
            String tenantId, boolean notFoundIsSuccess) throws MsoException {
        int pollingFrequency =
                Integer.parseInt(this.environment.getProperty(createPollIntervalProp, CREATE_POLL_INTERVAL_DEFAULT));
        LocalDateTime stopPolling = LocalDateTime.now().plusMinutes(timeoutMinutes);
        if (pollingFrequency > timeoutMinutes * 60) {
            logger.debug("Will not poll. Poll interval {} sec is greater then timeout {} sec", pollingFrequency,
                    timeoutMinutes * 60);
            stopPolling = LocalDateTime.now().minusMinutes(1);
        }
        Heat heatClient = getHeatClient(cloudSiteId, tenantId);
        while (true) {
            String stackName = stack.getStackName() + "/" + stack.getId();
            if (stack.getId() == null) {
                stackName = stack.getStackName();
            }
            Stack latestStack = queryHeatStack(heatClient, stackName);
            if (latestStack == null && notFoundIsSuccess) {
                return null;
            } else if (latestStack != null) {
                String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
                statusHandler.updateStackStatus(latestStack, requestId);
                if (stackStatus.equals(latestStack.getStackStatus())) {
                    if (LocalDateTime.now().isAfter(stopPolling)) {
                        logger.error("Polling of stack timed out with Status: {}", latestStack.getStackStatus());
                        return latestStack;
                    }
                    logger.debug("Will poll again until {}", stopPolling);
                    sleep(pollingFrequency * 1000L);
                } else {
                    return latestStack;
                }
            }
        }
    }

    protected void saveStackRequest(CreateStackParam request, String requestId, String stackName) {
        try {
            InfraActiveRequests foundRequest = requestDBClient.getInfraActiveRequestbyRequestId(requestId);
            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setEnvironment(request.getEnvironment());
            createStackRequest.setParameters(request.getParameters());
            String stackRequest = JSON_MAPPER.writeValueAsString(createStackRequest);
            CloudApiRequests cloudReq = new CloudApiRequests();
            cloudReq.setCloudIdentifier(stackName);
            cloudReq.setRequestBody(stackRequest);
            cloudReq.setRequestId(requestId);
            CloudApiRequests foundCloudReq = foundRequest.getCloudApiRequests().stream()
                    .filter(cloudReqToFind -> stackName.equals(cloudReq.getCloudIdentifier())).findAny().orElse(null);
            if (foundCloudReq != null) {
                foundCloudReq.setRequestBody(stackRequest);
            } else {
                foundRequest.getCloudApiRequests().add(cloudReq);
            }
            requestDBClient.updateInfraActiveRequests(foundRequest);
        } catch (Exception e) {
            logger.error("Error updating in flight request with Openstack Create Request", e);
        }
    }

    protected boolean isKeyPairFailure(String errorMessage) {
        return Pattern.compile(".*Key pair.*already exists.*").matcher(errorMessage).matches();
    }

    protected Stack handleUnknownCreateStackFailure(Stack stack, int timeoutMinutes, String cloudSiteId,
            String tenantId) throws MsoException {
        if (stack != null && !Strings.isNullOrEmpty(stack.getStackName()) && !Strings.isNullOrEmpty(stack.getId())) {
            return deleteStack(stack, timeoutMinutes, cloudSiteId, tenantId, false);
        } else {
            throw new StackCreationException("Cannot Find Stack Name or Id");
        }
    }

    private Stack deleteStack(Stack stack, int timeoutMinutes, String cloudSiteId, String tenantId,
            boolean pollForCompletion) throws MsoException {
        OpenStackRequest<Void> request = getHeatClient(cloudSiteId, tenantId).getStacks()
                .deleteByName(stack.getStackName() + "/" + stack.getId());
        executeAndRecordOpenstackRequest(request);
        logger.debug("Completed Executing executeAndRecordOpenstackRequest");
        if (pollForCompletion == true) {
            Stack currentStack =
                    pollStackForStatus(timeoutMinutes, stack, DELETE_IN_PROGRESS, cloudSiteId, tenantId, true);
            if (currentStack == null) {
                return currentStack;
            }
            postProcessStackDelete(currentStack);
            return currentStack;
        } else {
            logger.debug("Returning the stack");
            return stack;
        }
    }

    public void postProcessStackDelete(Stack stack) throws MsoException {
        logger.info("Performing post processing on delete stack {}", stack);
        if (stack != null && !Strings.isNullOrEmpty(stack.getStackStatus())) {
            if (!DELETE_COMPLETE.equals(stack.getStackStatus()))
                throw new StackRollbackException("Stack Deletion completed with status: " + stack.getStackStatus()
                        + " Status Reason: " + stack.getStackStatusReason());
        } else {
            throw new StackRollbackException("Cannot Find Stack Name or Id");
        }
    }

    protected Stack handleKeyPairConflict(String cloudSiteId, String tenantId, CreateStackParam stackCreate,
            int timeoutMinutes, boolean backout, Stack stack) throws MsoException {
        logger.info("Keypair conflict found on stack, attempting to clean up");
        try {
            Matcher m = Pattern.compile("'([^']+?)'").matcher(stack.getStackStatusReason());
            if (m.find()) {
                novaClient.deleteKeyPair(cloudSiteId, tenantId, m.group(1));
            }
        } catch (NovaClientException e) {
            logger.warn("Could not delete keypair", e);
        }

        handleUnknownCreateStackFailure(stack, timeoutMinutes, cloudSiteId, tenantId);
        Stack newStack = createStack(stackCreate, cloudSiteId, tenantId);
        newStack.setStackName(stackCreate.getStackName());
        return processCreateStack(cloudSiteId, tenantId, timeoutMinutes, backout, newStack, stackCreate, false);
    }

    /**
     * Query for a single stack (by Name) in a tenant. This call will always return a StackInfo object. If the stack
     * does not exist, an "empty" StackInfo will be returned - containing only the stack name and a status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @param cloudOwner the cloud owner of the cloud site in which to query
     * @param stackName The name of the stack to query (may be simple or canonical)
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    public StackInfo queryStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName)
            throws MsoException {
        logger.debug("Query HEAT stack: {} in tenant {}", stackName, tenantId);
        Heat heatClient = null;
        try {
            heatClient = getHeatClient(cloudSiteId, tenantId);
        } catch (MsoTenantNotFound e) {
            // Tenant doesn't exist, so stack doesn't either
            logger.debug("Tenant with id " + tenantId + "not found.", e);
            return new StackInfo(stackName, HeatStatus.NOTFOUND);
        } catch (MsoException me) {
            // Got an Openstack error. Propagate it
            logger.error("{} {} Openstack Exception on Token request: ", MessageEnum.RA_CONNECTION_EXCEPTION,
                    ErrorCode.AvailabilityError.getValue(), me);
            me.addContext("QueryStack");
            throw me;
        }

        // Query the Stack.
        // An MsoException will propagate transparently to the caller.
        Stack heatStack = queryHeatStack(heatClient, stackName);
        if (heatStack == null) {
            // Stack does not exist. Return a StackInfo with status NOTFOUND
            return new StackInfo(stackName, HeatStatus.NOTFOUND);
        }

        return new StackInfoMapper(heatStack).map();
    }

    /**
     * Delete a stack (by Name/ID) in a tenant. If the stack is not found, it will be considered a successful deletion.
     * The return value is a StackInfo object which contains the current stack status.
     *
     * The client may choose to let the adapter poll Openstack for completion of the stack deletion, or may handle
     * polling itself via separate query calls. In either case, a StackInfo object will be returned. When polling is
     * enabled, a final status of NOTFOUND is expected. When not polling, a status of DELETING is expected.
     *
     * There is no rollback from a successful stack deletion. A deletion failure will also result in an undefined stack
     * state - the components may or may not have been all or partially deleted, so the resulting stack must be
     * considered invalid.
     *
     * @param tenantId The Openstack ID of the tenant in which to perform the delete
     * @param cloudOwner the cloud owner of the cloud site in which to delete the stack
     * @param cloudSiteId The cloud identifier (may be a region) from which to delete the stack.
     * @param stackName The name/id of the stack to delete. May be simple or canonical
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public StackInfo deleteStack(String tenantId, String cloudOwner, String cloudSiteId, String stackName,
            boolean pollForCompletion, int timeoutMinutes) throws MsoException {
        Stack currentStack = queryHeatStack(stackName, cloudSiteId, tenantId);
        StackInfo stackInfo = null;
        if (currentStack == null || DELETE_COMPLETE.equals(currentStack.getStackStatus())) {
            stackInfo = new StackInfo(stackName, HeatStatus.NOTFOUND);
            stackInfo.setOperationPerformed(false);
        } else {
            currentStack = deleteStack(currentStack, timeoutMinutes, cloudSiteId, tenantId, pollForCompletion);
            stackInfo = new StackInfoMapper(currentStack).map();
            stackInfo.setName(stackName);
            stackInfo.setOperationPerformed(true);
            if (currentStack != null) {
                stackInfo.setCanonicalName(currentStack.getStackName() + "/" + currentStack.getId());
            }
        }
        return stackInfo;
    }

    /**
     * Validate parameters to be passed to Heat template. This method performs three functions: 1. Apply default values
     * to parameters which have them defined 2. Report any required parameters that are missing. This will generate an
     * exception in the caller, since stack create/update operations would fail. 3. Report and remove any extraneous
     * parameters. This will allow clients to pass supersets of parameters and not get errors.
     *
     * These functions depend on the HeatTemplate definition from the MSO Catalog DB, along with the input parameter
     * Map. The output is an updated parameter map. If the parameters are invalid for the template, an
     * IllegalArgumentException is thrown.
     */
    public Map<String, Object> validateStackParams(Map<String, Object> inputParams, HeatTemplate heatTemplate) {
        // Check that required parameters have been supplied for this template type
        StringBuilder missingParams = null;
        List<String> paramList = new ArrayList<>();

        // TODO: Enhance DB to support defaults for Heat Template parameters

        for (HeatTemplateParam parm : heatTemplate.getParameters()) {
            if (parm.isRequired() && !inputParams.containsKey(parm.getParamName())) {
                if (missingParams == null) {
                    missingParams = new StringBuilder(parm.getParamName());
                } else {
                    missingParams.append("," + parm.getParamName());
                }
            }
            paramList.add(parm.getParamName());
        }
        if (missingParams != null) {
            // Problem - missing one or more required parameters
            String error = "Missing Required inputs for HEAT Template: " + missingParams;
            logger.error("{} for HEAT Template {} Missing Required inputs for HEAT Template: {}",
                    MessageEnum.RA_MISSING_PARAM, ErrorCode.SchemaError.getValue(), missingParams);
            throw new IllegalArgumentException(error);
        }

        // Remove any extraneous parameters (don't throw an error)
        Map<String, Object> updatedParams = new HashMap<>();
        List<String> extraParams = new ArrayList<>();

        for (Entry<String, Object> entry : inputParams.entrySet()) {
            if (!paramList.contains(entry.getKey())) {
                // This is not a valid parameter for this template
                extraParams.add(entry.getKey());
            } else {
                updatedParams.put(entry.getKey(), entry.getValue());
            }
        }

        if (!extraParams.isEmpty()) {
            logger.warn("{} Heat Stack ({}) extra input params received: {} {}", MessageEnum.RA_GENERAL_WARNING,
                    heatTemplate.getTemplateName(), extraParams, ErrorCode.DataError.getValue());
        }

        return updatedParams;
    }


    /**
     * Get a Heat client for the Openstack Identity service. This requires a 'member'-level userId + password, which
     * will be retrieved from properties based on the specified cloud Id. The tenant in which to operate must also be
     * provided.
     * <p>
     * On successful authentication, the Heat object will be cached for the tenantID + cloudId so that it can be reused
     * without reauthenticating with Openstack every time.
     *
     * @return an authenticated Heat object
     */
    public Heat getHeatClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "orchestration");
        Heat heatClient = new Heat(keystone.getServiceUrl());
        heatClient.token(keystone.getId());
        return heatClient;
    }

    /*
     * Query for a Heat Stack. This function is needed in several places, so a common method is useful. This method
     * takes an authenticated Heat Client (which internally identifies the cloud & tenant to search), and returns a
     * Stack object if found, Null if not found, or an MsoOpenstackException if the Openstack API call fails.
     *
     * The stack name may be a simple name or a canonical name ("{name}/{id}"). When simple name is used, Openstack
     * always returns a 302 redirect which results in a 2nd request (to the canonical name). Note that query by
     * canonical name for a deleted stack returns a Stack object with status "DELETE_COMPLETE" while query by simple
     * name for a deleted stack returns HTTP 404.
     *
     * @param heatClient an authenticated Heat client
     *
     * @param stackName the stack name to query
     *
     * @return a Stack object that describes the current stack or null if the requested stack doesn't exist.
     *
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
     */
    public Stack queryHeatStack(Heat heatClient, String stackName) throws MsoException {
        if (stackName == null) {
            return null;
        }
        try {
            OpenStackRequest<Stack> request = heatClient.getStacks().byName(stackName);
            return executeAndRecordOpenstackRequest(request);
        } catch (OpenStackResponseException e) {
            logger.error("Error in Query Stack", e);
            if (e.getStatus() == 404) {
                logger.debug("queryHeatStack - stack not found: {}", stackName);
                return null;
            } else {
                // Convert the OpenStackResponseException to an MsoOpenstackException
                throw heatExceptionToMsoException(e, "QueryStack");
            }
        } catch (OpenStackConnectException e) {
            // Connection to Openstack failed
            throw heatExceptionToMsoException(e, "QueryAllStack");
        }
    }

    public Stack queryHeatStack(String stackName, String cloudSiteId, String tenantId) throws MsoException {
        if (stackName == null) {
            return null;
        }
        return queryHeatStack(getHeatClient(cloudSiteId, tenantId), stackName);
    }

    // TODO enhancement - just have this return the stack then we dont have to query again in deleteStack
    public Map<String, Object> queryStackForOutputs(String cloudSiteId, String cloudOwner, String tenantId,
            String stackName) throws MsoException {
        logger.debug("MsoHeatUtils.queryStackForOutputs)");
        StackInfo heatStack = this.queryStack(cloudSiteId, cloudOwner, tenantId, stackName);
        if (heatStack == null || heatStack.getStatus() == HeatStatus.NOTFOUND) {
            return null;
        }
        return heatStack.getOutputs();
    }

    public void copyStringOutputsToInputs(Map<String, Object> inputs, Map<String, Object> otherStackOutputs,
            boolean overWrite) {
        if (inputs == null || otherStackOutputs == null)
            return;
        for (String key : otherStackOutputs.keySet()) {
            if (!inputs.containsKey(key)) {
                Object obj = otherStackOutputs.get(key);
                if (obj instanceof String) {
                    inputs.put(key, otherStackOutputs.get(key));
                } else if (obj instanceof JsonNode) {
                    // This is a bit of mess - but I think it's the least impacting
                    // let's convert it BACK to a string - then it will get converted back later
                    try {
                        String str = this.convertNode((JsonNode) obj);
                        inputs.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for JsonNode {} ", key, e);
                        // effect here is this value will not have been copied to the inputs - and therefore will error
                        // out downstream
                    }
                } else if (obj instanceof java.util.LinkedHashMap) {
                    logger.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                    try {
                        String str = JSON_MAPPER.writeValueAsString(obj);
                        inputs.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for LinkedHashMap {} ", key, e);
                    }
                } else if (obj instanceof Integer) {
                    try {
                        String str = "" + obj;
                        inputs.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for Integer {} ", key, e);
                    }
                } else {
                    try {
                        String str = obj.toString();
                        inputs.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for Other {} ({}) ", key,
                                e.getMessage(), e);
                        // effect here is this value will not have been copied to the inputs - and therefore will error
                        // out downstream
                    }
                }
            }
        }
        return;
    }

    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.debug("Error converting json to string {} ", e.getMessage(), e);
        }
        return "[Error converting json to string]";
    }


    protected StringBuilder getOutputsAsStringBuilder(Stack heatStack) {
        // This should only be used as a utility to print out the stack outputs
        // to the log
        StringBuilder sb = new StringBuilder("");
        if (heatStack == null) {
            sb.append("(heatStack is null)");
            return sb;
        }
        List<Output> outputList = heatStack.getOutputs();
        if (outputList == null || outputList.isEmpty()) {
            sb.append("(outputs is empty)");
            return sb;
        }
        Map<String, Object> outputs = new HashMap<>();
        for (Output outputItem : outputList) {
            outputs.put(outputItem.getOutputKey(), outputItem.getOutputValue());
        }
        int counter = 0;
        sb.append("OUTPUTS:\n");
        for (String key : outputs.keySet()) {
            sb.append("outputs[").append(counter++).append("]: ").append(key).append("=");
            Object obj = outputs.get(key);
            if (obj instanceof String) {
                sb.append((String) obj).append(" (a string)");
            } else if (obj instanceof JsonNode) {
                sb.append(this.convertNode((JsonNode) obj)).append(" (a JsonNode)");
            } else if (obj instanceof java.util.LinkedHashMap) {
                try {
                    String str = JSON_MAPPER.writeValueAsString(obj);
                    sb.append(str).append(" (a java.util.LinkedHashMap)");
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                    sb.append("(a LinkedHashMap value that would not convert nicely)");
                }
            } else if (obj instanceof Integer) {
                String str = "";
                try {
                    str = obj.toString() + " (an Integer)\n";
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                    str = "(an Integer unable to call .toString() on)";
                }
                sb.append(str);
            } else if (obj instanceof ArrayList) {
                String str = "";
                try {
                    str = obj.toString() + " (an ArrayList)";
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                    str = "(an ArrayList unable to call .toString() on?)";
                }
                sb.append(str);
            } else if (obj instanceof Boolean) {
                String str = "";
                try {
                    str = obj.toString() + " (a Boolean)";
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                    str = "(an Boolean unable to call .toString() on?)";
                }
                sb.append(str);
            } else {
                String str = "";
                try {
                    str = obj.toString() + " (unknown Object type)";
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                    str = "(a value unable to call .toString() on?)";
                }
                sb.append(str);
            }
            sb.append("\n");
        }
        sb.append("[END]");
        return sb;
    }


    public void copyBaseOutputsToInputs(Map<String, Object> inputs, Map<String, Object> otherStackOutputs,
            List<String> paramNames, Map<String, String> aliases) {
        if (inputs == null || otherStackOutputs == null)
            return;
        for (String key : otherStackOutputs.keySet()) {
            if (paramNames != null) {
                if (!paramNames.contains(key) && !aliases.containsKey(key)) {
                    logger.debug("\tParameter {} is NOT defined to be in the template - do not copy to inputs", key);
                    continue;
                }
                if (aliases.containsKey(key)) {
                    logger.debug("Found an alias! Will move {} to {}", key, aliases.get(key));
                    Object obj = otherStackOutputs.get(key);
                    key = aliases.get(key);
                    otherStackOutputs.put(key, obj);
                }
            }
            if (!inputs.containsKey(key)) {
                Object obj = otherStackOutputs.get(key);
                logger.debug("\t**Adding {} to inputs (.toString()={}", key, obj.toString());
                if (obj instanceof String) {
                    logger.debug("\t\t**A String");
                    inputs.put(key, obj);
                } else if (obj instanceof Integer) {
                    logger.debug("\t\t**An Integer");
                    inputs.put(key, obj);
                } else if (obj instanceof JsonNode) {
                    logger.debug("\t\t**A JsonNode");
                    inputs.put(key, obj);
                } else if (obj instanceof Boolean) {
                    logger.debug("\t\t**A Boolean");
                    inputs.put(key, obj);
                } else if (obj instanceof java.util.LinkedHashMap) {
                    logger.debug("\t\t**A java.util.LinkedHashMap **");
                    inputs.put(key, obj);
                } else if (obj instanceof java.util.ArrayList) {
                    logger.debug("\t\t**An ArrayList");
                    inputs.put(key, obj);
                } else {
                    logger.debug("\t\t**UNKNOWN OBJECT TYPE");
                    inputs.put(key, obj);
                }
            } else {
                logger.debug("key={} is already in the inputs - will not overwrite", key);
            }
        }
        return;
    }

    public List<String> convertCdlToArrayList(String cdl) {
        String cdl2 = cdl.trim();
        String cdl3;
        if (cdl2.startsWith("[") && cdl2.endsWith("]")) {
            cdl3 = cdl2.substring(1, cdl2.lastIndexOf("]"));
        } else {
            cdl3 = cdl2;
        }
        return new ArrayList<>(Arrays.asList(cdl3.split(",")));
    }

    /**
     * New with 1707 - this method will convert all the String *values* of the inputs to their "actual" object type
     * (based on the param type: in the db - which comes from the template): (heat variable type) -> java Object type
     * string -> String number -> Integer json -> marshal object to json comma_delimited_list -> ArrayList boolean ->
     * Boolean if any of the conversions should fail, we will default to adding it to the inputs as a string - see if
     * Openstack can handle it. Also, will remove any params that are extra. Any aliases will be converted to their
     * appropriate name (anyone use this feature?)
     *
     * @param inputs - the Map<String, String> of the inputs received on the request
     * @param template the HeatTemplate object - this is so we can also verify if the param is valid for this template
     * @return HashMap<String, Object> of the inputs, cleaned and converted
     */
    public Map<String, Object> convertInputMap(Map<String, Object> inputs, HeatTemplate template) {
        HashMap<String, Object> newInputs = new HashMap<>();
        HashMap<String, HeatTemplateParam> params = new HashMap<>();
        HashMap<String, HeatTemplateParam> paramAliases = new HashMap<>();

        if (inputs == null) {
            return new HashMap<>();
        }

        for (HeatTemplateParam htp : template.getParameters()) {
            params.put(htp.getParamName(), htp);
            if (htp.getParamAlias() != null && !"".equals(htp.getParamAlias())) {
                logger.debug("\tFound ALIAS {} -> {}", htp.getParamName(), htp.getParamAlias());
                paramAliases.put(htp.getParamAlias(), htp);
            }
        }

        for (String key : inputs.keySet()) {
            boolean alias = false;
            String realName = null;
            if (!params.containsKey(key)) {
                // add check here for an alias
                if (!paramAliases.containsKey(key)) {
                    continue;
                } else {
                    alias = true;
                    realName = paramAliases.get(key).getParamName();
                }
            }
            String type = params.get(key).getParamType();
            if (type == null || "".equals(type)) {
                logger.debug("**PARAM_TYPE is null/empty for {}, will default to string", key);
                type = "string";
            }
            if ("string".equalsIgnoreCase(type)) {
                // Easiest!
                String str = inputs.get(key) != null ? inputs.get(key).toString() : null;
                if (alias)
                    newInputs.put(realName, str);
                else
                    newInputs.put(key, str);
            } else if ("number".equalsIgnoreCase(type)) {
                String integerString = inputs.get(key) != null ? inputs.get(key).toString() : null;
                Integer anInteger = null;
                try {
                    anInteger = Integer.parseInt(integerString);
                } catch (Exception e) {
                    logger.debug("Unable to convert {} to an integer!!", integerString, e);
                    anInteger = null;
                }
                if (anInteger != null) {
                    if (alias)
                        newInputs.put(realName, anInteger);
                    else
                        newInputs.put(key, anInteger);
                } else {
                    if (alias)
                        newInputs.put(realName, integerString);
                    else
                        newInputs.put(key, integerString);
                }
            } else if ("json".equalsIgnoreCase(type)) {
                Object jsonObj = inputs.get(key);
                Object json;
                try {
                    if (jsonObj instanceof String) {
                        json = JSON_MAPPER.readTree(jsonObj.toString());
                    } else {
                        // will already marshal to json without intervention
                        json = jsonObj;
                    }
                } catch (IOException e) {
                    logger.error("failed to map to json, directly converting to string instead", e);
                    json = jsonObj.toString();
                }
                if (alias)
                    newInputs.put(realName, json);
                else
                    newInputs.put(key, json);
            } else if ("comma_delimited_list".equalsIgnoreCase(type)) {
                String commaSeparated = inputs.get(key) != null ? inputs.get(key).toString() : null;
                try {
                    List<String> anArrayList = this.convertCdlToArrayList(commaSeparated);
                    if (alias)
                        newInputs.put(realName, anArrayList);
                    else
                        newInputs.put(key, anArrayList);
                } catch (Exception e) {
                    logger.debug("Unable to convert {} to an ArrayList!!", commaSeparated, e);
                    if (alias)
                        newInputs.put(realName, commaSeparated);
                    else
                        newInputs.put(key, commaSeparated);
                }
            } else if ("boolean".equalsIgnoreCase(type)) {
                String booleanString = inputs.get(key) != null ? inputs.get(key).toString() : null;
                Boolean aBool = Boolean.valueOf(booleanString);
                if (alias)
                    newInputs.put(realName, aBool);
                else
                    newInputs.put(key, aBool);
            } else {
                // it's null or something undefined - just add it back as a String
                String str = inputs.get(key).toString();
                if (alias)
                    newInputs.put(realName, str);
                else
                    newInputs.put(key, str);
            }
        }
        return newInputs;
    }

    /*******************************************************************************
     *
     * Methods (and associated utilities) to implement the VduPlugin interface
     *
     *******************************************************************************/

    /**
     * VduPlugin interface for instantiate function.
     *
     * Translate the VduPlugin parameters to the corresponding 'createStack' parameters, and then invoke the existing
     * function.
     */
    @Override
    public VduInstance instantiateVdu(CloudInfo cloudInfo, String instanceName, Map<String, Object> inputs,
            VduModelInfo vduModel, boolean rollbackOnFailure) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String cloudOwner = cloudInfo.getCloudOwner();
        String tenantId = cloudInfo.getTenantId();

        // Translate the VDU ModelInformation structure to that which is needed for
        // creating the Heat stack. Loop through the artifacts, looking specifically
        // for MAIN_TEMPLATE and ENVIRONMENT. Any other artifact will
        // be attached as a FILE.
        String heatTemplate = null;
        Map<String, Object> nestedTemplates = new HashMap<>();
        Map<String, Object> files = new HashMap<>();
        String heatEnvironment = null;

        for (VduArtifact vduArtifact : vduModel.getArtifacts()) {
            if (vduArtifact.getType() == ArtifactType.MAIN_TEMPLATE) {
                heatTemplate = new String(vduArtifact.getContent());
            } else if (vduArtifact.getType() == ArtifactType.NESTED_TEMPLATE) {
                nestedTemplates.put(vduArtifact.getName(), new String(vduArtifact.getContent()));
            } else if (vduArtifact.getType() == ArtifactType.ENVIRONMENT) {
                heatEnvironment = new String(vduArtifact.getContent());
            }
        }

        try {
            StackInfo stackInfo = createStack(cloudSiteId, cloudOwner, tenantId, instanceName, vduModel, heatTemplate,
                    inputs, true, // poll
                                  // for
                                  // completion
                    vduModel.getTimeoutMinutes(), heatEnvironment, nestedTemplates, files, rollbackOnFailure, false);

            // Populate a vduInstance from the StackInfo
            return stackInfoToVduInstance(stackInfo);
        } catch (Exception e) {
            throw new VduException("MsoHeatUtils (instantiateVDU): createStack Exception", e);
        }
    }


    /**
     * VduPlugin interface for query function.
     */
    @Override
    public VduInstance queryVdu(CloudInfo cloudInfo, String instanceId) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String cloudOwner = cloudInfo.getCloudOwner();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Query the Cloudify Deployment object and populate a VduInstance
            StackInfo stackInfo = queryStack(cloudSiteId, cloudOwner, tenantId, instanceId);

            return stackInfoToVduInstance(stackInfo);
        } catch (Exception e) {
            throw new VduException("MsoHeatUtile (queryVdu): queryStack Exception ", e);
        }
    }


    /**
     * VduPlugin interface for delete function.
     */
    @Override
    public VduInstance deleteVdu(CloudInfo cloudInfo, String instanceId, int timeoutMinutes) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String cloudOwner = cloudInfo.getCloudOwner();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Delete the Heat stack
            StackInfo stackInfo = deleteStack(tenantId, cloudOwner, cloudSiteId, instanceId, true, timeoutMinutes);

            // Populate a VduInstance based on the deleted Cloudify Deployment object
            VduInstance vduInstance = stackInfoToVduInstance(stackInfo);

            // Override return state to DELETED (HeatUtils sets to NOTFOUND)
            vduInstance.getStatus().setState(VduStateType.DELETED);

            return vduInstance;
        } catch (Exception e) {
            throw new VduException("Delete VDU Exception", e);
        }
    }


    /**
     * VduPlugin interface for update function.
     *
     * Update is currently not supported in the MsoHeatUtils implementation of VduPlugin. Just return a VduException.
     *
     */
    @Override
    public VduInstance updateVdu(CloudInfo cloudInfo, String instanceId, Map<String, Object> inputs,
            VduModelInfo vduModel, boolean rollbackOnFailure) throws VduException {
        throw new VduException("MsoHeatUtils: updateVdu interface not supported");
    }


    /*
     * Convert the local DeploymentInfo object (Cloudify-specific) to a generic VduInstance object
     */
    protected VduInstance stackInfoToVduInstance(StackInfo stackInfo) {
        VduInstance vduInstance = new VduInstance();

        // The full canonical name as the instance UUID
        vduInstance.setVduInstanceId(stackInfo.getCanonicalName());
        vduInstance.setVduInstanceName(stackInfo.getName());

        // Copy inputs and outputs
        vduInstance.setInputs(stackInfo.getParameters());
        vduInstance.setOutputs(stackInfo.getOutputs());

        // Translate the status elements
        vduInstance.setStatus(stackStatusToVduStatus(stackInfo));

        return vduInstance;
    }

    private VduStatus stackStatusToVduStatus(StackInfo stackInfo) {
        VduStatus vduStatus = new VduStatus();

        // Map the status fields to more generic VduStatus.
        // There are lots of HeatStatus values, so this is a bit long...
        HeatStatus heatStatus = stackInfo.getStatus();
        String statusMessage = stackInfo.getStatusMessage();

        if (heatStatus == HeatStatus.INIT || heatStatus == HeatStatus.BUILDING) {
            vduStatus.setState(VduStateType.INSTANTIATING);
            vduStatus.setLastAction((new PluginAction("create", IN_PROGRESS, statusMessage)));
        } else if (heatStatus == HeatStatus.NOTFOUND) {
            vduStatus.setState(VduStateType.NOTFOUND);
        } else if (heatStatus == HeatStatus.CREATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction("create", "complete", statusMessage)));
        } else if (heatStatus == HeatStatus.UPDATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction("update", "complete", statusMessage)));
        } else if (heatStatus == HeatStatus.UPDATING) {
            vduStatus.setState(VduStateType.UPDATING);
            vduStatus.setLastAction((new PluginAction("update", IN_PROGRESS, statusMessage)));
        } else if (heatStatus == HeatStatus.DELETING) {
            vduStatus.setState(VduStateType.DELETING);
            vduStatus.setLastAction((new PluginAction("delete", IN_PROGRESS, statusMessage)));
        } else if (heatStatus == HeatStatus.FAILED) {
            vduStatus.setState(VduStateType.FAILED);
            vduStatus.setErrorMessage(stackInfo.getStatusMessage());
        } else {
            vduStatus.setState(VduStateType.UNKNOWN);
        }

        return vduStatus;
    }

    public Resources queryStackResources(String cloudSiteId, String tenantId, String stackName, int nestedDepth)
            throws MsoException {
        Heat heatClient = getHeatClient(cloudSiteId, tenantId);
        OpenStackRequest<Resources> request =
                heatClient.getResources().listResources(stackName).queryParam("nested_depth", nestedDepth);
        return executeAndRecordOpenstackRequest(request, false);
    }

    public Events queryStackEvents(String cloudSiteId, String tenantId, String stackName, String stackId,
            int nestedDepth) throws MsoException {
        Heat heatClient = getHeatClient(cloudSiteId, tenantId);
        OpenStackRequest<Events> request =
                heatClient.getEvents().listEvents(stackName, stackId).queryParam("nested_depth", nestedDepth);
        return executeAndRecordOpenstackRequest(request, false);
    }

    public Stacks queryStacks(String cloudSiteId, String tenantId, int limit, String marker)
            throws HeatClientException {
        Heat heatClient;
        try {
            heatClient = getHeatClient(cloudSiteId, tenantId);
        } catch (MsoException e) {
            logger.error("Error Creating Heat Client", e);
            throw new HeatClientException("Error Creating Heat Client", e);
        }
        OpenStackRequest<Stacks> request =
                heatClient.getStacks().list().queryParam("limit", limit).queryParam("marker", marker);
        return executeAndRecordOpenstackRequest(request, false);
    }

    public <R> R executeHeatClientRequest(String url, String cloudSiteId, String tenantId, Class<R> returnType)
            throws MsoException {
        Heat heatClient = getHeatClient(cloudSiteId, tenantId);
        OpenStackRequest<R> request = heatClient.get(url, returnType);
        return executeAndRecordOpenstackRequest(request, false);
    }

    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.debug("Thread interrupted while sleeping", e);
            Thread.currentThread().interrupt();
        }
    }

    public void updateResourceStatus(String requestId, String resourceStatusMessage) {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId(requestId);
        request.setResourceStatusMessage(resourceStatusMessage);
        try {
            requestDBClient.patchInfraActiveRequests(request);
        } catch (HttpClientErrorException e) {
            logger.warn("Unable to update active request resource status");
        }
    }

    public int getVfHeatTimeoutValue(String modelCustomizationUuid, boolean isVolumeGroup) {
        int timeoutMinutes = DEFAULT_POLLING_TIMEOUT;
        try {
            VfModuleCustomization vfmc = null;
            if (modelCustomizationUuid != null) {
                vfmc = catalogClient.getVfModuleCustomizationByModelCuztomizationUUID(modelCustomizationUuid);
                if (vfmc != null) {
                    VfModule vf = vfmc.getVfModule();
                    if (vf != null) {
                        HeatTemplate heat = vf.getModuleHeatTemplate();
                        if (isVolumeGroup) {
                            heat = vf.getVolumeHeatTemplate();
                        }
                        if (heat != null && heat.getTimeoutMinutes() != null) {
                            if (heat.getTimeoutMinutes() < DEFAULT_POLLING_TIMEOUT) {
                                timeoutMinutes = heat.getTimeoutMinutes();
                            }
                        }
                    }
                } else {
                    logger.debug(
                            "Unable to find Vf Module Customization with model customization uuid {}. Using default timeout {}",
                            modelCustomizationUuid, timeoutMinutes);
                }
            }
        } catch (Exception e) {
            logger.warn("Exception occured while getting heat timeout value. Using default timeout {}", timeoutMinutes,
                    e);
        }
        return timeoutMinutes;
    }

    public int getNetworkHeatTimeoutValue(String modelCustomizationUuid, String networkType) {
        int timeoutMinutes = DEFAULT_POLLING_TIMEOUT;
        try {
            NetworkResource networkResource = null;
            if (isBlank(modelCustomizationUuid)) {
                if (isNotBlank(networkType)) {
                    networkResource = catalogClient.getNetworkResourceByModelName(networkType);
                }
            } else {
                NetworkResourceCustomization nrc =
                        catalogClient.getNetworkResourceCustomizationByModelCustomizationUUID(modelCustomizationUuid);
                if (nrc != null) {
                    networkResource = nrc.getNetworkResource();
                }
            }

            if (networkResource != null) {
                networkResource.getHeatTemplate().getTimeoutMinutes();
                HeatTemplate heat = networkResource.getHeatTemplate();
                if (heat != null && heat.getTimeoutMinutes() != null) {
                    if (heat.getTimeoutMinutes() < DEFAULT_POLLING_TIMEOUT) {
                        timeoutMinutes = heat.getTimeoutMinutes();
                    }
                }
            } else {
                logger.debug(
                        "Unable to find Network Resource with model customization uuid {} or network type {}. Using default timeout {}",
                        modelCustomizationUuid, networkType, timeoutMinutes);
            }
        } catch (Exception e) {
            logger.warn("Exception occured while getting heat timeout value. Using default timeout {}", timeoutMinutes,
                    e);
        }
        return timeoutMinutes;
    }

}
