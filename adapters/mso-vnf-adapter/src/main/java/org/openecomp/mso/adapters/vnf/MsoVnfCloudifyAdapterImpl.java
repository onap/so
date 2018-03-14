/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloudify.beans.DeploymentInfo;
import org.openecomp.mso.cloudify.beans.DeploymentStatus;
import org.openecomp.mso.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.openecomp.mso.cloudify.utils.MsoCloudifyUtils;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.utils.MsoHeatEnvironmentEntry;
import org.openecomp.mso.openstack.utils.MsoHeatEnvironmentParameter;
import org.openecomp.mso.openstack.utils.MsoKeystoneUtils;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebService(serviceName = "VnfAdapter", endpointInterface = "org.openecomp.mso.adapters.vnf.MsoVnfAdapter", targetNamespace = "http://org.openecomp.mso/vnf")
public class MsoVnfCloudifyAdapterImpl implements MsoVnfAdapter {

	CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	protected CloudConfig cloudConfig = cloudConfigFactory.getCloudConfig();

	MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	private static final String MSO_PROP_VNF_ADAPTER = "MSO_PROP_VNF_ADAPTER";
    private static final String MSO_CONFIGURATION_ERROR = "MsoConfigurationError";
    private static final String VNF_ADAPTER_SERVICE_NAME = "MSO-BPMN:MSO-VnfAdapter.";
    private static final String LOG_REPLY_NAME = "MSO-VnfAdapter:MSO-BPMN.";
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static final String CHECK_REQD_PARAMS = "org.openecomp.mso.adapters.vnf.checkRequiredParameters";
    private static final String ADD_GET_FILES_ON_VOLUME_REQ = "org.openecomp.mso.adapters.vnf.addGetFilesOnVolumeReq";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck () {
        LOGGER.debug ("Health check call in VNF Cloudify Adapter");
    }

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     * @see MsoVnfCloudifyAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfCloudifyAdapterImpl() {

    }

    /**
     * This constructor MUST be used if this class is called with the new operator.
     * @param msoPropFactory
     */
    public MsoVnfCloudifyAdapterImpl(MsoPropertiesFactory msoPropFactory, CloudConfigFactory cloudConfigFact) {
    	this.msoPropertiesFactory = msoPropFactory;
    	this.cloudConfigFactory = cloudConfigFact;
    }

    /**
     * This is the "Create VNF" web service implementation.
     * This function is now unsupported and will return an error.
     *
     */
    @Override
    public void createVnf (String cloudSiteId,
                           String tenantId,
                           String vnfType,
                           String vnfVersion,
                           String vnfName,
                           String requestType,
                           String volumeGroupHeatStackId,
                           Map <String, String> inputs,
                           Boolean failIfExists,
                           Boolean backout,
                           MsoRequest msoRequest,
                           Holder <String> vnfId,
                           Holder <Map <String, String>> outputs,
                           Holder <VnfRollback> rollback)
    	throws VnfException
    {
    	// This operation is no longer supported at the VNF level.  The adapter is only called to deploy modules.
    	LOGGER.debug ("CreateVNF command attempted but not supported");
    	throw new VnfException ("CreateVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This is the "Update VNF" web service implementation.
     * This function is now unsupported and will return an error.
     *
     */
    @Override
    public void updateVnf (String cloudSiteId,
                           String tenantId,
                           String vnfType,
                           String vnfVersion,
                           String vnfName,
                           String requestType,
                           String volumeGroupHeatStackId,
                           Map <String, String> inputs,
                           MsoRequest msoRequest,
                           Holder <Map <String, String>> outputs,
                           Holder <VnfRollback> rollback)
		throws VnfException
	{
    	// This operation is no longer supported at the VNF level.  The adapter is only called to deploy modules.
    	LOGGER.debug ("UpdateVNF command attempted but not supported");
    	throw new VnfException ("UpdateVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This is the "Query VNF" web service implementation.
     * 
     * This really should be QueryVfModule, but nobody ever changed it.
     * 
     * For Cloudify, this will look up a deployment by its deployment ID, which is really the same
     * as deployment name, since it assigned by the client when a deployment is created.
     * Also, the input cloudSiteId is used only to identify which Cloudify instance to query,
     * and the tenantId is ignored (since that really only applies for Openstack/Heat).
     *
     * The method returns an indicator that the VNF exists, along with its status and outputs.
     * The input "vnfName" will also be reflected back as its ID.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param tenantId Openstack tenant identifier - ignored for Cloudify
     * @param vnfName VNF Name (should match a deployment ID)
     * @param msoRequest Request tracking information for logs
     * @param vnfExists Flag reporting the result of the query
     * @param vnfId Holder for output VNF ID
     * @param outputs Holder for Map of VNF outputs from Cloudify deployment (assigned IPs, etc)
     */
    @Override
    public void queryVnf (String cloudSiteId,
                          String tenantId,
                          String vnfName,
                          MsoRequest msoRequest,
                          Holder <Boolean> vnfExists,
                          Holder <String> vnfId,
                          Holder <VnfStatus> status,
                          Holder <Map <String, String>> outputs)
        throws VnfException
    {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("QueryVnfCloudify");
        LOGGER.debug ("Querying VNF " + vnfName + " in " + cloudSiteId + "/" + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();
        long subStartTime = System.currentTimeMillis ();

    	MsoCloudifyUtils cloudifyUtils = new MsoCloudifyUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
    	DeploymentInfo deployment = null;
    	
    	try {
    		deployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, vnfName);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Cloudify", "Cloudify", "QueryDeployment", vnfName);
    	}
    	catch (MsoCloudifyManagerNotFound e) {
    		// This site does not have a Cloudify Manager.
    		// This isn't an error, just means we won't find the VNF here.
    		deployment = null;
    	}
    	catch (MsoException me) {
            // Failed to query the Deployment due to a cloudify exception.
            // Convert to a generic VnfException
            me.addContext ("QueryVNF");
            String error = "Query VNF (Cloudify): " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "QueryDeployment", vnfName);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "Cloudify", "QueryVNF", MsoLogger.ErrorCode.DataError, "Exception - queryDeployment", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
    	}
        	
    	if (deployment != null  &&  deployment.getStatus() != DeploymentStatus.NOTFOUND) {
            vnfExists.value = Boolean.TRUE;
            status.value = deploymentStatusToVnfStatus(deployment);
            vnfId.value = deployment.getId();
            outputs.value = copyStringOutputs (deployment.getOutputs ());

            LOGGER.debug ("VNF " + vnfName + " found in Cloudify, ID = " + vnfId.value);
        }
        else {
            vnfExists.value = Boolean.FALSE;
            status.value = VnfStatus.NOTFOUND;
            vnfId.value = null;
            outputs.value = new HashMap <String, String> (); // Return as an empty map

            LOGGER.debug ("VNF " + vnfName + " not found");
    	}
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully query VNF");
        return;
    }

    
    /**
     * This is the "Delete VNF" web service implementation.
     * This function is now unsupported and will return an error.
     *
     */
    @Override
    public void deleteVnf (String cloudSiteId,
                           String tenantId,
                           String vnfName,
                           MsoRequest msoRequest) throws VnfException {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("DeleteVnf");
    	
    	// This operation is no longer supported at the VNF level.  The adapter is only called to deploy modules.
    	LOGGER.debug ("DeleteVNF command attempted but not supported");
    	throw new VnfException ("DeleteVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation.
     * A rollback object is returned to the client in a successful creation
     * response. The client can pass that object as-is back to the rollbackVnf
     * operation to undo the creation.
     * 
     * TODO: This should be rollbackVfModule and/or rollbackVolumeGroup,
     * but APIs were apparently never updated.
     */
    @Override
    public void rollbackVnf (VnfRollback rollback) throws VnfException {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("RollbackVnf");
    	// rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            LOGGER.info (MessageEnum.RA_ROLLBACK_NULL, "OpenStack", "rollbackVnf");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Rollback request content is null");
            return;
        }

        // Don't rollback if nothing was done originally
        if (!rollback.getVnfCreated()) {
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Rollback VF Module - nothing to roll back");
            return;
        }
        
        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudSiteId ();
        String tenantId = rollback.getTenantId ();
        String vfModuleId = rollback.getVfModuleStackId ();

        MsoLogger.setLogContext (rollback.getMsoRequest());

        LOGGER.debug ("Rolling Back VF Module " + vfModuleId + " in " + cloudSiteId + "/" + tenantId);

    	MsoCloudifyUtils cloudifyUtils = new MsoCloudifyUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
    	DeploymentInfo deployment = null;

        // Use the MsoCloudifyUtils to delete the deployment. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        long subStartTime = System.currentTimeMillis ();
        try {
        	// KLUDGE - Cloudify requires Tenant Name for Openstack.  We have the ID.
        	//          Go directly to Keystone until APIs could be updated to supply the name.
        	MsoKeystoneUtils keystone = new MsoKeystoneUtils(MSO_PROP_VNF_ADAPTER);
        	MsoTenant msoTenant = keystone.queryTenant(tenantId, cloudSiteId);
        	String tenantName = (msoTenant != null? msoTenant.getTenantName() : tenantId);
        	
        	// TODO: Get a reasonable timeout.  Use a global property, or store the creation timeout in rollback object and use that.
            deployment = cloudifyUtils.uninstallAndDeleteDeployment(cloudSiteId, tenantName, vfModuleId, 5);
            LOGGER.debug("Rolled back deployment: " + deployment.getId());
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Cloudify", "Cloudify", "DeleteDeployment", null);
        } catch (MsoException me) {
            // Failed to rollback the VNF due to a cloudify exception.
            // Convert to a generic VnfException
            me.addContext ("RollbackVNF");
            String error = "Rollback VF Module: " + vfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "DeleteDeployment", null);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vfModuleId, cloudSiteId, tenantId, "Cloudify", "DeleteDeployment", MsoLogger.ErrorCode.DataError, "Exception - DeleteDeployment", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully roll back VF Module");
        return;
    }


    private VnfStatus deploymentStatusToVnfStatus (DeploymentInfo deployment) {
    	// Determine the status based on last action & status
    	// DeploymentInfo object should be enhanced to report a better status internally.
    	DeploymentStatus status = deployment.getStatus();
    	String lastAction = deployment.getLastAction();
    	
    	if (status == null  ||  lastAction == null) {
    		return VnfStatus.UNKNOWN;
    	}
    	else if (status == DeploymentStatus.NOTFOUND) {
			return VnfStatus.NOTFOUND;
	}
    	else if (status == DeploymentStatus.INSTALLED) {
    			return VnfStatus.ACTIVE;
    	}
    	else if (status == DeploymentStatus.CREATED) {
        	// Should have an INACTIVE status for this case.  Shouldn't really happen, but
    		// Install was never run, or Uninstall was done but deployment didn't get deleted.
        	return VnfStatus.UNKNOWN;
    	}
    	else if (status == DeploymentStatus.FAILED) {
    		return VnfStatus.FAILED;
    	}

    	return VnfStatus.UNKNOWN;
    }
    
    private Map <String, String> copyStringOutputs (Map <String, Object> stackOutputs) {
        Map <String, String> stringOutputs = new HashMap <String, String> ();
        for (String key : stackOutputs.keySet ()) {
            if (stackOutputs.get (key) instanceof String) {
                stringOutputs.put (key, (String) stackOutputs.get (key));
            } else if (stackOutputs.get(key) instanceof Integer)  {
            	try {
            		String str = "" + stackOutputs.get(key);
            		stringOutputs.put(key, str);
            	} catch (Exception e) {
            		LOGGER.debug("Unable to add " + key + " to outputs");
            	}
            } else if (stackOutputs.get(key) instanceof JsonNode) {
            	try {
            		String str = this.convertNode((JsonNode) stackOutputs.get(key));
            		stringOutputs.put(key, str);
            	} catch (Exception e) {
            		LOGGER.debug("Unable to add " + key + " to outputs - exception converting JsonNode");
            	}
            } else if (stackOutputs.get(key) instanceof java.util.LinkedHashMap) {
            	try {
					String str = JSON_MAPPER.writeValueAsString(stackOutputs.get(key));
            		stringOutputs.put(key, str);
            	} catch (Exception e) {
            		LOGGER.debug("Unable to add " + key + " to outputs - exception converting LinkedHashMap");
            	}
            } else {
            	try {
            		String str = stackOutputs.get(key).toString();
            		stringOutputs.put(key, str);
            	} catch (Exception e) {
            		LOGGER.debug("Unable to add " + key + " to outputs - unable to call .toString() " + e.getMessage());
            	}
            }
        }
        return stringOutputs;
    }


    private void sendMapToDebug(Map<String, Object> inputs, String optionalName) {
    	int i = 0;
    	StringBuilder sb = new StringBuilder(optionalName == null ? "\ninputs" : "\n" + optionalName);
    	if (inputs == null) {
    		sb.append("\tNULL");
    	}
    	else if (inputs.size() < 1) {
    		sb.append("\tEMPTY");
    	} else {
    		for (String str : inputs.keySet()) {
    			String outputString;
    			try {
    				outputString = inputs.get(str).toString();
    			} catch (Exception e) {
    				outputString = "Unable to call toString() on the value for " + str;
    			}
    			sb.append("\t\nitem " + i++ + ": '" + str + "'='" + outputString + "'");
    		}
    	}
    	LOGGER.debug(sb.toString());
    	return;	
    }
    
    private void sendMapToDebug(Map<String, String> inputs) {
    	int i = 0;
    	StringBuilder sb = new StringBuilder("inputs:");
    	if (inputs == null) {
    		sb.append("\tNULL");
    	}
    	else if (inputs.size() < 1) {
    		sb.append("\tEMPTY");
    	} else {
    		for (String str : inputs.keySet()) {
    			sb.append("\titem " + i++ + ": " + str + "=" + inputs.get(str));
    		}
    	}
    	LOGGER.debug(sb.toString());
    	return;
    }

    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            final String json = JSON_MAPPER.writeValueAsString(obj);
            return json;
        } catch (JsonParseException jpe) {
            LOGGER.debug("Error converting json to string " + jpe.getMessage());
        } catch (Exception e) {
            LOGGER.debug("Error converting json to string " + e.getMessage());
        }
        return "[Error converting json to string]";
    }

    private Map<String, String> convertMapStringObjectToStringString(Map<String, Object> objectMap) {
        if (objectMap == null) {
            return null;
        }
        Map<String, String> stringMap = new HashMap<String, String>();
        for (String key : objectMap.keySet()) {
            if (!stringMap.containsKey(key)) {
                Object obj = objectMap.get(key);
                if (obj instanceof String) {
                    stringMap.put(key, (String) objectMap.get(key));
                } else if (obj instanceof JsonNode ){
                    // This is a bit of mess - but I think it's the least impacting
                    // let's convert it BACK to a string - then it will get converted back later
                    try {
                        String str = this.convertNode((JsonNode) obj);
                        stringMap.put(key, str);
                    } catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for JsonNode "+ key);
                        //okay in this instance - only string values (fqdn) are expected to be needed
                    }
                } else if (obj instanceof java.util.LinkedHashMap) {
                    LOGGER.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                    try {
                        String str = JSON_MAPPER.writeValueAsString(obj);
                        stringMap.put(key, str);
                    } catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for LinkedHashMap "+ key);
					}
				}  else if (obj instanceof Integer) {
					try {
						String str = "" + obj;
						stringMap.put(key, str);
					} catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value for Integer "+ key);
                    }
                } else {
                    try {
						String str = obj.toString();
                        stringMap.put(key, str);
                    } catch (Exception e) {
						LOGGER.debug("DANGER WILL ROBINSON: unable to convert value "+ key + " (" + e.getMessage() + ")");
                    }
                }
            }
        }

        return stringMap;
    }

    /**
     * This is the "Create VF Module" web service implementation.
     * It will instantiate a new VF Module of the requested type in the specified cloud
     * and tenant. The tenant must exist before this service is called.
     *
     * If a VF Module with the same name already exists, this can be considered a
     * success or failure, depending on the value of the 'failIfExists' parameter.
     *
     * All VF Modules are defined in the MSO catalog. The caller must request
     * one of the pre-defined module types or an error will be returned. Within the
     * catalog, each VF Module references (among other things) a cloud template
     * which is used to deploy the required  artifacts (VMs, networks, etc.)
     * to the cloud.  In this adapter implementation, that artifact is expected
     * to be a Cloudify blueprint.
     *
     * Depending on the blueprint, a variable set of input parameters will
     * be defined, some of which are required. The caller is responsible to
     * pass the necessary input data for the module or an error will be thrown.
     *
     * The method returns the vfModuleId, a Map of output attributes, and a VnfRollback
     * object. This last object can be passed as-is to the rollbackVnf operation to
     * undo everything that was created for the Module. This is useful if a VF module
     * is successfully created but the orchestration fails on a subsequent step.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vfModuleType VF Module type key, should match a VNF definition in catalog DB.
     *        Deprecated - should use modelCustomizationUuid
     * @param vnfVersion VNF version key, should match a VNF definition in catalog DB
     *        Deprecated - VF Module versions also captured by modelCustomizationUuid
     * @param vfModuleName Name to be assigned to the new VF Module
     * @param requestType Indicates if this is a Volume Group or Module request
     * @param volumeGroupId Identifier (i.e. deployment ID) for a Volume Group
     *        to attach to a VF Module
     * @param baseVfModuleId Identifier (i.e. deployment ID) of the Base Module if
     *        this is an Add-on module
     * @param modelCustomizationUuid Unique ID for the VF Module's model.  Replaces
     *        the use of vfModuleType.
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered
     * @param backout Flag whether to suppress automatic backout (for testing)
     * @param msoRequest Request tracking information for logs
     * @param vnfId Holder for output VNF Cloudify Deployment ID
     * @param outputs Holder for Map of VNF outputs from Deployment (assigned IPs, etc)
     * @param rollback Holder for returning VnfRollback object
     */
    public void createVfModule(String cloudSiteId,
            String tenantId,
            String vfModuleType,
            String vnfVersion,
            String vfModuleName,
            String requestType,
            String volumeGroupId,
            String baseVfModuleId,
            String modelCustomizationUuid,
            Map <String, String> inputs,
            Boolean failIfExists,
            Boolean backout,
            MsoRequest msoRequest,
            Holder <String> vnfId,
            Holder <Map <String, String>> outputs,
            Holder <VnfRollback> rollback)
        throws VnfException
    {
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();
        
    	MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("CreateVfModule");

        // Require a model customization ID.  Every VF Module definition must have one.
        if (modelCustomizationUuid == null  ||  modelCustomizationUuid.isEmpty()) {
			LOGGER.debug("Missing required input: modelCustomizationUuid");
			String error = "Create vfModule error: Missing required input: modelCustomizationUuid";
            LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
                    "VF Module ModelCustomizationUuid", "null", "Cloudify", "", MsoLogger.ErrorCode.DataError, "Create VF Module: Missing required input: modelCustomizationUuid");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
            throw new VnfException(error, MsoExceptionCategory.USERDATA);
        }
        
        // Clean up some inputs to make comparisons easier
        if (requestType == null)
        	requestType = "";
        
        if ("".equals(volumeGroupId) || "null".equals(volumeGroupId))
        	volumeGroupId = null;  
        
        if ("".equals(baseVfModuleId) || "null".equals(baseVfModuleId))
        	baseVfModuleId = null;

        if (inputs == null) {
        	// Create an empty set of inputs
        	inputs = new HashMap<String,String>();
        	LOGGER.debug("inputs == null - setting to empty");
        } else {
        	this.sendMapToDebug(inputs);
        }
        
        // Check if this is for a "Volume" module
        boolean isVolumeRequest = false;
        if (requestType.startsWith("VOLUME")) {
        	isVolumeRequest = true;
        }

        LOGGER.debug("requestType = " + requestType + ", volumeGroupStackId = " + volumeGroupId + ", baseStackId = " + baseVfModuleId);

        // Build a default rollback object (no actions performed)
        VnfRollback vfRollback = new VnfRollback();
        vfRollback.setCloudSiteId(cloudSiteId);
        vfRollback.setTenantId(tenantId);
        vfRollback.setMsoRequest(msoRequest);
        vfRollback.setRequestType(requestType);
        vfRollback.setIsBase(false);	// Until we know better
        vfRollback.setVolumeGroupHeatStackId(volumeGroupId);
        vfRollback.setBaseGroupHeatStackId(baseVfModuleId);
        vfRollback.setModelCustomizationUuid(modelCustomizationUuid);
        vfRollback.setMode("CFY");
        
		rollback.value = vfRollback; // Default rollback - no updates performed

        // Get the VNF/VF Module definition from the Catalog DB first.
        // There are three relevant records:  VfModule, VfModuleCustomization, VnfResource

        CatalogDatabase db = CatalogDatabase.getInstance();
    	VfModule vf = null;
    	VnfResource vnfResource = null;
    	VfModuleCustomization vfmc = null;

        try {
        	vfmc = db.getVfModuleCustomizationByModelCustomizationId(modelCustomizationUuid);
        	
            if (vfmc == null) {
        		String error = "Create vfModule error: Unable to find vfModuleCust with modelCustomizationUuid=" + modelCustomizationUuid;
        		LOGGER.debug(error);
                LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
                            "VF Module ModelCustomizationUuid", modelCustomizationUuid, "CatalogDb", "", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
        		LOGGER.debug("Found vfModuleCust entry " + vfmc.toString());
            }

            // Get the vfModule and vnfResource records
        	vf = vfmc.getVfModule();
        	vnfResource = db.getVnfResourceByModelUuid(vf.getVnfResourceModelUUId());
        }
        catch (Exception e) {
            db.close ();
        	LOGGER.debug("unhandled exception in create VF - [Query]" + e.getMessage());
        	throw new VnfException("Exception during create VF " + e.getMessage());
        }

        //  Perform a version check against cloudSite
        // Obtain the cloud site information where we will create the VF Module
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite (cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new VnfException (new MsoCloudSiteNotFound (cloudSiteId));
        }
		MavenLikeVersioning aicV = new MavenLikeVersioning();
		aicV.setVersion(cloudSite.get().getAic_version());
    
		String vnfMin = vnfResource.getAicVersionMin();
		String vnfMax = vnfResource.getAicVersionMax();
		
		if ( (vnfMin != null && !(aicV.isMoreRecentThan(vnfMin) || aicV.isTheSameVersion(vnfMin))) ||
		     (vnfMax != null && aicV.isMoreRecentThan(vnfMax)))
		{
			// ERROR
			String error = "VNF Resource type: " + vnfResource.getModelName() + ", ModelUuid=" + vnfResource.getModelUuid() + " VersionMin=" + vnfMin + " VersionMax:" + vnfMax + " NOT supported on Cloud: " + cloudSite.get().getId() + " with AIC_Version:" + cloudSite.get().getAic_version();
			LOGGER.error(MessageEnum.RA_CONFIG_EXC, error, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - setVersion");
			LOGGER.debug(error);
			throw new VnfException(error, MsoExceptionCategory.USERDATA);
		}
		// End Version check
        
        
    	// Get Utilities for Cloudify.
        MsoCloudifyUtils cloudify = new MsoCloudifyUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
        DeploymentInfo cloudifyDeployment = null;
        
        // First, look up to see if the VF already exists.

        long subStartTime1 = System.currentTimeMillis ();
        try {
            cloudifyDeployment = cloudify.queryDeployment (cloudSiteId, tenantId, vfModuleName);
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Cloudify", "Cloudify", "QueryDeployment", vfModuleName);
        }
        catch (MsoException me) {
            // Failed to query the Deployment due to a cloudify exception.
            String error = "Create VF Module: Query " + vfModuleName + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Exception - queryDeployment", me);
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "QueryDeployment", vfModuleName);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);

            // Convert to a generic VnfException
            me.addContext ("CreateVFModule");
            throw new VnfException (me);
        }
        
        // More precise handling/messaging if the Module already exists
        if (cloudifyDeployment != null && !(cloudifyDeployment.getStatus () == DeploymentStatus.NOTFOUND)) {
        	// CREATED, INSTALLED, INSTALLING, FAILED, UNINSTALLING, UNKNOWN
        	DeploymentStatus status = cloudifyDeployment.getStatus();
			LOGGER.debug ("Found Existing Deployment, status=" + status);
			
        	if (status == DeploymentStatus.INSTALLED) {
        		// fail - it exists
        		if (failIfExists != null && failIfExists) {
        			String error = "Create VF: Deployment " + vfModuleName + " already exists in " + cloudSiteId + "/" + tenantId;
        			LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Deployment " + vfModuleName + " already exists");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        			throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, cloudifyDeployment.getId());
        		} else {
        			// Found existing deployment and client has not requested "failIfExists".
        			// Populate the outputs from the existing deployment.

        			vnfId.value = cloudifyDeployment.getId();
        			outputs.value = copyStringOutputs (cloudifyDeployment.getOutputs ());
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module (found existing)");
                    return;
        		}
        	}
        	// Check through various detailed error cases
        	if (status == DeploymentStatus.INSTALLING || status == DeploymentStatus.UNINSTALLING) {
        		// fail - it's in progress - return meaningful error
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; please wait for it to complete, or fix manually.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Deployment " + vfModuleName + " already exists");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, cloudifyDeployment.getId());
        	}
        	else if (status == DeploymentStatus.FAILED) {
        		// fail - it exists and is in a FAILED state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and is in FAILED state in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Deployment " + vfModuleName + " already exists and is in FAILED state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, cloudifyDeployment.getId());
        	}
        	else if (status == DeploymentStatus.UNKNOWN || status == DeploymentStatus.CREATED) {
        		// fail - it exists and is in a UNKNOWN state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Deployment " + vfModuleName + " already exists and is in " + status.toString() + " state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, cloudifyDeployment.getId());
        	}
        	else {
        		// Unexpected, since all known status values have been tested for
                String error = "Create VF: Deployment " + vfModuleName + " already exists with unexpected status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "Cloudify", "queryDeployment", MsoLogger.ErrorCode.DataError, "Deployment " + vfModuleName + " already exists and is in an unknown state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, cloudifyDeployment.getId());
        	}
        }
   
        
        // Collect outputs from Base Modules and Volume Modules
        Map<String, Object> baseModuleOutputs = null;
        Map<String, Object> volumeGroupOutputs = null;

        // If a Volume Group was provided, query its outputs for inclusion in Module input parameters
        if (volumeGroupId != null) {
            long subStartTime2 = System.currentTimeMillis ();
            DeploymentInfo volumeDeployment = null;
            try {
                volumeDeployment = cloudify.queryDeployment (cloudSiteId, tenantId, volumeGroupId);
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Success response from Cloudify", "Cloudify", "QueryDeployment", volumeGroupId);
            }
            catch (MsoException me) {
                // Failed to query the Volume GroupDeployment due to a cloudify exception.
                String error = "Create VF Module: Query Volume Group " + volumeGroupId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, volumeGroupId, cloudSiteId, tenantId, "Cloudify", "queryDeployment(volume)", MsoLogger.ErrorCode.DataError, "Exception - queryDeployment(volume)", me);
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "QueryDeployment(volume)", volumeGroupId);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);

                // Convert to a generic VnfException
                me.addContext ("CreateVFModule(QueryVolume)");
                throw new VnfException (me);
            }
            
	        if (volumeDeployment == null || volumeDeployment.getStatus() == DeploymentStatus.NOTFOUND) {
        	    String error = "Create VFModule: Attached Volume Group DOES NOT EXIST " + volumeGroupId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, volumeGroupId, cloudSiteId, tenantId, error, "Cloudify", "queryDeployment(volume)", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Attached Volume Group DOES NOT EXIST");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested volume group");
        		volumeGroupOutputs = volumeDeployment.getOutputs();
        		this.sendMapToDebug(volumeGroupOutputs, "volumeGroupOutputs");
        	}
        }
	
        // If this is an Add-On Module, query the Base Module outputs
        // Note: This will be performed whether or not the current request is for an
        //       Add-On Volume Group or Add-On VF Module

        if (vf.isBase()) {
            LOGGER.debug("This is a BASE Module request");
            vfRollback.setIsBase(true);
        } else {
            LOGGER.debug("This is an Add-On Module request");
            
            // Add-On Modules should always have a Base, but just treat as a warning if not provided.
            // Add-on Volume requests may or may not specify a base.
            if (!isVolumeRequest && baseVfModuleId == null) {
                LOGGER.debug ("WARNING:  Add-on Module request - no Base Module ID provided");
            }

            if (baseVfModuleId != null) {
	            long subStartTime2 = System.currentTimeMillis ();
	            DeploymentInfo baseDeployment = null;
	            try {
	                baseDeployment = cloudify.queryDeployment (cloudSiteId, tenantId, baseVfModuleId);
	                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Success response from Cloudify", "Cloudify", "QueryDeployment(Base)", baseVfModuleId);
	            }
	            catch (MsoException me) {
	                // Failed to query the Volume GroupDeployment due to a cloudify exception.
	                String error = "Create VF Module: Query Base " + baseVfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
	                LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, baseVfModuleId, cloudSiteId, tenantId, "Cloudify", "queryDeployment(Base)", MsoLogger.ErrorCode.DataError, "Exception - queryDeployment(Base)", me);
	                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "QueryDeployment(Base)", baseVfModuleId);
	                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
	
	                // Convert to a generic VnfException
	                me.addContext ("CreateVFModule(QueryBase)");
	                throw new VnfException (me);
	            }
	            
		        if (baseDeployment == null || baseDeployment.getStatus() == DeploymentStatus.NOTFOUND) {
	        	    String error = "Create VFModule: Base Module DOES NOT EXIST " + baseVfModuleId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
	        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, baseVfModuleId, cloudSiteId, tenantId, error, "Cloudify", "queryDeployment(Base)", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Base Module DOES NOT EXIST");
	                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
	        	    LOGGER.debug(error);
	        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
	        	} else {
	        		LOGGER.debug("Found base module");
	        		baseModuleOutputs = baseDeployment.getOutputs();
	        		this.sendMapToDebug(baseModuleOutputs, "baseModuleOutputs");
	        	}
            }
        }
	

        // Ready to deploy the new VNF
        
        // NOTE:  For this section, heatTemplate is used for both HEAT templates and Cloudify blueprints.
        // In final implementation (post-POC), the template object would either be generic or there would
        // be a separate DB Table/Object for Blueprints.

        try {
        	// NOTE: The template is fixed for the VF Module.  The environment is part of the customization.
            String heatTemplateArtifactUuid = null;
            String heatEnvironmentArtifactUuid = null;

			if (isVolumeRequest) {
				heatTemplateArtifactUuid = vf.getVolHeatTemplateArtifactUUId();
				heatEnvironmentArtifactUuid = vfmc.getVolEnvironmentArtifactUuid();
			} else {
				heatTemplateArtifactUuid = vf.getHeatTemplateArtifactUUId();
				heatEnvironmentArtifactUuid = vfmc.getHeatEnvironmentArtifactUuid();
			}
			
			if (heatTemplateArtifactUuid == null || heatTemplateArtifactUuid.equals("")) {
				String error = "Create: No Heat Template ID defined in catalog database for " + vfModuleType + ", reqType=" + requestType;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Template ID", vfModuleType, "Cloudify", "", MsoLogger.ErrorCode.DataError, "Create: No Heat Template ID defined in catalog database");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			}
			
			HeatTemplate heatTemplate = db.getHeatTemplateByArtifactUuidRegularQuery(heatTemplateArtifactUuid);

			if (heatTemplate == null) {
				String error = "Create VF/VNF: no entry found for heat template ID = " + heatTemplateArtifactUuid;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
						"Heat Template ID",
						String.valueOf(heatTemplateArtifactUuid), "Cloudify", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create VF/VNF: no entry found for heat template ID = " + heatTemplateArtifactUuid);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			}
			LOGGER.debug("Got HEAT Template record from DB");

			// Next get the Environment record.  This is optional.
            HeatEnvironment heatEnvironment = null;
            if (heatEnvironmentArtifactUuid != null && !heatEnvironmentArtifactUuid.equals(""))
            {
                heatEnvironment = db.getHeatEnvironmentByArtifactUuid(heatEnvironmentArtifactUuid);
                if (heatEnvironment == null) {
                    String error = "Create VFModule: undefined Heat Environment. VFModule=" + vfModuleType
                                   + ", Environment ID="
                                   + heatEnvironmentArtifactUuid;
                    LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Environment ID", String.valueOf(heatEnvironmentArtifactUuid), "Cloudify", "getEnvironment", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: undefined Heat Environment");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                    // Alarm on this error, configuration must be fixed
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                    throw new VnfException (error, MsoExceptionCategory.INTERNAL);
                }
                LOGGER.debug ("Got Heat Environment from DB");
            } else {
                LOGGER.debug ("no environment parameter found for this Type " + vfModuleType);
            }

            
            // NOTE:  Logic to support nested templates and "get_file" attachments was removed from
            //        Cloudify-based adapter.  Assumption is that the actual blueprints and associated
            //        artifacts have been pre-loaded to Cloudify.  If that changes, logic will need 
            //        to be added in to dynamically build and deploy the blueprint (with all associated
            //        artifacts required for the CSAR/ZIP) before starting the actual orchestration.
            

            // All variables converted to their native object types
            HashMap<String, Object> goldenInputs = new HashMap<String,Object>();
            List<String> extraInputs = new ArrayList<String>();

            // NOTE: SKIP THIS FOR CLOUDIFY for now.  Just use what was passed in.
            //  This whole section needs to be rewritten.
			Boolean skipInputChecks = false;
            
			if (skipInputChecks) {
				goldenInputs = new HashMap<String,Object>();
				for (String key : inputs.keySet()) {
					goldenInputs.put(key, inputs.get(key));
				}
			}
			else {
				// Build maps for the parameters (including aliases) to simplify checks
				HashMap<String, HeatTemplateParam> params = new HashMap<String, HeatTemplateParam>();
				
				Set<HeatTemplateParam> paramSet = heatTemplate.getParameters();
				LOGGER.debug("paramSet has " + paramSet.size() + " entries");
				
				for (HeatTemplateParam htp : paramSet) {
					params.put(htp.getParamName(), htp);

					// Include aliases.
					String alias = htp.getParamAlias();
					if (alias != null && !alias.equals("") && !params.containsKey(alias)) {
						params.put(alias, htp);
					}
				}
				
				// First, convert all inputs to their "template" type
				for (String key : inputs.keySet()) {
					if (params.containsKey(key)) {
						Object value = cloudify.convertInputValue(inputs.get(key), params.get(key));
						if (value != null) {
							goldenInputs.put(key, value);
						}
						else {
							LOGGER.debug("Failed to convert input " + key + "='" + inputs.get(key) + "' to " + params.get(key).getParamType());
						}
					} else {
						extraInputs.add(key);
					}
				}
				
				if (!extraInputs.isEmpty()) {
					LOGGER.debug("Ignoring extra inputs: " + extraInputs);
				}
				
				// Next add in Volume Group Outputs if there are any.  Copy directly without conversions.
				if (volumeGroupOutputs != null  &&  !volumeGroupOutputs.isEmpty()) {
					for (String key : volumeGroupOutputs.keySet()) {
						if (params.containsKey(key)  &&  !goldenInputs.containsKey(key)) {
							goldenInputs.put(key, volumeGroupOutputs.get(key));
						}
					}
				}
				
				// Next add in Base Module Outputs if there are any.  Copy directly without conversions.
				if (baseModuleOutputs != null  &&  !baseModuleOutputs.isEmpty()) {
					for (String key : baseModuleOutputs.keySet()) {
						if (params.containsKey(key)  &&  !goldenInputs.containsKey(key)) {
							goldenInputs.put(key, baseModuleOutputs.get(key));
						}
					}
				}
				
				// Last, add in values from the "environment" file.
				// These are added to the inputs, since Cloudify doesn't pass an environment file like Heat.
				if (heatEnvironment != null)
				{
					// TODO: This may take a different form for Cloudify, but for now process it
					//       with Heat environment file syntax
	                StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());
					MsoHeatEnvironmentEntry mhee = new MsoHeatEnvironmentEntry (sb);
					
					if (mhee.getParameters() != null) {
						for (MsoHeatEnvironmentParameter envParam : mhee.getParameters()) {
							// If this is a template input, copy to golden inputs
							String envKey = envParam.getName();
							if (params.containsKey(envKey)  &&  !goldenInputs.containsKey(envKey)) {
								Object value = cloudify.convertInputValue(envParam.getValue(), params.get(envKey));
								if (value != null) {
									goldenInputs.put(envKey, value);
								}
								else {
									LOGGER.debug("Failed to convert environment parameter " + envKey + "='" + envParam.getValue() + "' to " + params.get(envKey).getParamType());
								}
							}
						}
					}
	            }
				else {
	            	LOGGER.debug("NO ENVIRONMENT for this entry");
	            }

	            this.sendMapToDebug(goldenInputs, "Final inputs sent to Cloudify");

				
	            // Check that required parameters have been supplied from any of the sources
	            String missingParams = null;
	            boolean checkRequiredParameters = true;
	            try {
	                String propertyString = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER)
	                                                     .getProperty (MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS,null);
	                if ("false".equalsIgnoreCase (propertyString) || "n".equalsIgnoreCase (propertyString)) {
	                    checkRequiredParameters = false;
	                    LOGGER.debug ("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
	                                  + MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS);
	                }
	            } catch (Exception e) {
	                // No problem - default is true
	                LOGGER.debug ("An exception occured trying to get property " + MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS, e);
	            }
	            
	            
	            for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
	                if (parm.isRequired () && (!goldenInputs.containsKey (parm.getParamName ()))) {
                        LOGGER.debug ("adding to missing parameters list: " + parm.getParamName ());
                        if (missingParams == null) {
                            missingParams = parm.getParamName ();
                        } else {
                            missingParams += "," + parm.getParamName ();
                        }
	                }
	            }
			
	            if (missingParams != null) {
	            	if (checkRequiredParameters) {
	            		// Problem - missing one or more required parameters
	            		String error = "Create VFModule: Missing Required inputs: " + missingParams;
	            		LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams, "Cloudify", "", MsoLogger.ErrorCode.DataError, "Create VFModule: Missing Required inputs");
	                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
	            		throw new VnfException (error, MsoExceptionCategory.USERDATA);
	            	} else {
	            		LOGGER.debug ("found missing parameters [" + missingParams + "] - but checkRequiredParameters is false - will not block");
	            	}
	            } else {
	                LOGGER.debug ("No missing parameters found - ok to proceed");
	            }
	
			} // NOTE: END PARAMETER CHECKING

			// Ready to deploy the VF Module.
			// First step - make sure the blueprint is loaded into Cloudify.
			
			String blueprintName = heatTemplate.getTemplateName();
			String blueprint = heatTemplate.getTemplateBody();
			
			// Use the main blueprint name as the blueprint ID (strip yaml extensions).
            String blueprintId = blueprintName;
            if (blueprintId.endsWith(".yaml"))
            	blueprintId = blueprintId.substring(0,blueprintId.lastIndexOf(".yaml"));

			try {
				if (! cloudify.isBlueprintLoaded (cloudSiteId, blueprintId)) {
					LOGGER.debug ("Blueprint " + blueprintId + " is not loaded.  Will upload it now.");

					Map<String,byte[]> blueprintFiles = new HashMap<String,byte[]>();

					blueprintFiles.put(blueprintName, blueprint.getBytes());
					
		            // TODO:  Implement nested blueprint logic based on Cloudify structures.
					//        For now, just use the Heat structures.
					//        The query returns a map of String->Object, where the map keys provide one layer of
					//        indirection from the Heat template names.  For this case, assume the map key matches
					//        the nested blueprint name.
		            Map <String, Object> nestedBlueprints = db.getNestedTemplates (heatTemplate.getArtifactUuid());
		            if (nestedBlueprints != null) {
			            for (String nestedBlueprintName: nestedBlueprints.keySet()) {
			            	String nestedBlueprint = (String) nestedBlueprints.get(nestedBlueprintName);
			            	blueprintFiles.put(nestedBlueprintName, nestedBlueprint.getBytes());
			            }
		            }

		            // TODO:  Implement file artifact logic based on Cloudify structures.  
		            //        For now, just use the Heat structures.
		            Map<String, HeatFiles> heatFiles = db.getHeatFilesForVfModule (vf.getModelUUID());
		            if (heatFiles != null) {
			            for (String heatFileName: heatFiles.keySet()) {
			            	String heatFile = heatFiles.get(heatFileName).getFileBody();
			            	blueprintFiles.put(heatFileName, heatFile.getBytes());
			            }
		            }
		            
		            // Upload the blueprint package
					cloudify.uploadBlueprint(cloudSiteId, blueprintId, blueprintName, blueprintFiles, false);

				}
			}
			catch (MsoException me) {
                me.addContext ("CreateVFModule");
                String error = "Create VF Module: Upload blueprint failed.  Blueprint=" + blueprintName + ": " + me;
                LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "Cloudify", "", MsoLogger.ErrorCode.DataError, "MsoException - uploadBlueprint", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new VnfException (me);
				
			}
			
            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.
            long createDeploymentStarttime = System.currentTimeMillis ();
            try {
            	// KLUDGE - Cloudify requires Tenant Name for Openstack.  We have the ID.
            	//          Go directly to Keystone until APIs could be updated to supply the name.
            	MsoKeystoneUtils keystone = new MsoKeystoneUtils(MSO_PROP_VNF_ADAPTER);
            	MsoTenant msoTenant = keystone.queryTenant(tenantId, cloudSiteId);
            	String tenantName = (msoTenant != null? msoTenant.getTenantName() : tenantId);
            	
            	if (backout == null) {
            		backout = true;
            	}
            	cloudifyDeployment = cloudify.createAndInstallDeployment (cloudSiteId,
                                              tenantName,
                                              vfModuleName,
                                              blueprintId,
                                              goldenInputs,
                                              true,
                                              heatTemplate.getTimeoutMinutes (),
                                              backout.booleanValue());
            	
                LOGGER.recordMetricEvent (createDeploymentStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Cloudify", "Cloudify", "CreateDeployment", vfModuleName);
            } catch (MsoException me) {
                me.addContext ("CreateVFModule");
                String error = "Create VF Module " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + me;
                LOGGER.recordMetricEvent (createDeploymentStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "CreateDeployment", vfModuleName);
                LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "Cloudify", "", MsoLogger.ErrorCode.DataError, "MsoException - createDeployment", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new VnfException (me);
            } catch (NullPointerException npe) {
                String error = "Create VFModule " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + npe;
                LOGGER.recordMetricEvent (createDeploymentStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "CreateDeployment", vfModuleName);
                LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "Cloudify", "", MsoLogger.ErrorCode.DataError, "NullPointerException - createDeployment", npe);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                LOGGER.debug("NULL POINTER EXCEPTION at cloudify.createAndInstallDeployment");
                //npe.addContext ("CreateVNF");
                throw new VnfException ("NullPointerException during cloudify.createAndInstallDeployment");
            } catch (Exception e) {
                LOGGER.recordMetricEvent (createDeploymentStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while creating deployment with Cloudify", "Cloudify", "CreateDeployment", vfModuleName);
                LOGGER.debug("unhandled exception at cloudify.createAndInstallDeployment");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while creating deployment with Cloudify");
            	throw new VnfException("Exception during cloudify.createAndInstallDeployment! " + e.getMessage());
            }      	
        } catch (Exception e) {
        	LOGGER.debug("unhandled exception in create VF");
        	throw new VnfException("Exception during create VF " + e.getMessage());

        } finally {
            // Make sure DB session is closed
            db.close ();
        }

        // Reach this point if create is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfCreated (true);
        vfRollback.setVnfId (cloudifyDeployment.getId());
        vnfId.value = cloudifyDeployment.getId();
        outputs.value = copyStringOutputs (cloudifyDeployment.getOutputs ());        	

        rollback.value = vfRollback;

        LOGGER.debug ("VF Module " + vfModuleName + " successfully created");
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module");
        return;
    }

    public void deleteVfModule (String cloudSiteId,
                           String tenantId,
                           String vnfName,
                           MsoRequest msoRequest,
                           Holder <Map <String, String>> outputs) throws VnfException {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("DeleteVf");
        LOGGER.debug ("Deleting VF " + vnfName + " in " + cloudSiteId + "/" + tenantId);
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        MsoCloudifyUtils cloudify = new MsoCloudifyUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
    	
        // 1702 capture the output parameters on a delete
        // so we'll need to query first
        DeploymentInfo deployment = null;
        try {
        	deployment = cloudify.queryDeployment(cloudSiteId, tenantId, vnfName);
        } catch (MsoException me) {
            // Failed to query the deployment.  Convert to a generic VnfException
            me.addContext ("DeleteVFModule");
            String error = "Delete VFModule: Query to get outputs: " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "Cloudify", "QueryDeployment", null);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "Cloudify", "QueryDeployment", MsoLogger.ErrorCode.DataError, "Exception - QueryDeployment", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }
        // call method which handles the conversion from Map<String,Object> to Map<String,String> for our expected Object types
        outputs.value = convertMapStringObjectToStringString(deployment.getOutputs());

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        long subStartTime = System.currentTimeMillis ();
        try {
            cloudify.uninstallAndDeleteDeployment(cloudSiteId, tenantId, vnfName, 5);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from DeleteDeployment", "Cloudify", "DeleteDeployment", vnfName);
        } catch (MsoException me) {
            me.addContext ("DeleteVfModule");
            // Convert to a generic VnfException
            String error = "Delete VF: " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "DeleteDeployment", "DeleteDeployment", vnfName);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vnfName, cloudSiteId, tenantId, "DeleteDeployment", "DeleteDeployment", MsoLogger.ErrorCode.DataError, "Exception - DeleteDeployment: " + me.getMessage());
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        // On success, nothing is returned.
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully delete VF");
        return;
    }

    // TODO:  Should Update be supported for Cloudify?  What would this look like?
    @Override
    public void updateVfModule (String cloudSiteId,
                           String tenantId,
                           String vnfType,
                           String vnfVersion,
                           String vnfName,
                           String requestType,
                           String volumeGroupHeatStackId,
                           String baseVfHeatStackId,
                           String vfModuleStackId,
                           String modelCustomizationUuid,
                           Map <String, String> inputs,
                           MsoRequest msoRequest,
                           Holder <Map <String, String>> outputs,
                           Holder <VnfRollback> rollback) throws VnfException
        {
        	// This operation is not currently supported for Cloudify-orchestrated VF Modules.
        	LOGGER.debug ("Update VF Module command attempted but not supported");
        	throw new VnfException ("UpdateVfModule:  Unsupported command", MsoExceptionCategory.USERDATA);
        }

}
