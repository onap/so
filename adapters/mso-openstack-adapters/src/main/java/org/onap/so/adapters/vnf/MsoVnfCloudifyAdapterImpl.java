/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.vnf;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import org.onap.so.logger.LoggingAnchor;
import com.woorea.openstack.heat.Heat;
import org.onap.so.adapters.vnf.exceptions.VnfAlreadyExists;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.cloudify.beans.DeploymentInfo;
import org.onap.so.cloudify.beans.DeploymentStatus;
import org.onap.so.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.onap.so.cloudify.utils.MsoCloudifyUtils;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.utils.MsoHeatEnvironmentEntry;
import org.onap.so.openstack.utils.MsoHeatEnvironmentParameter;
import org.onap.so.openstack.utils.MsoKeystoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@WebService(serviceName = "VnfAdapter", endpointInterface = "org.onap.so.adapters.vnf.MsoVnfAdapter",
        targetNamespace = "http://org.onap.so/vnf")
public class MsoVnfCloudifyAdapterImpl implements MsoVnfAdapter {

    private static Logger logger = LoggerFactory.getLogger(MsoVnfCloudifyAdapterImpl.class);

    private static final String CHECK_REQD_PARAMS = "org.onap.so.adapters.vnf.checkRequiredParameters";
    private static final String CLOUDIFY = "Cloudify";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String BRACKETS = LoggingAnchor.NINE;
    private static final String OPENSTACK = "OpenStack";

    @Autowired
    protected CloudConfig cloudConfig;

    @Autowired
    private VFModuleCustomizationRepository vfModuleCustomRepo;

    @Autowired
    private Environment environment;

    @Autowired
    protected MsoKeystoneUtils keystoneUtils;

    @Autowired
    protected MsoCloudifyUtils cloudifyUtils;

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     *
     * @see MsoVnfCloudifyAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfCloudifyAdapterImpl() {

    }

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck() {
        logger.debug("Health check call in VNF Cloudify Adapter");
    }

    /**
     * This is the "Create VNF" web service implementation. This function is now unsupported and will return an error.
     *
     */
    @Override
    public void createVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfType, String vnfVersion,
            String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, Object> inputs,
            Boolean failIfExists, Boolean backout, Boolean enableBridge, MsoRequest msoRequest, Holder<String> vnfId,
            Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        // This operation is no longer supported at the VNF level. The adapter is only called to deploy modules.
        logger.debug("CreateVNF command attempted but not supported");
        throw new VnfException("CreateVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This is the "Update VNF" web service implementation. This function is now unsupported and will return an error.
     *
     */
    @Override
    public void updateVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfType, String vnfVersion,
            String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, Object> inputs,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback)
            throws VnfException {
        // This operation is no longer supported at the VNF level. The adapter is only called to deploy modules.
        logger.debug("UpdateVNF command attempted but not supported");
        throw new VnfException("UpdateVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This is the "Query VNF" web service implementation.
     *
     * This really should be QueryVfModule, but nobody ever changed it.
     *
     * For Cloudify, this will look up a deployment by its deployment ID, which is really the same as deployment name,
     * since it assigned by the client when a deployment is created. Also, the input cloudSiteId is used only to
     * identify which Cloudify instance to query, and the tenantId is ignored (since that really only applies for
     * Openstack/Heat).
     *
     * The method returns an indicator that the VNF exists, along with its status and outputs. The input "vnfName" will
     * also be reflected back as its ID.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param cloudOwner cloud owner of the cloud site in which to query
     * @param tenantId Openstack tenant identifier - ignored for Cloudify
     * @param vnfName VNF Name (should match a deployment ID)
     * @param msoRequest Request tracking information for logs
     * @param vnfExists Flag reporting the result of the query
     * @param vnfId Holder for output VNF ID
     * @param outputs Holder for Map of VNF outputs from Cloudify deployment (assigned IPs, etc)
     */
    @Override
    public void queryVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfName, MsoRequest msoRequest,
            Holder<Boolean> vnfExists, Holder<String> vnfId, Holder<VnfStatus> status,
            Holder<Map<String, String>> outputs) throws VnfException {
        logger.debug("Querying VNF {} in {}", vnfName, cloudSiteId + "/" + tenantId);

        DeploymentInfo deployment = null;

        try {
            deployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, vnfName);
        } catch (MsoCloudifyManagerNotFound e) {
            // This site does not have a Cloudify Manager.
            // This isn't an error, just means we won't find the VNF here.
            deployment = null;
        } catch (MsoException me) {
            // Failed to query the Deployment due to a cloudify exception.
            logger.debug("Failed to query the Deployment due to a cloudify exception");
            // Convert to a generic VnfException
            me.addContext("QueryVNF");
            String error = "Query VNF (Cloudify): " + vnfName + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + me;
            logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), vnfName, cloudOwner, cloudSiteId, tenantId,
                    CLOUDIFY, "QueryVNF", ErrorCode.DataError.getValue(), "Exception - queryDeployment", me);
            logger.debug(error);
            throw new VnfException(me);
        }

        if (deployment != null && deployment.getStatus() != DeploymentStatus.NOTFOUND) {
            vnfExists.value = Boolean.TRUE;
            status.value = deploymentStatusToVnfStatus(deployment);
            vnfId.value = deployment.getId();
            outputs.value = copyStringOutputs(deployment.getOutputs());

            logger.debug("VNF {} found in Cloudify, ID = {}", vnfName, vnfId.value);
        } else {
            vnfExists.value = Boolean.FALSE;
            status.value = VnfStatus.NOTFOUND;
            vnfId.value = null;
            outputs.value = new HashMap<String, String>(); // Return as an empty map

            logger.debug("VNF {} not found", vnfName);
        }
    }


    /**
     * This is the "Delete VNF" web service implementation. This function is now unsupported and will return an error.
     *
     */
    @Override
    public void deleteVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfName, MsoRequest msoRequest)
            throws VnfException {

        // This operation is no longer supported at the VNF level. The adapter is only called to deploy modules.
        logger.debug("DeleteVNF command attempted but not supported");
        throw new VnfException("DeleteVNF:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation. A rollback object is returned to the
     * client in a successful creation response. The client can pass that object as-is back to the rollbackVnf operation
     * to undo the creation.
     *
     * TODO: This should be rollbackVfModule and/or rollbackVolumeGroup, but APIs were apparently never updated.
     */
    @Override
    public void rollbackVnf(VnfRollback rollback) throws VnfException {
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            logger.info(LoggingAnchor.THREE, MessageEnum.RA_ROLLBACK_NULL.toString(), OPENSTACK, "rollbackVnf");
            return;
        }

        // Don't rollback if nothing was done originally
        if (!rollback.getVnfCreated()) {
            return;
        }

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudSiteId();
        String cloudOwner = rollback.getCloudOwner();
        String tenantId = rollback.getTenantId();
        String vfModuleId = rollback.getVfModuleStackId();

        logger.debug("Rolling Back VF Module {} in {}", vfModuleId, cloudOwner + "/" + cloudSiteId + "/" + tenantId);

        DeploymentInfo deployment = null;

        // Use the MsoCloudifyUtils to delete the deployment. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        try {
            // KLUDGE - Cloudify requires Tenant Name for Openstack. We have the ID.
            // Go directly to Keystone until APIs could be updated to supply the name.
            MsoTenant msoTenant = keystoneUtils.queryTenant(tenantId, cloudSiteId);
            String tenantName = (msoTenant != null ? msoTenant.getTenantName() : tenantId);

            // TODO: Get a reasonable timeout. Use a global property, or store the creation timeout in rollback object
            // and use that.
            deployment = cloudifyUtils.uninstallAndDeleteDeployment(cloudSiteId, tenantName, vfModuleId, 5);
            logger.debug("Rolled back deployment: {}", deployment.getId());
        } catch (MsoException me) {
            // Failed to rollback the VNF due to a cloudify exception.
            // Convert to a generic VnfException
            me.addContext("RollbackVNF");
            String error = "Rollback VF Module: " + vfModuleId + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + me;
            logger.error(BRACKETS, MessageEnum.RA_DELETE_VNF_ERR.toString(), vfModuleId, cloudOwner, cloudSiteId,
                    tenantId, CLOUDIFY, "DeleteDeployment", ErrorCode.DataError.getValue(),
                    "Exception - DeleteDeployment", me);
            logger.debug(error);
            throw new VnfException(me);
        }
    }


    private VnfStatus deploymentStatusToVnfStatus(DeploymentInfo deployment) {
        // Determine the status based on last action & status
        // DeploymentInfo object should be enhanced to report a better status internally.
        DeploymentStatus status = deployment.getStatus();
        String lastAction = deployment.getLastAction();

        if (status == null || lastAction == null) {
            return VnfStatus.UNKNOWN;
        } else if (status == DeploymentStatus.NOTFOUND) {
            return VnfStatus.NOTFOUND;
        } else if (status == DeploymentStatus.INSTALLED) {
            return VnfStatus.ACTIVE;
        } else if (status == DeploymentStatus.CREATED) {
            // Should have an INACTIVE status for this case. Shouldn't really happen, but
            // Install was never run, or Uninstall was done but deployment didn't get deleted.
            return VnfStatus.UNKNOWN;
        } else if (status == DeploymentStatus.FAILED) {
            return VnfStatus.FAILED;
        }

        return VnfStatus.UNKNOWN;
    }

    private Map<String, String> copyStringOutputs(Map<String, Object> stackOutputs) {
        Map<String, String> stringOutputs = new HashMap<>();
        for (Map.Entry<String, Object> entry : stackOutputs.entrySet()) {
            if (entry.getValue() instanceof String) {
                stringOutputs.put(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                try {
                    String str = "" + entry.getValue();
                    stringOutputs.put(entry.getKey(), str);
                } catch (Exception e) {
                    logger.error("Unable to add " + entry.getKey() + " to outputs", e);
                }
            } else if (entry.getValue() instanceof JsonNode) {
                try {
                    String str = this.convertNode((JsonNode) entry.getValue());
                    stringOutputs.put(entry.getKey(), str);
                } catch (Exception e) {
                    logger.error("Unable to add " + entry.getKey() + " to outputs - exception converting JsonNode", e);
                }
            } else if (entry.getValue() instanceof java.util.LinkedHashMap) {
                try {
                    String str = JSON_MAPPER.writeValueAsString(entry.getValue());
                    stringOutputs.put(entry.getKey(), str);
                } catch (Exception e) {
                    logger.error("Unable to add " + entry.getKey() + " to outputs - exception converting LinkedHashMap",
                            e);
                }
            } else {
                try {
                    String str = entry.getValue().toString();
                    stringOutputs.put(entry.getKey(), str);
                } catch (Exception e) {
                    logger.error("Unable to add " + entry.getKey() + " to outputs - unable to call .toString() ", e);
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
        } else if (inputs.size() < 1) {
            sb.append("\tEMPTY");
        } else {
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                String outputString;
                try {
                    outputString = entry.getValue().toString();
                } catch (Exception e) {
                    outputString = "Unable to call toString() on the value for " + entry.getKey();
                }
                sb.append("\t\nitem " + i++ + ": '" + entry.getKey() + "'='" + outputString + "'");
            }
        }
        logger.debug(sb.toString());
    }

    private void sendMapToDebug(Map<String, Object> inputs) {
        int i = 0;
        StringBuilder sb = new StringBuilder("inputs:");
        if (inputs == null) {
            sb.append("\tNULL");
        } else if (inputs.size() < 1) {
            sb.append("\tEMPTY");
        } else {
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                sb.append("\titem " + i++ + ": " + entry.getKey() + "=" + entry.getValue());
            }
        }
        logger.debug(sb.toString());
    }

    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            final String json = JSON_MAPPER.writeValueAsString(obj);
            return json;
        } catch (JsonParseException jpe) {
            logger.error("Error converting json to string ", jpe);
        } catch (Exception e) {
            logger.error("Error converting json to string ", e);
        }
        return "[Error converting json to string]";
    }

    private Map<String, String> convertMapStringObjectToStringString(Map<String, Object> objectMap) {
        if (objectMap == null) {
            return null;
        }
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if (!stringMap.containsKey(entry.getKey())) {
                Object obj = entry.getValue();
                if (obj instanceof String) {
                    stringMap.put(entry.getKey(), (String) entry.getValue());
                } else if (obj instanceof JsonNode) {
                    // This is a bit of mess - but I think it's the least impacting
                    // let's convert it BACK to a string - then it will get converted back later
                    try {
                        String str = this.convertNode((JsonNode) obj);
                        stringMap.put(entry.getKey(), str);
                    } catch (Exception e) {
                        logger.error("DANGER WILL ROBINSON: unable to convert value for JsonNode " + entry.getKey(), e);
                        // okay in this instance - only string values (fqdn) are expected to be needed
                    }
                } else if (obj instanceof java.util.LinkedHashMap) {
                    logger.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                    try {
                        String str = JSON_MAPPER.writeValueAsString(obj);
                        stringMap.put(entry.getKey(), str);
                    } catch (Exception e) {
                        logger.error(
                                "DANGER WILL ROBINSON: unable to convert value for LinkedHashMap " + entry.getKey(), e);
                    }
                } else if (obj instanceof Integer) {
                    try {
                        String str = "" + obj;
                        stringMap.put(entry.getKey(), str);
                    } catch (Exception e) {
                        logger.error("DANGER WILL ROBINSON: unable to convert value for Integer " + entry.getKey(), e);
                    }
                } else {
                    try {
                        String str = obj.toString();
                        stringMap.put(entry.getKey(), str);
                    } catch (Exception e) {
                        logger.error("DANGER WILL ROBINSON: unable to convert value " + entry.getKey(), e);
                    }
                }
            }
        }

        return stringMap;
    }

    /**
     * This is the "Create VF Module" web service implementation. It will instantiate a new VF Module of the requested
     * type in the specified cloud and tenant. The tenant must exist before this service is called.
     *
     * If a VF Module with the same name already exists, this can be considered a success or failure, depending on the
     * value of the 'failIfExists' parameter.
     *
     * All VF Modules are defined in the MSO catalog. The caller must request one of the pre-defined module types or an
     * error will be returned. Within the catalog, each VF Module references (among other things) a cloud template which
     * is used to deploy the required artifacts (VMs, networks, etc.) to the cloud. In this adapter implementation, that
     * artifact is expected to be a Cloudify blueprint.
     *
     * Depending on the blueprint, a variable set of input parameters will be defined, some of which are required. The
     * caller is responsible to pass the necessary input data for the module or an error will be thrown.
     *
     * The method returns the vfModuleId, a Map of output attributes, and a VnfRollback object. This last object can be
     * passed as-is to the rollbackVnf operation to undo everything that was created for the Module. This is useful if a
     * VF module is successfully created but the orchestration fails on a subsequent step.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param cloudOwner cloud owner of the cloud site in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vfModuleType VF Module type key, should match a VNF definition in catalog DB. Deprecated - should use
     *        modelCustomizationUuid
     * @param vnfVersion VNF version key, should match a VNF definition in catalog DB Deprecated - VF Module versions
     *        also captured by modelCustomizationUuid
     * @param genericVnfId Generic VNF ID
     * @param vfModuleName Name to be assigned to the new VF Module
     * @param vfModuleId Id of the new VF Module
     * @param requestType Indicates if this is a Volume Group or Module request
     * @param volumeGroupId Identifier (i.e. deployment ID) for a Volume Group to attach to a VF Module
     * @param baseVfModuleId Identifier (i.e. deployment ID) of the Base Module if this is an Add-on module
     * @param modelCustomizationUuid Unique ID for the VF Module's model. Replaces the use of vfModuleType.
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered
     * @param backout Flag whether to suppress automatic backout (for testing)
     * @param msoRequest Request tracking information for logs
     * @param vnfId Holder for output VNF Cloudify Deployment ID
     * @param outputs Holder for Map of VNF outputs from Deployment (assigned IPs, etc)
     * @param rollback Holder for returning VnfRollback object
     */
    @Override
    public void createVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vfModuleType,
            String vnfVersion, String genericVnfId, String vfModuleName, String vfModuleId, String requestType,
            String volumeGroupId, String baseVfModuleId, String modelCustomizationUuid, Map<String, Object> inputs,
            Boolean failIfExists, Boolean backout, Boolean enableBridge, MsoRequest msoRequest, Holder<String> vnfId,
            Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {

        // Require a model customization ID. Every VF Module definition must have one.
        if (modelCustomizationUuid == null || modelCustomizationUuid.isEmpty()) {
            logger.debug("Missing required input: modelCustomizationUuid");
            String error = "Create vfModule error: Missing required input: modelCustomizationUuid";
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                    "VF Module ModelCustomizationUuid", CLOUDIFY, ErrorCode.DataError.getValue(),
                    "Create VF Module: Missing required input: modelCustomizationUuid");
            logger.debug(error);
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
            inputs = new HashMap<>();
            logger.debug("inputs == null - setting to empty");
        } else {
            this.sendMapToDebug(inputs);
        }

        // Check if this is for a "Volume" module
        boolean isVolumeRequest = false;
        if (requestType.startsWith("VOLUME")) {
            isVolumeRequest = true;
        }

        logger.debug("requestType = " + requestType + ", volumeGroupStackId = " + volumeGroupId + ", baseStackId = "
                + baseVfModuleId);

        // Build a default rollback object (no actions performed)
        VnfRollback vfRollback = new VnfRollback();
        vfRollback.setCloudSiteId(cloudSiteId);
        vfRollback.setCloudOwner(cloudOwner);
        vfRollback.setTenantId(tenantId);
        vfRollback.setMsoRequest(msoRequest);
        vfRollback.setRequestType(requestType);
        vfRollback.setIsBase(false); // Until we know better
        vfRollback.setVolumeGroupHeatStackId(volumeGroupId);
        vfRollback.setBaseGroupHeatStackId(baseVfModuleId);
        vfRollback.setModelCustomizationUuid(modelCustomizationUuid);
        vfRollback.setMode("CFY");

        rollback.value = vfRollback; // Default rollback - no updates performed

        // Get the VNF/VF Module definition from the Catalog DB first.
        // There are three relevant records: VfModule, VfModuleCustomization, VnfResource

        VfModule vf = null;
        VnfResource vnfResource = null;
        VfModuleCustomization vfmc = null;

        try {
            vfmc = vfModuleCustomRepo.findFirstByModelCustomizationUUIDOrderByCreatedDesc(modelCustomizationUuid);

            if (vfmc == null) {
                String error = "Create vfModule error: Unable to find vfModuleCust with modelCustomizationUuid="
                        + modelCustomizationUuid;
                logger.debug(error);
                logger.error(LoggingAnchor.FIVE, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                        "VF Module " + "ModelCustomizationUuid", modelCustomizationUuid, "CatalogDb",
                        ErrorCode.DataError.getValue(), error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found vfModuleCust entry " + vfmc.toString());
            }

            // Get the vfModule and vnfResource records
            vf = vfmc.getVfModule();
            vnfResource = vfmc.getVfModule().getVnfResources();
        } catch (Exception e) {

            logger.error("unhandled exception in create VF - [Query]", e);
            throw new VnfException("Exception during create VF " + e.getMessage());
        }

        // Perform a version check against cloudSite
        // Obtain the cloud site information where we will create the VF Module
        Optional<CloudSite> cloudSiteOp = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOp.isPresent()) {
            throw new VnfException(new MsoCloudSiteNotFound(cloudSiteId));
        }
        CloudSite cloudSite = cloudSiteOp.get();
        MavenLikeVersioning aicV = new MavenLikeVersioning();
        aicV.setVersion(cloudSite.getCloudVersion());

        String vnfMin = vnfResource.getAicVersionMin();
        String vnfMax = vnfResource.getAicVersionMax();

        if ((vnfMin != null && !(aicV.isMoreRecentThan(vnfMin) || aicV.isTheSameVersion(vnfMin)))
                || (vnfMax != null && aicV.isMoreRecentThan(vnfMax))) {
            // ERROR
            String error = "VNF Resource type: " + vnfResource.getModelName() + ", ModelUuid="
                    + vnfResource.getModelUUID() + " VersionMin=" + vnfMin + " VersionMax:" + vnfMax
                    + " NOT supported on Cloud: " + cloudSiteId + " with AIC_Version:" + cloudSite.getCloudVersion();
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_CONFIG_EXC.toString(), error, OPENSTACK,
                    ErrorCode.BusinessProcessError.getValue(), "Exception - setVersion");
            logger.debug(error);
            throw new VnfException(error, MsoExceptionCategory.USERDATA);
        }
        // End Version check


        DeploymentInfo cloudifyDeployment = null;

        // First, look up to see if the VF already exists.

        try {
            cloudifyDeployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, vfModuleName);
        } catch (MsoException me) {
            // Failed to query the Deployment due to a cloudify exception.
            String error = "Create VF Module: Query " + vfModuleName + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + me;
            logger.error(LoggingAnchor.EIGHT, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudSiteId,
                    tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                    "Exception - queryDeployment", me);
            logger.debug(error);

            // Convert to a generic VnfException
            me.addContext("CreateVFModule");
            throw new VnfException(me);
        }

        // More precise handling/messaging if the Module already exists
        if (cloudifyDeployment != null && !(cloudifyDeployment.getStatus() == DeploymentStatus.NOTFOUND)) {
            // CREATED, INSTALLED, INSTALLING, FAILED, UNINSTALLING, UNKNOWN
            DeploymentStatus status = cloudifyDeployment.getStatus();
            logger.debug("Found Existing Deployment, status=" + status);

            if (status == DeploymentStatus.INSTALLED) {
                // fail - it exists
                if (failIfExists != null && failIfExists) {
                    String error = "Create VF: Deployment " + vfModuleName + " already exists in " + cloudOwner + "/"
                            + cloudSiteId + "/" + tenantId;
                    logger.error(BRACKETS, MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName, cloudOwner,
                            cloudSiteId, tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                            "Deployment " + vfModuleName + " already exists");
                    logger.debug(error);
                    throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                            cloudifyDeployment.getId());
                } else {
                    // Found existing deployment and client has not requested "failIfExists".
                    // Populate the outputs from the existing deployment.

                    vnfId.value = cloudifyDeployment.getId();
                    outputs.value = copyStringOutputs(cloudifyDeployment.getOutputs());
                    return;
                }
            }
            // Check through various detailed error cases
            if (status == DeploymentStatus.INSTALLING || status == DeploymentStatus.UNINSTALLING) {
                // fail - it's in progress - return meaningful error
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; please wait for it to complete, or fix manually.";
                logger.error(BRACKETS, MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                        "Deployment " + vfModuleName + " already exists");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId, cloudifyDeployment.getId());
            } else if (status == DeploymentStatus.FAILED) {
                // fail - it exists and is in a FAILED state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and is in FAILED state in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                logger.error(BRACKETS, MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                        "Deployment " + vfModuleName + " already " + "exists and is in FAILED state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId, cloudifyDeployment.getId());
            } else if (status == DeploymentStatus.UNKNOWN || status == DeploymentStatus.CREATED) {
                // fail - it exists and is in a UNKNOWN state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; requires manual intervention.";
                logger.error(BRACKETS, MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                        "Deployment " + vfModuleName + " already " + "exists and is in " + status.toString()
                                + " state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId, cloudifyDeployment.getId());
            } else {
                // Unexpected, since all known status values have been tested for
                String error = "Create VF: Deployment " + vfModuleName + " already exists with unexpected status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; requires manual intervention.";
                logger.error(BRACKETS, MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, "queryDeployment", ErrorCode.DataError.getValue(),
                        "Deployment " + vfModuleName + " already " + "exists and is in an unknown state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId, cloudifyDeployment.getId());
            }
        }


        // Collect outputs from Base Modules and Volume Modules
        Map<String, Object> baseModuleOutputs = null;
        Map<String, Object> volumeGroupOutputs = null;

        // If a Volume Group was provided, query its outputs for inclusion in Module input parameters
        if (volumeGroupId != null) {
            DeploymentInfo volumeDeployment = null;
            try {
                volumeDeployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, volumeGroupId);
            } catch (MsoException me) {
                // Failed to query the Volume GroupDeployment due to a cloudify exception.
                String error = "Create VF Module: Query Volume Group " + volumeGroupId + " in " + cloudOwner + "/"
                        + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), volumeGroupId, cloudOwner, cloudSiteId,
                        tenantId, CLOUDIFY, "queryDeployment(volume)", ErrorCode.DataError.getValue(),
                        "Exception - queryDeployment(volume)", me);
                logger.debug(error);
                // Convert to a generic VnfException
                me.addContext("CreateVFModule(QueryVolume)");
                throw new VnfException(me);
            }

            if (volumeDeployment == null || volumeDeployment.getStatus() == DeploymentStatus.NOTFOUND) {
                String error = "Create VFModule: Attached Volume Group DOES NOT EXIST " + volumeGroupId + " in "
                        + cloudSiteId + "/" + tenantId + " USER ERROR";
                logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), volumeGroupId, cloudSiteId, tenantId,
                        error, CLOUDIFY, "queryDeployment(volume)", ErrorCode.BusinessProcessError.getValue(),
                        "Create VFModule: Attached Volume Group DOES NOT EXIST");
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested volume group");
                volumeGroupOutputs = volumeDeployment.getOutputs();
                this.sendMapToDebug(volumeGroupOutputs, "volumeGroupOutputs");
            }
        }

        // If this is an Add-On Module, query the Base Module outputs
        // Note: This will be performed whether or not the current request is for an
        // Add-On Volume Group or Add-On VF Module

        if (vf.getIsBase()) {
            logger.debug("This is a BASE Module request");
            vfRollback.setIsBase(true);
        } else {
            logger.debug("This is an Add-On Module request");

            // Add-On Modules should always have a Base, but just treat as a warning if not provided.
            // Add-on Volume requests may or may not specify a base.
            if (!isVolumeRequest && baseVfModuleId == null) {
                logger.debug("WARNING:  Add-on Module request - no Base Module ID provided");
            }

            if (baseVfModuleId != null) {
                DeploymentInfo baseDeployment = null;
                try {
                    baseDeployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, baseVfModuleId);
                } catch (MsoException me) {
                    // Failed to query the Volume GroupDeployment due to a cloudify exception.
                    String error = "Create VF Module: Query Base " + baseVfModuleId + " in " + cloudOwner + "/"
                            + cloudSiteId + "/" + tenantId + ": " + me;
                    logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), baseVfModuleId, cloudOwner,
                            cloudSiteId, tenantId, CLOUDIFY, "queryDeployment(Base)", ErrorCode.DataError.getValue(),
                            "Exception - queryDeployment(Base)", me);
                    logger.debug(error);
                    // Convert to a generic VnfException
                    me.addContext("CreateVFModule(QueryBase)");
                    throw new VnfException(me);
                }

                if (baseDeployment == null || baseDeployment.getStatus() == DeploymentStatus.NOTFOUND) {
                    String error = "Create VFModule: Base Module DOES NOT EXIST " + baseVfModuleId + " in "
                            + cloudSiteId + "/" + tenantId + " USER ERROR";
                    logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), baseVfModuleId, cloudSiteId,
                            tenantId, error, CLOUDIFY, "queryDeployment(Base)",
                            ErrorCode.BusinessProcessError.getValue(),
                            "Create VFModule: Base " + "Module DOES NOT EXIST");
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                } else {
                    logger.debug("Found base module");
                    baseModuleOutputs = baseDeployment.getOutputs();
                    this.sendMapToDebug(baseModuleOutputs, "baseModuleOutputs");
                }
            }
        }


        // Ready to deploy the new VNF

        // NOTE: For this section, heatTemplate is used for both HEAT templates and Cloudify blueprints.
        // In final implementation (post-POC), the template object would either be generic or there would
        // be a separate DB Table/Object for Blueprints.


        // NOTE: The template is fixed for the VF Module. The environment is part of the customization.
        HeatTemplate heatTemplate = null;
        HeatEnvironment heatEnvironment = null;
        if (isVolumeRequest) {
            heatTemplate = vf.getVolumeHeatTemplate();
            heatEnvironment = vfmc.getVolumeHeatEnv();
        } else {
            heatTemplate = vf.getModuleHeatTemplate();
            heatEnvironment = vfmc.getHeatEnvironment();
        }

        if (heatTemplate == null) {
            String error = "UpdateVF: No Heat Template ID defined in catalog database for " + vfModuleType
                    + ", modelCustomizationUuid=" + modelCustomizationUuid + ", vfModuleUuid=" + vf.getModelUUID()
                    + ", reqType=" + requestType;
            logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Template ID",
                    vfModuleType, OPENSTACK, ErrorCode.DataError.getValue(), error);
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got HEAT Template from DB: {}", heatTemplate.getHeatTemplate());
        }

        if (heatEnvironment == null) {
            String error = "Update VNF: undefined Heat Environment. VF=" + vfModuleType + ", modelCustomizationUuid="
                    + modelCustomizationUuid + ", vfModuleUuid=" + vf.getModelUUID() + ", reqType=" + requestType;
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Environment ID",
                    OPENSTACK, ErrorCode.DataError.getValue(), error);
            // Alarm on this error, configuration must be fixed
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got Heat Environment from DB: {}", heatEnvironment.getEnvironment());
        }


        try {
            // All variables converted to their native object types
            HashMap<String, Object> goldenInputs = new HashMap<>();
            List<String> extraInputs = new ArrayList<>();

            // NOTE: SKIP THIS FOR CLOUDIFY for now. Just use what was passed in.
            // This whole section needs to be rewritten.
            Boolean skipInputChecks = false;

            if (skipInputChecks) {
                goldenInputs = new HashMap<>();
                for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                    goldenInputs.put(entry.getKey(), entry.getValue());
                }
            } else {
                // Build maps for the parameters (including aliases) to simplify checks
                HashMap<String, HeatTemplateParam> params = new HashMap<>();

                Set<HeatTemplateParam> paramSet = heatTemplate.getParameters();
                logger.debug("paramSet has {} entries", paramSet.size());

                for (HeatTemplateParam htp : paramSet) {
                    params.put(htp.getParamName(), htp);

                    // Include aliases.
                    String alias = htp.getParamAlias();
                    if (alias != null && !"".equals(alias) && !params.containsKey(alias)) {
                        params.put(alias, htp);
                    }
                }

                // First, convert all inputs to their "template" type
                for (String key : inputs.keySet()) {
                    if (params.containsKey(key)) {
                        Object value = cloudifyUtils.convertInputValue(inputs.get(key), params.get(key));
                        if (value != null) {
                            goldenInputs.put(key, value);
                        } else {
                            logger.debug("Failed to convert input " + key + "='" + inputs.get(key) + "' to "
                                    + params.get(key).getParamType());
                        }
                    } else {
                        extraInputs.add(key);
                    }
                }

                if (!extraInputs.isEmpty()) {
                    logger.debug("Ignoring extra inputs: " + extraInputs);
                }

                // Next add in Volume Group Outputs if there are any. Copy directly without conversions.
                if (volumeGroupOutputs != null && !volumeGroupOutputs.isEmpty()) {
                    for (Map.Entry<String, Object> entry : volumeGroupOutputs.entrySet()) {
                        if (params.containsKey(entry.getKey()) && !goldenInputs.containsKey(entry.getKey())) {
                            goldenInputs.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                // Next add in Base Module Outputs if there are any. Copy directly without conversions.
                if (baseModuleOutputs != null && !baseModuleOutputs.isEmpty()) {
                    for (Map.Entry<String, Object> entry : baseModuleOutputs.entrySet()) {
                        if (params.containsKey(entry.getKey()) && !goldenInputs.containsKey(entry.getKey())) {
                            goldenInputs.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                // Last, add in values from the "environment" file.
                // These are added to the inputs, since Cloudify doesn't pass an environment file like Heat.

                // TODO: This may take a different form for Cloudify, but for now process it
                // with Heat environment file syntax
                StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());
                MsoHeatEnvironmentEntry mhee = new MsoHeatEnvironmentEntry(sb);

                if (mhee.getParameters() != null) {
                    for (MsoHeatEnvironmentParameter envParam : mhee.getParameters()) {
                        // If this is a template input, copy to golden inputs
                        String envKey = envParam.getName();
                        if (params.containsKey(envKey) && !goldenInputs.containsKey(envKey)) {
                            Object value = cloudifyUtils.convertInputValue(envParam.getValue(), params.get(envKey));
                            if (value != null) {
                                goldenInputs.put(envKey, value);
                            } else {
                                logger.debug("Failed to convert environment parameter " + envKey + "='"
                                        + envParam.getValue() + "' to " + params.get(envKey).getParamType());
                            }
                        }
                    }
                }

                this.sendMapToDebug(goldenInputs, "Final inputs sent to Cloudify");


                // Check that required parameters have been supplied from any of the sources
                String missingParams = null;
                boolean checkRequiredParameters = true;
                try {
                    String propertyString = this.environment.getProperty(MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS);
                    if ("false".equalsIgnoreCase(propertyString) || "n".equalsIgnoreCase(propertyString)) {
                        checkRequiredParameters = false;
                        logger.debug("CheckRequiredParameters is FALSE. Will still check but then skip blocking... {}",
                                MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS);
                    }
                } catch (Exception e) {
                    // No problem - default is true
                    logger.error("An exception occured trying to get property {}",
                            MsoVnfCloudifyAdapterImpl.CHECK_REQD_PARAMS, e);
                }


                for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                    if (parm.isRequired() && (!goldenInputs.containsKey(parm.getParamName()))) {
                        logger.debug("adding to missing parameters list: {}", parm.getParamName());
                        if (missingParams == null) {
                            missingParams = parm.getParamName();
                        } else {
                            missingParams += "," + parm.getParamName();
                        }
                    }
                }

                if (missingParams != null) {
                    if (checkRequiredParameters) {
                        // Problem - missing one or more required parameters
                        String error = "Create VFModule: Missing Required inputs: " + missingParams;
                        logger.error(LoggingAnchor.FIVE, MessageEnum.RA_MISSING_PARAM.toString(), missingParams,
                                CLOUDIFY, ErrorCode.DataError.getValue(), "Create VFModule: Missing Required inputs");
                        logger.debug(error);
                        throw new VnfException(error, MsoExceptionCategory.USERDATA);
                    } else {
                        logger.debug("found missing parameters [" + missingParams
                                + "] - but checkRequiredParameters is false -" + " will not block");
                    }
                } else {
                    logger.debug("No missing parameters found - ok to proceed");
                }

            } // NOTE: END PARAMETER CHECKING

            // Ready to deploy the VF Module.
            // *First step - make sure the blueprint is loaded into Cloudify.
            String blueprintName = heatTemplate.getTemplateName();
            String blueprint = heatTemplate.getTemplateBody();
            String blueprintId = blueprintName;

            // Use the main blueprint name as the blueprint ID (strip yaml extensions).
            if (blueprintId.endsWith(".yaml"))
                blueprintId = blueprintId.substring(0, blueprintId.lastIndexOf(".yaml"));

            try {
                if (!cloudifyUtils.isBlueprintLoaded(cloudSiteId, blueprintId)) {
                    logger.debug("Blueprint " + blueprintId + " is not loaded.  Will upload it now.");

                    Map<String, byte[]> blueprintFiles = new HashMap<>();

                    blueprintFiles.put(blueprintName, blueprint.getBytes());

                    // TODO: Implement nested blueprint logic based on Cloudify structures.
                    // For now, just use the Heat structures.
                    // The query returns a map of String->Object, where the map keys provide one layer of
                    // indirection from the Heat template names. For this case, assume the map key matches
                    // the nested blueprint name.
                    List<HeatTemplate> nestedBlueprints = heatTemplate.getChildTemplates();
                    if (nestedBlueprints != null) {
                        for (HeatTemplate nestedBlueprint : nestedBlueprints) {
                            blueprintFiles.put(nestedBlueprint.getTemplateName(),
                                    nestedBlueprint.getTemplateBody().getBytes());
                        }
                    }

                    // TODO: Implement file artifact logic based on Cloudify structures.
                    // For now, just use the Heat structures.
                    List<HeatFiles> heatFiles = vf.getHeatFiles();
                    if (heatFiles != null) {
                        for (HeatFiles heatFile : heatFiles) {
                            blueprintFiles.put(heatFile.getFileName(), heatFile.getFileBody().getBytes());
                        }
                    }

                    // Upload the blueprint package
                    cloudifyUtils.uploadBlueprint(cloudSiteId, blueprintId, blueprintName, blueprintFiles, false);

                }
            }

            catch (MsoException me) {
                me.addContext("CreateVFModule");
                String error = "Create VF Module: Upload blueprint failed.  Blueprint=" + blueprintName + ": " + me;
                logger.error(LoggingAnchor.SEVEN, MessageEnum.RA_CREATE_VNF_ERR.toString(), vfModuleType, cloudSiteId,
                        tenantId, CLOUDIFY, ErrorCode.DataError.getValue(), "MsoException - uploadBlueprint", me);
                logger.debug(error);
                throw new VnfException(me);
            }

            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.
            try {
                // KLUDGE - Cloudify requires Tenant Name for Openstack. We have the ID.
                // Go directly to Keystone until APIs could be updated to supply the name.
                MsoTenant msoTenant = keystoneUtils.queryTenant(tenantId, cloudSiteId);
                String tenantName = (msoTenant != null ? msoTenant.getTenantName() : tenantId);

                if (backout == null) {
                    backout = true;
                }

                cloudifyDeployment = cloudifyUtils.createAndInstallDeployment(cloudSiteId, tenantName, vfModuleName,
                        blueprintId, goldenInputs, true, heatTemplate.getTimeoutMinutes(), backout.booleanValue());

            } catch (MsoException me) {
                me.addContext("CreateVFModule");
                String error = "Create VF Module " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/"
                        + tenantId + ": " + me;
                logger.error(LoggingAnchor.EIGHT, MessageEnum.RA_CREATE_VNF_ERR.toString(), vfModuleType, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, ErrorCode.DataError.getValue(),
                        "MsoException - createDeployment", me);
                logger.debug(error);
                throw new VnfException(me);
            } catch (NullPointerException npe) {
                String error = "Create VFModule " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/"
                        + tenantId + ": " + npe;
                logger.error(LoggingAnchor.EIGHT, MessageEnum.RA_CREATE_VNF_ERR.toString(), vfModuleType, cloudOwner,
                        cloudSiteId, tenantId, CLOUDIFY, ErrorCode.DataError.getValue(),
                        "NullPointerException - createDeployment", npe);
                logger.debug(error);
                logger.debug("NULL POINTER EXCEPTION at cloudify.createAndInstallDeployment");
                // npe.addContext ("CreateVNF");
                throw new VnfException("NullPointerException during cloudify.createAndInstallDeployment");
            } catch (Exception e) {
                logger.error("unhandled exception at cloudify.createAndInstallDeployment", e);
                throw new VnfException("Exception during cloudify.createAndInstallDeployment! " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("unhandled exception in create VF", e);
            throw new VnfException("Exception during create VF " + e.getMessage());

        }

        // Reach this point if create is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfCreated(true);
        vfRollback.setVnfId(cloudifyDeployment.getId());
        vnfId.value = cloudifyDeployment.getId();
        outputs.value = copyStringOutputs(cloudifyDeployment.getOutputs());

        rollback.value = vfRollback;

        logger.debug("VF Module successfully created {}", vfModuleName);

    }

    public void deleteVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vnfName,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs) throws VnfException {
        logger.debug("Deleting VF " + vnfName + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId);

        // 1702 capture the output parameters on a delete
        // so we'll need to query first
        DeploymentInfo deployment = null;
        try {
            deployment = cloudifyUtils.queryDeployment(cloudSiteId, tenantId, vnfName);
        } catch (MsoException me) {
            // Failed to query the deployment. Convert to a generic VnfException
            me.addContext("DeleteVFModule");
            String error = "Delete VFModule: Query to get outputs: " + vnfName + " in " + cloudOwner + "/" + cloudSiteId
                    + "/" + tenantId + ": " + me;
            logger.error(BRACKETS, MessageEnum.RA_QUERY_VNF_ERR.toString(), vnfName, cloudOwner, cloudSiteId, tenantId,
                    CLOUDIFY, "QueryDeployment", ErrorCode.DataError.getValue(), "Exception - QueryDeployment", me);
            logger.debug(error);
            throw new VnfException(me);
        }
        // call method which handles the conversion from Map<String,Object> to Map<String,String> for our expected
        // Object types
        outputs.value = convertMapStringObjectToStringString(deployment.getOutputs());

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        try {
            cloudifyUtils.uninstallAndDeleteDeployment(cloudSiteId, tenantId, vnfName, 5);
        } catch (MsoException me) {
            me.addContext("DeleteVfModule");
            // Convert to a generic VnfException
            String error =
                    "Delete VF: " + vnfName + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
            logger.error(BRACKETS, MessageEnum.RA_DELETE_VNF_ERR.toString(), vnfName, cloudOwner, cloudSiteId, tenantId,
                    "DeleteDeployment", "DeleteDeployment", ErrorCode.DataError.getValue(),
                    "Exception - DeleteDeployment: " + me.getMessage());
            logger.debug(error);
            throw new VnfException(me);
        }

        // On success, nothing is returned.
        return;
    }

    // TODO: Should Update be supported for Cloudify? What would this look like?
    @Override
    public void updateVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vnfType,
            String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId,
            String baseVfHeatStackId, String vfModuleStackId, String modelCustomizationUuid, Map<String, Object> inputs,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback)
            throws VnfException {
        // This operation is not currently supported for Cloudify-orchestrated VF Modules.
        logger.debug("Update VF Module command attempted but not supported");
        throw new VnfException("UpdateVfModule:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

}
