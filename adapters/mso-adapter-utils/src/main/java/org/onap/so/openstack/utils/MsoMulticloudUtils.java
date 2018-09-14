/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.cloud.authentication.AuthenticationMethodFactory;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.beans.HeatCacheEntry;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.exceptions.MsoStackAlreadyExists;
import org.onap.so.openstack.exceptions.MsoTenantNotFound;
import org.onap.so.openstack.mappers.StackInfoMapper;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.heat.model.Stack.Output;
import com.woorea.openstack.heat.model.Stacks;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.utils.KeystoneUtils;

@Component
public class MsoMulticloudUtils extends MsoHeatUtils implements VduPlugin{

    private static final String TOKEN_AUTH = "TokenAuth";

    private static final String QUERY_ALL_STACKS = "QueryAllStacks";

    private static final String DELETE_STACK = "DeleteStack";

    private static final String HEAT_ERROR = "HeatError";

    private static final String CREATE_STACK = "CreateStack";

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, MsoMulticloudUtils.class);

    @Autowired
    private Environment environment;

    @Autowired
    private AuthenticationMethodFactory authenticationMethodFactory;

    @Autowired
    private MsoTenantUtilsFactory tenantUtilsFactory;

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
     * @param backout Donot delete stack on create Failure - defaulted to True
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */

    @SuppressWarnings("unchecked")
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

        // EWMMC - Get out the directives, if present.
    	String oof_directives = null;
    	String sdnc_directives = null;

		String key = "oof_directives";
		if (!stackInputs.isEmpty() && stackInputs.containsKey(key)) {
			oof_directives = (String) stackInputs.get(key);
			stackInputs.remove(key);
		}
		key = "sdnc_directives";
		if (!stackInputs.isEmpty() && stackInputs.containsKey(key)) {
			oof_directives = (String) stackInputs.get(key);
			stackInputs.remove(key);
		}

    	CreateStackParam stack = createStackParam(stackName, heatTemplate, stackInputs, timeoutMinutes, environment, files, heatFiles);

        // EWMMC - this is where we need to call the multicloud adapter instead of heat
        Stack heatStack = null;
        try {
            // Execute the actual Openstack command to create the Heat stack
            OpenStackRequest <Stack> request = heatClient.getStacks ().create (stack);
            // Begin X-Auth-User
            // Obtain an MSO token for the tenant
            CloudIdentity cloudIdentity = cloudSite.getIdentityService();
            // cloudIdentity.getMsoId(), cloudIdentity.getMsoPass()
            //req
            request.header ("X-Auth-User", cloudIdentity.getMsoId ());
            request.header ("X-Auth-Key", CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass ()));
            LOGGER.debug ("headers added, about to executeAndRecordOpenstackRequest");
            //LOGGER.debug(this.requestToStringBuilder(stack).toString());
            // END - try to fix X-Auth-User
            heatStack = executeAndRecordOpenstackRequest (request);
        } catch (OpenStackResponseException e) {
            // Since this came on the 'Create Stack' command, nothing was changed
            // in the cloud. Return the error as an exception.
            if (e.getStatus () == 409) {
                // Stack already exists. Return a specific error for this case
                MsoStackAlreadyExists me = new MsoStackAlreadyExists (stackName, tenantId, cloudSiteId);
                me.addContext (CREATE_STACK);
                throw me;
            } else {
                // Convert the OpenStackResponseException to an MsoOpenstackException
            	LOGGER.debug("ERROR STATUS = " + e.getStatus() + ",\n" + e.getMessage() + "\n" + e.getLocalizedMessage());
                throw heatExceptionToMsoException (e, CREATE_STACK);
            }
        } catch (OpenStackConnectException e) {
            // Error connecting to Openstack instance. Convert to an MsoException
            throw heatExceptionToMsoException (e, CREATE_STACK);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, CREATE_STACK);
        }

        // Subsequent access by the canonical name "<stack name>/<stack-id>".
        // Otherwise, simple query by name returns a 302 redirect.
        // NOTE: This is specific to the v1 Orchestration API.
        String canonicalName = stackName + "/" + heatStack.getId ();

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
            LOGGER.debug("createPollInterval=" + createPollInterval + ", pollTimeout=" + pollTimeout);

            while (true) {
                try {
                    heatStack = queryHeatStack (heatClient, canonicalName);
// EWMMC                    LOGGER.debug (heatStack.getStackStatus () + " (" + canonicalName + ")");
//                    try {
//                        LOGGER.debug("Current stack " + this.getOutputsAsStringBuilder(heatStack).toString());
//                    } catch (Exception e) {
//                        LOGGER.debug("an error occurred trying to print out the current outputs of the stack", e);
//                    }

                    if ("CREATE_IN_PROGRESS".equals (heatStack.getStackStatus ())) {
                        // Stack creation is still running.
                        // Sleep and try again unless timeout has been reached
                        if (pollTimeout <= 0) {
                            // Note that this should not occur, since there is a timeout specified
                            // in the Openstack call.
                            LOGGER.error (MessageEnum.RA_CREATE_STACK_TIMEOUT, cloudSiteId, tenantId, stackName, heatStack.getStackStatus (), "", "", MsoLogger.ErrorCode.AvailabilityError, "Create stack timeout");
                            createTimedOut = true;
                            break;
                        }

                        sleep(createPollInterval * 1000L);

                        pollTimeout -= createPollInterval;
                		LOGGER.debug("pollTimeout remaining: " + pollTimeout);
                    } else {
                    	//save off the status & reason msg before we attempt delete
                    	stackErrorStatusReason.append("Stack error (" + heatStack.getStackStatus() + "): " + heatStack.getStackStatusReason());
                        break;
                    }
                } catch (MsoException me) {
                	// Cannot query the stack status. Something is wrong.
                	// Try to roll back the stack
                	if (!backout)
                	{
                		LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Create Stack errored, stack deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in Create Stack, stack deletion suppressed");
                	}
                	else
                	{
                		try {
                			LOGGER.debug("Create Stack error - unable to query for stack status - attempting to delete stack: " + canonicalName + " - This will likely fail and/or we won't be able to query to see if delete worked");
                			OpenStackRequest <Void> request = heatClient.getStacks ().deleteByName (canonicalName);
                			executeAndRecordOpenstackRequest (request);
                			// this may be a waste of time - if we just got an exception trying to query the stack - we'll just
                			// get another one, n'est-ce pas?
                			boolean deleted = false;
                			while (!deleted) {
                				try {
                					heatStack = queryHeatStack(heatClient, canonicalName);
                					if (heatStack != null) {
                    					LOGGER.debug(heatStack.getStackStatus());
                    					if ("DELETE_IN_PROGRESS".equals(heatStack.getStackStatus())) {
                    						if (deletePollTimeout <= 0) {
                    							LOGGER.error (MessageEnum.RA_CREATE_STACK_TIMEOUT, cloudSiteId, tenantId, stackName,
                    									heatStack.getStackStatus (), "", "", MsoLogger.ErrorCode.AvailabilityError,
                    									"Rollback: DELETE stack timeout");
                    							break;
                    						} else {
                    							sleep(deletePollInterval * 1000L);
                    							deletePollTimeout -= deletePollInterval;
                    						}
                    					} else if ("DELETE_COMPLETE".equals(heatStack.getStackStatus())){
                    						LOGGER.debug("DELETE_COMPLETE for " + canonicalName);
                    						deleted = true;
                    						continue;
                    					} else {
                    						//got a status other than DELETE_IN_PROGRESS or DELETE_COMPLETE - so break and evaluate
                    						break;
                    					}
                    				} else {
                    					// assume if we can't find it - it's deleted
                    					LOGGER.debug("heatStack returned null - assume the stack " + canonicalName + " has been deleted");
                    					deleted = true;
                    					continue;
                					}

                				} catch (Exception e3) {
                					// Just log this one. We will report the original exception.
                					LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack: Nested exception rolling back stack: " + e3, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack: Nested exception rolling back stack on error on query");

                				}
                			}
                		} catch (Exception e2) {
                			// Just log this one. We will report the original exception.
                			LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack: Nested exception rolling back stack: " + e2, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack: Nested exception rolling back stack");
                		}
                	}

                    // Propagate the original exception from Stack Query.
                    me.addContext (CREATE_STACK);
                    throw me;
                }
            }

            if (!"CREATE_COMPLETE".equals (heatStack.getStackStatus ())) {
                LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack error:  Polling complete with non-success status: "
                              + heatStack.getStackStatus () + ", " + heatStack.getStackStatusReason (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack error");

                // Rollback the stack creation, since it is in an indeterminate state.
                if (!backout)
                {
                	LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Create Stack errored, stack deletion suppressed", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack error, stack deletion suppressed");
                }
                else
                {
                	try {
                		LOGGER.debug("Create Stack errored - attempting to DELETE stack: " + canonicalName);
                		LOGGER.debug("deletePollInterval=" + deletePollInterval + ", deletePollTimeout=" + deletePollTimeout);
                		OpenStackRequest <Void> request = heatClient.getStacks ().deleteByName (canonicalName);
                		executeAndRecordOpenstackRequest (request);
                		boolean deleted = false;
                		while (!deleted) {
                			try {
                				heatStack = queryHeatStack(heatClient, canonicalName);
                				if (heatStack != null) {
                					LOGGER.debug(heatStack.getStackStatus() + " (" + canonicalName + ")");
                					if ("DELETE_IN_PROGRESS".equals(heatStack.getStackStatus())) {
                						if (deletePollTimeout <= 0) {
                							LOGGER.error (MessageEnum.RA_CREATE_STACK_TIMEOUT, cloudSiteId, tenantId, stackName,
                									heatStack.getStackStatus (), "", "", MsoLogger.ErrorCode.AvailabilityError,
                									"Rollback: DELETE stack timeout");
                							break;
                						} else {
                							sleep(deletePollInterval * 1000L);
                							deletePollTimeout -= deletePollInterval;
                							LOGGER.debug("deletePollTimeout remaining: " + deletePollTimeout);
                						}
                					} else if ("DELETE_COMPLETE".equals(heatStack.getStackStatus())){
                						LOGGER.debug("DELETE_COMPLETE for " + canonicalName);
                						deleted = true;
                						continue;
                					} else if ("DELETE_FAILED".equals(heatStack.getStackStatus())) {
                						// Warn about this (?) - but still throw the original exception
                						LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Create Stack errored, stack deletion FAILED", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create Stack error, stack deletion FAILED");
                						LOGGER.debug("Stack deletion FAILED on a rollback of a create - " + canonicalName + ", status=" + heatStack.getStackStatus() + ", reason=" + heatStack.getStackStatusReason());
                						break;
                					} else {
                						//got a status other than DELETE_IN_PROGRESS or DELETE_COMPLETE - so break and evaluate
                						break;
                					}
                				} else {
                					// assume if we can't find it - it's deleted
                					LOGGER.debug("heatStack returned null - assume the stack " + canonicalName + " has been deleted");
                					deleted = true;
                					continue;
                				}

                			} catch (MsoException me2) {
                				// We got an exception on the delete - don't throw this exception - throw the original - just log.
                				LOGGER.debug("Exception thrown trying to delete " + canonicalName + " on a create->rollback: " + me2.getContextMessage(), me2);
                				LOGGER.warn(MessageEnum.RA_CREATE_STACK_ERR, "Create Stack errored, then stack deletion FAILED - exception thrown", "", "", MsoLogger.ErrorCode.BusinessProcesssError, me2.getContextMessage());
                			}

                		} // end while !deleted
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
//                		MsoOpenstackException me = new MsoOpenstackException(0, "", stackErrorStatusReason.toString());
 //               		me.addContext(CREATE_STACK);
  //              		alarmLogger.sendAlarm(HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
   //             		throw me;
                	} catch (Exception e2) {
                		// shouldn't happen - but handle
                		LOGGER.error (MessageEnum.RA_CREATE_STACK_ERR, "Create Stack: Nested exception rolling back stack: " + e2, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in Create Stack: rolling back stack");
                	}
                }
                MsoOpenstackException me = new MsoOpenstackException(0, "", stackErrorStatusReason.toString());
                me.addContext(CREATE_STACK);
                alarmLogger.sendAlarm(HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage());
                throw me;
            }

        } else {
            // Get initial status, since it will have been null after the create.
            heatStack = queryHeatStack (heatClient, canonicalName);
            LOGGER.debug (heatStack.getStackStatus ());
        }

        return new StackInfoMapper(heatStack).map();
    }

    /**
     * Query for a single stack (by Name) in a tenant. This call will always return a
     * StackInfo object. If the stack does not exist, an "empty" StackInfo will be
     * returned - containing only the stack name and a status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @param stackName The name of the stack to query (may be simple or canonical)
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    public StackInfo queryStack (String cloudSiteId, String tenantId, String stackName) throws MsoException {
        LOGGER.debug ("Query HEAT stack: " + stackName + " in tenant " + tenantId);

        // Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        LOGGER.debug("Found: " + cloudSite.toString());

        // EWMMC - deleted code to get a Heat heatClient - do we need some kind of multicloud client  instead

        // Query the Stack.
        // An MsoException will propagate transparently to the caller.
        // EWMMC - will need to change to call multicloud
        Stack heatStack = null; //queryHeatStack (heatClient, stackName);

        if (heatStack == null) {
            // Stack does not exist. Return a StackInfo with status NOTFOUND
            return new StackInfo (stackName, HeatStatus.NOTFOUND);
        }

        return new StackInfoMapper(heatStack).map();
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
			Map<String,Object> inputs,  // EWMMC - will OOF items come as part of this ?
			VduModelInfo vduModel,      //         or would it be part of this - shouldn't be - OOF info is not 'model'
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
                    true,	// poll for completion
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
    		StackInfo stackInfo = deleteStack (tenantId, cloudSiteId, instanceId, true);

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
    private VduInstance stackInfoToVduInstance (StackInfo stackInfo)
    {
    	VduInstance vduInstance = new VduInstance();

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

    private void sleep(long time) {
    	try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.debug ("Thread interrupted while sleeping", e);
            Thread.currentThread().interrupt();
        }
    }
}
