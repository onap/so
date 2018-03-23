/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

/**
 * This VNF Adapter implementation is based on the VDU Plugin model.  It assumes that each
 * VF Module definition in the MSO catalog is expressed via a set of template and/or file
 * artifacts that are appropriate for some specific sub-orchestrator that provides an
 * implementation of the VduPlugin interface.  This adapter handles all of the common
 * VF Module logic, including:
 * - catalog lookups for artifact retrieval
 * - parameter filtering and validation
 * - base and volume module queries
 * - rollback logic
 * - logging and error handling
 * 
 * Then based on the orchestration mode of the VNF, it will invoke different VDU plug-ins
 * to perform the low level instantiations, deletions, and queries.  At this time, the
 * set of available plug-ins is hard-coded, though in the future a dynamic selection
 * is expected (e.g. via a service-provider interface).
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

import org.openecomp.mso.adapters.vdu.CloudInfo;
import org.openecomp.mso.adapters.vdu.VduException;
import org.openecomp.mso.adapters.vdu.VduInstance;
import org.openecomp.mso.adapters.vdu.VduModelInfo;
import org.openecomp.mso.adapters.vdu.VduPlugin;
import org.openecomp.mso.adapters.vdu.VduStateType;
import org.openecomp.mso.adapters.vdu.VduStatus;
import org.openecomp.mso.adapters.vdu.mapper.VfModuleCustomizationToVduMapper;
import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloudify.utils.MsoCloudifyUtils;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
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
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.utils.MsoHeatEnvironmentEntry;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebService(serviceName = "VnfAdapter", endpointInterface = "org.openecomp.mso.adapters.vnf.MsoVnfAdapter", targetNamespace = "http://org.openecomp.mso/vnf")
public class MsoVnfPluginAdapterImpl implements MsoVnfAdapter {

	CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	protected CloudConfig cloudConfig = cloudConfigFactory.getCloudConfig();
	protected MsoHeatUtils heatUtils;
	protected VfModuleCustomizationToVduMapper vduMapper;
	protected MsoCloudifyUtils cloudifyUtils;
	
	MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	private static final String MSO_PROP_VNF_ADAPTER = "MSO_PROP_VNF_ADAPTER";
    private static final String MSO_CONFIGURATION_ERROR = "MsoConfigurationError";
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static final String CHECK_REQD_PARAMS = "org.openecomp.mso.adapters.vnf.checkRequiredParameters";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck () {
        LOGGER.debug ("Health check call in VNF Plugin Adapter");
    }

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     * @see MsoVnfPluginAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfPluginAdapterImpl() {

    }

    /**
     * This constructor MUST be used if this class is called with the new operator.
     * @param msoPropFactory
     */
    public MsoVnfPluginAdapterImpl(MsoPropertiesFactory msoPropFactory, CloudConfigFactory cloudConfigFact) {
    	this.msoPropertiesFactory = msoPropFactory;
    	this.cloudConfigFactory = cloudConfigFact;
    	heatUtils = new MsoHeatUtils(MSO_PROP_VNF_ADAPTER, msoPropertiesFactory, cloudConfigFactory);
    	vduMapper = new VfModuleCustomizationToVduMapper();
    	cloudifyUtils = new MsoCloudifyUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
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
     * The method returns an indicator that the VNF exists, along with its status and outputs.
     * The input "vnfName" will also be reflected back as its ID.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param tenantId Openstack tenant identifier
     * @param vnfNameOrId VNF Name or ID to query
     * @param msoRequest Request tracking information for logs
     * @param vnfExists Flag reporting the result of the query
     * @param vnfId Holder for output VNF ID
     * @param outputs Holder for Map of outputs from the deployed VF Module (assigned IPs, etc)
     */
    @Override
    public void queryVnf (String cloudSiteId,
                          String tenantId,
                          String vnfNameOrId,
                          MsoRequest msoRequest,
                          Holder <Boolean> vnfExists,
                          Holder <String> vnfId,
                          Holder <VnfStatus> status,
                          Holder <Map <String, String>> outputs)
        throws VnfException
    {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("QueryVnf");
        LOGGER.debug ("Querying VNF " + vnfNameOrId + " in " + cloudSiteId + "/" + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();
        long subStartTime = System.currentTimeMillis ();
        
        VduInstance vduInstance = null;
    	CloudInfo cloudInfo = new CloudInfo(cloudSiteId, tenantId, null);

        VduPlugin vduPlugin = getVduPlugin(cloudSiteId);
        
    	try {
    		vduInstance =  vduPlugin.queryVdu(cloudInfo, vnfNameOrId);
    		LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received VDU Query response", "VDU", "QueryVDU", vnfNameOrId);
    	}
    	catch (VduException e) {
    		// Failed to query the VDU due to a plugin exception.
    		e.addContext ("QueryVNF");
            String error = "Query VNF (VDU): " + vnfNameOrId + " in " + cloudSiteId + "/" + tenantId + ": " + e;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "QueryVNF", vnfNameOrId);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfNameOrId, cloudSiteId, tenantId, "VDU", "QueryVNF", MsoLogger.ErrorCode.DataError, "Exception - queryVDU", e);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (e);
    	}
        	
    	if (vduInstance != null  &&  vduInstance.getStatus().getState() != VduStateType.NOTFOUND) {
            vnfExists.value = Boolean.TRUE;
            status.value = vduStatusToVnfStatus(vduInstance);
            vnfId.value = vduInstance.getVduInstanceId();
            outputs.value = copyStringOutputs (vduInstance.getOutputs ());

            LOGGER.debug ("VNF " + vnfNameOrId + " found, ID = " + vnfId.value);
    	}
        else {
            vnfExists.value = Boolean.FALSE;
            status.value = VnfStatus.NOTFOUND;
            vnfId.value = null;
            outputs.value = new HashMap <String, String> (); // Return as an empty map

            LOGGER.debug ("VNF " + vnfNameOrId + " not found");
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
        CloudInfo cloudInfo = new CloudInfo (cloudSiteId, tenantId, null);

        String vfModuleId = rollback.getVfModuleStackId ();

        MsoLogger.setLogContext (rollback.getMsoRequest());

        LOGGER.debug ("Rolling Back VF Module " + vfModuleId + " in " + cloudSiteId + "/" + tenantId);

        VduInstance vduInstance = null;

        // Use the VduPlugin to delete the VF Module.
        VduPlugin vduPlugin = getVduPlugin(cloudSiteId);

        long subStartTime = System.currentTimeMillis ();
        try {
        	// TODO: Get a reasonable timeout.  Use a global property, or store the creation timeout in rollback object and use that.
            vduInstance = vduPlugin.deleteVdu(cloudInfo, vfModuleId, 5);
            
            LOGGER.debug("Rolled back VDU instantiation: " + vduInstance.getVduInstanceId());
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from VDU Plugin", "VDU", "DeleteVdu", null);
        }
        catch (VduException ve) {
            // Failed to rollback the VF Module due to a plugin exception.
            // Convert to a generic VnfException
            ve.addContext ("RollbackVFModule");
            String error = "Rollback VF Module: " + vfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + ve;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "DeleteVdu", null);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vfModuleId, cloudSiteId, tenantId, "VDU", "DeleteVdu", MsoLogger.ErrorCode.DataError, "Exception - DeleteVdu", ve);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (ve);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully roll back VF Module");
        return;
    }


    private VnfStatus vduStatusToVnfStatus (VduInstance vdu) {
    	// Determine the status based on last action & status
    	// DeploymentInfo object should be enhanced to report a better status internally.
    	VduStatus vduStatus = vdu.getStatus();
    	VduStateType status = vduStatus.getState();
    	
    	if (status == null) {
    		return VnfStatus.UNKNOWN;
    	}
    	else if (status == VduStateType.NOTFOUND) {
			return VnfStatus.NOTFOUND;
	}
    	else if (status == VduStateType.INSTANTIATED) {
    			return VnfStatus.ACTIVE;
    	}
    	else if (status == VduStateType.FAILED) {
    		return VnfStatus.FAILED;
    	}

    	return VnfStatus.UNKNOWN;
    }
    
    /*
	 * Normalize an input value to an Object, based on the target parameter type.
	 * If the type is not recognized, it will just be returned unchanged (as a string).
	 */
	private Object convertInputValue (String inputValue, HeatTemplateParam templateParam)
	{
		String type = templateParam.getParamType();
		LOGGER.debug("Parameter: " + templateParam.getParamName() + " is of type " + type);
		
		if (type.equalsIgnoreCase("number")) {
			try {
				return Integer.valueOf(inputValue);
			}
			catch (Exception e) {
				LOGGER.debug("Unable to convert " + inputValue + " to an integer!");
				return null;
			}
		} else if (type.equalsIgnoreCase("json")) {
			try {
				JsonNode jsonNode = new ObjectMapper().readTree(inputValue);
				return jsonNode;
			}
			catch (Exception e) {
				LOGGER.debug("Unable to convert " + inputValue + " to a JsonNode!");
				return null;
			}
		} else if (type.equalsIgnoreCase("boolean")) {
			return new Boolean(inputValue);
		}
		
		// Nothing else matched.  Return the original string
		return inputValue;
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
     * All VF Modules are defined in the MSO catalog. The caller must request one of
     * the pre-defined module types or an error will be returned. Within the catalog,
     * each VF Module references (among other things) a collection of artifacts that
     * are used to deploy the required cloud resources (VMs, networks, etc.).
     *
     * Depending on the module templates, a variable set of input parameters will
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
     * @param vnfId Holder for output VF Module instance ID in the cloud
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
                    "VF Module ModelCustomizationUuid", "null", "VDU", "", MsoLogger.ErrorCode.DataError, "Create VF Module: Missing required input: modelCustomizationUuid");
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
    	VfModule vfModule = null;
    	VnfResource vnfResource = null;
    	VfModuleCustomization vfModuleCust = null;

        try {
        	vfModuleCust = db.getVfModuleCustomizationByModelCustomizationId(modelCustomizationUuid);
        	
            if (vfModuleCust == null) {
        		String error = "Create vfModule error: Unable to find vfModuleCust with modelCustomizationUuid=" + modelCustomizationUuid;
        		LOGGER.debug(error);
                LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
                            "VF Module ModelCustomizationUuid", modelCustomizationUuid, "CatalogDb", "", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
        		LOGGER.debug("Found vfModuleCust entry " + vfModuleCust.toString());
            }

            // Get the vfModule and vnfResource records
        	vfModule = vfModuleCust.getVfModule();
        	vnfResource = db.getVnfResourceByModelUuid(vfModule.getVnfResourceModelUUId());
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
        
        
		VduInstance vduInstance = null;
        CloudInfo cloudInfo = new CloudInfo (cloudSiteId, tenantId, null);
        
        // Use the VduPlugin.
        VduPlugin vduPlugin = getVduPlugin(cloudSiteId);
        
        // First, look up to see if the VF already exists.

        long subStartTime1 = System.currentTimeMillis ();
        try {
            vduInstance = vduPlugin.queryVdu (cloudInfo, vfModuleName);
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from VduPlugin", "VDU", "QueryVDU", vfModuleName);
        }
        catch (VduException me) {
            // Failed to query the VDU due to a plugin exception.
            String error = "Create VF Module: Query " + vfModuleName + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "Exception - queryVdu", me);
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "QueryVdu", vfModuleName);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);

            // Convert to a generic VnfException
            me.addContext ("CreateVFModule");
            throw new VnfException (me);
        }
        
        // More precise handling/messaging if the Module already exists
        if (vduInstance != null && !(vduInstance.getStatus().getState() == VduStateType.NOTFOUND)) {
        	VduStateType status = vduInstance.getStatus().getState();
			LOGGER.debug ("Found Existing VDU, status=" + status);
			
        	if (status == VduStateType.INSTANTIATED) {
        		if (failIfExists != null && failIfExists) {
            		// fail - it exists
        			String error = "Create VF: Deployment " + vfModuleName + " already exists in " + cloudSiteId + "/" + tenantId;
        			LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "VF Module " + vfModuleName + " already exists");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        			throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, vduInstance.getVduInstanceId());
        		} else {
        			// Found existing deployment and client has not requested "failIfExists".
        			// Populate the outputs from the existing deployment.

        			vnfId.value = vduInstance.getVduInstanceId();
        			outputs.value = copyStringOutputs (vduInstance.getOutputs ());
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module (found existing)");
                    return;
        		}
        	}
        	// Check through various detailed error cases
        	else if (status == VduStateType.INSTANTIATING || status == VduStateType.DELETING || status == VduStateType.UPDATING) {
        		// fail - it's in progress - return meaningful error
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; please wait for it to complete, or fix manually.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "VF Module " + vfModuleName + " already exists");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, vduInstance.getVduInstanceId());
        	}
        	else if (status == VduStateType.FAILED) {
        		// fail - it exists and is in a FAILED state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and is in FAILED state in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "VF Module " + vfModuleName + " already exists and is in FAILED state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, vduInstance.getVduInstanceId());
        	}
        	else if (status == VduStateType.UNKNOWN) {
        		// fail - it exists and is in a UNKNOWN state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "VF Module " + vfModuleName + " already exists and is in " + status.toString() + " state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, vduInstance.getVduInstanceId());
        	}
        	else {
        		// Unexpected, since all known status values have been tested for
                String error = "Create VF: Deployment " + vfModuleName + " already exists with unexpected status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "VDU", "queryVdu", MsoLogger.ErrorCode.DataError, "VF Module " + vfModuleName + " already exists and is in an unknown state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, vduInstance.getVduInstanceId());
        	}
        }
   
        
        // Collect outputs from Base Modules and Volume Modules
        Map<String, Object> baseModuleOutputs = null;
        Map<String, Object> volumeGroupOutputs = null;

        // If a Volume Group was provided, query its outputs for inclusion in Module input parameters
        if (volumeGroupId != null) {
            long subStartTime2 = System.currentTimeMillis ();
            VduInstance volumeVdu = null;
            try {
                volumeVdu = vduPlugin.queryVdu (cloudInfo, volumeGroupId);
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Success response from VduPlugin", "VDU", "QueryVdu", volumeGroupId);
            }
            catch (VduException me) {
                // Failed to query the Volume Group VDU due to a plugin exception.
                String error = "Create VF Module: Query Volume Group " + volumeGroupId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, volumeGroupId, cloudSiteId, tenantId, "VDU", "queryVdu(volume)", MsoLogger.ErrorCode.DataError, "Exception - queryVdu(volume)", me);
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "QueryVdu(volume)", volumeGroupId);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);

                // Convert to a generic VnfException
                me.addContext ("CreateVFModule(QueryVolume)");
                throw new VnfException (me);
            }
            
	        if (volumeVdu == null || volumeVdu.getStatus().getState() == VduStateType.NOTFOUND) {
        	    String error = "Create VFModule: Attached Volume Group DOES NOT EXIST " + volumeGroupId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, volumeGroupId, cloudSiteId, tenantId, error, "VDU", "queryVdu(volume)", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Attached Volume Group DOES NOT EXIST");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested volume group");
        		volumeGroupOutputs = volumeVdu.getOutputs();
        		this.sendMapToDebug(volumeGroupOutputs, "volumeGroupOutputs");
        	}
        }
	
        // If this is an Add-On Module, query the Base Module outputs
        // Note: This will be performed whether or not the current request is for an
        //       Add-On Volume Group or Add-On VF Module

        if (vfModule.isBase()) {
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
	            VduInstance baseVdu = null;
	            try {
	                baseVdu = vduPlugin.queryVdu (cloudInfo, baseVfModuleId);
	                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Success response from VduPlugin", "VDU", "QueryVdu(Base)", baseVfModuleId);
	            }
	            catch (MsoException me) {
	                // Failed to query the Base VF Module due to a Vdu Plugin exception.
	                String error = "Create VF Module: Query Base " + baseVfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
	                LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, baseVfModuleId, cloudSiteId, tenantId, "VDU", "queryVdu(Base)", MsoLogger.ErrorCode.DataError, "Exception - queryVdu(Base)", me);
	                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "QueryVdu(Base)", baseVfModuleId);
	                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
	
	                // Convert to a generic VnfException
	                me.addContext ("CreateVFModule(QueryBase)");
	                throw new VnfException (me);
	            }
	            
		        if (baseVdu == null || baseVdu.getStatus().getState() == VduStateType.NOTFOUND) {
	        	    String error = "Create VFModule: Base Module DOES NOT EXIST " + baseVfModuleId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
	        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, baseVfModuleId, cloudSiteId, tenantId, error, "VDU", "queryVdu(Base)", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Base Module DOES NOT EXIST");
	                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
	        	    LOGGER.debug(error);
	        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
	        	} else {
	        		LOGGER.debug("Found base module");
	        		baseModuleOutputs = baseVdu.getOutputs();
	        		this.sendMapToDebug(baseModuleOutputs, "baseModuleOutputs");
	        	}
            }
        }

        // NOTE:  For this section, heatTemplate is used for all template artifacts.
        // In final implementation (post-POC), the template object would either be generic or there would
        // be a separate DB Table/Object for different sub-orchestrators.

        // NOTE: The template is fixed for the VF Module.  The environment is part of the customization.

        	// NOTE: The template is fixed for the VF Module.  The environment is part of the customization.
            String heatTemplateArtifactUuid = null;
            String heatEnvironmentArtifactUuid = null;

			if (isVolumeRequest) {
				heatTemplateArtifactUuid = vfModule.getVolHeatTemplateArtifactUUId();
				heatEnvironmentArtifactUuid = vfModuleCust.getVolEnvironmentArtifactUuid();
			} else {
				heatTemplateArtifactUuid = vfModule.getHeatTemplateArtifactUUId();
				heatEnvironmentArtifactUuid = vfModuleCust.getHeatEnvironmentArtifactUuid();
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

            
         // Create the combined set of parameters from the incoming request, base-module outputs,
            // volume-module outputs.  Also, convert all variables to their native object types.
            
            HashMap<String, Object> goldenInputs = new HashMap<String,Object>();
            List<String> extraInputs = new ArrayList<String>();

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
    					Object value = convertInputValue(inputs.get(key), params.get(key));
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
				
				// TODO:  The model should support a mechanism to pre-assign default parameter values
				// per "customization" (i.e. usage) of a given module.  In HEAT, this is specified by
				// an Environment file.  There is not a general mechanism in the model to handle this.
				// For the general case, any such parameter/values can be added dynamically to the
				// inputs (only if not already specified).
				
				
	            // Check that required parameters have been supplied from any of the sources
	            String missingParams = null;
	            boolean checkRequiredParameters = true;
	            try {
	                String propertyString = this.msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_VNF_ADAPTER)
	                		.getProperty(MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS,null);
	                if ("false".equalsIgnoreCase (propertyString) || "n".equalsIgnoreCase (propertyString)) {
	                    checkRequiredParameters = false;
	                    LOGGER.debug ("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
	                                  + MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS);
	                }
	            } catch (Exception e) {
	                // No problem - default is true
	                LOGGER.debug ("An exception occured trying to get property " + MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS, e);
	            }
	            
	            // Do the actual parameter checking.
	            // Include looking at the ENV file as a valid definition of a parameter value.
	            // TODO:  This handling of ENV applies only to Heat.  A general mechanism to
	            // support pre-set parameter/values does not yet exist in the model.
	            //
				StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());
				MsoHeatEnvironmentEntry mhee = new MsoHeatEnvironmentEntry (sb);
	            for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
	                if (parm.isRequired () && (!goldenInputs.containsKey (parm.getParamName ()))) {
	                    if (mhee != null && mhee.containsParameter(parm.getParamName())) {
	                        LOGGER.debug ("Required parameter " + parm.getParamName ()
	                                      + " appears to be in environment - do not count as missing");
	                    } else {
		                    LOGGER.debug ("adding to missing parameters list: " + parm.getParamName ());
		                    if (missingParams == null) {
		                        missingParams = parm.getParamName ();
		                    } else {
		                        missingParams += "," + parm.getParamName ();
		                    }
	                    }
	                }
	            }
			
	            if (missingParams != null) {
	            	if (checkRequiredParameters) {
	            		// Problem - missing one or more required parameters
	            		String error = "Create VFModule: Missing Required inputs: " + missingParams;
	            		LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams, "VDU", "", MsoLogger.ErrorCode.DataError, "Create VFModule: Missing Required inputs");
	                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
	            		throw new VnfException (error, MsoExceptionCategory.USERDATA);
	            	} else {
	            		LOGGER.debug ("found missing parameters [" + missingParams + "] - but checkRequiredParameters is false - will not block");
	            	}
	            } else {
	                LOGGER.debug ("No missing parameters found - ok to proceed");
	            }

			} // NOTE: END PARAMETER CHECKING

			// Here we go...  ready to deploy the VF Module.
	        long instantiateVduStartTime = System.currentTimeMillis ();
	        if (backout == null) backout = true;
	        
			try {
				// Construct the VDU Model structure to pass to the targeted VduPlugin
				VduModelInfo vduModel = null;
				if (! isVolumeRequest) {
					vduModel = vduMapper.mapVfModuleCustomizationToVdu(vfModuleCust);
				} else {
					vduModel = vduMapper.mapVfModuleCustVolumeToVdu(vfModuleCust);
				}
			
				// Invoke the VduPlugin to instantiate the VF Module
				vduInstance = vduPlugin.instantiateVdu(cloudInfo, vfModuleName, goldenInputs, vduModel, backout);
				
	            LOGGER.recordMetricEvent (instantiateVduStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from VduPlugin", "VDU", "instantiateVdu", vfModuleName);
			}
			catch (VduException me) {
	            // Failed to instantiate the VDU.
	            me.addContext ("CreateVFModule");
	            String error = "Create VF Module " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + me;
	            LOGGER.recordMetricEvent (instantiateVduStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "instantiateVdu", vfModuleName);
	            LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "VDU", "", MsoLogger.ErrorCode.DataError, "MsoException - instantiateVdu", me);
	            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
	            // Convert to a generic VnfException
	            throw new VnfException (me);
	        }
		    catch (NullPointerException npe) {
		        String error = "Create VFModule " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + npe;
		        LOGGER.recordMetricEvent (instantiateVduStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error, "VDU", "instantiateVdu", vfModuleName);
		        LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "VDU", "", MsoLogger.ErrorCode.DataError, "NullPointerException - instantiateVdu", npe);
		        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, error);
		        LOGGER.debug("NULL POINTER EXCEPTION at vduPlugin.instantiateVdu", npe);
		        throw new VnfException ("NullPointerException during instantiateVdu");
		    }
			catch (Exception e) {
		        String error = "Create VFModule " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + e;
		        LOGGER.recordMetricEvent (instantiateVduStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, error, "VDU", "instantiateVdu", vfModuleName);
		        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, error);
		        LOGGER.debug("Unhandled exception at vduPlugin.instantiateVdu", e);
			} finally {
				db.close();
			}

        // Reach this point if create is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfCreated (true);
        vfRollback.setVnfId (vduInstance.getVduInstanceId());
        vnfId.value = vduInstance.getVduInstanceId();
        outputs.value = copyStringOutputs (vduInstance.getOutputs ());        	

        rollback.value = vfRollback;

        LOGGER.debug ("VF Module " + vfModuleName + " successfully created");
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module");
        return;
    }

    public void deleteVfModule (String cloudSiteId,
            String tenantId,
            String vfModuleId,
            MsoRequest msoRequest,
            Holder <Map <String, String>> outputs) throws VnfException
	{
		MsoLogger.setLogContext (msoRequest);
		MsoLogger.setServiceName ("DeleteVfModule");
		
		LOGGER.debug ("Deleting VF Module " + vfModuleId + " in " + cloudSiteId + "/" + tenantId);
		// Will capture execution time for metrics
		long startTime = System.currentTimeMillis ();
		
		// Capture the output parameters on a delete, so need to query first
		VduInstance vduInstance = null;
		CloudInfo cloudInfo = new CloudInfo(cloudSiteId, tenantId, null);
		
		// Use the VduPlugin.
		VduPlugin vduPlugin = getVduPlugin(cloudSiteId);
		
		try {
			vduInstance = vduPlugin.queryVdu (cloudInfo, vfModuleId);
			LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received VDU Query response", "VDU", "QueryVDU", vfModuleId);
		}
		catch (VduException e) {
			// Failed to query the VDU due to a plugin exception.
			// Convert to a generic VnfException
			e.addContext ("QueryVFModule");
			String error = "Query VfModule (VDU): " + vfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + e;
			LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "VDU", "QueryVNF", vfModuleId);
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleId, cloudSiteId, tenantId, "VDU", "QueryVFModule", MsoLogger.ErrorCode.DataError, "Exception - queryVDU", e);
			LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
			throw new VnfException (e);
		}
		
		// call method which handles the conversion from Map<String,Object> to Map<String,String> for our expected Object types
		outputs.value = convertMapStringObjectToStringString(vduInstance.getOutputs());
		
		// Use the VduPlugin to delete the VDU.
		// The possible outcomes of deleteVdu are
		// - a vnfInstance object with status of DELETED (success)
		// - a vnfInstance object with status of NOTFOUND (VDU did not exist, treat as success)
		// - a vnfInstance object with status of FAILED (error)
		// Also, VduException could be thrown.
		long subStartTime = System.currentTimeMillis ();
		try {
			// TODO:  Get an appropriate timeout value - require access to the model
			vduPlugin.deleteVdu(cloudInfo, vfModuleId, 5);
			LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from deleteVdu", "VDU", "DeleteVdu", vfModuleId);
		} catch (VduException me) {
			me.addContext ("DeleteVfModule");
			// Convert to a generic VnfException
			String error = "Delete VF: " + vfModuleId + " in " + cloudSiteId + "/" + tenantId + ": " + me;
			LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "DeleteVdu", "DeleteVdu", vfModuleId);
			LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vfModuleId, cloudSiteId, tenantId, "VDU", "DeleteVdu", MsoLogger.ErrorCode.DataError, "Exception - DeleteVdu: " + me.getMessage());
			LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
			throw new VnfException (me);
		}
		
		// On success, nothing is returned.
		LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully delete VF");
		return;
	}
	
	// Update VF Module not yet implemented for generic VDU plug-in model.
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
		// This operation is not currently supported for VduPlugin-orchestrated VF Modules.
		LOGGER.debug ("Update VF Module command attempted but not supported");
		throw new VnfException ("UpdateVfModule:  Unsupported command", MsoExceptionCategory.USERDATA);
	}
    
    /*
     * Dynamic selection of a VduPlugin version.  For initial tests, base on the "orchestrator"
     * defined for the target cloud.  Should really be looking at the VNF Model (ochestration_mode)
     * but we don't currently have access to that in Query and Delete cases.
     */
    private VduPlugin getVduPlugin (String cloudSiteId) {
    	Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
    	if (!cloudSite.isPresent()) {
    		String orchestrator = cloudSite.get().getOrchestrator();
    		
    		if (orchestrator.equalsIgnoreCase("CLOUDIFY")) {
    			return cloudifyUtils;   			
    		}
    		else if (orchestrator.equalsIgnoreCase("HEAT")) {
    			return heatUtils;
    		}
    	}
    	
    	// Default - return HEAT plugin, though will fail later
    	return heatUtils;
    }

}
