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

package org.openecomp.mso.adapters.vnf;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.adapters.vnf.exceptions.VnfNotFound;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfComponent;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtilsWithUpdate;
import org.openecomp.mso.openstack.utils.MsoHeatEnvironmentEntry;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

@WebService(serviceName = "VnfAdapter", endpointInterface = "org.openecomp.mso.adapters.vnf.MsoVnfAdapter", targetNamespace = "http://com.att.mso/vnf")
public class MsoVnfAdapterImpl implements MsoVnfAdapter {

	CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	protected CloudConfig cloudConfig = null;

	MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	private static final String MSO_PROP_VNF_ADAPTER = "MSO_PROP_VNF_ADAPTER";
    private static final String MSO_CONFIGURATION_ERROR = "MsoConfigurationError";
    private static final String VNF_ADAPTER_SERVICE_NAME = "MSO-BPMN:MSO-VnfAdapter.";
    private static final String LOG_REPLY_NAME = "MSO-VnfAdapter:MSO-BPMN.";
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static final String CHECK_REQD_PARAMS = "org.openecomp.mso.adapters.vnf.checkRequiredParameters";
    private static final String ADD_GET_FILES_ON_VOLUME_REQ = "org.openecomp.mso.adapters.vnf.addGetFilesOnVolumeReq";

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck () {
        LOGGER.debug ("Health check call in VNF Adapter");
    }

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     * @see MsoVnfAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfAdapterImpl() {

    }

    /**
     * This constructor MUST be used if this class is called with the new operator.
     * @param msoPropFactory
     */
    public MsoVnfAdapterImpl(MsoPropertiesFactory msoPropFactory, CloudConfigFactory cloudConfigFact) {
    	this.msoPropertiesFactory = msoPropFactory;
    	this.cloudConfigFactory = cloudConfigFact;
    }

    /**
     * This is the "Create VNF" web service implementation.
     * It will create a new VNF of the requested type in the specified cloud
     * and tenant. The tenant must exist before this service is called.
     *
     * If a VNF with the same name already exists, this can be considered a
     * success or failure, depending on the value of the 'failIfExists' parameter.
     *
     * All VNF types will be defined in the MSO catalog. The caller must request
     * one of these pre-defined types or an error will be returned. Within the
     * catalog, each VNF type references (among other things) a Heat template
     * which is used to deploy the required VNF artifacts (VMs, networks, etc.)
     * to the cloud.
     *
     * Depending on the Heat template, a variable set of input parameters will
     * be defined, some of which are required. The caller is responsible to
     * pass the necessary input data for the VNF or an error will be thrown.
     *
     * The method returns the vnfId (the canonical name), a Map of VNF output
     * attributes, and a VnfRollback object. This last object can be passed
     * as-is to the rollbackVnf operation to undo everything that was created
     * for the VNF. This is useful if a VNF is successfully created but the
     * orchestrator fails on a subsequent operation.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vnfType VNF type key, should match a VNF definition in catalog DB
     * @param vnfVersion VNF version key, should match a VNF definition in catalog DB
     * @param vnfName Name to be assigned to the new VNF
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered
     *        a success or failure
     * @param msoRequest Request tracking information for logs
     * @param vnfId Holder for output VNF Openstack ID
     * @param outputs Holder for Map of VNF outputs from heat (assigned IPs, etc)
     * @param rollback Holder for returning VnfRollback object
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
                           Holder <VnfRollback> rollback) throws VnfException {
    	// Create a hook here to catch shortcut createVf requests:
    	if (requestType != null) {
    		if (requestType.startsWith("VFMOD")) {
    			LOGGER.debug("Calling createVfModule from createVnf -- requestType=" + requestType);
    			String newRequestType = requestType.substring(5);
    			String vfVolGroupHeatStackId = "";
    			String vfBaseHeatStackId = "";
    			try {
    				if (volumeGroupHeatStackId != null) {
    					vfVolGroupHeatStackId = volumeGroupHeatStackId.substring(0, volumeGroupHeatStackId.lastIndexOf("|"));
    					vfBaseHeatStackId = volumeGroupHeatStackId.substring(volumeGroupHeatStackId.lastIndexOf("|")+1);
    				}
    			} catch (Exception e) {
    				// might be ok - both are just blank
    				LOGGER.debug("ERROR trying to parse the volumeGroupHeatStackId " + volumeGroupHeatStackId);
    			}
    			this.createVfModule(cloudSiteId, 
    					tenantId, 
    					vnfType, 
    					vnfVersion, 
    					vnfName, 
    					newRequestType, 
    					vfVolGroupHeatStackId, 
    					vfBaseHeatStackId, 
    					inputs, 
    					failIfExists, 
    					backout, 
    					msoRequest, 
    					vnfId, 
    					outputs, 
    					rollback);
    			return;
    		}
    	} 
    	// createVf will know if the requestType starts with "X" that it's the "old" way
    	StringBuilder newRequestTypeSb = new StringBuilder("X");
    	String vfVolGroupHeatStackId = "";
    	String vfBaseHeatStackId = "";
    	if (requestType != null) {
    		newRequestTypeSb.append(requestType);
    	}
		this.createVfModule(cloudSiteId, 
				tenantId, 
				vnfType, 
				vnfVersion, 
				vnfName, 
				newRequestTypeSb.toString(), 
				vfVolGroupHeatStackId, 
				vfBaseHeatStackId, 
				inputs, 
				failIfExists, 
				backout, 
				msoRequest, 
				vnfId, 
				outputs, 
				rollback);
    	return;
    	// End createVf shortcut
    }

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
                           Holder <VnfRollback> rollback) throws VnfException {
    	MsoLogger.setLogContext (msoRequest.getRequestId (), msoRequest.getServiceInstanceId ());
    	MsoLogger.setServiceName ("UpdateVnf");
    	String requestTypeString = "";
        if (requestType != null && !requestType.equals("")) {
        	requestTypeString = requestType;
        }
        String nestedStackId = null;
        if (volumeGroupHeatStackId != null && !volumeGroupHeatStackId.equals("")) {
        	nestedStackId = volumeGroupHeatStackId;
        }

        LOGGER.debug ("Updating VNF: " + vnfName + " of type " + vnfType + "in " + cloudSiteId + "/" + tenantId);
        LOGGER.debug("requestTypeString = " + requestTypeString + ", nestedStackId = " + nestedStackId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Build a default rollback object (no actions performed)
        VnfRollback vnfRollback = new VnfRollback ();
        vnfRollback.setCloudSiteId (cloudSiteId);
        vnfRollback.setTenantId (tenantId);
        vnfRollback.setMsoRequest (msoRequest);
        vnfRollback.setRequestType(requestTypeString);

        // First, look up to see if the VNF already exists.
        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
        MsoHeatUtilsWithUpdate heatU = new MsoHeatUtilsWithUpdate (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        StackInfo heatStack = null;
        long queryStackStarttime1 = System.currentTimeMillis ();
        try {
            heatStack = heat.queryStack (cloudSiteId, tenantId, vnfName);
            LOGGER.recordMetricEvent (queryStackStarttime1, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vnfName);
        } catch (MsoException me) {
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext ("UpdateVNF");
            String error = "Update VNF: Query " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (queryStackStarttime1, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vnfName);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in updateVnf", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        if (heatStack == null || heatStack.getStatus () == HeatStatus.NOTFOUND) {
            // Not Found
            String error = "Update VNF: Stack " + vnfName + " does not exist in " + cloudSiteId + "/" + tenantId;
            LOGGER.error (MessageEnum.RA_VNF_NOT_EXIST, vnfName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: Stack " + vnfName + " does not exist");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
            throw new VnfNotFound (cloudSiteId, tenantId, vnfName);
        } else {
            LOGGER.debug ("Found Existing stack, status=" + heatStack.getStatus ());
            // Populate the outputs from the existing stack.
            outputs.value = copyStringOutputs (heatStack.getOutputs ());
            rollback.value = vnfRollback; // Default rollback - no updates performed
        }
        
        // 1604 Cinder Volume support - handle a nestedStackId if sent (volumeGroupHeatStackId):
        StackInfo nestedHeatStack = null;
        long queryStackStarttime2 = System.currentTimeMillis ();
        if (nestedStackId != null) {
        	try {
        		LOGGER.debug("Querying for nestedStackId = " + nestedStackId);
        		nestedHeatStack = heat.queryStack(cloudSiteId, tenantId, nestedStackId);
                LOGGER.recordMetricEvent (queryStackStarttime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vnfName);
        	} catch (MsoException me) {
        	    // Failed to query the Stack due to an openstack exception.
        	    // Convert to a generic VnfException
        	    me.addContext ("UpdateVNF");
        	    String error = "Update VNF: Attached heatStack ID Query " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.recordMetricEvent (queryStackStarttime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vnfName);
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.AvailabilityError, "Exception trying to query nested stack", me);
        		LOGGER.debug("ERROR trying to query nested stack= " + error);
        	    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
        	    throw new VnfException (me);
        	}
        	if (nestedHeatStack == null || nestedHeatStack.getStatus() == HeatStatus.NOTFOUND) {
        	    String error = "Update VNF: Attached heatStack ID DOES NOT EXIST " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, error, "OpenStack", "QueryStack", MsoLogger.ErrorCode.AvailabilityError, "Attached heatStack ID DOES NOT EXIST");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested heat stack - copying values to inputs");
        		this.sendMapToDebug(inputs);
        		heat.copyStringOutputsToInputs(inputs, nestedHeatStack.getOutputs(), false);      
        		this.sendMapToDebug(inputs);
        	}
        }

        // Ready to deploy the new VNF

        try(CatalogDatabase db = new CatalogDatabase ()) {
            // Retrieve the VNF definition
            VnfResource vnf;
            if (vnfVersion != null && !vnfVersion.isEmpty ()) {
                vnf = db.getVnfResource (vnfType, vnfVersion);
            } else {
                vnf = db.getVnfResource (vnfType);
            }
            if (vnf == null) {
                String error = "Update VNF: Unknown VNF Type: " + vnfType;
                LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "VNF Type", vnfType, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: Unknown VNF Type");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                throw new VnfException (error, MsoExceptionCategory.USERDATA);
            }
            LOGGER.debug ("Got VNF definition from Catalog: " + vnf.toString ());

            // Currently, all VNFs are orchestrated via HEAT
            if (!"HEAT".equals (vnf.getOrchestrationMode ())) {
                String error = "Update VNF: Configuration error: VNF=" + vnfType;
                LOGGER.error (MessageEnum.RA_CONFIG_EXC, " VNF=" + vnfType, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: Configuration error");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                // Alarm on this error, configuration must be fixed
                alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
                throw new VnfException (error, MsoExceptionCategory.INTERNAL);
            }
            
            //1604 - Need to handle an updateVolume request. 
            VnfComponent vnfComponent = null;
            if (requestTypeString != null && !requestTypeString.equals("")) {
            	LOGGER.debug("About to query for vnfComponent id = " + vnf.getId() + ", type = " + requestTypeString.toUpperCase());
            	vnfComponent = db.getVnfComponent(vnf.getId(), requestTypeString.toUpperCase());
            	if (vnfComponent == null) {
            		String error = "Update VNF: Cannot find VNF Component entry for: " + vnfType + ", type = " + requestTypeString.toUpperCase();
            		LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "VNF Type", vnfType, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: Cannot find VNF Component entry");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
            		throw new VnfException (error, MsoExceptionCategory.USERDATA);
            	}
            	LOGGER.debug("FOUND VnfComponent: " + vnfComponent.toString());
            }

            HeatTemplate heatTemplate = db.getHeatTemplate (vnf.getTemplateId ());
            if (heatTemplate == null) {
                String error = "Update VNF: undefined Heat Template. VNF=" + vnfType;
                LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Template ID", String.valueOf(vnf.getTemplateId ()), "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: undefined Heat Template");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                // Alarm on this error, configuration must be fixed
                alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                throw new VnfException (error, MsoExceptionCategory.INTERNAL);
            }

            // If this is a component request - get the template associated for volumes
            // May change this - for now get both templates - but volume will be 2nd, which makes sense
            // for the rest of the code. Same with envt later
			if (vnfComponent != null) {
				LOGGER.debug("Querying db to find component template " + vnfComponent.getHeatTemplateId());
				heatTemplate = db.getHeatTemplate(vnfComponent
						.getHeatTemplateId());
				if (heatTemplate == null) {
					String error = "Update VNF: undefined Heat Template for Volume Component. VNF="
							+ vnfType;
					LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
							"Heat Template ID",
							String.valueOf(vnfComponent.getHeatTemplateId()), "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: undefined Heat Template for Volume Component");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
					alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
							MsoAlarmLogger.CRITICAL, error);
					throw new VnfException(error, MsoExceptionCategory.INTERNAL);
				}
			}

            LOGGER.debug ("Got HEAT Template from DB: " + heatTemplate.toString ());
            
            // Add check for any Environment variable
            HeatEnvironment heatEnvironment = null;
            String heatEnvironmentString = null;

            if (vnf.getEnvironmentId () != null) {
                LOGGER.debug ("about to call getHeatEnvironment with :" + vnf.getEnvironmentId () + ":");
                heatEnvironment = db.getHeatEnvironment (vnf.getEnvironmentId ());
                if (heatEnvironment == null) {

                    String error = "Create VNF: undefined Heat Environment. VNF=" + vnfType
                                   + ", Environment ID="
                                   + vnf.getEnvironmentId ();
                    LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Environment ID", String.valueOf(vnf.getEnvironmentId ()), "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Create VNF: undefined Heat Environment");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                    // Alarm on this error, configuration must be fixed
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                    throw new VnfException (error, MsoExceptionCategory.INTERNAL);
                } else {
                    LOGGER.debug ("Got Heat Environment from DB: " + heatEnvironment.toString ());
                    heatEnvironmentString = heatEnvironment.getEnvironment (); //this.parseEnvironment (heatEnvironment.getEnvironment ());
                    LOGGER.debug ("After parsing: " + heatEnvironmentString);
                }
            } else {
                LOGGER.debug ("no environment parameter for this VNF " + vnfType);
            }
            
            //1604 - override the VNF environment with the one for the component
            if(vnfComponent != null) {
                if (vnfComponent.getHeatEnvironmentId () != null) {
                    LOGGER.debug ("about to call getHeatEnvironment with :" + vnfComponent.getHeatEnvironmentId () + ":");
                    heatEnvironment = db.getHeatEnvironment (vnfComponent.getHeatEnvironmentId ());
                    if (heatEnvironment == null) {
                        String error = "Update VNF: undefined Heat Environment. VNF=" + vnfType
                                       + ", Environment ID="
                                       + vnfComponent.getHeatEnvironmentId ();
                        LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Environment ID", String.valueOf(vnfComponent.getHeatEnvironmentId ()), "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: undefined Heat Environment");
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                        // Alarm on this error, configuration must be fixed
                        alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                        throw new VnfException (error, MsoExceptionCategory.INTERNAL);
                    } else {
                        LOGGER.debug ("Got Heat Environment from DB: " + heatEnvironment.toString ());
                        heatEnvironmentString = heatEnvironment.getEnvironment (); //this.parseEnvironment (heatEnvironment.getEnvironment ());
                        LOGGER.debug ("after parsing: " + heatEnvironmentString);
                    }
                } else {
                    LOGGER.debug ("no environment parameter for this VNF VOLUME component " + vnfType);
                }
            }
            // End 1604
            
            
            LOGGER.debug ("In MsoVnfAdapterImpl, about to call db.getNestedTemplates avec templateId="
                          + heatTemplate.getId ());
            Map <String, Object> nestedTemplates = db.getNestedTemplates (heatTemplate.getId ());
            Map <String, Object> nestedTemplatesChecked = new HashMap <String, Object> ();
            if (nestedTemplates != null) {
                // for debugging print them out
                LOGGER.debug ("Contents of nestedTemplates - to be added to files: on stack:");
                for (String providerResourceFile : nestedTemplates.keySet ()) {
                    String providerResourceFileChecked = providerResourceFile; //this.enforceFilePrefix (providerResourceFile);
                    String childTemplateBody = (String) nestedTemplates.get (providerResourceFile);
                    nestedTemplatesChecked.put (providerResourceFileChecked, childTemplateBody);
                    LOGGER.debug (providerResourceFileChecked + " -> " + childTemplateBody);
                }
            } else {
                LOGGER.debug ("No nested templates found - nothing to do here");
                nestedTemplatesChecked = null;
            }

            // Also add the files: for any get_files associated with this vnf_resource_id
            // *if* there are any
            LOGGER.debug ("In MsoVnfAdapterImpl.updateVnf, about to call db.getHeatFiles avec vnfResourceId="
                          + vnf.getId ());
            Map <String, HeatFiles> heatFiles = db.getHeatFiles (vnf.getId ());
            Map <String, Object> heatFilesObjects = new HashMap <String, Object> ();
            if (heatFiles != null) {
                // add these to stack - to be done in createStack
                // here, we will map them to Map<String, Object> from Map<String, HeatFiles>
                // this will match the nested templates format
                LOGGER.debug ("Contents of heatFiles - to be added to files: on stack:");

                for (String heatFileName : heatFiles.keySet ()) {
                    String heatFileBody = heatFiles.get (heatFileName).getFileBody ();
                    // Remove the file:/// enforcement for get_file:
                    //String heatFileNameChecked = this.enforceFilePrefix (heatFileName);
                    String heatFileNameChecked = heatFileName;
                    LOGGER.debug (heatFileNameChecked + " -> " + heatFileBody);
                    heatFilesObjects.put (heatFileNameChecked, heatFileBody);
                }
            } else {
                LOGGER.debug ("No heat files found -nothing to do here");
                heatFilesObjects = null;
            }

            // Check that required parameters have been supplied
            String missingParams = null;
            List <String> paramList = new ArrayList <String> ();

            // New for 1510 - consult the PARAM_ALIAS field to see if we've been
            // supplied an alias. Only check if we don't find it initially.
            // Also new in 1510 - don't flag missing parameters if there's an environment - because they might be there.
            // And also new - add parameter to turn off checking all together if we find we're blocking orders we
            // shouldn't
            boolean haveEnvironmentParameters = false;
            boolean checkRequiredParameters = true;
            try {
                String propertyString = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER)
                                                     .getProperty (MsoVnfAdapterImpl.CHECK_REQD_PARAMS,null);
                if ("false".equalsIgnoreCase (propertyString) || "n".equalsIgnoreCase (propertyString)) {
                    checkRequiredParameters = false;
                    LOGGER.debug ("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
                                  + MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
                }
            } catch (Exception e) {
                // No problem - default is true
                LOGGER.debug ("An exception occured trying to get property " + MsoVnfAdapterImpl.CHECK_REQD_PARAMS, e);
            }
            // 1604 - Add enhanced environment & parameter checking
            // Part 1: parse envt entries to see if reqd parameter is there (before used a simple grep
            // Part 2: only submit to openstack the parameters in the envt that are in the heat template
            // Note this also removes any comments
            MsoHeatEnvironmentEntry mhee = null;
            if (heatEnvironmentString != null && heatEnvironmentString.toLowerCase ().contains ("parameters:")) {
            	LOGGER.debug("Enhanced environment checking enabled - 1604");
                haveEnvironmentParameters = true;
                StringBuilder sb = new StringBuilder(heatEnvironmentString);
                //LOGGER.debug("About to create MHEE with " + sb);
                mhee = new MsoHeatEnvironmentEntry(sb);
                StringBuilder sb2 = new StringBuilder("\nHeat Template Parameters:\n");
                for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                	sb2.append("\t" + parm.getParamName() + ", required=" + parm.isRequired());
                }
                if (!mhee.isValid()) {
                	sb2.append("Environment says it's not valid! " + mhee.getErrorString());
                } else {
                	sb2.append("\nEnvironment:");
                	sb2.append(mhee.toFullString());
                }
                LOGGER.debug(sb2.toString());
            } else {
            	LOGGER.debug("NO ENVIRONMENT for this entry");
            }

            for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
                LOGGER.debug ("Parameter:'" + parm.getParamName ()
                              + "', isRequired="
                              + parm.isRequired ()
                              + ", alias="
                              + parm.getParamAlias ());
                if (parm.isRequired () && (inputs == null || !inputs.containsKey (parm.getParamName ()))) {
                    if (inputs.containsKey (parm.getParamAlias ())) {
                        // They've submitted using an alias name. Remove that from inputs, and add back using real name.
                        String realParamName = parm.getParamName ();
                        String alias = parm.getParamAlias ();
                        String value = inputs.get (alias);
                        LOGGER.debug ("*Found an Alias: paramName=" + realParamName
                                      + ",alias="
                                      + alias
                                      + ",value="
                                      + value);
                        inputs.remove (alias);
                        inputs.put (realParamName, value);
                        LOGGER.debug (alias + " entry removed from inputs, added back using " + realParamName);
                    } 
                    // enhanced - check if it's in the Environment (note: that method 
                    else if (mhee != null && mhee.containsParameter(parm.getParamName())) {

                        LOGGER.debug ("Required parameter " + parm.getParamName ()
                                      + " appears to be in environment - do not count as missing");
                    }
                    else {
                        LOGGER.debug ("adding to missing parameters list: " + parm.getParamName ());
                        if (missingParams == null) {
                            missingParams = parm.getParamName ();
                        } else {
                            missingParams += "," + parm.getParamName ();
                        }
                    }
                }
                paramList.add (parm.getParamName ());
            }
            if (missingParams != null) {
                // Problem - missing one or more required parameters
            	if (checkRequiredParameters) {
                String error = "Update VNF: Missing Required inputs: " + missingParams;
                LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Update VNF: Missing Required inputs");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
                throw new VnfException (error, MsoExceptionCategory.USERDATA);
            	} else {
            		LOGGER.debug ("found missing parameters - but checkRequiredParameters is false - will not block");
            	}
            } else {
                LOGGER.debug ("No missing parameters found - ok to proceed");
            }
            
            // Here - modify heatEnvironmentString
            StringBuilder parsedEnvironmentString = null; 
            String newEnvironmentString = null;
            if (mhee != null) {
            	LOGGER.debug("Environment before:\n" + heatEnvironmentString);
            	parsedEnvironmentString = mhee.toFullStringExcludeNonParams(heatTemplate.getParameters());
            	LOGGER.debug("Environment after:\n" + parsedEnvironmentString.toString());
            	newEnvironmentString = parsedEnvironmentString.toString();
            }

            // Remove any extraneous parameters (don't throw an error)
            if (inputs != null) {
                List <String> extraParams = new ArrayList <String> ();
                extraParams.addAll (inputs.keySet ());
                // This is not a valid parameter for this template
                extraParams.removeAll (paramList);
                if (!extraParams.isEmpty ()) {
                	LOGGER.warn (MessageEnum.RA_VNF_EXTRA_PARAM, vnfType, extraParams.toString(), "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "VNF Extra params");
                    inputs.keySet ().removeAll (extraParams);
                }
            }

            // "Fix" the template if it has CR/LF (getting this from Oracle)
            String template = heatTemplate.getHeatTemplate ();
            template = template.replaceAll ("\r\n", "\n");

            // Have the tenant. Now deploy the stack itself
            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.
            long updateStackStarttime = System.currentTimeMillis ();
            try {
                heatStack = heatU.updateStack (cloudSiteId,
                                               tenantId,
                                               vnfName,
                                               template,
                                               copyStringInputs (inputs),
                                               true,
                                               heatTemplate.getTimeoutMinutes (),
                                               newEnvironmentString,
                                               //heatEnvironmentString,
                                               nestedTemplatesChecked,
                                               heatFilesObjects);
                LOGGER.recordMetricEvent (updateStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "UpdateStack", vnfName);
            } catch (MsoException me) {
                me.addContext ("UpdateVNF");
                String error = "Update VNF " + vnfType + " in " + cloudSiteId + "/" + tenantId + ": " + me;
                LOGGER.recordMetricEvent (updateStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "UpdateStack", vnfName);
                LOGGER.error (MessageEnum.RA_UPDATE_VNF_ERR, vnfType, cloudSiteId, tenantId, "OpenStack", "updateStack", MsoLogger.ErrorCode.DataError, "Exception - updateStack", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new VnfException (me);
            }
        }

        // Reach this point if updateStack is successful.
        // Populate remaining rollback info and response parameters.
        vnfRollback.setVnfId (heatStack.getCanonicalName ());
        vnfRollback.setVnfCreated (true);

        outputs.value = copyStringOutputs (heatStack.getOutputs ());
        rollback.value = vnfRollback;
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully update VNF");
        return;
    }

    /**
     * This is the "Query VNF" web service implementation.
     * It will look up a VNF by name or ID in the specified cloud and tenant.
     *
     * The method returns an indicator that the VNF exists, its Openstack internal
     * ID, its status, and the set of outputs (from when the stack was created).
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param tenantId Openstack tenant identifier
     * @param vnfName VNF Name or Openstack ID
     * @param msoRequest Request tracking information for logs
     * @param vnfExists Flag reporting the result of the query
     * @param vnfId Holder for output VNF Openstack ID
     * @param outputs Holder for Map of VNF outputs from heat (assigned IPs, etc)
     */
    @Override
    public void queryVnf (String cloudSiteId,
                          String tenantId,
                          String vnfName,
                          MsoRequest msoRequest,
                          Holder <Boolean> vnfExists,
                          Holder <String> vnfId,
                          Holder <VnfStatus> status,
                          Holder <Map <String, String>> outputs) throws VnfException {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("QueryVnf");
        LOGGER.debug ("Querying VNF " + vnfName + " in " + cloudSiteId + "/" + tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        StackInfo heatStack = null;
        long subStartTime = System.currentTimeMillis ();
        try {
            heatStack = heat.queryStack (cloudSiteId, tenantId, vnfName);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vnfName);
        } catch (MsoException me) {
            me.addContext ("QueryVNF");
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            String error = "Query VNF: " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vnfName);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "QueryVNF", MsoLogger.ErrorCode.DataError, "Exception - queryStack", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        // Populate the outputs based on the returned Stack information
        //
        if (heatStack == null || heatStack.getStatus () == HeatStatus.NOTFOUND) {
            // Not Found
            vnfExists.value = Boolean.FALSE;
            status.value = VnfStatus.NOTFOUND;
            vnfId.value = null;
            outputs.value = new HashMap <String, String> (); // Return as an empty map

            LOGGER.debug ("VNF " + vnfName + " not found");
        } else {
            vnfExists.value = Boolean.TRUE;
            status.value = stackStatusToVnfStatus (heatStack.getStatus ());
            vnfId.value = heatStack.getCanonicalName ();
            outputs.value = copyStringOutputs (heatStack.getOutputs ());

            LOGGER.debug ("VNF " + vnfName + " found, ID = " + vnfId.value);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully query VNF");
        return;
    }

    /**
     * This is the "Delete VNF" web service implementation.
     * It will delete a VNF by name or ID in the specified cloud and tenant.
     *
     * The method has no outputs.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to delete
     * @param tenantId Openstack tenant identifier
     * @param vnfName VNF Name or Openstack ID
     * @param msoRequest Request tracking information for logs
     */
    @Override
    public void deleteVnf (String cloudSiteId,
                           String tenantId,
                           String vnfName,
                           MsoRequest msoRequest) throws VnfException {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("DeleteVnf");
        LOGGER.debug ("Deleting VNF " + vnfName + " in " + cloudSiteId + "/" + tenantId);
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        long subStartTime = System.currentTimeMillis ();
        try {
            heat.deleteStack (tenantId, cloudSiteId, vnfName, true);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteStack", vnfName);
        } catch (MsoException me) {
            me.addContext ("DeleteVNF");
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            String error = "Delete VNF: " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteStack", vnfName);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "DeleteVNF", MsoLogger.ErrorCode.DataError, "Exception - DeleteVNF", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        // On success, nothing is returned.
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully delete VNF");
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation.
     * A rollback object is returned to the client in a successful creation
     * response. The client can pass that object as-is back to the rollbackVnf
     * operation to undo the creation.
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

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudSiteId ();
        String tenantId = rollback.getTenantId ();
        String vnfId = rollback.getVnfId ();

        MsoLogger.setLogContext (rollback.getMsoRequest());

        LOGGER.debug ("Rolling Back VNF " + vnfId + " in " + cloudSiteId + "/" + tenantId);

        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        long subStartTime = System.currentTimeMillis ();
        try {
            heat.deleteStack (tenantId, cloudSiteId, vnfId, true);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteStack", null);
        } catch (MsoException me) {
            // Failed to rollback the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext ("RollbackVNF");
            String error = "Rollback VNF: " + vnfId + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteStack", null);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vnfId, cloudSiteId, tenantId, "OpenStack", "DeleteStack", MsoLogger.ErrorCode.DataError, "Exception - DeleteStack", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully roll back VNF");
        return;
    }

    private VnfStatus stackStatusToVnfStatus (HeatStatus stackStatus) {
        switch (stackStatus) {
            case CREATED:
                return VnfStatus.ACTIVE;
            case UPDATED:
                return VnfStatus.ACTIVE;
            case FAILED:
                return VnfStatus.FAILED;
            default:
                return VnfStatus.UNKNOWN;
        }
    }

    private Map <String, String> copyStringOutputs (Map <String, Object> stackOutputs) {
        Map <String, String> stringOutputs = new HashMap <String, String> ();
        for (String key : stackOutputs.keySet ()) {
            if (stackOutputs.get (key) instanceof String) {
                stringOutputs.put (key, (String) stackOutputs.get (key));
            }
        }
        return stringOutputs;
    }

    private Map <String, Object> copyStringInputs (Map <String, String> stringInputs) {
        return new HashMap <String, Object> (stringInputs);
    }

    /*
     * a helper method to make sure that any resource_registry entry of the format
     * "xx::xx" : yyy.yaml (or yyy.template)
     * has the file name prepended with "file:///"
     * Return a String of the environment body that's passed in.
     * Have to be careful not to mess up the original formatting.
     */
    private String parseEnvironment (String environment) {
        StringBuilder sb = new StringBuilder ();
        try (Scanner scanner = new Scanner (environment)) {
            scanner.useDelimiter ("\n");
            String line = null;
            Pattern resource = Pattern.compile ("\\s*\"\\w+::\\S+\"\\s*:");
            LOGGER.debug ("regex pattern for finding a resource_registry: \\s*\"\\w+::\\S+\"\\s*:");
            while (scanner.hasNextLine ()) {
                line = scanner.nextLine ();
                if (line.toLowerCase ().contains ("resource_registry")) {
                    sb.append (line + "\n");
                    boolean done = false;
                    // basically keep scanning until EOF or parameters: section
                    while (scanner.hasNextLine () && !done) {
                        line = scanner.nextLine ();
                        if ("parameters:".equalsIgnoreCase (line.trim ())) {
                            sb.append (line + "\n");
                            done = true;
                            break;
                        }
                        Matcher m = resource.matcher (line);
                        if (m.find ()) {
                            sb.append (m.group ());
                            String secondPart = line.substring (m.end ()).trim ();
                            String output = secondPart;
                            if (secondPart.endsWith (".yaml")
                                || secondPart.endsWith (".template") && !secondPart.startsWith ("file:///")) {
                                output = "file:///" + secondPart;
                                LOGGER.debug ("changed " + secondPart + " to " + output);
                            } // don't do anything if it's not .yaml or .template
                            sb.append (" " + output + "\n");
                        } else {
                            sb.append (line + "\n");
                        }
                    }
                } else {
                    sb.append (line + "\n");
                    continue;
                }
            }
            scanner.close ();
        } catch (Exception e) {
            LOGGER.debug ("Error trying to scan " + environment, e);
            return environment;
        }
        return sb.toString ();
    }

    /*
     * helper class to add file:/// to the Provider_Resource_File entry in HEAT_NESTED_TEMPLATE
     * and the File_Name entry in HEAT_FILES if the file:/// part is missing.
     */
    private String enforceFilePrefix (String string) {
        if (string.trim ().startsWith ("file:///")) {
            // just leave it
            return string;
        }
        if (string.trim ().endsWith (".yaml") || string.trim ().endsWith (".template")) {
            // only .yaml or .template are valid anyway - otherwise don't bother
            return "file:///" + string.trim ();
        } else {
            LOGGER.debug (string + " is NOT a .yaml or .template file");
        }
        return string;
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

    public void createVfModule(String cloudSiteId,
            String tenantId,
            String vnfType,
            String vnfVersion,
            String vnfName,
            String requestType,
            String volumeGroupHeatStackId,
            String baseVfHeatStackId,
            Map <String, String> inputs,
            Boolean failIfExists,
            Boolean backout,
            MsoRequest msoRequest,
            Holder <String> vnfId,
            Holder <Map <String, String>> outputs,
            Holder <VnfRollback> rollback) throws VnfException {
    	String vfModuleName = vnfName;
    	String vfModuleType = vnfType;
    	String vfVersion = vnfVersion;
    	MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("CreateVfModule");
    	String requestTypeString = "";
        if (requestType != null && !requestType.equals("")) {
        	requestTypeString = requestType;
        }
        String nestedStackId = null;
        if (volumeGroupHeatStackId != null && !volumeGroupHeatStackId.equals("")) {
        	if (!volumeGroupHeatStackId.equalsIgnoreCase("null")) {
        		nestedStackId = volumeGroupHeatStackId;
        	}
        }
        String nestedBaseStackId = null;
        if (baseVfHeatStackId != null && !baseVfHeatStackId.equals("")) {
        	if (!baseVfHeatStackId.equalsIgnoreCase("null")) {
        		nestedBaseStackId = baseVfHeatStackId;
        	}
        }
        
        if (inputs == null) {
        	// Create an empty set of inputs
        	inputs = new HashMap<String,String>();
        	LOGGER.debug("inputs == null - setting to empty");
        } else {
        	this.sendMapToDebug(inputs);
        }
        //This method will also handle doing things the "old" way - i.e., just orchestrate a VNF
        boolean oldWay = false;
        if (requestTypeString.startsWith("X")) {
        	oldWay = true;
        	LOGGER.debug("orchestrating a VNF - *NOT* a module!");
        	requestTypeString = requestTypeString.substring(1);
        }
        
        // 1607 - let's parse out the request type we're being sent
        boolean isBaseRequest = false;
        boolean isVolumeRequest = false;
        if (requestTypeString.startsWith("VOLUME")) {
        	isVolumeRequest = true;
        } 

        LOGGER.debug("requestTypeString = " + requestTypeString + ", nestedStackId = " + nestedStackId + ", nestedBaseStackId = " + nestedBaseStackId);
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Build a default rollback object (no actions performed)
        VnfRollback vfRollback = new VnfRollback();
        vfRollback.setCloudSiteId(cloudSiteId);
        vfRollback.setTenantId(tenantId);
        vfRollback.setMsoRequest(msoRequest);
        vfRollback.setRequestType(requestTypeString);
        vfRollback.setVolumeGroupHeatStackId(volumeGroupHeatStackId);
        vfRollback.setBaseGroupHeatStackId(baseVfHeatStackId);
        vfRollback.setIsBase(isBaseRequest);
        
        // First, look up to see if the VF already exists.
        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        StackInfo heatStack = null;
        long subStartTime1 = System.currentTimeMillis ();
        try {
            heatStack = heat.queryStack (cloudSiteId, tenantId, vfModuleName);
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vfModuleName);
        } catch (MsoException me) {
            String error = "Create VF Module: Query " + vfModuleName + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
            LOGGER.recordMetricEvent (subStartTime1, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vfModuleName);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Exception - queryStack", me);
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext ("CreateVFModule");
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }
        // New with 1607 - more precise handling/messaging if the stack already exists 
        if (heatStack != null && !(heatStack.getStatus () == HeatStatus.NOTFOUND)) {
        	// INIT, CREATED, NOTFOUND, FAILED, BUILDING, DELETING, UNKNOWN, UPDATING, UPDATED
        	HeatStatus status = heatStack.getStatus();
        	if (status == HeatStatus.INIT || status == HeatStatus.BUILDING || status == HeatStatus.DELETING || status == HeatStatus.UPDATING) {
        		// fail - it's in progress - return meaningful error
                String error = "Create VF: Stack " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; please wait for it to complete, or fix manually.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Stack " + vfModuleName + " already exists");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, heatStack.getCanonicalName ());
        	}
        	if (status == HeatStatus.FAILED) {
        		// fail - it exists and is in a FAILED state
                String error = "Create VF: Stack " + vfModuleName + " already exists and is in FAILED state in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Stack " + vfModuleName + " already exists and is in FAILED state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, heatStack.getCanonicalName ());	
        	}
        	if (status == HeatStatus.UNKNOWN || status == HeatStatus.UPDATED) {
        		// fail - it exists and is in a FAILED state
                String error = "Create VF: Stack " + vfModuleName + " already exists and has status " + status.toString() + " in " + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Stack " + vfModuleName + " already exists and is in UPDATED or UNKNOWN state");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
                throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, heatStack.getCanonicalName ());	
        	}
        	if (status == HeatStatus.CREATED) {
        		// fail - it exists 
        		if (failIfExists != null && failIfExists) {
        			String error = "Create VF: Stack " + vfModuleName + " already exists in " + cloudSiteId + "/" + tenantId;
        			LOGGER.error (MessageEnum.RA_VNF_ALREADY_EXIST, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.DataError, "Stack " + vfModuleName + " already exists");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        			throw new VnfAlreadyExists (vfModuleName, cloudSiteId, tenantId, heatStack.getCanonicalName ());	
        		} else {
        			LOGGER.debug ("Found Existing stack, status=" + heatStack.getStatus ());
        			// Populate the outputs from the existing stack.
        			vnfId.value = heatStack.getCanonicalName ();
        			outputs.value = copyStringOutputs (heatStack.getOutputs ());
        			rollback.value = vfRollback; // Default rollback - no updates performed
        		}
        	}
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module");
            return;
        	
        }
        
        // handle a nestedStackId if sent- this one would be for the volume - so applies to both Vf and Vnf
        StackInfo nestedHeatStack = null;
        long subStartTime2 = System.currentTimeMillis ();
        if (nestedStackId != null) {
        	try {
        		LOGGER.debug("Querying for nestedStackId = " + nestedStackId);
        		nestedHeatStack = heat.queryStack(cloudSiteId, tenantId, nestedStackId);
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vfModuleName);
        	} catch (MsoException me) {
        	    // Failed to query the Stack due to an openstack exception.
        	    // Convert to a generic VnfException
        	    me.addContext ("CreateVFModule");
        	    String error = "Create VFModule: Attached heatStack ID Query " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.recordMetricEvent (subStartTime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vfModuleName);
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "OpenStack", "queryStack", MsoLogger.ErrorCode.BusinessProcesssError, "MsoException trying to query nested stack", me);
        		LOGGER.debug("ERROR trying to query nested stack= " + error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
        	    throw new VnfException (me);
        	}
        	if (nestedHeatStack == null || nestedHeatStack.getStatus() == HeatStatus.NOTFOUND) {
        	    String error = "Create VFModule: Attached heatStack ID DOES NOT EXIST " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, error, "OpenStack", "queryStack", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Attached heatStack ID DOES NOT EXIST");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested volume heat stack - copying values to inputs");
        		this.sendMapToDebug(inputs);
        		heat.copyStringOutputsToInputs(inputs, nestedHeatStack.getOutputs(), false);      
        		this.sendMapToDebug(inputs);
        	}
        }
        
        // handle a nestedBaseStackId if sent- this is the stack ID of the base. Should be null for VNF requests
        StackInfo nestedBaseHeatStack = null;
        long subStartTime3 = System.currentTimeMillis ();
        if (nestedBaseStackId != null) {
        	try {
        		LOGGER.debug("Querying for nestedBaseStackId = " + nestedBaseStackId);
        		nestedBaseHeatStack = heat.queryStack(cloudSiteId, tenantId, nestedBaseStackId);
                LOGGER.recordMetricEvent (subStartTime3, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "QueryStack", vfModuleName);
        	} catch (MsoException me) {
        	    // Failed to query the Stack due to an openstack exception.
        	    // Convert to a generic VnfException
        	    me.addContext ("CreateVFModule");
        	    String error = "Create VFModule: Attached baseHeatStack ID Query " + nestedBaseStackId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.recordMetricEvent (subStartTime3, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", vfModuleName);
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.BusinessProcesssError, "MsoException trying to query nested base stack", me);
        		LOGGER.debug("ERROR trying to query nested base stack= " + error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
        	    throw new VnfException (me);
        	}
        	if (nestedBaseHeatStack == null || nestedBaseHeatStack.getStatus() == HeatStatus.NOTFOUND) {
        	    String error = "Create VFModule: Attached base heatStack ID DOES NOT EXIST " + nestedBaseStackId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, error, "OpenStack", "QueryStack", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: Attached base heatStack ID DOES NOT EXIST");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested base heat stack - copying values to inputs");
        		this.sendMapToDebug(inputs);
        		heat.copyStringOutputsToInputs(inputs, nestedBaseHeatStack.getOutputs(), false);      
        		this.sendMapToDebug(inputs);
        	}
        }
        
        // Ready to deploy the new VNF
        
        try (CatalogDatabase db = new CatalogDatabase()) {
            // Retrieve the VF 
        	VfModule vf = null;
        	VnfResource vnfResource = null;
        	LOGGER.debug("version: " + vfVersion);
			if (!oldWay) {
				// Need to handle old and new schema methods - for a time. Try the new way first.
				if (vfVersion != null && !vfVersion.isEmpty()) {
					vf = db.getVfModuleType(vfModuleType, vfVersion);
					if (vf == null) {
						LOGGER.debug("Unable to find " + vfModuleType + " and version=" + vfVersion + " in the TYPE column - will try in MODEL_NAME");
						vf = db.getVfModuleModelName(vfModuleType, vfVersion);
						if (vf == null) {
							LOGGER.debug("Unable to find " + vfModuleType + " and version=" + vfVersion + " in the MODEL_NAME field either - ERROR");
						}
					}
				} else {
					vf = db.getVfModuleType(vfModuleType);
					if (vf == null) {
						LOGGER.debug("Unable to find " + vfModuleType + " in the TYPE column - will try in MODEL_NAME");
						vf = db.getVfModuleModelName(vfModuleType);
						if (vf == null) {
							LOGGER.debug("Unable to find " + vfModuleType + " in the MODEL_NAME field either - ERROR");
						}
					}
				}
				if (vf == null) {
					String error = "Create VF Module: Unable to determine specific VF Module Type: "
							+ vfModuleType;
					if (vfVersion != null && !vfVersion.isEmpty()) {
						error += " with version = " + vfVersion;
					}
					LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
							"VF Module Type", vfModuleType, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Create VF Module: Unable to determine specific VF Module Type");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
					throw new VnfException(error, MsoExceptionCategory.USERDATA);
				}
				LOGGER.debug("Got VF module definition from Catalog: "
						+ vf.toString());

				if (vf.isBase()) {
					isBaseRequest = true;
					LOGGER.debug("This is a BASE VF request!");
				} else {
					LOGGER.debug("This is *not* a BASE VF request!");
					if (!isVolumeRequest && nestedBaseStackId == null) {
						LOGGER.debug("DANGER WILL ROBINSON! This is unexpected - no nestedBaseStackId with this non-base request");
					}
				}
			} else {
				if (vfVersion != null && !vfVersion.isEmpty()) {
					vnfResource = db.getVnfResource(vnfType, vnfVersion);
				} else {
					vnfResource = db.getVnfResource(vnfType);
				}
				if (vnfResource == null) {
					String error = "Create VNF: Unknown VNF Type: " + vnfType;
					LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM, "VNF Type",
							vnfType, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Create VNF: Unknown VNF Type");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
					throw new VnfException(error, MsoExceptionCategory.USERDATA);
				}
				LOGGER.debug("Got VNF module definition from Catalog: "
						+ vnfResource.toString());
			}
			// By here - we have either a vf or vnfResource

            //1607 - Add version check
            // First - see if it's in the VnfResource record
            // if we have a vf Module - then we have to query to get the VnfResource record.
            if (!oldWay) {
            	if (vf.getVnfResourceId() != null) { 
            		int vnfResourceId = vf.getVnfResourceId();
            		vnfResource = db.getVnfResourceById(vnfResourceId);
            		if (vnfResource == null) {
            			LOGGER.debug("Unable to find vnfResource at " + vnfResourceId + " will not error for now...");
            		}
            	}
            } 
            String minVersionVnf = null;
            String maxVersionVnf = null;
            if (vnfResource != null) {
            	try {
            		minVersionVnf = vnfResource.getAicVersionMin();
            		maxVersionVnf = vnfResource.getAicVersionMax();
            	} catch (Exception e) {
            		LOGGER.debug("Unable to pull min/max version for this VNF Resource entry");
            		minVersionVnf = null;
            		maxVersionVnf = null;
            	}
            	if (minVersionVnf != null && minVersionVnf.equals("")) {
            		minVersionVnf = null;
            	}
            	if (maxVersionVnf != null && maxVersionVnf.equals("")) {
            		maxVersionVnf = null;
            	}
            }
			if (minVersionVnf != null && maxVersionVnf != null) {
				MavenLikeVersioning aicV = new MavenLikeVersioning();
				CloudSite cloudSite = null;
				String aicVersion = "";
				if (this.cloudConfig == null) {
					this.cloudConfig = this.cloudConfigFactory.getCloudConfig();
				}
				// double check
				if (this.cloudConfig != null) {
					cloudSite = this.cloudConfig.getCloudSite(cloudSiteId);
					if (cloudSite != null) {
						aicV.setVersion(cloudSite.getAic_version());
						if ((aicV.isMoreRecentThan(minVersionVnf) || aicV.isTheSameVersion(minVersionVnf)) // aic >= min
								&& (aicV.isTheSameVersion(maxVersionVnf) || !(aicV.isMoreRecentThan(maxVersionVnf)))) { //aic <= max
							LOGGER.debug("VNF Resource " + vnfResource.getVnfType() + " VersionMin=" + minVersionVnf + " VersionMax:" + maxVersionVnf + " supported on Cloud: " + cloudSite.getId() + " with AIC_Version:" + cloudSite.getAic_version());
						} else {
							// ERROR
							String error = "VNF Resource type: " + vnfResource.getVnfType() + " VersionMin=" + minVersionVnf + " VersionMax:" + maxVersionVnf + " NOT supported on Cloud: " + cloudSite.getId() + " with AIC_Version:" + cloudSite.getAic_version();
							LOGGER.error(MessageEnum.RA_CONFIG_EXC, error, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - setVersion");
							LOGGER.debug(error);
							throw new VnfException(error, MsoExceptionCategory.USERDATA);
						}
					} // let this error out downstream to avoid introducing uncertainty at this stage
				} else {
					LOGGER.debug("cloudConfig is NULL - cannot check cloud site version");
				}

			} else {
				LOGGER.debug("AIC Version not set in VNF_Resource - this is expected thru 1607 - do not error here - not checked.");
			}
			// End Version check 1607

            // with VF_MODULE - we have both the non-vol and vol template/envs in that object
            // with VNF_RESOURCE - we use the old methods. 
            Integer heatTemplateId = null;
            Integer heatEnvtId = null;
            
			if (!oldWay) {
				if (isVolumeRequest) {
					heatTemplateId = vf.getVolTemplateId();
					heatEnvtId = vf.getVolEnvironmentId();
				} else {
					heatTemplateId = vf.getTemplateId();
					heatEnvtId = vf.getEnvironmentId();
				}
			} else {
				if (isVolumeRequest) {
					VnfComponent vnfComponent = null;
					vnfComponent = db.getVnfComponent(vnfResource.getId(), "VOLUME");
	            	if (vnfComponent == null) {
	            		String error = "Create VNF: Cannot find VNF Component entry for: " + vnfType + ", type = VOLUME";
	            		LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "VNF Type", vnfType, "OpenStack", "getVnfComponent", MsoLogger.ErrorCode.DataError, "Create VNF: Cannot find VNF Component entry");
                        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
	            		throw new VnfException (error, MsoExceptionCategory.USERDATA);
	            	} else {
	            		heatTemplateId = vnfComponent.getHeatTemplateId();
	            		heatEnvtId = vnfComponent.getHeatEnvironmentId();
	            	}
				} else {
					heatTemplateId = vnfResource.getTemplateId();
					heatEnvtId = vnfResource.getEnvironmentId();
				}
			}
			// By the time we get here - heatTemplateId and heatEnvtId should be populated (or null)
			HeatTemplate heatTemplate = null;
			if (heatTemplateId == null) {
				String error = "Create: No Heat Template ID defined in catalog database for " + vnfType + ", reqType=" + requestTypeString;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Template ID", vnfType, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Create: No Heat Template ID defined in catalog database");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
						MsoAlarmLogger.CRITICAL, error);
				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			} else {
				heatTemplate = db.getHeatTemplate(heatTemplateId);
			}
			if (heatTemplate == null) {
				String error = "Create VF/VNF: no entry found for heat template ID = " + heatTemplateId;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
						"Heat Template ID",
						String.valueOf(heatTemplateId), "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "Create VF/VNF: no entry found for heat template ID = " + heatTemplateId);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
						MsoAlarmLogger.CRITICAL, error);
				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			}
			LOGGER.debug("Got HEAT Template from DB");
            
            HeatEnvironment heatEnvironment = null;
            String heatEnvironmentString = null;

            if (heatEnvtId != null && heatEnvtId != 0) {
                LOGGER.debug ("about to call getHeatEnvironment with :" + heatEnvtId + ":");
                heatEnvironment = db.getHeatEnvironment (heatEnvtId);
                if (heatEnvironment == null) {
                    String error = "Create VFModule: undefined Heat Environment. VFModule=" + vfModuleType
                                   + ", Environment ID="
                                   + heatEnvtId;
                    LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Environment ID", String.valueOf(heatEnvtId), "OpenStack", "getHeatEnvironment", MsoLogger.ErrorCode.BusinessProcesssError, "Create VFModule: undefined Heat Environment");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                    // Alarm on this error, configuration must be fixed
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                    throw new VnfException (error, MsoExceptionCategory.INTERNAL);
                } else {
                    LOGGER.debug ("Got Heat Environment from DB: " + heatEnvironment.toString ());
                    heatEnvironmentString = heatEnvironment.getEnvironment (); //this.parseEnvironment (heatEnvironment.getEnvironment ());
                    LOGGER.debug ("after parsing: " + heatEnvironmentString);
                }
            } else {
                LOGGER.debug ("no environment parameter found for this Type " + vfModuleType);
            }
            
            // 1510 - Add the files: for nested templates *if* there are any
            LOGGER.debug ("In MsoVnfAdapterImpl, createVfModule about to call db.getNestedTemplates avec templateId="
                          + heatTemplate.getId ());
            Map <String, Object> nestedTemplates = db.getNestedTemplates (heatTemplate.getId ());
            Map <String, Object> nestedTemplatesChecked = new HashMap <String, Object> ();
            if (nestedTemplates != null) {
                // for debugging print them out
                LOGGER.debug ("Contents of nestedTemplates - to be added to files: on stack:");
                for (String providerResourceFile : nestedTemplates.keySet ()) {
                    String providerResourceFileChecked = providerResourceFile; //this.enforceFilePrefix (providerResourceFile);
                    String childTemplateBody = (String) nestedTemplates.get (providerResourceFile);
                    LOGGER.debug (providerResourceFileChecked + " -> " + childTemplateBody);
                    nestedTemplatesChecked.put (providerResourceFileChecked, childTemplateBody);
                }
            } else {
                LOGGER.debug ("No nested templates found - nothing to do here");
                nestedTemplatesChecked = null; // just to make sure
            }

            // 1510 - Also add the files: for any get_files associated with this vnf_resource_id
            // *if* there are any
            Map<String, HeatFiles> heatFiles = null;
			Map<String, Object> heatFilesObjects = new HashMap<String, Object>();

            // Add ability to turn on adding get_files with volume requests (by property).
            boolean addGetFilesOnVolumeReq = false;
            try {
            	String propertyString = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_VNF_ADAPTER).getProperty(MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ, null);
            	if ("true".equalsIgnoreCase(propertyString) || "y".equalsIgnoreCase(propertyString)) {
            		addGetFilesOnVolumeReq = true;
            		LOGGER.debug("AddGetFilesOnVolumeReq - setting to true! " + propertyString);
            	}
            } catch (Exception e) {
            	LOGGER.debug("An error occured trying to get property " + MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ + " - default to false", e);
            }

			if (!isVolumeRequest || addGetFilesOnVolumeReq) {
				if (oldWay) {
					LOGGER.debug("In MsoVnfAdapterImpl createVfModule, about to call db.getHeatFiles avec vnfResourceId="
							+ vnfResource.getId());
					heatFiles = db.getHeatFiles(vnfResource.getId());
				} else {
					// 1607 - now use VF_MODULE_TO_HEAT_FILES table
					LOGGER.debug("In MsoVnfAdapterImpl createVfModule, about to call db.getHeatFilesForVfModule avec vfModuleId="
							+ vf.getId());
					heatFiles = db
							.getHeatFilesForVfModule(vf.getId());
				}
				if (heatFiles != null) {
					// add these to stack - to be done in createStack
					// here, we will map them to Map<String, Object> from
					// Map<String, HeatFiles>
					// this will match the nested templates format
					LOGGER.debug("Contents of heatFiles - to be added to files: on stack:");

					for (String heatFileName : heatFiles.keySet()) {
						if (heatFileName.startsWith("_ERROR|")) {
							// This means there was an invalid entry in VF_MODULE_TO_HEAT_FILES table - the heat file it pointed to could not be found.
							String heatFileId = heatFileName.substring(heatFileName.lastIndexOf("|")+1);
							String error = "Create: No HEAT_FILES entry in catalog database for " + vfModuleType + " at HEAT_FILES index=" + heatFileId;
							LOGGER.debug(error);
							LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "HEAT_FILES entry not found at " + heatFileId, vfModuleType, "OpenStack", "", MsoLogger.ErrorCode.BusinessProcesssError, "HEAT_FILES entry not found");
                            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
							// Alarm on this error, configuration must be fixed
							alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
							throw new VnfException (error, MsoExceptionCategory.INTERNAL);
						}
						String heatFileBody = heatFiles.get(heatFileName)
								.getFileBody();
						String heatFileNameChecked = heatFileName;
						LOGGER.debug(heatFileNameChecked + " -> "
								+ heatFileBody);
						heatFilesObjects.put(heatFileNameChecked, heatFileBody);
					}
				} else {
					LOGGER.debug("No heat files found -nothing to do here");
					heatFilesObjects = null;
				}
			} else {
					LOGGER.debug("Volume request - DO NOT CHECK for HEAT_FILES");
			}

            // Check that required parameters have been supplied
            String missingParams = null;
            List <String> paramList = new ArrayList <String> ();

            // New for 1510 - consult the PARAM_ALIAS field to see if we've been
            // supplied an alias. Only check if we don't find it initially.
            // Also new in 1510 - don't flag missing parameters if there's an environment - because they might be there.
            // And also new - add parameter to turn off checking all together if we find we're blocking orders we
            // shouldn't
            boolean haveEnvironmentParameters = false;
            boolean checkRequiredParameters = true;
            try {
                String propertyString = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER)
                                                     .getProperty (MsoVnfAdapterImpl.CHECK_REQD_PARAMS,null);
                if ("false".equalsIgnoreCase (propertyString) || "n".equalsIgnoreCase (propertyString)) {
                    checkRequiredParameters = false;
                    LOGGER.debug ("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
                                  + MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
                }
            } catch (Exception e) {
                // No problem - default is true
                LOGGER.debug ("An exception occured trying to get property " + MsoVnfAdapterImpl.CHECK_REQD_PARAMS, e);
            }
            // 1604 - Add enhanced environment & parameter checking
            // Part 1: parse envt entries to see if reqd parameter is there (before used a simple grep
            // Part 2: only submit to openstack the parameters in the envt that are in the heat template
            // Note this also removes any comments
            MsoHeatEnvironmentEntry mhee = null;
            if (heatEnvironmentString != null && heatEnvironmentString.contains ("parameters:")) {
                //LOGGER.debug ("Have an Environment argument with a parameters: section - will bypass checking for valid params - but will still check for aliases");
            	LOGGER.debug("Enhanced environment checking enabled - 1604");
                haveEnvironmentParameters = true;
                StringBuilder sb = new StringBuilder(heatEnvironmentString);
                //LOGGER.debug("About to create MHEE with " + sb);
                mhee = new MsoHeatEnvironmentEntry(sb);
                StringBuilder sb2 = new StringBuilder("\nHeat Template Parameters:\n");
                for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                	sb2.append("\t" + parm.getParamName() + ", required=" + parm.isRequired());
                }
                if (!mhee.isValid()) {
                	sb2.append("Environment says it's not valid! " + mhee.getErrorString());
                } else {
                	sb2.append("\nEnvironment:");
                	sb2.append(mhee.toFullString());
                }
                LOGGER.debug(sb2.toString());
            } else {
            	LOGGER.debug("NO ENVIRONMENT for this entry");
            }
            // This is kind of a mess. inputs is a Map<String, String> --
            // if one of the parameters is json - we need to pass String, JsonNode -
            // so we will store off the parameters that are json in its own HashMap
            // if there are any json params - then we convert inputs to a Map<String, Object>
            // and pass that to createStack
            HashMap<String, JsonNode> jsonParams = new HashMap<String, JsonNode>();
            boolean hasJson = false;

            for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
                LOGGER.debug ("Parameter:'" + parm.getParamName ()
                              + "', isRequired="
                              + parm.isRequired ()
                              + ", alias="
                              + parm.getParamAlias ());
                // New 1607 - support json type
                String parameterType = parm.getParamType();
                if (parameterType == null || parameterType.trim().equals("")) {
                	parameterType = "String";
                }
                JsonNode jsonNode = null;
                if (parameterType.equalsIgnoreCase("json") && inputs != null) {
                	if (inputs.containsKey(parm.getParamName()) ) {
                		hasJson = true;
                		String jsonString = null;
                		try {
                			jsonString = inputs.get(parm.getParamName());
                			jsonNode = new ObjectMapper().readTree(jsonString);
                		} catch (JsonParseException jpe) {
                			//TODO - what to do here?
                			//for now - send the error to debug, but just leave it as a String
                			String errorMessage = jpe.getMessage();
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " - " + errorMessage);
                			hasJson = false;
                			jsonNode = null;
                		} catch (Exception e) {
                			// or here?
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " " + e.getMessage());
                			hasJson = false;
                			jsonNode = null;
                		}
                		if (jsonNode != null) {
                			jsonParams.put(parm.getParamName(), jsonNode);
                		}
                	} else if (inputs.containsKey(parm.getParamAlias())) {
                		hasJson = true;
                		String jsonString = null;
                   		try {
                			jsonString = inputs.get(parm.getParamAlias());
                			jsonNode = new ObjectMapper().readTree(jsonString);
                		} catch (JsonParseException jpe) {
                			//TODO - what to do here?
                			//for now - send the error to debug, but just leave it as a String
                			String errorMessage = jpe.getMessage();
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " - " + errorMessage);
                			hasJson = false;
                			jsonNode = null;
                		} catch (Exception e) {
                			// or here?
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " " + e.getMessage());
                			hasJson = false;
                			jsonNode = null;
                		}
                   		if (jsonNode != null) {
                   			// Notice here - we add it to the jsonParams hashMap with the actual name -
                   			// then manipulate the inputs so when we check for aliases below - it will not
                   			// get flagged.
                   			jsonParams.put(parm.getParamName(), jsonNode);
                   			inputs.remove(parm.getParamAlias());
                   			inputs.put(parm.getParamName(), jsonString);
                   		}
                	} //TODO add a check for the parameter in the env file
                }
                if (parm.isRequired () && (inputs == null || !inputs.containsKey (parm.getParamName ()))) {
                	// Check if they have an alias
                	LOGGER.debug("**Parameter " + parm.getParamName() + " is required and not in the inputs...");
                    if (inputs.containsKey (parm.getParamAlias ())) {
                        // They've submitted using an alias name. Remove that from inputs, and add back using real name.
                        String realParamName = parm.getParamName ();
                        String alias = parm.getParamAlias ();
                        String value = inputs.get (alias);
                        LOGGER.debug ("*Found an Alias: paramName=" + realParamName
                                      + ",alias="
                                      + alias
                                      + ",value="
                                      + value);
                        inputs.remove (alias);
                        inputs.put (realParamName, value);
                        LOGGER.debug (alias + " entry removed from inputs, added back using " + realParamName);
                    } 
                    // enhanced - check if it's in the Environment (note: that method 
                    else if (mhee != null && mhee.containsParameter(parm.getParamName())) {

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
                paramList.add (parm.getParamName ());
            }
            if (missingParams != null) {
            	if (checkRequiredParameters) {
            		// Problem - missing one or more required parameters
            		String error = "Create VFModule: Missing Required inputs: " + missingParams;
            		LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Create VFModule: Missing Required inputs");
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
            		throw new VnfException (error, MsoExceptionCategory.USERDATA);
            	} else {
            		LOGGER.debug ("found missing parameters - but checkRequiredParameters is false - will not block");
            	}
            } else {
                LOGGER.debug ("No missing parameters found - ok to proceed");
            }
            
            // Here - modify heatEnvironmentString
            StringBuilder parsedEnvironmentString = null; 
            String newEnvironmentString = null;
            if (mhee != null) {
            	LOGGER.debug("Environment before:\n" + heatEnvironmentString);
            	parsedEnvironmentString = mhee.toFullStringExcludeNonParams(heatTemplate.getParameters());
            	LOGGER.debug("Environment after:\n" + parsedEnvironmentString.toString());
            	newEnvironmentString = parsedEnvironmentString.toString();
            }

            // Remove any extraneous parameters (don't throw an error)
            if (inputs != null) {
                List <String> extraParams = new ArrayList <String> ();
                extraParams.addAll (inputs.keySet ());
                extraParams.removeAll (paramList);
                if (!extraParams.isEmpty ()) {
                    LOGGER.warn (MessageEnum.RA_VNF_EXTRA_PARAM, vnfType, extraParams.toString(), "OpenStack", "", MsoLogger.ErrorCode.DataError, "Extra params");
                    inputs.keySet ().removeAll (extraParams);
                }
            }
            // 1607 - when we get here - we have clean inputs. Check if we have
            Map<String, Object> inputsTwo = null;
            if (hasJson && jsonParams.size() > 0) {
            	inputsTwo = new HashMap<String, Object>();
            	for (String keyParamName : inputs.keySet()) {
            		if (jsonParams.containsKey(keyParamName)) {
            			inputsTwo.put(keyParamName, jsonParams.get(keyParamName));
            		} else {
            			inputsTwo.put(keyParamName, inputs.get(keyParamName));
            		}
            	}
            }

            // "Fix" the template if it has CR/LF (getting this from Oracle)
            String template = heatTemplate.getHeatTemplate ();
            template = template.replaceAll ("\r\n", "\n");

            // Have the tenant. Now deploy the stack itself
            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.
            long createStackStarttime = System.currentTimeMillis ();
            try {
                // heatStack = heat.createStack(cloudSiteId, tenantId, vnfName, template, inputs, true,
                // heatTemplate.getTimeoutMinutes());
            	if (backout == null) {
            		backout = true;
            	}
            	if (heat != null) {
            		LOGGER.debug("heat is not null!!");
            	}

            	if (!hasJson) {
            		heatStack = heat.createStack (cloudSiteId,
                                              tenantId,
                                              vfModuleName,
                                              template,
                                              inputs,
                                              true,
                                              heatTemplate.getTimeoutMinutes (),
                                              newEnvironmentString,
                                              //heatEnvironmentString,
                                              nestedTemplatesChecked,
                                              heatFilesObjects,
                                              backout.booleanValue());
                LOGGER.recordMetricEvent (createStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "CreateStack", vfModuleName);
            	} else {
            		heatStack = heat.createStack (cloudSiteId,
                                              tenantId,
                                              vfModuleName,
                                              template,
                                              inputsTwo,
                                              true,
                                              heatTemplate.getTimeoutMinutes (),
                                              newEnvironmentString,
                                              //heatEnvironmentString,
                                              nestedTemplatesChecked,
                                              heatFilesObjects,
                                              backout.booleanValue());

            	}
                LOGGER.recordMetricEvent (createStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "CreateStack", vfModuleName);
            } catch (MsoException me) {
                me.addContext ("CreateVFModule");
                String error = "Create VF Module " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + me;
                LOGGER.recordMetricEvent (createStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "CreateStack", vfModuleName);
                LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "MsoException - createStack", me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new VnfException (me);
            } catch (NullPointerException npe) {
                String error = "Create VFModule " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + npe;
                LOGGER.recordMetricEvent (createStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "CreateStack", vfModuleName);
                LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "NullPointerException - createStack", npe);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                LOGGER.debug("NULL POINTER EXCEPTION at heat.createStack");
                //npe.addContext ("CreateVNF");
                throw new VnfException ("NullPointerException during heat.createStack");
            } catch (Exception e) {
                LOGGER.recordMetricEvent (createStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while creating stack with OpenStack", "OpenStack", "CreateStack", vfModuleName);
                LOGGER.debug("unhandled exception at heat.createStack");
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while creating stack with OpenStack");
            	throw new VnfException("Exception during heat.createStack! " + e.getMessage());
            }
        } catch (Exception e) {
        	LOGGER.debug("unhandled exception in create VF");
        	throw new VnfException("Exception during create VF " + e.getMessage());
        	
        }

        // Reach this point if createStack is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfId (heatStack.getCanonicalName ());
        vfRollback.setVnfCreated (true);

        vnfId.value = heatStack.getCanonicalName ();
        outputs.value = copyStringOutputs (heatStack.getOutputs ());
        rollback.value = vfRollback;

        LOGGER.debug ("VF Module " + vfModuleName + " successfully created");
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully create VF Module");
        return;

    	
    }
    
    public void deleteVfModule (String cloudSiteId,
                           String tenantId,
                           String vnfName,
                           MsoRequest msoRequest) throws VnfException {
        MsoLogger.setLogContext (msoRequest);
    	MsoLogger.setServiceName ("DeleteVf");
        LOGGER.debug ("Deleting VF " + vnfName + " in " + cloudSiteId + "/" + tenantId);
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        long subStartTime = System.currentTimeMillis ();
        try {
            heat.deleteStack (tenantId, cloudSiteId, vnfName, true);
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from Open Stack", "OpenStack", "DeleteStack", vnfName);
        } catch (MsoException me) {
            me.addContext ("DeleteVNF");
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            String error = "Delete VF: " + vnfName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "DeleteStack", vnfName);
            LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "DeleteStack", MsoLogger.ErrorCode.DataError, "Exception - deleteStack", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        // On success, nothing is returned.
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully delete VF");
        return;
    }

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
                           Map <String, String> inputs,
                           MsoRequest msoRequest,
                           Holder <Map <String, String>> outputs,
                           Holder <VnfRollback> rollback) throws VnfException {
    	String vfModuleName = vnfName;
    	String vfModuleType = vnfType;
    	String vfVersion = vnfVersion;
    	String methodName = "updateVfModule";
    	MsoLogger.setLogContext (msoRequest.getRequestId (), msoRequest.getServiceInstanceId ());
    	String serviceName = VNF_ADAPTER_SERVICE_NAME + methodName;
    	MsoLogger.setServiceName (serviceName);

    	String requestTypeString = "";
        if (requestType != null && !requestType.equals("")) {
        	requestTypeString = requestType;
        }
        String nestedStackId = null;
        if (volumeGroupHeatStackId != null && !volumeGroupHeatStackId.equals("")) {
        	if (!volumeGroupHeatStackId.equalsIgnoreCase("null")) {
        		nestedStackId = volumeGroupHeatStackId;
        	}
        }
        String nestedBaseStackId = null;
        if (baseVfHeatStackId != null && !baseVfHeatStackId.equals("")) {
        	if (!baseVfHeatStackId.equalsIgnoreCase("null")) {
        		nestedBaseStackId = baseVfHeatStackId;
        	}
        }

        if (inputs == null) {
        	// Create an empty set of inputs
        	inputs = new HashMap<String,String>();
        	LOGGER.debug("inputs == null - setting to empty");
        } else {
        	this.sendMapToDebug(inputs);
        }
        boolean isBaseRequest = false;
        boolean isVolumeRequest = false;
        if (requestTypeString.startsWith("VOLUME")) {
        	isVolumeRequest = true;
        }
        if (vfModuleName == null || vfModuleName.trim().equals("")) {
        	if (vfModuleStackId != null) {
        		vfModuleName = this.getVfModuleNameFromModuleStackId(vfModuleStackId);
        	}
        }

        LOGGER.debug ("Updating VFModule: " + vfModuleName + " of type " + vfModuleType + "in " + cloudSiteId + "/" + tenantId);
        LOGGER.debug("requestTypeString = " + requestTypeString + ", nestedStackId = " + nestedStackId + ", nestedBaseStackId = " + nestedBaseStackId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis ();

        // Build a default rollback object (no actions performed)
        VnfRollback vfRollback = new VnfRollback ();
        vfRollback.setCloudSiteId (cloudSiteId);
        vfRollback.setTenantId (tenantId);
        vfRollback.setMsoRequest (msoRequest);
        vfRollback.setRequestType(requestTypeString);
        vfRollback.setVolumeGroupHeatStackId(volumeGroupHeatStackId);
        vfRollback.setBaseGroupHeatStackId(baseVfHeatStackId);
        vfRollback.setIsBase(isBaseRequest);
        vfRollback.setVfModuleStackId(vfModuleStackId);

        // First, look up to see if the VNF already exists.
        MsoHeatUtils heat = new MsoHeatUtils (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);
        MsoHeatUtilsWithUpdate heatU = new MsoHeatUtilsWithUpdate (MSO_PROP_VNF_ADAPTER, msoPropertiesFactory,cloudConfigFactory);

        StackInfo heatStack = null;
        long queryStackStarttime = System.currentTimeMillis ();
        LOGGER.debug("UpdateVfModule - querying for " + vfModuleName);
        try {
            heatStack = heat.queryStack (cloudSiteId, tenantId, vfModuleName);
            LOGGER.recordMetricEvent (queryStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully receive response from Open Stack", "OpenStack", "QueryStack", null);
        } catch (MsoException me) {
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext ("UpdateVFModule");
            String error = "Update VFModule: Query " + vfModuleName + " in " + cloudSiteId + "/" + tenantId + ": " + me;
            LOGGER.recordMetricEvent (queryStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", null);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Exception - QueryStack", me);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
            throw new VnfException (me);
        }

        //TODO - do we need to check for the other status possibilities?
        if (heatStack == null || heatStack.getStatus () == HeatStatus.NOTFOUND) {
            // Not Found
            String error = "Update VF: Stack " + vfModuleName + " does not exist in " + cloudSiteId + "/" + tenantId;
            LOGGER.error (MessageEnum.RA_VNF_NOT_EXIST, vfModuleName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, error);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
            throw new VnfNotFound (cloudSiteId, tenantId, vfModuleName);
        } else {
            LOGGER.debug ("Found Existing stack, status=" + heatStack.getStatus ());
            // Populate the outputs from the existing stack.
            outputs.value = copyStringOutputs (heatStack.getOutputs ());
            rollback.value = vfRollback; // Default rollback - no updates performed
        }

        // 1604 Cinder Volume support - handle a nestedStackId if sent (volumeGroupHeatStackId):
        StackInfo nestedHeatStack = null;
        long queryStackStarttime2 = System.currentTimeMillis ();
        if (nestedStackId != null) {
        	try {
        		LOGGER.debug("Querying for nestedStackId = " + nestedStackId);
        		nestedHeatStack = heat.queryStack(cloudSiteId, tenantId, nestedStackId);
                LOGGER.recordMetricEvent (queryStackStarttime2, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully receive response from Open Stack", "OpenStack", "QueryStack", null);
        	} catch (MsoException me) {
        	    // Failed to query the Stack due to an openstack exception.
        	    // Convert to a generic VnfException
        	    me.addContext ("UpdateVFModule");
        	    String error = "Update VF: Attached heatStack ID Query " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.recordMetricEvent (queryStackStarttime2, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", null);
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Exception - " + error, me);
        		LOGGER.debug("ERROR trying to query nested stack= " + error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
        	    throw new VnfException (me);
        	}
        	if (nestedHeatStack == null || nestedHeatStack.getStatus() == HeatStatus.NOTFOUND) {
        		MsoLogger.setServiceName (serviceName);
        	    String error = "Update VFModule: Attached volume heatStack ID DOES NOT EXIST " + nestedStackId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vnfName, cloudSiteId, tenantId, error, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, error);
        	    LOGGER.debug(error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested heat stack - copying values to inputs");
        		this.sendMapToDebug(inputs);
        		heat.copyStringOutputsToInputs(inputs, nestedHeatStack.getOutputs(), false);
        		this.sendMapToDebug(inputs);
        	}
        }
        // handle a nestedBaseStackId if sent - this is the stack ID of the base.
        StackInfo nestedBaseHeatStack = null;
        if (nestedBaseStackId != null) {
            long queryStackStarttime3 = System.currentTimeMillis ();
        	try {
        		LOGGER.debug("Querying for nestedBaseStackId = " + nestedBaseStackId);
        		nestedBaseHeatStack = heat.queryStack(cloudSiteId, tenantId, nestedBaseStackId);
                LOGGER.recordMetricEvent (queryStackStarttime3, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully receive response from Open Stack", "OpenStack", "QueryStack", null);
        	} catch (MsoException me) {
        	    // Failed to query the Stack due to an openstack exception.
        	    // Convert to a generic VnfException
        	    me.addContext ("UpdateVfModule");
        	    String error = "Update VFModule: Attached baseHeatStack ID Query " + nestedBaseStackId + " in " + cloudSiteId + "/" + tenantId + ": " + me ;
                LOGGER.recordMetricEvent (queryStackStarttime3, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "QueryStack", null);
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, "Exception - " + error, me);
        		LOGGER.debug("ERROR trying to query nested base stack= " + error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
        	    throw new VnfException (me);
        	}
        	if (nestedBaseHeatStack == null || nestedBaseHeatStack.getStatus() == HeatStatus.NOTFOUND) {
        		MsoLogger.setServiceName (serviceName);
        	    String error = "Update VFModule: Attached base heatStack ID DOES NOT EXIST " + nestedBaseStackId + " in " + cloudSiteId + "/" + tenantId + " USER ERROR"  ;
        	    LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR, vfModuleName, cloudSiteId, tenantId, error, "OpenStack", "QueryStack", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
        	    LOGGER.debug(error);
        	    throw new VnfException (error, MsoExceptionCategory.USERDATA);
        	} else {
        		LOGGER.debug("Found nested base heat stack - copying values to inputs");
        		this.sendMapToDebug(inputs);
        		heat.copyStringOutputsToInputs(inputs, nestedBaseHeatStack.getOutputs(), false);
        		this.sendMapToDebug(inputs);
        	}
        }

        // Ready to deploy the new VNF

        try (CatalogDatabase db = new CatalogDatabase ()) {
            // Retrieve the VF definition
            //VnfResource vnf;
        	VfModule vf = null;
            if (vfVersion != null && !vfVersion.isEmpty ()) {
            	vf = db.getVfModuleType(vfModuleType, vfVersion);
            	if (vf == null) {
            		LOGGER.debug("Unable to find " + vfModuleType + " and version = " + vfVersion + " in the TYPE column - will try in MODEL_NAME");
            		vf = db.getVfModuleModelName(vfModuleType, vfVersion);
            		if (vf == null) {
            			LOGGER.debug("Unable to find " + vfModuleType + " and version = " + vfVersion + " in the MODEL_NAME field either - ERROR");
            		}
            	}
            } else {
                vf = db.getVfModuleType(vfModuleType);
            	if (vf == null) {
            		LOGGER.debug("Unable to find " + vfModuleType + " in the TYPE column - will try in MODEL_NAME");
            		vf = db.getVfModuleModelName(vfModuleType);
            		if (vf == null) {
            			LOGGER.debug("Unable to find " + vfModuleType + " in the MODEL_NAME field either - ERROR");
            		}
            	}
            }
            if (vf == null) {
                String error = "Update VFModule: Unknown VF Module Type: " + vfModuleType;
                if (vfVersion != null && !vfVersion.isEmpty()) {
                	error += " with version = " + vfVersion;
                }
                LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "VF Module Type", vfModuleType, "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, error);
                throw new VnfException (error, MsoExceptionCategory.USERDATA);
            }
            LOGGER.debug ("Got VF module definition from Catalog: " + vf.toString ());
            
            HeatTemplate heatTemplate = null;
            Integer heatTemplateId = null;
            Integer heatEnvtId = null;
			if (!isVolumeRequest) {
				heatTemplateId = vf.getTemplateId();
				heatEnvtId = vf.getEnvironmentId();
			} else {
				heatTemplateId = vf.getVolTemplateId();
				heatEnvtId = vf.getVolEnvironmentId();
			}
			if (heatTemplateId == null) {
				String error = "UpdateVF: No Heat Template ID defined in catalog database for " + vfModuleType + ", reqType=" + requestTypeString;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Template ID", vfModuleType, "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
						MsoAlarmLogger.CRITICAL, error);
				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			} else {
				heatTemplate = db.getHeatTemplate(heatTemplateId);
			}

			if (heatTemplate == null) {
				String error = "Update VNF: undefined Heat Template. VF="
						+ vfModuleType + ", heat template id = " + heatTemplateId;
				LOGGER.error(MessageEnum.RA_VNF_UNKNOWN_PARAM,
						"Heat Template ID",
						String.valueOf(heatTemplateId), "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
				// Alarm on this error, configuration must be fixed
				alarmLogger.sendAlarm(MSO_CONFIGURATION_ERROR,
						MsoAlarmLogger.CRITICAL, error);

				throw new VnfException(error, MsoExceptionCategory.INTERNAL);
			}

            LOGGER.debug ("Got HEAT Template from DB: " + heatTemplate.toString ());

            // Add check for any Environment variable
            HeatEnvironment heatEnvironment = null;
            String heatEnvironmentString = null;

            if (heatEnvtId != null) {
                LOGGER.debug ("about to call getHeatEnvironment with :" + heatEnvtId + ":");
                heatEnvironment = db.getHeatEnvironment (heatEnvtId);
                if (heatEnvironment == null) {

                    String error = "Update VNF: undefined Heat Environment. VF=" + vfModuleType
                                   + ", Environment ID="
                                   + heatEnvtId;
                    LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "Heat Environment ID", String.valueOf(heatEnvtId), "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
                    // Alarm on this error, configuration must be fixed
                    alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);

                    throw new VnfException (error, MsoExceptionCategory.INTERNAL);
                } else {
                    LOGGER.debug ("Got Heat Environment from DB: " + heatEnvironment.toString ());
                    heatEnvironmentString = heatEnvironment.getEnvironment (); //this.parseEnvironment (heatEnvironment.getEnvironment ());
                    LOGGER.debug ("After parsing: " + heatEnvironmentString);
                }
            } else {
                LOGGER.debug ("no environment parameter for this VFModuleType " + vfModuleType);
            }


            LOGGER.debug ("In MsoVnfAdapterImpl, about to call db.getNestedTemplates avec templateId="
                          + heatTemplate.getId ());
            Map <String, Object> nestedTemplates = db.getNestedTemplates (heatTemplate.getId ());
            Map <String, Object> nestedTemplatesChecked = new HashMap <String, Object> ();
            if (nestedTemplates != null) {
                // for debugging print them out
                LOGGER.debug ("Contents of nestedTemplates - to be added to files: on stack:");
                for (String providerResourceFile : nestedTemplates.keySet ()) {
                    String providerResourceFileChecked = providerResourceFile; //this.enforceFilePrefix (providerResourceFile);
                    String childTemplateBody = (String) nestedTemplates.get (providerResourceFile);
                    nestedTemplatesChecked.put (providerResourceFileChecked, childTemplateBody);
                    LOGGER.debug (providerResourceFileChecked + " -> " + childTemplateBody);
                }
            } else {
                LOGGER.debug ("No nested templates found - nothing to do here");
                nestedTemplatesChecked = null;
            }

            // Also add the files: for any get_files associated with this VfModule
            // *if* there are any
            LOGGER.debug ("In MsoVnfAdapterImpl.updateVfModule, about to call db.getHeatFiles avec vfModuleId="
                          + vf.getId ());

            Map <String, HeatFiles> heatFiles = null;
//            Map <String, HeatFiles> heatFiles = db.getHeatFiles (vnf.getId ());
            Map <String, Object> heatFilesObjects = new HashMap <String, Object> ();

            // Add ability to turn on adding get_files with volume requests (by property).
            boolean addGetFilesOnVolumeReq = false;
            try {
            	String propertyString = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_VNF_ADAPTER).getProperty(MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ, null);
            	if ("true".equalsIgnoreCase(propertyString) || "y".equalsIgnoreCase(propertyString)) {
            		addGetFilesOnVolumeReq = true;
            		LOGGER.debug("AddGetFilesOnVolumeReq - setting to true! " + propertyString);
            	}
            } catch (Exception e) {
            	LOGGER.debug("An error occured trying to get property " + MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ + " - default to false", e);
            }
            if (!isVolumeRequest || addGetFilesOnVolumeReq) {
            	heatFiles = db.getHeatFilesForVfModule(vf.getId());
                if (heatFiles != null) {
                    // add these to stack - to be done in createStack
                    // here, we will map them to Map<String, Object> from Map<String, HeatFiles>
                    // this will match the nested templates format
                    LOGGER.debug ("Contents of heatFiles - to be added to files: on stack:");

                    for (String heatFileName : heatFiles.keySet ()) {
						if (heatFileName.startsWith("_ERROR|")) {
							// This means there was an invalid entry in VF_MODULE_TO_HEAT_FILES table - the heat file it pointed to could not be found.
							String heatFileId = heatFileName.substring(heatFileName.lastIndexOf("|")+1);
							String error = "Create: No HEAT_FILES entry in catalog database for " + vfModuleType + " at HEAT_FILES index=" + heatFileId;
							LOGGER.debug(error);
							LOGGER.error (MessageEnum.RA_VNF_UNKNOWN_PARAM, "HEAT_FILES entry not found at " + heatFileId, vfModuleType, "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
							// Alarm on this error, configuration must be fixed
							alarmLogger.sendAlarm (MSO_CONFIGURATION_ERROR, MsoAlarmLogger.CRITICAL, error);
							throw new VnfException (error, MsoExceptionCategory.INTERNAL);
						}
                        String heatFileBody = heatFiles.get (heatFileName).getFileBody ();
                        LOGGER.debug (heatFileName + " -> " + heatFileBody);
                        heatFilesObjects.put (heatFileName, heatFileBody);
                    }
                } else {
                    LOGGER.debug ("No heat files found -nothing to do here");
                    heatFilesObjects = null;
                }
            }

            // Check that required parameters have been supplied
            String missingParams = null;
            List <String> paramList = new ArrayList <String> ();

            // New for 1510 - consult the PARAM_ALIAS field to see if we've been
            // supplied an alias. Only check if we don't find it initially.
            // Also new in 1510 - don't flag missing parameters if there's an environment - because they might be there.
            // And also new - add parameter to turn off checking all together if we find we're blocking orders we
            // shouldn't
            boolean haveEnvironmentParameters = false;
            boolean checkRequiredParameters = true;
            try {
                String propertyString = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER)
                                                     .getProperty (MsoVnfAdapterImpl.CHECK_REQD_PARAMS,null);
                if ("false".equalsIgnoreCase (propertyString) || "n".equalsIgnoreCase (propertyString)) {
                    checkRequiredParameters = false;
                    LOGGER.debug ("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
                                  + MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
                }
            } catch (Exception e) {
                // No problem - default is true
                LOGGER.debug ("An exception occured trying to get property " + MsoVnfAdapterImpl.CHECK_REQD_PARAMS, e);
            }
            // 1604 - Add enhanced environment & parameter checking
            // Part 1: parse envt entries to see if reqd parameter is there (before used a simple grep
            // Part 2: only submit to openstack the parameters in the envt that are in the heat template
            // Note this also removes any comments
            MsoHeatEnvironmentEntry mhee = null;
            if (heatEnvironmentString != null && heatEnvironmentString.toLowerCase ().contains ("parameters:")) {
            	LOGGER.debug("Enhanced environment checking enabled - 1604");
                haveEnvironmentParameters = true;
                StringBuilder sb = new StringBuilder(heatEnvironmentString);
                //LOGGER.debug("About to create MHEE with " + sb);
                mhee = new MsoHeatEnvironmentEntry(sb);
                StringBuilder sb2 = new StringBuilder("\nHeat Template Parameters:\n");
                for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                	sb2.append("\t" + parm.getParamName() + ", required=" + parm.isRequired());
                }
                if (!mhee.isValid()) {
                	sb2.append("Environment says it's not valid! " + mhee.getErrorString());
                } else {
                	sb2.append("\nEnvironment:");
                	sb2.append(mhee.toFullString());
                }
                LOGGER.debug(sb2.toString());
            } else {
            	LOGGER.debug("NO ENVIRONMENT for this entry");
            }

            // New for 1607 - support params of json type
            HashMap<String, JsonNode> jsonParams = new HashMap<String, JsonNode>();
            boolean hasJson = false;
            
            for (HeatTemplateParam parm : heatTemplate.getParameters ()) {
                LOGGER.debug ("Parameter:'" + parm.getParamName ()
                              + "', isRequired="
                              + parm.isRequired ()
                              + ", alias="
                              + parm.getParamAlias ());
                // handle json
                String parameterType = parm.getParamType();
                if (parameterType == null || parameterType.trim().equals("")) {
                	parameterType = "String";
                }
                JsonNode jsonNode = null;
                if (parameterType.equalsIgnoreCase("json") && inputs != null) {
                	if (inputs.containsKey(parm.getParamName()) ) {
                		hasJson = true;
                		String jsonString = null;
                		try {
                			jsonString = inputs.get(parm.getParamName());
                			jsonNode = new ObjectMapper().readTree(jsonString);
                		} catch (JsonParseException jpe) {
                			//TODO - what to do here?
                			//for now - send the error to debug, but just leave it as a String
                			String errorMessage = jpe.getMessage();
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " - " + errorMessage);
                			hasJson = false;
                			jsonNode = null;
                		} catch (Exception e) {
                			// or here?
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " " + e.getMessage());
                			hasJson = false;
                			jsonNode = null;
                		}
                		if (jsonNode != null) {
                			jsonParams.put(parm.getParamName(), jsonNode);
                		}
                	} else if (inputs.containsKey(parm.getParamAlias())) {
                		hasJson = true;
                		String jsonString = null;
                   		try {
                			jsonString = inputs.get(parm.getParamAlias());
                			jsonNode = new ObjectMapper().readTree(jsonString);
                		} catch (JsonParseException jpe) {
                			//TODO - what to do here?
                			//for now - send the error to debug, but just leave it as a String
                			String errorMessage = jpe.getMessage();
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " - " + errorMessage);
                			hasJson = false;
                			jsonNode = null;
                		} catch (Exception e) {
                			// or here?
                			LOGGER.debug("Json Error Converting " + parm.getParamName() + " " + e.getMessage());
                			hasJson = false;
                			jsonNode = null;
                		}
                   		if (jsonNode != null) {
                   			// Notice here - we add it to the jsonParams hashMap with the actual name -
                   			// then manipulate the inputs so when we check for aliases below - it will not
                   			// get flagged.
                   			jsonParams.put(parm.getParamName(), jsonNode);
                   			inputs.remove(parm.getParamAlias());
                   			inputs.put(parm.getParamName(), jsonString);
                   		}
                	} //TODO add a check for the parameter in the env file
                }

                if (parm.isRequired () && (inputs == null || !inputs.containsKey (parm.getParamName ()))) {
                    if (inputs.containsKey (parm.getParamAlias ())) {
                        // They've submitted using an alias name. Remove that from inputs, and add back using real name.
                        String realParamName = parm.getParamName ();
                        String alias = parm.getParamAlias ();
                        String value = inputs.get (alias);
                        LOGGER.debug ("*Found an Alias: paramName=" + realParamName
                                      + ",alias="
                                      + alias
                                      + ",value="
                                      + value);
                        inputs.remove (alias);
                        inputs.put (realParamName, value);
                        LOGGER.debug (alias + " entry removed from inputs, added back using " + realParamName);
                    }
                    // enhanced - check if it's in the Environment (note: that method
                    else if (mhee != null && mhee.containsParameter(parm.getParamName())) {

                        LOGGER.debug ("Required parameter " + parm.getParamName ()
                                      + " appears to be in environment - do not count as missing");
                    }
                    else {
                        LOGGER.debug ("adding to missing parameters list: " + parm.getParamName ());
                        if (missingParams == null) {
                            missingParams = parm.getParamName ();
                        } else {
                            missingParams += "," + parm.getParamName ();
                        }
                    }
                }
                paramList.add (parm.getParamName ());
            }
            if (missingParams != null) {
                // Problem - missing one or more required parameters
            	if (checkRequiredParameters) {
                String error = "Update VNF: Missing Required inputs: " + missingParams;
                LOGGER.error (MessageEnum.RA_MISSING_PARAM, missingParams, "OpenStack", "", MsoLogger.ErrorCode.DataError, error);
                    LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, error);
                throw new VnfException (error, MsoExceptionCategory.USERDATA);
            	} else {
            		LOGGER.debug ("found missing parameters - but checkRequiredParameters is false - will not block");
            	}
            } else {
                LOGGER.debug ("No missing parameters found - ok to proceed");
            }

            // Here - modify heatEnvironmentString
            StringBuilder parsedEnvironmentString = null;
            String newEnvironmentString = null;
            if (mhee != null) {
            	LOGGER.debug("Environment before:\n" + heatEnvironmentString);
            	parsedEnvironmentString = mhee.toFullStringExcludeNonParams(heatTemplate.getParameters());
            	LOGGER.debug("Environment after:\n" + parsedEnvironmentString.toString());
            	newEnvironmentString = parsedEnvironmentString.toString();
            }

            // Remove any extraneous parameters (don't throw an error)
            if (inputs != null) {
                List <String> extraParams = new ArrayList <String> ();
                extraParams.addAll (inputs.keySet ());
                // This is not a valid parameter for this template
                extraParams.removeAll (paramList);
                if (!extraParams.isEmpty ()) {
                	LOGGER.warn (MessageEnum.RA_VNF_EXTRA_PARAM, vnfType, extraParams.toString(), "OpenStack", "", MsoLogger.ErrorCode.DataError, "Extra params");
                    inputs.keySet ().removeAll (extraParams);
                }
            }
            // 1607 - when we get here - we have clean inputs. Create inputsTwo in case we have json
            Map<String, Object> inputsTwo = null;
            if (hasJson && jsonParams.size() > 0) {
            	inputsTwo = new HashMap<String, Object>();
            	for (String keyParamName : inputs.keySet()) {
            		if (jsonParams.containsKey(keyParamName)) {
            			inputsTwo.put(keyParamName, jsonParams.get(keyParamName));
            		} else {
            			inputsTwo.put(keyParamName, inputs.get(keyParamName));
            		}
            	}
            }

            // "Fix" the template if it has CR/LF (getting this from Oracle)
            String template = heatTemplate.getHeatTemplate ();
            template = template.replaceAll ("\r\n", "\n");

            // Have the tenant. Now deploy the stack itself
            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.
            long updateStackStarttime = System.currentTimeMillis ();
            try {
            	if (!hasJson) {
            		heatStack = heatU.updateStack (cloudSiteId,
                                               tenantId,
                                               vfModuleName,
                                               template,
                                               copyStringInputs (inputs),
                                               true,
                                               heatTemplate.getTimeoutMinutes (),
                                               newEnvironmentString,
                                               //heatEnvironmentString,
                                               nestedTemplatesChecked,
                                               heatFilesObjects);
            		LOGGER.recordMetricEvent (updateStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully receive response from Open Stack", "OpenStack", "UpdateStack", null);
            	} else {
            		heatStack = heatU.updateStack (cloudSiteId,
                                               tenantId,
                                               vfModuleName,
                                               template,
                                               inputsTwo,
                                               true,
                                               heatTemplate.getTimeoutMinutes (),
                                               newEnvironmentString,
                                               //heatEnvironmentString,
                                               nestedTemplatesChecked,
                                               heatFilesObjects);
            		LOGGER.recordMetricEvent (updateStackStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully receive response from Open Stack", "OpenStack", "UpdateStack", null);
            		
            	}
            } catch (MsoException me) {
                me.addContext ("UpdateVFModule");
                String error = "Update VFModule " + vfModuleType + " in " + cloudSiteId + "/" + tenantId + ": " + me;
                LOGGER.recordMetricEvent (updateStackStarttime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error, "OpenStack", "UpdateStack", null);
                LOGGER.error (MessageEnum.RA_UPDATE_VNF_ERR, vfModuleType, cloudSiteId, tenantId, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception - " + error, me);
                LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, error);
                throw new VnfException (me);
            }
        }

        // Reach this point if updateStack is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfId (heatStack.getCanonicalName ());
        vfRollback.setVnfCreated (true);

        outputs.value = copyStringOutputs (heatStack.getOutputs ());
        rollback.value = vfRollback;
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully update VF Module");
        return;
    }

    private String getVfModuleNameFromModuleStackId(String vfModuleStackId) {
    	// expected format of vfModuleStackId is "MSOTEST51-vSAMP3_base_module-0/1fc1f86c-7b35-447f-99a6-c23ec176ae24"
    	// before the "/" is the vfModuleName and after the "/" is the heat stack id in Openstack
    	if (vfModuleStackId == null)
    		return null;
    	int index = vfModuleStackId.lastIndexOf('/');
    	if (index <= 0) 
    		return null;
    	String vfModuleName = null;
    	try {
    		vfModuleName = vfModuleStackId.substring(0, index);
    	} catch (Exception e) {
    		vfModuleName = null;
    	}
    	return vfModuleName;
    }

}
