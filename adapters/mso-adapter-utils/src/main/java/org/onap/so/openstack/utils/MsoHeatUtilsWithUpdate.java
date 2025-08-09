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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.exceptions.MsoStackNotFound;
import org.onap.so.openstack.mappers.StackInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.heat.model.Stack.Output;
import com.woorea.openstack.heat.model.UpdateStackParam;

@Component
public class MsoHeatUtilsWithUpdate extends MsoHeatUtils {

    private static final String UPDATE_STACK = "UpdateStack";
    private static final Logger logger = LoggerFactory.getLogger(MsoHeatUtilsWithUpdate.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static final String EXCEPTION = "Exception :";

    @Autowired
    private Environment environment;
    /*
     * Keep these methods around for backward compatibility
     */

    public StackInfo updateStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName,
            String heatTemplate, Map<String, Object> stackInputs, boolean pollForCompletion, int timeoutMinutes)
            throws MsoException {
        // Keeping this method to allow compatibility with no environment or files variable sent. In this case,
        // simply return the new method with the environment variable set to null.
        return this.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate, stackInputs,
                pollForCompletion, timeoutMinutes, null, null, null);
    }

    public StackInfo updateStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName,
            String heatTemplate, Map<String, Object> stackInputs, boolean pollForCompletion, int timeoutMinutes,
            String environment) throws MsoException {
        // Keeping this method to allow compatibility with no environment variable sent. In this case,
        // simply return the new method with the files variable set to null.
        return this.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate, stackInputs,
                pollForCompletion, timeoutMinutes, environment, null, null);
    }

    public StackInfo updateStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName,
            String heatTemplate, Map<String, Object> stackInputs, boolean pollForCompletion, int timeoutMinutes,
            String environment, Map<String, Object> files) throws MsoException {
        return this.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate, stackInputs,
                pollForCompletion, timeoutMinutes, environment, files, null);
    }

    /**
     * Update a Stack in the specified cloud location and tenant. The Heat template and parameter map are passed in as
     * arguments, along with the cloud access credentials. It is expected that parameters have been validated and
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
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param cloudSiteId The cloud identifier (may be a region) in which to create the tenant.
     * @param stackName The name of the stack to update
     * @param heatTemplate The Heat template
     * @param stackInputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param environment An optional yaml-format string to specify environmental parameters
     * @param files a Map<String, Object> for listing child template IDs
     * @param heatFiles a Map<String, Object> for listing get_file entries (fileName, fileBody)
     * @return A StackInfo object
     * @throws MsoException Thrown if the Openstack API call returns an exception.
     */

    public StackInfo updateStack(String cloudSiteId, String cloudOwner, String tenantId, String stackName,
            String heatTemplate, Map<String, Object> stackInputs, boolean pollForCompletion, int timeoutMinutes,
            String environment, Map<String, Object> files, Map<String, Object> heatFiles) throws MsoException {
        boolean heatEnvtVariable = true;
        if (environment == null || "".equalsIgnoreCase(environment.trim())) {
            heatEnvtVariable = false;
        }
        boolean haveFiles = true;
        if (files == null || files.isEmpty()) {
            haveFiles = false;
        }
        boolean haveHeatFiles = true;
        if (heatFiles == null || heatFiles.isEmpty()) {
            haveHeatFiles = false;
        }

        Heat heatClient = getHeatClient(cloudSiteId, tenantId);

        // Perform a query first to get the current status
        Stack heatStack = queryHeatStack(heatClient, stackName);
        if (heatStack == null || "DELETE_COMPLETE".equals(heatStack.getStackStatus())) {
            // Not found. Return a StackInfo with status NOTFOUND
            throw new MsoStackNotFound(stackName, tenantId, cloudSiteId);
        }

        // Use canonical name "<stack name>/<stack-id>" to update the stack.
        // Otherwise, update by name returns a 302 redirect.
        // NOTE: This is specific to the v1 Orchestration API.
        String canonicalName = heatStack.getStackName() + "/" + heatStack.getId();

        logger.debug("Ready to Update Stack ({}) with input params: {}", canonicalName, stackInputs);
        // force entire stackInput object to generic Map<String, Object> for openstack compatibility
        Map<String, Object> normalized = new HashMap<>();
        try {
            normalized = JSON_MAPPER.readValue(JSON_MAPPER.writeValueAsString(stackInputs),
                    new TypeReference<HashMap<String, Object>>() {});
        } catch (IOException e1) {
            logger.debug("could not map json", e1);
        }
        // Build up the stack update parameters
        // Disable auto-rollback, because error reason is lost. Always rollback in the code.
        UpdateStackParam stack = new UpdateStackParam();
        stack.setTimeoutMinutes(timeoutMinutes);
        stack.setParameters(normalized);
        stack.setTemplate(heatTemplate);
        stack.setDisableRollback(true);
        // TJM add envt to stack
        if (heatEnvtVariable) {
            stack.setEnvironment(environment);
        }

        // Handle nested templates & get_files here. if we have both - must combine
        // and then add to stack (both are part of "files:" being added to stack)
        if (haveFiles && haveHeatFiles) {
            // Let's do this here - not in the bean
            logger.debug("Found files AND heatFiles - combine and add!");
            Map<String, Object> combinedFiles = new HashMap<>();
            for (String keyString : files.keySet()) {
                combinedFiles.put(keyString, files.get(keyString));
            }
            for (String keyString : heatFiles.keySet()) {
                combinedFiles.put(keyString, heatFiles.get(keyString));
            }
            stack.setFiles(combinedFiles);
        } else {
            // Handle case where we have one or neither
            if (haveFiles) {
                stack.setFiles(files);
            }
            if (haveHeatFiles) {
                // setFiles method modified to handle adding a map.
                stack.setFiles(heatFiles);
            }
        }

        try {
            // Execute the actual Openstack command to update the Heat stack
            OpenStackRequest<Void> request = heatClient.getStacks().update(canonicalName, stack);
            executeAndRecordOpenstackRequest(request);
        } catch (OpenStackBaseException e) {
            // Since this came on the 'Update Stack' command, nothing was changed
            // in the cloud. Rethrow the error as an MSO exception.
            throw heatExceptionToMsoException(e, UPDATE_STACK);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException(e, UPDATE_STACK);
        }

        // If client has requested a final response, poll for stack completion
        Stack updateStack = null;
        if (pollForCompletion) {
            // Set a time limit on overall polling.
            // Use the resource (template) timeout for Openstack (expressed in minutes)
            // and add one poll interval to give Openstack a chance to fail on its own.
            int createPollInterval = Integer
                    .parseInt(this.environment.getProperty(createPollIntervalProp, CREATE_POLL_INTERVAL_DEFAULT));
            int pollTimeout = (timeoutMinutes * 60) + createPollInterval;

            boolean loopAgain = true;
            while (loopAgain) {
                try {
                    updateStack = queryHeatStack(heatClient, canonicalName);
                    logger.debug("{} ({}) ", updateStack.getStackStatus(), canonicalName);
                    try {
                        logger.debug("Current stack {}",
                                this.getOutputsAsStringBuilderWithUpdate(heatStack).toString());
                    } catch (Exception e) {
                        logger.debug("an error occurred trying to print out the current outputs of the stack", e);
                    }


                    if ("UPDATE_IN_PROGRESS".equals(updateStack.getStackStatus())) {
                        // Stack update is still running.
                        // Sleep and try again unless timeout has been reached
                        if (pollTimeout <= 0) {
                            // Note that this should not occur, since there is a timeout specified
                            // in the Openstack call.
                            logger.error(
                                    "{} Cloud site: {} Tenant: {} Stack: {} Stack status: {} {} Update stack timeout",
                                    MessageEnum.RA_UPDATE_STACK_TIMEOUT, cloudSiteId, tenantId, stackName,
                                    updateStack.getStackStatus(), ErrorCode.AvailabilityError.getValue());
                            loopAgain = false;
                        } else {
                            try {
                                Thread.sleep(createPollInterval * 1000L);
                            } catch (InterruptedException e) {
                                // If we are interrupted, we should stop ASAP.
                                loopAgain = false;
                                // Set again the interrupted flag
                                Thread.currentThread().interrupt();
                            }
                        }
                        pollTimeout -= createPollInterval;
                        logger.debug("pollTimeout remaining: {}", pollTimeout);
                    } else {
                        loopAgain = false;
                    }
                } catch (MsoException e) {
                    // Cannot query the stack. Something is wrong.

                    // TODO: No way to roll back the stack at this point. What to do?
                    e.addContext(UPDATE_STACK);
                    throw e;
                }
            }

            if (!"UPDATE_COMPLETE".equals(updateStack.getStackStatus())) {
                logger.error("{} Stack status: {} Stack status reason: {} {} Update Stack error",
                        MessageEnum.RA_UPDATE_STACK_ERR, updateStack.getStackStatus(),
                        updateStack.getStackStatusReason(), ErrorCode.DataError.getValue());

                // TODO: No way to roll back the stack at this point. What to do?
                // Throw a 'special case' of MsoOpenstackException to report the Heat status
                MsoOpenstackException me = null;
                if ("UPDATE_IN_PROGRESS".equals(updateStack.getStackStatus())) {
                    me = new MsoOpenstackException(0, "", "Stack Update Timeout");
                } else {
                    String error =
                            "Stack error (" + updateStack.getStackStatus() + "): " + updateStack.getStackStatusReason();
                    me = new MsoOpenstackException(0, "", error);
                }
                me.addContext(UPDATE_STACK);
                throw me;
            }

        } else {
            // Return the current status.
            updateStack = queryHeatStack(heatClient, canonicalName);
            if (updateStack != null) {
                logger.debug("UpdateStack, status = {}", updateStack.getStackStatus());
            } else {
                logger.debug("UpdateStack, stack not found");
            }
        }
        return new StackInfoMapper(updateStack).map();
    }

    private StringBuilder getOutputsAsStringBuilderWithUpdate(Stack heatStack) {
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
                sb.append(this.convertNodeWithUpdate((JsonNode) obj)).append(" (a JsonNode)");
            } else if (obj instanceof java.util.LinkedHashMap) {
                try {
                    String str = JSON_MAPPER.writeValueAsString(obj);
                    sb.append(str).append(" (a java.util.LinkedHashMap)");
                } catch (Exception e) {
                    logger.debug(EXCEPTION, e);
                    sb.append("(a LinkedHashMap value that would not convert nicely)");
                }
            } else if (obj instanceof Integer) {
                String str = "";
                try {
                    str = obj.toString() + " (an Integer)\n";
                } catch (Exception e) {
                    logger.debug(EXCEPTION, e);
                    str = "(an Integer unable to call .toString() on)";
                }
                sb.append(str);
            } else if (obj instanceof ArrayList) {
                String str = "";
                try {
                    str = obj.toString() + " (an ArrayList)";
                } catch (Exception e) {
                    logger.debug(EXCEPTION, e);
                    str = "(an ArrayList unable to call .toString() on?)";
                }
                sb.append(str);
            } else if (obj instanceof Boolean) {
                String str = "";
                try {
                    str = obj.toString() + " (a Boolean)";
                } catch (Exception e) {
                    logger.debug(EXCEPTION, e);
                    str = "(an Boolean unable to call .toString() on?)";
                }
                sb.append(str);
            } else {
                String str = "";
                try {
                    str = obj.toString() + " (unknown Object type)";
                } catch (Exception e) {
                    logger.debug(EXCEPTION, e);
                    str = "(a value unable to call .toString() on?)";
                }
                sb.append(str);
            }
            sb.append("\n");
        }
        sb.append("[END]");
        return sb;
    }

    private String convertNodeWithUpdate(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.debug("Error converting json to string {} ", e.getMessage(), e);
        }
        return "[Error converting json to string]";
    }

}
