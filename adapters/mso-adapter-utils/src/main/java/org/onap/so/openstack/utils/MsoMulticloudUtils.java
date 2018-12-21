/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.client.HttpClient;
import org.onap.so.client.RestClient;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.TargetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.CreateStackParam;

@Component
public class MsoMulticloudUtils extends MsoHeatUtils implements VduPlugin{

    public static final String OOF_DIRECTIVES = "oof_directives";
    public static final String SDNC_DIRECTIVES = "sdnc_directives";
    public static final String VNF_ID = "vnf_id";
    public static final String VF_MODULE_ID = "vf_module_id";
    public static final String TEMPLATE_TYPE = "template_type";
    public static final List<String> MULTICLOUD_INPUTS =
            Arrays.asList(OOF_DIRECTIVES, SDNC_DIRECTIVES, TEMPLATE_TYPE);

    private static final Logger logger = LoggerFactory.getLogger(MsoMulticloudUtils.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Autowired
    private Environment environment;


    /******************************************************************************
     *
     * Methods (and associated utilities) to implement the VduPlugin interface
     *
     *******************************************************************************/

    /**
     * Create a new Stack in the specified cloud location and tenant. The Heat template
     * and parameter map are passed in as arguments, along with the cloud access credentials.
     * It is expected that parameters have been validated and contain at minimum the required
     * parameters for the given template with no extra (undefined) parameters..
     *
     * The Stack name supplied by the caller must be unique in the scope of this tenant.
     * However, it should also be globally unique, as it will be the identifier for the
     * resource going forward in Inventory. This latter is managed by the higher levels
     * invoking this function.
     *
     * The caller may choose to let this function poll Openstack for completion of the
     * stack creation, or may handle polling itself via separate calls to query the status.
     * In either case, a StackInfo object will be returned containing the current status.
     * When polling is enabled, a status of CREATED is expected. When not polling, a
     * status of BUILDING is expected.
     *
     * An error will be thrown if the requested Stack already exists in the specified
     * Tenant and Cloud.
     *
     * For 1510 - add "environment", "files" (nested templates), and "heatFiles" (get_files) as
     * parameters for createStack. If environment is non-null, it will be added to the stack.
     * The nested templates and get_file entries both end up being added to the "files" on the
     * stack. We must combine them before we add them to the stack if they're both non-null.
     *
     * @param cloudSiteId The cloud (may be a region) in which to create the stack.
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param stackName The name of the stack to create
     * @param heatTemplate The Heat template
     * @param stackInputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param environment An optional yaml-format string to specify environmental parameters
     * @param files a Map<String, Object> that lists the child template IDs (file is the string, object is an int of
     *        Template id)
     * @param heatFiles a Map<String, Object> that lists the get_file entries (fileName, fileBody)
     * @param backout Do not delete stack on create Failure - defaulted to True
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */

    @SuppressWarnings("unchecked")
    @Override
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  String environment,
                                  Map <String, Object> files,
                                  Map <String, Object> heatFiles,
                                  boolean backout) throws MsoException {

        logger.trace("Started MsoMulticloudUtils.createStack");

        // Get the directives, if present.
        String oofDirectives = "{}";
        String sdncDirectives = "{}";
        String genericVnfId = "";
        String vfModuleId = "";
        String templateType = "";

        for (String key: MULTICLOUD_INPUTS) {
            if (!stackInputs.isEmpty() && stackInputs.containsKey(key)) {
                if (key == OOF_DIRECTIVES) {
                    oofDirectives = (String) stackInputs.get(key);
                }
                if (key == SDNC_DIRECTIVES) {
                    sdncDirectives = (String) stackInputs.get(key);
                }
                if (key == TEMPLATE_TYPE) {
                    templateType = (String) stackInputs.get(key);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Found %s: %s", key, stackInputs.get(key)));
                }
                stackInputs.remove(key);
            }
        }

        if (!stackInputs.isEmpty() && stackInputs.containsKey(VF_MODULE_ID)){
            vfModuleId = (String) stackInputs.get(VF_MODULE_ID);
        }
        if (!stackInputs.isEmpty() && stackInputs.containsKey(VNF_ID)){
            genericVnfId = (String) stackInputs.get(VNF_ID);
        }

        // create the multicloud payload
        CreateStackParam stack = createStackParam(stackName, heatTemplate, stackInputs, timeoutMinutes, environment, files, heatFiles);

        MulticloudRequest multicloudRequest= new MulticloudRequest();

        multicloudRequest.setGenericVnfId(genericVnfId);
        multicloudRequest.setVfModuleId(vfModuleId);
        multicloudRequest.setTemplateType(templateType);
        multicloudRequest.setTemplateData(stack);
        multicloudRequest.setOofDirectives(getDirectiveNode(oofDirectives));
        multicloudRequest.setSdncDirectives(getDirectiveNode(sdncDirectives));
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Multicloud Request is: %s", multicloudRequest.toString()));
        }

        String multicloudEndpoint = getMulticloudEndpoint(cloudSiteId, null);
        RestClient multicloudClient = getMulticloudClient(multicloudEndpoint);

        Response response = multicloudClient.post(multicloudRequest);

        StackInfo createInfo = new StackInfo();
        createInfo.setName(stackName);

        MulticloudCreateResponse multicloudResponseBody = null;
        if (response.hasEntity()) {
            multicloudResponseBody = getCreateBody((java.io.InputStream)response.getEntity());
        }
        if (response.getStatus() == Response.Status.CREATED.getStatusCode() && response.hasEntity()) {
            createInfo.setCanonicalName(stackName + "/" + multicloudResponseBody.getWorkloadId());
            if (logger.isDebugEnabled()) {
                logger.debug("Multicloud Create Response Body: " + multicloudResponseBody);
            }
            return getStackStatus(cloudSiteId, tenantId, createInfo.getCanonicalName(), pollForCompletion, timeoutMinutes, backout);
        } else {
            StringBuilder stackErrorStatusReason = new StringBuilder(response.getStatusInfo().getReasonPhrase());
            if (null != multicloudResponseBody) {
                stackErrorStatusReason.append(multicloudResponseBody.toString());
            }
            MsoOpenstackException me = new MsoOpenstackException(0, "", stackErrorStatusReason.toString());
            me.addContext(CREATE_STACK);
            alarmLogger.sendAlarm(HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
            throw me;
        }
    }

    @Override
    public Map<String, Object> queryStackForOutputs(String cloudSiteId,
                                                           String tenantId, String stackName) throws MsoException {
        logger.debug("MsoHeatUtils.queryStackForOutputs)");
        StackInfo heatStack = this.queryStack(cloudSiteId, tenantId, stackName);
        if (heatStack == null || heatStack.getStatus() == HeatStatus.NOTFOUND) {
            return null;
        }
        return heatStack.getOutputs();
    }

    /**
     * Query for a single stack (by ID) in a tenant. This call will always return a
     * StackInfo object. If the stack does not exist, an "empty" StackInfo will be
     * returned - containing only the stack name and a status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @param stackId The ID of the stack to query
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    @Override
    public StackInfo queryStack (String cloudSiteId, String tenantId, String instanceId) throws MsoException {
        if (logger.isDebugEnabled()) {
            logger.debug (String.format("Query multicloud HEAT stack: %s in tenant %s", instanceId, tenantId));
        }
        String stackName = null;
        String stackId = null;
        int offset = instanceId.indexOf('/');
        if (offset > 0 && offset < (instanceId.length() - 1)) {
            stackName = instanceId.substring(0, offset);
            stackId = instanceId.substring(offset + 1);
        } else {
            stackName = instanceId;
            stackId = instanceId;
        }

        StackInfo returnInfo = new StackInfo();
        returnInfo.setName(stackName);

        String multicloudEndpoint = getMulticloudEndpoint(cloudSiteId, stackId);
        RestClient multicloudClient = getMulticloudClient(multicloudEndpoint);

        if (multicloudClient != null) {
            Response response = multicloudClient.get();
            if (logger.isDebugEnabled()) {
                logger.debug (String.format("Mulicloud GET Response: %s", response.toString()));
            }

            MulticloudQueryResponse multicloudQueryBody = null;
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                returnInfo.setStatus(HeatStatus.NOTFOUND);
                returnInfo.setStatusMessage(response.getStatusInfo().getReasonPhrase());
            } else if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
                multicloudQueryBody = getQueryBody((java.io.InputStream)response.getEntity());
                returnInfo.setCanonicalName(stackName + "/" + multicloudQueryBody.getWorkloadId());
                returnInfo.setStatus(getHeatStatus(multicloudQueryBody.getWorkloadStatus()));
                returnInfo.setStatusMessage(multicloudQueryBody.getWorkloadStatus());
                if (logger.isDebugEnabled()) {
                    logger.debug("Multicloud Create Response Body: " + multicloudQueryBody.toString());
                }
            } else {
                returnInfo.setStatus(HeatStatus.FAILED);
                returnInfo.setStatusMessage(response.getStatusInfo().getReasonPhrase());
            }
        }

        return returnInfo;
    }

    public StackInfo deleteStack (String cloudSiteId, String tenantId, String instanceId) throws MsoException {
        if (logger.isDebugEnabled()) {
            logger.debug (String.format("Delete multicloud HEAT stack: %s in tenant %s", instanceId, tenantId));
        }
        String stackName = null;
        String stackId = null;
        int offset = instanceId.indexOf('/');
        if (offset > 0 && offset < (instanceId.length() - 1)) {
            stackName = instanceId.substring(0, offset);
            stackId = instanceId.substring(offset + 1);
        } else {
            stackName = instanceId;
            stackId = instanceId;
        }

        StackInfo returnInfo = new StackInfo();
        returnInfo.setName(stackName);
        Response response = null;

        String multicloudEndpoint = getMulticloudEndpoint(cloudSiteId, stackId);
        RestClient multicloudClient = getMulticloudClient(multicloudEndpoint);

        if (multicloudClient != null) {
            response = multicloudClient.delete();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Multicloud Delete response is: %s", response.getEntity().toString()));
            }

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                returnInfo.setStatus(HeatStatus.NOTFOUND);
                returnInfo.setStatusMessage(response.getStatusInfo().getReasonPhrase());
            } else if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return getStackStatus(cloudSiteId, tenantId, instanceId);
            } else {
                returnInfo.setStatus(HeatStatus.FAILED);
                returnInfo.setStatusMessage(response.getStatusInfo().getReasonPhrase());
            }

        }
        returnInfo.setStatus(mapResponseToHeatStatus(response));
        return returnInfo;
    }

    // ---------------------------------------------------------------
    // PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    private HeatStatus getHeatStatus(String workloadStatus) {
        if (workloadStatus.length() == 0) return HeatStatus.INIT;
        if ("CREATE_IN_PROGRESS".equals(workloadStatus)) return HeatStatus.BUILDING;
        if ("CREATE_COMPLETE".equals(workloadStatus)) return HeatStatus.CREATED;
        if ("CREATE_FAILED".equals(workloadStatus)) return HeatStatus.FAILED;
        if ("DELETE_IN_PROGRESS".equals(workloadStatus)) return HeatStatus.DELETING;
        if ("DELETE_COMPLETE".equals(workloadStatus)) return HeatStatus.NOTFOUND;
        if ("DELETE_FAILED".equals(workloadStatus)) return HeatStatus.FAILED;
        if ("UPDATE_IN_PROGRESS".equals(workloadStatus)) return HeatStatus.UPDATING;
        if ("UPDATE_FAILED".equals(workloadStatus)) return HeatStatus.FAILED;
        if ("UPDATE_COMPLETE".equals(workloadStatus)) return HeatStatus.UPDATED;
        return HeatStatus.UNKNOWN;
    }

    private StackInfo getStackStatus(String cloudSiteId, String tenantId, String instanceId) throws MsoException {
        return getStackStatus(cloudSiteId, tenantId, instanceId, false, 0, false);
    }

    private StackInfo getStackStatus(String cloudSiteId, String tenantId, String instanceId, boolean pollForCompletion, int timeoutMinutes, boolean backout) throws MsoException {
        StackInfo stackInfo = new StackInfo();

        // If client has requested a final response, poll for stack completion
        if (pollForCompletion) {
            // Set a time limit on overall polling.
            // Use the resource (template) timeout for Openstack (expressed in minutes)
            // and add one poll interval to give Openstack a chance to fail on its own.s

            int createPollInterval = Integer.parseInt(this.environment.getProperty(createPollIntervalProp, createPollIntervalDefault));
            int pollTimeout = (timeoutMinutes * 60) + createPollInterval;
            // New 1610 - poll on delete if we rollback - use same values for now
            int deletePollInterval = createPollInterval;
            int deletePollTimeout = pollTimeout;
            boolean createTimedOut = false;
            StringBuilder stackErrorStatusReason = new StringBuilder("");
            logger.debug("createPollInterval=" + createPollInterval + ", pollTimeout=" + pollTimeout);

            while (true) {
                try {
                    stackInfo = queryStack(cloudSiteId, tenantId, instanceId);
                    logger.debug (stackInfo.getStatus() + " (" + instanceId + ")");

                    if (HeatStatus.BUILDING.equals(stackInfo.getStatus())) {
                        // Stack creation is still running.
                        // Sleep and try again unless timeout has been reached
                        if (pollTimeout <= 0) {
                            // Note that this should not occur, since there is a timeout specified
                            // in the Openstack (multicloud?) call.
                            logger.error(String.format("%s %s %s %s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_TIMEOUT.toString(), cloudSiteId, tenantId, instanceId, stackInfo.getStatus(), "", "", MsoLogger.ErrorCode.AvailabilityError.getValue(), "Create stack timeout"));
                            createTimedOut = true;
                            break;
                        }

                        sleep(createPollInterval * 1000L);

                        pollTimeout -= createPollInterval;
                        logger.debug("pollTimeout remaining: " + pollTimeout);
                    } else {
                        //save off the status & reason msg before we attempt delete
                        stackErrorStatusReason.append("Stack error (" + stackInfo.getStatus() + "): " + stackInfo.getStatusMessage());
                        break;
                    }
                } catch (MsoException me) {
                    // Cannot query the stack status. Something is wrong.
                    // Try to roll back the stack
                    if (!backout) {
                        logger.warn(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack error, stack deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Exception in Create Stack, stack deletion suppressed"));
                    } else {
                        try {
                            logger.debug("Create Stack error - unable to query for stack status - attempting to delete stack: " + instanceId + " - This will likely fail and/or we won't be able to query to see if delete worked");
                            StackInfo deleteInfo = deleteStack(cloudSiteId, tenantId, instanceId);
                            // this may be a waste of time - if we just got an exception trying to query the stack - we'll just
                            // get another one, n'est-ce pas?
                            boolean deleted = false;
                            while (!deleted) {
                                try {
                                    StackInfo queryInfo = queryStack(cloudSiteId, tenantId, instanceId);
                                    logger.debug("Deleting " + instanceId + ", status: " + queryInfo.getStatus());
                                    if (HeatStatus.DELETING.equals(queryInfo.getStatus())) {
                                        if (deletePollTimeout <= 0) {
                                            logger.error(String.format("%s %s %s %s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_TIMEOUT.toString(), cloudSiteId, tenantId, instanceId,
                                                    queryInfo.getStatus(), "", "", MsoLogger.ErrorCode.AvailabilityError.getValue(),
                                                    "Rollback: DELETE stack timeout"));
                                            break;
                                        } else {
                                            sleep(deletePollInterval * 1000L);
                                            deletePollTimeout -= deletePollInterval;
                                        }
                                    } else if (HeatStatus.NOTFOUND.equals(queryInfo.getStatus())){
                                        logger.debug("DELETE_COMPLETE for " + instanceId);
                                        deleted = true;
                                        continue;
                                    } else {
                                        //got a status other than DELETE_IN_PROGRESS or DELETE_COMPLETE - so break and evaluate
                                        break;
                                    }
                                } catch (Exception e3) {
                                    // Just log this one. We will report the original exception.
                                    logger.error(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack: Nested exception rolling back stack: " + e3, "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Create Stack: Nested exception rolling back stack on error on query"));
                                }
                            }
                        } catch (Exception e2) {
                            // Just log this one. We will report the original exception.
                            logger.error(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack: Nested exception rolling back stack: " + e2, "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Create Stack: Nested exception rolling back stack"));
                        }
                    }

                    // Propagate the original exception from Stack Query.
                    me.addContext (CREATE_STACK);
                    throw me;
                }
            }

            if (!HeatStatus.CREATED.equals(stackInfo.getStatus())) {
                logger.error(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack error:  Polling complete with non-success status: "
                              + stackInfo.getStatus () + ", " + stackInfo.getStatusMessage(), "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Create Stack error"));

                // Rollback the stack creation, since it is in an indeterminate state.
                if (!backout) {
                    logger.warn(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack errored, stack deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Create Stack error, stack deletion suppressed"));
                }
                else
                {
                    try {
                        logger.debug("Create Stack errored - attempting to DELETE stack: " + instanceId);
                        logger.debug("deletePollInterval=" + deletePollInterval + ", deletePollTimeout=" + deletePollTimeout);
                        StackInfo deleteInfo = deleteStack(cloudSiteId, tenantId, instanceId);
                        boolean deleted = false;
                        while (!deleted) {
                            try {
                                StackInfo queryInfo = queryStack(cloudSiteId, tenantId, instanceId);
                                logger.debug("Deleting " + instanceId + ", status: " + queryInfo.getStatus());
                                if (HeatStatus.DELETING.equals(queryInfo.getStatus())) {
                                    if (deletePollTimeout <= 0) {
                                        logger.error(String.format("%s %s %s %s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_TIMEOUT.toString(), cloudSiteId, tenantId, instanceId,
                                                queryInfo.getStatus(), "", "", MsoLogger.ErrorCode.AvailabilityError.getValue(),
                                                "Rollback: DELETE stack timeout"));
                                        break;
                                    } else {
                                        sleep(deletePollInterval * 1000L);
                                        deletePollTimeout -= deletePollInterval;
                                    }
                                } else if (HeatStatus.NOTFOUND.equals(queryInfo.getStatus())){
                                    logger.debug("DELETE_COMPLETE for " + instanceId);
                                    deleted = true;
                                    continue;
                                } else {
                                    //got a status other than DELETE_IN_PROGRESS or DELETE_COMPLETE - so break and evaluate
                                    logger.warn(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack errored, stack deletion FAILED", "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Create Stack error, stack deletion FAILED"));
                                    logger.debug("Stack deletion FAILED on a rollback of a create - " + instanceId + ", status=" + queryInfo.getStatus() + ", reason=" + queryInfo.getStatusMessage());
                                    break;
                                }
                            } catch (MsoException me2) {
                                // Just log this one. We will report the original exception.
                                logger.debug("Exception thrown trying to delete " + instanceId + " on a create->rollback: " + me2.getContextMessage(), me2);
                                logger.warn(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack errored, then stack deletion FAILED - exception thrown", "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), me2.getContextMessage()));
                            }
                        }
                        StringBuilder errorContextMessage;
                        if (createTimedOut) {
                            errorContextMessage = new StringBuilder("Stack Creation Timeout");
                        } else {
                            errorContextMessage  = stackErrorStatusReason;
                        }
                        if (deleted) {
                            errorContextMessage.append(" - stack successfully deleted");
                        } else {
                            errorContextMessage.append(" - encountered an error trying to delete the stack");
                        }
                    } catch (MsoException e2) {
                        // shouldn't happen - but handle
                        logger.error(String.format("%s %s %s %s %d %s", MessageEnum.RA_CREATE_STACK_ERR.toString(), "Create Stack: Nested exception rolling back stack: " + e2, "", "", MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Exception in Create Stack: rolling back stack"));
                    }
                }
                MsoOpenstackException me = new MsoOpenstackException(0, "", stackErrorStatusReason.toString());
                me.addContext(CREATE_STACK);
                alarmLogger.sendAlarm(HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
                throw me;
            }
        } else {
            // Get initial status, since it will have been null after the create.
            stackInfo = queryStack(cloudSiteId, tenantId, instanceId);
            logger.debug("Multicloud stack query status is: " + stackInfo.getStatus());
        }
        return stackInfo;
    }

    private HeatStatus mapResponseToHeatStatus(Response response) {
        if (response.getStatusInfo().getStatusCode() == Response.Status.OK.getStatusCode()) {
            return HeatStatus.CREATED;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.CREATED.getStatusCode()) {
            return HeatStatus.CREATED;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
            return HeatStatus.CREATED;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.BAD_REQUEST.getStatusCode()) {
            return HeatStatus.FAILED;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            return HeatStatus.FAILED;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
            return HeatStatus.NOTFOUND;
        } else if (response.getStatusInfo().getStatusCode() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            return HeatStatus.FAILED;
        } else {
            return HeatStatus.UNKNOWN;
        }
    }

    private MulticloudCreateResponse getCreateBody(java.io.InputStream in) {
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\Z");
        String body = "";
        if (scanner.hasNext()) {
            body = scanner.next();
        }
        scanner.close();

        try {
            return new ObjectMapper().readerFor(MulticloudCreateResponse.class).readValue(body);
        } catch (Exception e) {
            logger.debug("Exception retrieving multicloud vfModule POST response body " + e);
        }
        return null;
    }

    private MulticloudQueryResponse getQueryBody(java.io.InputStream in) {
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\Z");
        String body = "";
        if (scanner.hasNext()) {
            body = scanner.next();
        }
        scanner.close();

        try {
            return new ObjectMapper().readerFor(MulticloudQueryResponse.class).readValue(body);
        } catch (Exception e) {
            logger.debug("Exception retrieving multicloud workload query response body " + e);
        }
        return null;
    }

    private String getMulticloudEndpoint(String cloudSiteId, String workloadId) throws MsoCloudSiteNotFound {

        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(() -> new MsoCloudSiteNotFound(cloudSiteId));
        String endpoint = cloudSite.getIdentityService().getIdentityUrl();

        if (workloadId != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Multicloud Endpoint is: %s/%s", endpoint, workloadId));
            }
            return String.format("%s/%s", endpoint, workloadId);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Multicloud Endpoint is: %s", endpoint));
            }
            return endpoint;
        }
    }

    private RestClient getMulticloudClient(String endpoint) {
        RestClient client = null;
        try {
            client = new HttpClient(UriBuilder.fromUri(endpoint).build().toURL(),
                    MediaType.APPLICATION_JSON.toString(), TargetEntity.MULTICLOUD);
        } catch (MalformedURLException e) {
            logger.debug(String.format("Encountered malformed URL error getting multicloud rest client %s", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.debug(String.format("Encountered illegal argument getting multicloud rest client %s",e.getMessage()));
        } catch (UriBuilderException e) {
            logger.debug(String.format("Encountered URI builder error getting multicloud rest client %s", e.getMessage()));
        }
        return client;
    }

    private JsonNode getDirectiveNode(String directives) throws MsoException {
        try {
            return JSON_MAPPER.readTree(directives);
        } catch (Exception e) {
            logger.error(String.format("%s %s %s %s %d %s",
                    MessageEnum.RA_CREATE_STACK_ERR.toString(),
                    "Create Stack: " + e, "", "",
                    MsoLogger.ErrorCode.BusinessProcesssError.getValue(),
                    "Exception in Create Stack: Invalid JSON format of directives" + directives));
            MsoException me = new MsoAdapterException("Invalid JSON format of directives parameter: " + directives);
            me.addContext(CREATE_STACK);
            throw me;
        }
    }

    /**
     * VduPlugin interface for instantiate function.
     *
     * Translate the VduPlugin parameters to the corresponding 'createStack' parameters,
     * and then invoke the existing function.
     */
    @Override
    public VduInstance instantiateVdu (
            CloudInfo cloudInfo,
            String instanceName,
            Map<String,Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
        throws VduException
    {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        // Translate the VDU ModelInformation structure to that which is needed for
        // creating the Heat stack.  Loop through the artifacts, looking specifically
        // for MAIN_TEMPLATE and ENVIRONMENT.  Any other artifact will
        // be attached as a FILE.
        String heatTemplate = null;
        Map<String,Object> nestedTemplates = new HashMap<>();
        Map<String,Object> files = new HashMap<>();
        String heatEnvironment = null;

        for (VduArtifact vduArtifact: vduModel.getArtifacts()) {
            if (vduArtifact.getType() == ArtifactType.MAIN_TEMPLATE) {
                heatTemplate = new String(vduArtifact.getContent());
            }
            else if (vduArtifact.getType() == ArtifactType.NESTED_TEMPLATE) {
                nestedTemplates.put(vduArtifact.getName(), new String(vduArtifact.getContent()));
            }
            else if (vduArtifact.getType() == ArtifactType.ENVIRONMENT) {
                heatEnvironment = new String(vduArtifact.getContent());
            }
        }

        try {
            StackInfo stackInfo = createStack (cloudSiteId,
                    tenantId,
                    instanceName,
                    heatTemplate,
                    inputs,
                    true,    // poll for completion
                    vduModel.getTimeoutMinutes(),
                    heatEnvironment,
                    nestedTemplates,
                    files,
                    rollbackOnFailure);
            // Populate a vduInstance from the StackInfo
            return stackInfoToVduInstance(stackInfo);
        }
        catch (Exception e) {
            throw new VduException ("MsoMulticloudUtils (instantiateVDU): createStack Exception", e);
        }
    }


    /**
     * VduPlugin interface for query function.
     */
    @Override
    public VduInstance queryVdu (CloudInfo cloudInfo, String instanceId)
        throws VduException
    {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Query the Cloudify Deployment object and  populate a VduInstance
            StackInfo stackInfo = queryStack (cloudSiteId, tenantId, instanceId);

            return stackInfoToVduInstance(stackInfo);
        }
        catch (Exception e) {
            throw new VduException ("MsoMulticloudUtils (queryVdu): queryStack Exception ", e);
        }
    }


    /**
     * VduPlugin interface for delete function.
     */
    @Override
    public VduInstance deleteVdu (CloudInfo cloudInfo, String instanceId, int timeoutMinutes)
        throws VduException
    {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Delete the Multicloud stack
            StackInfo stackInfo = deleteStack (cloudSiteId, tenantId, instanceId);

            // Populate a VduInstance based on the deleted Cloudify Deployment object
            VduInstance vduInstance = stackInfoToVduInstance(stackInfo);

            // Override return state to DELETED (MulticloudUtils sets to NOTFOUND)
            vduInstance.getStatus().setState(VduStateType.DELETED);

            return vduInstance;
        }
        catch (Exception e) {
            throw new VduException ("Delete VDU Exception", e);
        }
    }


    /**
     * VduPlugin interface for update function.
     *
     * Update is currently not supported in the MsoMulticloudUtils implementation of VduPlugin.
     * Just return a VduException.
     *
     */
    @Override
    public VduInstance updateVdu (
            CloudInfo cloudInfo,
            String instanceId,
            Map<String,Object> inputs,
            VduModelInfo vduModel,
            boolean rollbackOnFailure)
        throws VduException
    {
        throw new VduException ("MsoMulticloudUtils: updateVdu interface not supported");
    }


    /*
     * Convert the local DeploymentInfo object (Cloudify-specific) to a generic VduInstance object
     */
    protected VduInstance stackInfoToVduInstance (StackInfo stackInfo)
    {
        VduInstance vduInstance = new VduInstance();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("StackInfo to convert: %s", stackInfo.getParameters().toString()));
        }
        // The full canonical name as the instance UUID
        vduInstance.setVduInstanceId(stackInfo.getCanonicalName());
        vduInstance.setVduInstanceName(stackInfo.getName());

        // Copy inputs and outputs
        vduInstance.setInputs(stackInfo.getParameters());
        vduInstance.setOutputs(stackInfo.getOutputs());

        // Translate the status elements
        vduInstance.setStatus(stackStatusToVduStatus (stackInfo));

        return vduInstance;
    }

    private VduStatus stackStatusToVduStatus (StackInfo stackInfo)
    {
        VduStatus vduStatus = new VduStatus();

        // Map the status fields to more generic VduStatus.
        // There are lots of HeatStatus values, so this is a bit long...
        HeatStatus heatStatus = stackInfo.getStatus();
        String statusMessage = stackInfo.getStatusMessage();
        logger.debug("HeatStatus = " + heatStatus + " msg = " + statusMessage);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Stack Status: %s", heatStatus.toString()));
            logger.debug(String.format("Stack Status Message: %s", statusMessage));
        }

        if (heatStatus == HeatStatus.INIT  ||  heatStatus == HeatStatus.BUILDING) {
            vduStatus.setState(VduStateType.INSTANTIATING);
            vduStatus.setLastAction((new PluginAction ("create", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.NOTFOUND) {
            vduStatus.setState(VduStateType.NOTFOUND);
        }
        else if (heatStatus == HeatStatus.CREATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction ("create", "complete", statusMessage)));
        }
        else if (heatStatus == HeatStatus.UPDATED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
            vduStatus.setLastAction((new PluginAction ("update", "complete", statusMessage)));
        }
        else if (heatStatus == HeatStatus.UPDATING) {
            vduStatus.setState(VduStateType.UPDATING);
            vduStatus.setLastAction((new PluginAction ("update", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.DELETING) {
            vduStatus.setState(VduStateType.DELETING);
            vduStatus.setLastAction((new PluginAction ("delete", "in_progress", statusMessage)));
        }
        else if (heatStatus == HeatStatus.FAILED) {
            vduStatus.setState(VduStateType.FAILED);
            vduStatus.setErrorMessage(stackInfo.getStatusMessage());
        } else {
            vduStatus.setState(VduStateType.UNKNOWN);
        }

        return vduStatus;
    }
}
