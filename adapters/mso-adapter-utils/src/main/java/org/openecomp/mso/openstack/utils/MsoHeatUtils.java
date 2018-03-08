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

package org.openecomp.mso.openstack.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.openecomp.mso.openstack.exceptions.MsoStackAlreadyExists;
import org.openecomp.mso.openstack.exceptions.MsoTenantNotFound;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
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

public class MsoHeatUtils extends MsoCommonUtils {

	private MsoPropertiesFactory msoPropertiesFactory;

	private CloudConfigFactory cloudConfigFactory;

    private static final String TOKEN_AUTH = "TokenAuth";

    private static final String QUERY_ALL_STACKS = "QueryAllStacks";

    private static final String DELETE_STACK = "DeleteStack";

    private static final String HEAT_ERROR = "HeatError";

    private static final String CREATE_STACK = "CreateStack";

    // Cache Heat Clients statically. Since there is just one MSO user, there is no
    // benefit to re-authentication on every request (or across different flows). The
    // token will be used until it expires.
    //
    // The cache key is "tenantId:cloudId"
    private static Map <String, HeatCacheEntry> heatClientCache = new HashMap <> ();

    // Fetch cloud configuration each time (may be cached in CloudConfig class)
    protected CloudConfig cloudConfig;

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    protected MsoJavaProperties msoProps = null;

    // Properties names and variables (with default values)
    protected String createPollIntervalProp = "ecomp.mso.adapters.heat.create.pollInterval";
    private String deletePollIntervalProp = "ecomp.mso.adapters.heat.delete.pollInterval";
    private String deletePollTimeoutProp = "ecomp.mso.adapters.heat.delete.pollTimeout";

    protected int createPollIntervalDefault = 15;
    private int deletePollIntervalDefault = 15;
    private int deletePollTimeoutDefault = 300;
    private String msoPropID;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * This constructor MUST be used ONLY in the JUNIT tests, not for real code.
     * The MsoPropertiesFactory will be added by EJB injection.
     *
     * @param msoPropID ID of the mso pro config as defined in web.xml
     * @param msoPropFactory The mso properties factory instanciated by EJB injection
     * @param cloudConfFactory the Cloud Config instantiated by EJB injection
     */
    public MsoHeatUtils (String msoPropID, MsoPropertiesFactory msoPropFactory, CloudConfigFactory cloudConfFactory) {
    	msoPropertiesFactory = msoPropFactory;
    	cloudConfigFactory = cloudConfFactory;
    	this.msoPropID = msoPropID;
    	// Dynamically get properties each time (in case reloaded).

    	try {
			msoProps = msoPropertiesFactory.getMsoJavaProperties (msoPropID);
		} catch (MsoPropertiesException e) {
			LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Mso Properties ID not found in cache: " + msoPropID, "", "", MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
		}
        cloudConfig = cloudConfigFactory.getCloudConfig ();
        LOGGER.debug("MsoHeatUtils:" + msoPropID);

    }


    /**
     * keep this old method signature here to maintain backwards compatibility. keep others as well.
     * this method does not include environment, files, or heatFiles
     */
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes) throws MsoException {
        // Just call the new method with the environment & files variable set to null
        return this.createStack (cloudSiteId,
                                 tenantId,
                                 stackName,
                                 heatTemplate,
                                 stackInputs,
                                 pollForCompletion,
                                 timeoutMinutes,
                                 null,
                                 null,
                                 null,
                                 true);
    }

    // This method has environment, but not files or heatFiles
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  String environment) throws MsoException {
        // Just call the new method with the files/heatFiles variables set to null
        return this.createStack (cloudSiteId,
                                 tenantId,
                                 stackName,
                                 heatTemplate,
                                 stackInputs,
                                 pollForCompletion,
                                 timeoutMinutes,
                                 environment,
                                 null,
                                 null,
                                 true);
    }

    // This method has environment and files, but not heatFiles.
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  String environment,
                                  Map <String, Object> files) throws MsoException {
        return this.createStack (cloudSiteId,
                                 tenantId,
                                 stackName,
                                 heatTemplate,
                                 stackInputs,
                                 pollForCompletion,
                                 timeoutMinutes,
                                 environment,
                                 files,
                                 null,
                                 true);
    }

    // This method has environment, files, heatfiles
    public StackInfo createStack (String cloudSiteId,
                                  String tenantId,
                                  String stackName,
                                  String heatTemplate,
                                  Map <String, ?> stackInputs,
                                  boolean pollForCompletion,
                                  int timeoutMinutes,
                                  String environment,
                                  Map <String, Object> files,
                                  Map <String, Object> heatFiles) throws MsoException {
        return this.createStack (cloudSiteId,
                                 tenantId,
                                 stackName,
                                 heatTemplate,
                                 stackInputs,
                                 pollForCompletion,
                                 timeoutMinutes,
                                 environment,
                                 files,
                                 heatFiles,
                                 true);
    }

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
        // Create local variables checking to see if we have an environment, nested, get_files
        // Could later add some checks to see if it's valid.
        boolean haveEnvtVariable = true;
        if (environment == null || "".equalsIgnoreCase (environment.trim ())) {
            haveEnvtVariable = false;
            LOGGER.debug ("createStack called with no environment variable");
        } else {
            LOGGER.debug ("createStack called with an environment variable: " + environment);
        }

        boolean haveFiles = true;
        if (files == null || files.isEmpty ()) {
            haveFiles = false;
            LOGGER.debug ("createStack called with no files / child template ids");
        } else {
            LOGGER.debug ("createStack called with " + files.size () + " files / child template ids");
        }

        boolean haveHeatFiles = true;
        if (heatFiles == null || heatFiles.isEmpty ()) {
            haveHeatFiles = false;
            LOGGER.debug ("createStack called with no heatFiles");
        } else {
            LOGGER.debug ("createStack called with " + heatFiles.size () + " heatFiles");
        }

        // Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        LOGGER.debug("Found: " + cloudSite.toString());
        // Get a Heat client. They are cached between calls (keyed by tenantId:cloudId)
        // This could throw MsoTenantNotFound or MsoOpenstackException (both propagated)
        Heat heatClient = getHeatClient (cloudSite, tenantId);
        if (heatClient != null) {
        	LOGGER.debug("Found: " + heatClient.toString());
        }

        LOGGER.debug ("Ready to Create Stack (" + heatTemplate + ") with input params: " + stackInputs);

        // Build up the stack to create
        // Disable auto-rollback, because error reason is lost. Always rollback in the code.
        CreateStackParam stack = new CreateStackParam ();
        stack.setStackName (stackName);
        stack.setTimeoutMinutes (timeoutMinutes);
        stack.setParameters ((Map <String, Object>) stackInputs);
        stack.setTemplate (heatTemplate);
        stack.setDisableRollback (true);
        // TJM New for PO Adapter - add envt variable
        if (haveEnvtVariable) {
            LOGGER.debug ("Found an environment variable - value: " + environment);
            stack.setEnvironment (environment);
        }
        // Now handle nested templates or get_files - have to combine if we have both
        // as they're both treated as "files:" on the stack.
        if (haveFiles && haveHeatFiles) {
            // Let's do this here - not in the bean
            LOGGER.debug ("Found files AND heatFiles - combine and add!");
            Map <String, Object> combinedFiles = new HashMap <> ();
            for (String keyString : files.keySet ()) {
                combinedFiles.put (keyString, files.get (keyString));
            }
            for (String keyString : heatFiles.keySet ()) {
                combinedFiles.put (keyString, heatFiles.get (keyString));
            }
            stack.setFiles (combinedFiles);
        } else {
            // Handle if we only have one or neither:
            if (haveFiles) {
                LOGGER.debug ("Found files - adding to stack");
                stack.setFiles (files);
            }
            if (haveHeatFiles) {
                LOGGER.debug ("Found heatFiles - adding to stack");
                // the setFiles was modified to handle adding the entries
                stack.setFiles (heatFiles);
            }
        }

        Stack heatStack = null;
        try {
            // Execute the actual Openstack command to create the Heat stack
            OpenStackRequest <Stack> request = heatClient.getStacks ().create (stack);
            // Begin X-Auth-User
            // Obtain an MSO token for the tenant
            CloudIdentity cloudIdentity = cloudSite.getIdentityService ();
            // cloudIdentity.getMsoId(), cloudIdentity.getMsoPass()
            //req
            request.header ("X-Auth-User", cloudIdentity.getMsoId ());
            request.header ("X-Auth-Key", cloudIdentity.getMsoPass ());
            LOGGER.debug ("headers added, about to executeAndRecordOpenstackRequest");
            LOGGER.debug(this.requestToStringBuilder(stack).toString());
            // END - try to fix X-Auth-User
            heatStack = executeAndRecordOpenstackRequest (request, msoProps);
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
            // and add one poll interval to give Openstack a chance to fail on its own.
            int createPollInterval = msoProps.getIntProperty (createPollIntervalProp, createPollIntervalDefault);
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
                    LOGGER.debug (heatStack.getStackStatus () + " (" + canonicalName + ")");
                    try {
                        LOGGER.debug("Current stack " + this.getOutputsAsStringBuilder(heatStack).toString());
                    } catch (Exception e) {
                        LOGGER.debug("an error occurred trying to print out the current outputs of the stack", e);
                    }

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
                        try {
                            Thread.sleep (createPollInterval * 1000L);
                        } catch (InterruptedException e) {
                            LOGGER.debug ("Thread interrupted while sleeping", e);
                        }

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
                			executeAndRecordOpenstackRequest (request, msoProps);
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
                    							try {
                    								Thread.sleep(deletePollInterval * 1000L);
                    							} catch (InterruptedException ie) {
                    								LOGGER.debug("Thread interrupted while sleeping", ie);
                    							}
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
                		executeAndRecordOpenstackRequest (request, msoProps);
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
                							try {
                								Thread.sleep(deletePollInterval * 1000L);
                							} catch (InterruptedException ie) {
                								LOGGER.debug("Thread interrupted while sleeping", ie);
                							}
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

        return new StackInfo (heatStack);
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

        // Get a Heat client. They are cached between calls (keyed by tenantId:cloudId)
        Heat heatClient = null;
        try {
            heatClient = getHeatClient (cloudSite, tenantId);
            if (heatClient != null) {
            	LOGGER.debug("Found: " + heatClient.toString());
            }
        } catch (MsoTenantNotFound e) {
            // Tenant doesn't exist, so stack doesn't either
            LOGGER.debug ("Tenant with id " + tenantId + "not found.", e);
            return new StackInfo (stackName, HeatStatus.NOTFOUND);
        } catch (MsoException me) {
            // Got an Openstack error. Propagate it
            LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Exception on Token request: " + me, "Openstack", "", MsoLogger.ErrorCode.AvailabilityError, "Connection Exception");
            me.addContext ("QueryStack");
            throw me;
        }

        // Query the Stack.
        // An MsoException will propagate transparently to the caller.
        Stack heatStack = queryHeatStack (heatClient, stackName);

        if (heatStack == null) {
            // Stack does not exist. Return a StackInfo with status NOTFOUND
            StackInfo stackInfo = new StackInfo (stackName, HeatStatus.NOTFOUND);
            return stackInfo;
        }

        return new StackInfo (heatStack);
    }

    /**
     * Delete a stack (by Name/ID) in a tenant. If the stack is not found, it will be
     * considered a successful deletion. The return value is a StackInfo object which
     * contains the current stack status.
     *
     * The client may choose to let the adapter poll Openstack for completion of the
     * stack deletion, or may handle polling itself via separate query calls. In either
     * case, a StackInfo object will be returned. When polling is enabled, a final
     * status of NOTFOUND is expected. When not polling, a status of DELETING is expected.
     *
     * There is no rollback from a successful stack deletion. A deletion failure will
     * also result in an undefined stack state - the components may or may not have been
     * all or partially deleted, so the resulting stack must be considered invalid.
     *
     * @param tenantId The Openstack ID of the tenant in which to perform the delete
     * @param cloudSiteId The cloud identifier (may be a region) from which to delete the stack.
     * @param stackName The name/id of the stack to delete. May be simple or canonical
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public StackInfo deleteStack (String tenantId,
                                  String cloudSiteId,
                                  String stackName,
                                  boolean pollForCompletion) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        LOGGER.debug("Found: " + cloudSite.toString());

        // Get a Heat client. They are cached between calls (keyed by tenantId:cloudId)
        Heat heatClient = null;
        try {
            heatClient = getHeatClient (cloudSite, tenantId);
            if (heatClient != null) {
            	LOGGER.debug("Found: " + heatClient.toString());
            }
        } catch (MsoTenantNotFound e) {
            // Tenant doesn't exist, so stack doesn't either
            LOGGER.debug ("Tenant with id " + tenantId + "not found.", e);
            return new StackInfo (stackName, HeatStatus.NOTFOUND);
        } catch (MsoException me) {
            // Got an Openstack error. Propagate it
            LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack", "Openstack Exception on Token request: " + me, "Openstack", "", MsoLogger.ErrorCode.AvailabilityError, "Connection Exception");
            me.addContext (DELETE_STACK);
            throw me;
        }

        // OK if stack not found, perform a query first
        Stack heatStack = queryHeatStack (heatClient, stackName);
        if (heatStack == null || "DELETE_COMPLETE".equals (heatStack.getStackStatus ())) {
            // Not found. Return a StackInfo with status NOTFOUND
            return new StackInfo (stackName, HeatStatus.NOTFOUND);
        }

        // Delete the stack.

        // Use canonical name "<stack name>/<stack-id>" to delete.
        // Otherwise, deletion by name returns a 302 redirect.
        // NOTE: This is specific to the v1 Orchestration API.
        String canonicalName = heatStack.getStackName () + "/" + heatStack.getId ();

        try {
            OpenStackRequest <Void> request = null;
            if(null != heatClient) {
                request = heatClient.getStacks ().deleteByName (canonicalName);
            }
            else {
                LOGGER.debug ("Heat Client is NULL" );
            }
            
            executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                // Not found. We are OK with this. Return a StackInfo with status NOTFOUND
                return new StackInfo (stackName, HeatStatus.NOTFOUND);
            } else {
                // Convert the OpenStackResponseException to an MsoOpenstackException
                throw heatExceptionToMsoException (e, DELETE_STACK);
            }
        } catch (OpenStackConnectException e) {
            // Error connecting to Openstack instance. Convert to an MsoException
            throw heatExceptionToMsoException (e, DELETE_STACK);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, DELETE_STACK);
        }

        // Requery the stack for current status.
        // It will probably still exist with "DELETE_IN_PROGRESS" status.
        heatStack = queryHeatStack (heatClient, canonicalName);

        if (pollForCompletion) {
            // Set a timeout on polling
            int pollInterval = msoProps.getIntProperty (deletePollIntervalProp, deletePollIntervalDefault);
            int pollTimeout = msoProps.getIntProperty (deletePollTimeoutProp, deletePollTimeoutDefault);

            // When querying by canonical name, Openstack returns DELETE_COMPLETE status
            // instead of "404" (which would result from query by stack name).
            while (heatStack != null && !"DELETE_COMPLETE".equals (heatStack.getStackStatus ())) {
                LOGGER.debug ("Stack status: " + heatStack.getStackStatus ());

                if ("DELETE_FAILED".equals (heatStack.getStackStatus ())) {
                    // Throw a 'special case' of MsoOpenstackException to report the Heat status
                    String error = "Stack delete error (" + heatStack.getStackStatus ()
                                   + "): "
                                   + heatStack.getStackStatusReason ();
                    MsoOpenstackException me = new MsoOpenstackException (0, "", error);
                    me.addContext (DELETE_STACK);

                    // Alarm this condition, stack deletion failed
                    alarmLogger.sendAlarm (HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage ());

                    throw me;
                }

                if (pollTimeout <= 0) {
                    LOGGER.error (MessageEnum.RA_DELETE_STACK_TIMEOUT, cloudSiteId, tenantId, stackName, heatStack.getStackStatus (), "", "", MsoLogger.ErrorCode.AvailabilityError, "Delete Stack Timeout");

                    // Throw a 'special case' of MsoOpenstackException to report the Heat status
                    MsoOpenstackException me = new MsoOpenstackException (0, "", "Stack Deletion Timeout");
                    me.addContext (DELETE_STACK);

                    // Alarm this condition, stack deletion failed
                    alarmLogger.sendAlarm (HEAT_ERROR, MsoAlarmLogger.CRITICAL, me.getContextMessage ());

                    throw me;
                }

                try {
                    Thread.sleep (pollInterval * 1000L);
                } catch (InterruptedException e) {
                    LOGGER.debug ("Thread interrupted while sleeping", e);
                }

                pollTimeout -= pollInterval;

                heatStack = queryHeatStack (heatClient, canonicalName);
            }

            // The stack is gone when this point is reached
            return new StackInfo (stackName, HeatStatus.NOTFOUND);
        }

        // Return the current status (if not polling, the delete may still be in progress)
        StackInfo stackInfo = new StackInfo (heatStack);
        stackInfo.setName (stackName);

        return stackInfo;
    }

    /**
     * Query for all stacks in a tenant site. This call will return a List of StackInfo
     * objects, one for each deployed stack.
     *
     * Note that this is limited to a single site. To ensure that a tenant is truly
     * empty would require looping across all tenant endpoints.
     *
     * @param tenantId The Openstack ID of the tenant to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query.
     * @return A List of StackInfo objects
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public List <StackInfo> queryAllStacks (String tenantId, String cloudSiteId) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        // Get a Heat client. They are cached between calls (keyed by tenantId:cloudId)
        Heat heatClient = getHeatClient (cloudSite, tenantId);

        try {
            OpenStackRequest <Stacks> request = heatClient.getStacks ().list ();
            Stacks stacks = executeAndRecordOpenstackRequest (request, msoProps);

            List <StackInfo> stackList = new ArrayList <> ();

            // Not sure if returns an empty list or null if no stacks exist
            if (stacks != null) {
                for (Stack stack : stacks) {
                    stackList.add (new StackInfo (stack));
                }
            }

            return stackList;
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                // Not sure if this can happen, but return an empty list
                LOGGER.debug ("queryAllStacks - stack not found: ");
                return new ArrayList <> ();
            } else {
                // Convert the OpenStackResponseException to an MsoOpenstackException
                throw heatExceptionToMsoException (e, QUERY_ALL_STACKS);
            }
        } catch (OpenStackConnectException e) {
            // Error connecting to Openstack instance. Convert to an MsoException
            throw heatExceptionToMsoException (e, QUERY_ALL_STACKS);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, QUERY_ALL_STACKS);
        }
    }

    /**
     * Validate parameters to be passed to Heat template. This method performs
     * three functions:
     * 1. Apply default values to parameters which have them defined
     * 2. Report any required parameters that are missing. This will generate an
     * exception in the caller, since stack create/update operations would fail.
     * 3. Report and remove any extraneous parameters. This will allow clients to
     * pass supersets of parameters and not get errors.
     *
     * These functions depend on the HeatTemplate definition from the MSO Catalog DB,
     * along with the input parameter Map. The output is an updated parameter map.
     * If the parameters are invalid for the template, an IllegalArgumentException
     * is thrown.
     */
    public Map <String, Object> validateStackParams (Map <String, Object> inputParams,
                                                     HeatTemplate heatTemplate) throws IllegalArgumentException {
        // Check that required parameters have been supplied for this template type
        StringBuilder missingParams = null;
        List <String> paramList = new ArrayList <> ();

        // TODO: Enhance DB to support defaults for Heat Template parameters

        for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
            if (parm.isRequired () && !inputParams.containsKey (parm.getParamName ())) {
                if (missingParams == null) {
                    missingParams = new StringBuilder(parm.getParamName());
                } else {
                    missingParams.append("," + parm.getParamName());
                }
            }
            paramList.add (parm.getParamName ());
        }
        if (missingParams != null) {
            // Problem - missing one or more required parameters
            String error = "Missing Required inputs for HEAT Template: " + missingParams;
            LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams + " for HEAT Template", "", "", MsoLogger.ErrorCode.SchemaError, "Missing Required inputs for HEAT Template: " + missingParams);
            throw new IllegalArgumentException (error);
        }

        // Remove any extraneous parameters (don't throw an error)
        Map <String, Object> updatedParams = new HashMap <> ();
        List <String> extraParams = new ArrayList <> ();
        for (String key : inputParams.keySet ()) {
            if (!paramList.contains (key)) {
                // This is not a valid parameter for this template
                extraParams.add (key);
            } else {
                updatedParams.put (key, inputParams.get (key));
            }
        }
        if (!extraParams.isEmpty ()) {
            LOGGER.warn (MessageEnum.RA_GENERAL_WARNING, "Heat Stack (" + heatTemplate.getTemplateName ()
                         + ") extra input params received: "
                         + extraParams, "", "", MsoLogger.ErrorCode.DataError, "Heat Stack (" + heatTemplate.getTemplateName () + ") extra input params received: "+ extraParams);
        }

        return updatedParams;
    }

    // ---------------------------------------------------------------
    // PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    /**
     * Get a Heat client for the Openstack Identity service.
     * This requires a 'member'-level userId + password, which will be retrieved from
     * properties based on the specified cloud Id. The tenant in which to operate
     * must also be provided.
     * <p>
     * On successful authentication, the Heat object will be cached for the
     * tenantID + cloudId so that it can be reused without reauthenticating with
     * Openstack every time.
     *
     * @return an authenticated Heat object
     */
    public Heat getHeatClient (CloudSite cloudSite, String tenantId) throws MsoException {
        String cloudId = cloudSite.getId ();

        // Check first in the cache of previously authorized clients
        String cacheKey = cloudId + ":" + tenantId;
        if (heatClientCache.containsKey (cacheKey)) {
            if (!heatClientCache.get (cacheKey).isExpired ()) {
                LOGGER.debug ("Using Cached HEAT Client for " + cacheKey);
                return heatClientCache.get (cacheKey).getHeatClient ();
            } else {
                // Token is expired. Remove it from cache.
                heatClientCache.remove (cacheKey);
                LOGGER.debug ("Expired Cached HEAT Client for " + cacheKey);
            }
        }

        // Obtain an MSO token for the tenant
        CloudIdentity cloudIdentity = cloudSite.getIdentityService ();
        LOGGER.debug("Found: " + cloudIdentity.toString());
        String keystoneUrl = cloudIdentity.getKeystoneUrl (cloudId, msoPropID);
        LOGGER.debug("keystoneUrl=" + keystoneUrl);
        Keystone keystoneTenantClient = new Keystone (keystoneUrl);
        Access access = null;
        try {
        	Authentication credentials = cloudIdentity.getAuthentication ();

        	OpenStackRequest <Access> request = keystoneTenantClient.tokens ()
                       .authenticate (credentials).withTenantId (tenantId);

            access = executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 401) {
                // Authentication error.
                String error = "Authentication Failure: tenant=" + tenantId + ",cloud=" + cloudIdentity.getId ();
                alarmLogger.sendAlarm ("MsoAuthenticationError", MsoAlarmLogger.CRITICAL, error);
                throw new MsoAdapterException (error);
            } else {
                throw keystoneErrorToMsoException (e, TOKEN_AUTH);
            }
        } catch (OpenStackConnectException e) {
            // Connection to Openstack failed
            MsoIOException me = new MsoIOException (e.getMessage (), e);
            me.addContext (TOKEN_AUTH);
            throw me;
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, TOKEN_AUTH);
        }

        // For DCP/LCP, the region should be the cloudId.
        String region = cloudSite.getRegionId ();
        String heatUrl = null;
        try {
            heatUrl = KeystoneUtils.findEndpointURL (access.getServiceCatalog (), "orchestration", region, "public");
            LOGGER.debug("heatUrl=" + heatUrl + ", region=" + region);
        } catch (RuntimeException e) {
            // This comes back for not found (probably an incorrect region ID)
            String error = "Orchestration service not found: region=" + region + ",cloud=" + cloudIdentity.getId ();
            alarmLogger.sendAlarm ("MsoConfigurationError", MsoAlarmLogger.CRITICAL, error);
            throw new MsoAdapterException (error, e);
        }

        Heat heatClient = new Heat (heatUrl);
        heatClient.token (access.getToken ().getId ());

        heatClientCache.put (cacheKey,
                             new HeatCacheEntry (heatUrl,
                                                 access.getToken ().getId (),
                                                 access.getToken ().getExpires ()));
        LOGGER.debug ("Caching HEAT Client for " + cacheKey);

        return heatClient;
    }

    /**
     * Forcibly expire a HEAT client from the cache. This call is for use by
     * the KeystoneClient in case where a tenant is deleted. In that case,
     * all cached credentials must be purged so that fresh authentication is
     * done if a similarly named tenant is re-created.
     * <p>
     * Note: This is probably only applicable to dev/test environments where
     * the same Tenant Name is repeatedly used for creation/deletion.
     * <p>
     *
     */
    public static void expireHeatClient (String tenantId, String cloudId) {
        String cacheKey = cloudId + ":" + tenantId;
        if (heatClientCache.containsKey (cacheKey)) {
            heatClientCache.remove (cacheKey);
            LOGGER.debug ("Deleted Cached HEAT Client for " + cacheKey);
        }
    }

    /*
     * Query for a Heat Stack. This function is needed in several places, so
     * a common method is useful. This method takes an authenticated Heat Client
     * (which internally identifies the cloud & tenant to search), and returns
     * a Stack object if found, Null if not found, or an MsoOpenstackException
     * if the Openstack API call fails.
     *
     * The stack name may be a simple name or a canonical name ("{name}/{id}").
     * When simple name is used, Openstack always returns a 302 redirect which
     * results in a 2nd request (to the canonical name). Note that query by
     * canonical name for a deleted stack returns a Stack object with status
     * "DELETE_COMPLETE" while query by simple name for a deleted stack returns
     * HTTP 404.
     *
     * @param heatClient an authenticated Heat client
     *
     * @param stackName the stack name to query
     *
     * @return a Stack object that describes the current stack or null if the
     * requested stack doesn't exist.
     *
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
     */
    protected Stack queryHeatStack (Heat heatClient, String stackName) throws MsoException {
        if (stackName == null) {
            return null;
        }
        try {
            OpenStackRequest <Stack> request = heatClient.getStacks ().byName (stackName);
            return executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                LOGGER.debug ("queryHeatStack - stack not found: " + stackName);
                return null;
            } else {
                // Convert the OpenStackResponseException to an MsoOpenstackException
                throw heatExceptionToMsoException (e, "QueryStack");
            }
        } catch (OpenStackConnectException e) {
            // Connection to Openstack failed
            throw heatExceptionToMsoException (e, "QueryAllStack");
        }
    }

    /*
     * An entry in the Heat Client Cache. It saves the Heat client object
     * along with the token expiration. After this interval, this cache
     * item will no longer be used.
     */
    private static class HeatCacheEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        private String heatUrl;
        private String token;
        private Calendar expires;

        public HeatCacheEntry (String heatUrl, String token, Calendar expires) {
            this.heatUrl = heatUrl;
            this.token = token;
            this.expires = expires;
        }

        public Heat getHeatClient () {
            Heat heatClient = new Heat (heatUrl);
            heatClient.token (token);
            return heatClient;
        }

        public boolean isExpired () {
            return expires == null || System.currentTimeMillis() > expires.getTimeInMillis();

        }
    }

    /**
     * Clean up the Heat client cache to remove expired entries.
     */
    public static void heatCacheCleanup () {
        for (String cacheKey : heatClientCache.keySet ()) {
            if (heatClientCache.get (cacheKey).isExpired ()) {
                heatClientCache.remove (cacheKey);
                LOGGER.debug ("Cleaned Up Cached Heat Client for " + cacheKey);
            }
        }
    }

    /**
     * Reset the Heat client cache.
     * This may be useful if cached credentials get out of sync.
     */
    public static void heatCacheReset () {
        heatClientCache = new HashMap <> ();
    }

	public Map<String, Object> queryStackForOutputs(String cloudSiteId,
			String tenantId, String stackName) throws MsoException {
		LOGGER.debug("MsoHeatUtils.queryStackForOutputs)");
		StackInfo heatStack = this.queryStack(cloudSiteId, tenantId, stackName);
		if (heatStack == null || heatStack.getStatus() == HeatStatus.NOTFOUND) {
			return null;
		}
		Map<String, Object> outputs = heatStack.getOutputs();
		return outputs;
	}

	public void queryAndCopyOutputsToInputs(String cloudSiteId,
			String tenantId, String stackName, Map<String, String> inputs,
			boolean overWrite) throws MsoException {
		LOGGER.debug("MsoHeatUtils.queryAndCopyOutputsToInputs");
		Map<String, Object> outputs = this.queryStackForOutputs(cloudSiteId,
				tenantId, stackName);
		this.copyStringOutputsToInputs(inputs, outputs, overWrite);
		return;
	}

	public void copyStringOutputsToInputs(Map<String, String> inputs,
			Map<String, Object> otherStackOutputs, boolean overWrite) {
		if (inputs == null || otherStackOutputs == null)
			return;
		for (String key : otherStackOutputs.keySet()) {
			if (!inputs.containsKey(key)) {
				Object obj = otherStackOutputs.get(key);
				if (obj instanceof String) {
					inputs.put(key, (String) otherStackOutputs.get(key));
				} else if (obj instanceof JsonNode ){
					// This is a bit of mess - but I think it's the least impacting
					// let's convert it BACK to a string - then it will get converted back later
					try {
						String str = this.convertNode((JsonNode) obj);
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for JsonNode "+ key, e);
						//effect here is this value will not have been copied to the inputs - and therefore will error out downstream
					}
				} else if (obj instanceof java.util.LinkedHashMap) {
					LOGGER.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
					try {
						String str = JSON_MAPPER.writeValueAsString(obj);
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for LinkedHashMap "+ key, e);
					}
				} else if (obj instanceof Integer) {
					try {
						String str = "" + obj;
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for Integer "+ key, e);
					}
				} else {
					try {
						String str = obj.toString();
						inputs.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for Other "+ key +" (" + e.getMessage() + ")", e);
						//effect here is this value will not have been copied to the inputs - and therefore will error out downstream
					}
				}
			}
		}
		return;
	}
	public StringBuilder requestToStringBuilder(CreateStackParam stack) {
		StringBuilder sb = new StringBuilder();
		sb.append("Stack:\n");
		sb.append("\tStackName: " + stack.getStackName());
		sb.append("\tTemplateUrl: " + stack.getTemplateUrl());
		sb.append("\tTemplate: " + stack.getTemplate());
		sb.append("\tEnvironment: " + stack.getEnvironment());
		sb.append("\tTimeout: " + stack.getTimeoutMinutes());
		sb.append("\tParameters:\n");
		Map<String, Object> params = stack.getParameters();
		if (params == null || params.size() < 1) {
			sb.append("\nNONE");
		} else {
			for (String key : params.keySet()) {
				if (params.get(key) instanceof String) {
					sb.append("\n").append(key).append("=").append((String) params.get(key));
				} else if (params.get(key) instanceof JsonNode) {
					String jsonStringOut = this.convertNode((JsonNode)params.get(key));
					sb.append("\n").append(key).append("=").append(jsonStringOut);
				} else if (params.get(key) instanceof Integer) {
					String integerOut = "" + params.get(key);
					sb.append("\n").append(key).append("=").append(integerOut);

				} else {
					try {
						String str = params.get(key).toString();
						sb.append("\n").append(key).append("=").append(str);
					} catch (Exception e) {
						LOGGER.debug("Exception :",e);
					}
				}
			}
		}
		return sb;
	}

	private String convertNode(final JsonNode node) {
		try {
			final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
			final String json = JSON_MAPPER.writeValueAsString(obj);
			return json;
		} catch (Exception e) {
			LOGGER.debug("Error converting json to string " + e.getMessage(), e);
		}
		return "[Error converting json to string]";
	}


	private StringBuilder getOutputsAsStringBuilder(Stack heatStack) {
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
					LOGGER.debug("Exception :",e);
					sb.append("(a LinkedHashMap value that would not convert nicely)");
				}				
			} else if (obj instanceof Integer) {
				String str = "";
				try {
					str = obj.toString() + " (an Integer)\n";
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
					str = "(an Integer unable to call .toString() on)";
				}
				sb.append(str);
			} else if (obj instanceof ArrayList) {
				String str = "";
				try {
					str = obj.toString() + " (an ArrayList)";
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
					str = "(an ArrayList unable to call .toString() on?)";
				}
				sb.append(str);
			} else if (obj instanceof Boolean) {
				String str = "";
				try {
					str = obj.toString() + " (a Boolean)";
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
					str = "(an Boolean unable to call .toString() on?)";
				}
				sb.append(str);
			}
			else {
				String str = "";
				try {
					str = obj.toString() + " (unknown Object type)";
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
					str = "(a value unable to call .toString() on?)";
				}
				sb.append(str);
			}
			sb.append("\n");
		}
		sb.append("[END]");
		return sb;
	}
	
	
	public void copyBaseOutputsToInputs(Map<String, Object> inputs,
			Map<String, Object> otherStackOutputs, ArrayList<String> paramNames, HashMap<String, String> aliases) {
		if (inputs == null || otherStackOutputs == null)
			return;
		for (String key : otherStackOutputs.keySet()) {
			if (paramNames != null) {
				if (!paramNames.contains(key) && !aliases.containsKey(key)) {
					LOGGER.debug("\tParameter " + key + " is NOT defined to be in the template - do not copy to inputs");
					continue;
				}
				if (aliases.containsKey(key)) {
					LOGGER.debug("Found an alias! Will move " + key + " to " + aliases.get(key));
					Object obj = otherStackOutputs.get(key);
					key = aliases.get(key);
					otherStackOutputs.put(key, obj);
				}
			}
			if (!inputs.containsKey(key)) {
				Object obj = otherStackOutputs.get(key);
				LOGGER.debug("\t**Adding " + key + " to inputs (.toString()=" + obj.toString());
				if (obj instanceof String) {
					LOGGER.debug("\t\t**A String");
					inputs.put(key, obj);
				} else if (obj instanceof Integer) {
					LOGGER.debug("\t\t**An Integer");
					inputs.put(key, obj);
				} else if (obj instanceof JsonNode) {
					LOGGER.debug("\t\t**A JsonNode");
					inputs.put(key, obj);
				} else if (obj instanceof Boolean) {
					LOGGER.debug("\t\t**A Boolean");
					inputs.put(key, obj);
				} else if (obj instanceof java.util.LinkedHashMap) {
					LOGGER.debug("\t\t**A java.util.LinkedHashMap **");
					//Object objJson = this.convertObjectToJsonNode(obj.toString());
					//if (objJson == null) {
					//	LOGGER.debug("\t\tFAILED!! Will just put LinkedHashMap on the inputs");
					inputs.put(key, obj);
					//}
					//else {
					//	LOGGER.debug("\t\tSuccessfully converted to JsonNode: " + objJson.toString());
					//	inputs.put(key, objJson);
					//}
				} else if (obj instanceof java.util.ArrayList) {
					LOGGER.debug("\t\t**An ArrayList");
					inputs.put(key, obj);
				} else {
					LOGGER.debug("\t\t**UNKNOWN OBJECT TYPE");
					inputs.put(key, obj);
				}
			} else {
				LOGGER.debug("key=" + key + " is already in the inputs - will not overwrite");
			}
		}
		return;
	}
	
	public JsonNode convertObjectToJsonNode(Object lhm) {
		if (lhm == null) {
			return null;
		}
		JsonNode jsonNode = null;
		try {
			String jsonString = lhm.toString();
			jsonNode = new ObjectMapper().readTree(jsonString);
		} catch (Exception e) {
			LOGGER.debug("Unable to convert " + lhm.toString() + " to a JsonNode " + e.getMessage(), e);
			jsonNode = null;
		}
		return jsonNode;
	}
	
	public ArrayList<String> convertCdlToArrayList(String cdl) {
		String cdl2 = cdl.trim();
		String cdl3;
		if (cdl2.startsWith("[") && cdl2.endsWith("]")) {
			cdl3 = cdl2.substring(1, cdl2.lastIndexOf("]"));
		} else {
			cdl3 = cdl2;
		}
		ArrayList<String> list = new ArrayList<>(Arrays.asList(cdl3.split(",")));
		return list;
	}
	
    /**
     * New with 1707 - this method will convert all the String *values* of the inputs
     * to their "actual" object type (based on the param type: in the db - which comes from the template):
     * (heat variable type) -> java Object type
     * string -> String
     * number -> Integer
     * json -> JsonNode
     * comma_delimited_list -> ArrayList
     * boolean -> Boolean
     * if any of the conversions should fail, we will default to adding it to the inputs
     * as a string - see if Openstack can handle it.
     * Also, will remove any params that are extra.
     * Any aliases will be converted to their appropriate name (anyone use this feature?)
     * @param inputs - the Map<String, String> of the inputs received on the request
     * @param template the HeatTemplate object - this is so we can also verify if the param is valid for this template
     * @return HashMap<String, Object> of the inputs, cleaned and converted
     */
	public HashMap<String, Object> convertInputMap(Map<String, String> inputs, HeatTemplate template) {
		HashMap<String, Object> newInputs = new HashMap<>();
		HashMap<String, HeatTemplateParam> params = new HashMap<>();
		HashMap<String, HeatTemplateParam> paramAliases = new HashMap<>();
		
		if (inputs == null) {
			LOGGER.debug("convertInputMap - inputs is null - nothing to do here");
			return new HashMap<>();
		}
		
		LOGGER.debug("convertInputMap in MsoHeatUtils called, with " + inputs.size() + " inputs, and template " + template.getArtifactUuid());
		try {
			LOGGER.debug(template.toString());
			Set<HeatTemplateParam> paramSet = template.getParameters();
			LOGGER.debug("paramSet has " + paramSet.size() + " entries");
		} catch (Exception e) {
			LOGGER.debug("Exception occurred in convertInputMap:" + e.getMessage(), e);
		}
		
		for (HeatTemplateParam htp : template.getParameters()) {
			LOGGER.debug("Adding " + htp.getParamName());
			params.put(htp.getParamName(), htp);
			if (htp.getParamAlias() != null && !"".equals(htp.getParamAlias())) {
				LOGGER.debug("\tFound ALIAS " + htp.getParamName() + "->" + htp.getParamAlias());
				paramAliases.put(htp.getParamAlias(), htp);
			}
		}
		LOGGER.debug("Now iterate through the inputs...");
		for (String key : inputs.keySet()) {
			LOGGER.debug("key=" + key);
			boolean alias = false;
			String realName = null;
			if (!params.containsKey(key)) {
				LOGGER.debug(key + " is not a parameter in the template! - check for an alias");
				// add check here for an alias
				if (!paramAliases.containsKey(key)) {
					LOGGER.debug("The parameter " + key + " is in the inputs, but it's not a parameter for this template - omit");
					continue;
				} else {
					alias = true;
					realName = paramAliases.get(key).getParamName();
					LOGGER.debug("FOUND AN ALIAS! Will use " + realName + " in lieu of give key/alias " + key);
				}
			}
			String type = params.get(key).getParamType();
			if (type == null || "".equals(type)) {
				LOGGER.debug("**PARAM_TYPE is null/empty for " + key + ", will default to string");
				type = "string";
			}
			LOGGER.debug("Parameter: " + key + " is of type " + type);
			if ("string".equalsIgnoreCase(type)) {
				// Easiest!
				String str = inputs.get(key);
				if (alias) 
					newInputs.put(realName, str);
				else 
					newInputs.put(key, str);
			} else if ("number".equalsIgnoreCase(type)) {
				String integerString = inputs.get(key);
				Integer anInteger = null;
				try {
					anInteger = Integer.parseInt(integerString);
				} catch (Exception e) {
					LOGGER.debug("Unable to convert " + integerString + " to an integer!!", e);
					anInteger = null;
				}
				if (anInteger != null) {
					if (alias)
						newInputs.put(realName, anInteger);
					else
						newInputs.put(key, anInteger);
				}
				else {
					if (alias)
						newInputs.put(realName, integerString);
					else
						newInputs.put(key, integerString);
				}
			} else if ("json".equalsIgnoreCase(type)) {
				String jsonString = inputs.get(key);
    			JsonNode jsonNode = null;
    			try {
    				jsonNode = new ObjectMapper().readTree(jsonString);
    			} catch (Exception e) {
					LOGGER.debug("Unable to convert " + jsonString + " to a JsonNode!!", e);
					jsonNode = null;
    			}
    			if (jsonNode != null) {
    				if (alias)
    					newInputs.put(realName, jsonNode);
    				else
    					newInputs.put(key, jsonNode);
    			}
    			else {
    				if (alias)
    					newInputs.put(realName, jsonString);
    				else
    					newInputs.put(key, jsonString);
    			}
			} else if ("comma_delimited_list".equalsIgnoreCase(type)) {
				String commaSeparated = inputs.get(key);
				try {
					ArrayList<String> anArrayList = this.convertCdlToArrayList(commaSeparated);
					if (alias)
						newInputs.put(realName, anArrayList);
					else
						newInputs.put(key, anArrayList);
				} catch (Exception e) {
					LOGGER.debug("Unable to convert " + commaSeparated + " to an ArrayList!!", e);
					if (alias)
						newInputs.put(realName, commaSeparated);
					else
						newInputs.put(key, commaSeparated);
				}
			} else if ("boolean".equalsIgnoreCase(type)) {
				String booleanString = inputs.get(key);
				Boolean aBool = Boolean.valueOf(booleanString);
				if (alias)
					newInputs.put(realName, aBool);
				else
					newInputs.put(key, aBool);
			} else {
				// it's null or something undefined - just add it back as a String
				String str = inputs.get(key);
				if (alias)
					newInputs.put(realName, str);
				else
					newInputs.put(key, str);
			}
		}
		return newInputs;
	}

}
