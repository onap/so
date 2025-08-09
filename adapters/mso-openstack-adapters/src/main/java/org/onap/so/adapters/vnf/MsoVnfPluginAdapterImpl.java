/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

/**
 * This VNF Adapter implementation is based on the VDU Plugin model. It assumes that each VF Module definition in the
 * MSO catalog is expressed via a set of template and/or file artifacts that are appropriate for some specific
 * sub-orchestrator that provides an implementation of the VduPlugin interface. This adapter handles all of the common
 * VF Module logic, including: - catalog lookups for artifact retrieval - parameter filtering and validation - base and
 * volume module queries - rollback logic - logging and error handling
 *
 * Then based on the orchestration mode of the VNF, it will invoke different VDU plug-ins to perform the low level
 * instantiations, deletions, and queries. At this time, the set of available plug-ins is hard-coded, though in the
 * future a dynamic selection is expected (e.g. via a service-provider interface).
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
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduPlugin;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.adapters.vdu.mapper.VfModuleCustomizationToVduMapper;
import org.onap.so.adapters.vnf.exceptions.VnfAlreadyExists;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.utils.MsoHeatEnvironmentEntry;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoKeystoneUtils;
import org.onap.so.openstack.utils.MsoMulticloudUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebService(serviceName = "VnfAdapter", endpointInterface = "org.onap.so.adapters.vnf.MsoVnfAdapter",
        targetNamespace = "http://org.onap.so/vnf")
@Component
@Transactional
public class MsoVnfPluginAdapterImpl {

    private static final String MSO_CONFIGURATION_ERROR = "MsoConfigurationError";
    private static Logger logger = LoggerFactory.getLogger(MsoVnfPluginAdapterImpl.class);

    private static final String CHECK_REQD_PARAMS = "org.onap.so.adapters.vnf.checkRequiredParameters";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Autowired
    protected CloudConfig cloudConfig;

    @Autowired
    private VFModuleCustomizationRepository vfModuleCustomRepo;

    @Autowired
    private Environment environment;

    @Autowired
    protected MsoKeystoneUtils keystoneUtils;

    @Autowired
    protected MsoHeatUtils heatUtils;

    @Autowired
    protected MsoMulticloudUtils multicloudUtils;

    @Autowired
    protected VfModuleCustomizationToVduMapper vduMapper;

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */

    public void healthCheck() {
        logger.debug("Health check call in VNF Plugin Adapter");
    }

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     * 
     * @see MsoVnfPluginAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfPluginAdapterImpl() {

    }

    /**
     * This is the "Create VNF" web service implementation. This function is now unsupported and will return an error.
     *
     */
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
     * The method returns an indicator that the VNF exists, along with its status and outputs. The input "vnfName" will
     * also be reflected back as its ID.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param tenantId Openstack tenant identifier
     * @param vnfNameOrId VNF Name or ID to query
     * @param msoRequest Request tracking information for logs
     * @param vnfExists Flag reporting the result of the query
     * @param vnfId Holder for output VNF ID
     * @param outputs Holder for Map of outputs from the deployed VF Module (assigned IPs, etc)
     */
    public void queryVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfNameOrId,
            MsoRequest msoRequest, Holder<Boolean> vnfExists, Holder<String> vnfId, Holder<VnfStatus> status,
            Holder<Map<String, String>> outputs) throws VnfException {
        logger.debug("Querying VNF {} in {}/{}/{}", vnfNameOrId, cloudOwner, cloudSiteId, tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis();
        long subStartTime = System.currentTimeMillis();

        VduInstance vduInstance = null;
        CloudInfo cloudInfo = new CloudInfo(cloudSiteId, cloudOwner, tenantId, null);

        VduPlugin vduPlugin = getVduPlugin(cloudSiteId, cloudOwner);

        try {
            vduInstance = vduPlugin.queryVdu(cloudInfo, vnfNameOrId);
        } catch (VduException e) {
            // Failed to query the VDU due to a plugin exception.
            // Convert to a generic VnfException
            e.addContext("QueryVNF");
            String error = "Query VNF (VDU): " + vnfNameOrId + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + e;
            logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), vnfNameOrId, cloudOwner,
                    cloudSiteId, tenantId, "VDU", "QueryVNF", ErrorCode.DataError.getValue(), "Exception - queryVDU",
                    e);
            logger.debug(error);
            throw new VnfException(e);
        }

        if (vduInstance != null && vduInstance.getStatus().getState() != VduStateType.NOTFOUND) {
            vnfExists.value = Boolean.TRUE;
            status.value = vduStatusToVnfStatus(vduInstance);
            vnfId.value = vduInstance.getVduInstanceId();
            outputs.value = copyStringOutputs(vduInstance.getOutputs());

            logger.debug("VNF {} found, ID = {}", vnfNameOrId, vnfId.value);
        } else {
            vnfExists.value = Boolean.FALSE;
            status.value = VnfStatus.NOTFOUND;
            vnfId.value = null;
            outputs.value = new HashMap<String, String>(); // Return as an empty map

            logger.debug("VNF {} not found", vnfNameOrId);
        }
        return;
    }


    /**
     * This is the "Delete VNF" web service implementation. This function is now unsupported and will return an error.
     *
     */

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

    public void rollbackVnf(VnfRollback rollback) throws VnfException {
        long startTime = System.currentTimeMillis();
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            logger.info("{} {} {}", MessageEnum.RA_ROLLBACK_NULL.toString(), "OpenStack", "rollbackVnf");
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
        CloudInfo cloudInfo = new CloudInfo(cloudSiteId, cloudOwner, tenantId, null);

        String vfModuleId = rollback.getVfModuleStackId();

        logger.debug("Rolling Back VF Module {} in {}/{}/{}", vfModuleId, cloudOwner, cloudSiteId, tenantId);

        VduInstance vduInstance = null;

        // Use the VduPlugin to delete the VF Module.
        VduPlugin vduPlugin = getVduPlugin(cloudSiteId, cloudOwner);

        long subStartTime = System.currentTimeMillis();
        try {
            // TODO: Get a reasonable timeout. Use a global property, or store the creation timeout in rollback object
            // and use that.
            vduInstance = vduPlugin.deleteVdu(cloudInfo, vfModuleId, 5);

            logger.debug("Rolled back VDU instantiation: {}", vduInstance.getVduInstanceId());
        } catch (VduException ve) {
            // Failed to rollback the VF Module due to a plugin exception.
            // Convert to a generic VnfException
            ve.addContext("RollbackVFModule");
            String error = "Rollback VF Module: " + vfModuleId + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + ve;
            logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_DELETE_VNF_ERR.toString(), vfModuleId, cloudOwner,
                    cloudSiteId, tenantId, "VDU", "DeleteVdu", ErrorCode.DataError.getValue(), "Exception - DeleteVdu",
                    ve);
            logger.debug(error);
            throw new VnfException(ve);
        }
        return;
    }


    private VnfStatus vduStatusToVnfStatus(VduInstance vdu) {
        // Determine the status based on last action & status
        // DeploymentInfo object should be enhanced to report a better status internally.
        VduStatus vduStatus = vdu.getStatus();
        VduStateType status = vduStatus.getState();

        if (status == null) {
            return VnfStatus.UNKNOWN;
        } else if (status == VduStateType.NOTFOUND) {
            return VnfStatus.NOTFOUND;
        } else if (status == VduStateType.INSTANTIATED) {
            return VnfStatus.ACTIVE;
        } else if (status == VduStateType.FAILED) {
            return VnfStatus.FAILED;
        }

        return VnfStatus.UNKNOWN;
    }

    /*
     * Normalize an input value to an Object, based on the target parameter type. If the type is not recognized, it will
     * just be returned unchanged (as a string).
     */
    private Object convertInputValue(Object inputValue, HeatTemplateParam templateParam) {
        String type = templateParam.getParamType();
        logger.debug("Parameter: {} is of type ", templateParam.getParamName(), type);

        if (type.equalsIgnoreCase("number")) {
            try {
                return Integer.valueOf(inputValue.toString());
            } catch (Exception e) {
                logger.debug("Unable to convert {} to an integer!", inputValue, e);
                return null;
            }
        } else if (type.equalsIgnoreCase("json")) {
            try {
                JsonNode jsonNode = JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(inputValue));
                return jsonNode;
            } catch (Exception e) {
                logger.debug("Unable to convert {} to a JsonNode!", inputValue, e);
                return null;
            }
        } else if (type.equalsIgnoreCase("boolean")) {
            return new Boolean(inputValue.toString());
        }

        // Nothing else matched. Return the original string
        return inputValue;
    }

    private Map<String, String> copyStringOutputs(Map<String, Object> stackOutputs) {
        Map<String, String> stringOutputs = new HashMap<String, String>();
        for (String key : stackOutputs.keySet()) {
            if (stackOutputs.get(key) instanceof String) {
                stringOutputs.put(key, (String) stackOutputs.get(key));
            } else if (stackOutputs.get(key) instanceof Integer) {
                try {
                    String str = "" + stackOutputs.get(key);
                    stringOutputs.put(key, str);
                } catch (Exception e) {
                    logger.debug("Unable to add {} to outputs", key, e);
                }
            } else if (stackOutputs.get(key) instanceof JsonNode) {
                try {
                    String str = this.convertNode((JsonNode) stackOutputs.get(key));
                    stringOutputs.put(key, str);
                } catch (Exception e) {
                    logger.debug("Unable to add {} to outputs - exception converting JsonNode", key, e);
                }
            } else if (stackOutputs.get(key) instanceof java.util.LinkedHashMap) {
                try {
                    String str = JSON_MAPPER.writeValueAsString(stackOutputs.get(key));
                    stringOutputs.put(key, str);
                } catch (Exception e) {
                    logger.debug("Unable to add {} to outputs - exception converting LinkedHashMap", key, e);
                }
            } else {
                try {
                    String str = stackOutputs.get(key).toString();
                    stringOutputs.put(key, str);
                } catch (Exception e) {
                    logger.debug("Unable to add {} to outputs - unable to call .toString() {}", key, e.getMessage(), e);
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
        logger.debug(sb.toString());
        return;
    }

    private void sendMapToDebug(Map<String, Object> inputs) {
        int i = 0;
        StringBuilder sb = new StringBuilder("inputs:");
        if (inputs == null) {
            sb.append("\tNULL");
        } else if (inputs.size() < 1) {
            sb.append("\tEMPTY");
        } else {
            for (String str : inputs.keySet()) {
                sb.append("\titem " + i++ + ": " + str + "=" + inputs.get(str));
            }
        }
        logger.debug(sb.toString());
        return;
    }

    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            final String json = JSON_MAPPER.writeValueAsString(obj);
            return json;
        } catch (JsonParseException jpe) {
            logger.debug("Error converting json to string {}", jpe.getMessage());
        } catch (Exception e) {
            logger.debug("Error converting json to string {}", e.getMessage());
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
                } else if (obj instanceof JsonNode) {
                    // This is a bit of mess - but I think it's the least impacting
                    // let's convert it BACK to a string - then it will get converted back later
                    try {
                        String str = this.convertNode((JsonNode) obj);
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for JsonNode {}", key, e);
                        // okay in this instance - only string values (fqdn) are expected to be needed
                    }
                } else if (obj instanceof java.util.LinkedHashMap) {
                    logger.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                    try {
                        String str = JSON_MAPPER.writeValueAsString(obj);
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for LinkedHashMap {}", key, e);
                    }
                } else if (obj instanceof Integer) {
                    try {
                        String str = "" + obj;
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for Integer {}", key, e);
                    }
                } else {
                    try {
                        String str = obj.toString();
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value {} ({})", key, e.getMessage(), e);
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
     * error will be returned. Within the catalog, each VF Module references (among other things) a collection of
     * artifacts that are used to deploy the required cloud resources (VMs, networks, etc.).
     *
     * Depending on the module templates, a variable set of input parameters will be defined, some of which are
     * required. The caller is responsible to pass the necessary input data for the module or an error will be thrown.
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
     * @param vnfId - VNF ID
     * @param vfModuleName Name to be assigned to the new VF Module
     * @param vfModuleId Id fo the new VF Module
     * @param requestType Indicates if this is a Volume Group or Module request
     * @param volumeGroupId Identifier (i.e. deployment ID) for a Volume Group to attach to a VF Module
     * @param baseVfModuleId Identifier (i.e. deployment ID) of the Base Module if this is an Add-on module
     * @param modelCustomizationUuid Unique ID for the VF Module's model. Replaces the use of vfModuleType.
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered
     * @param backout Flag whether to suppress automatic backout (for testing)
     * @param msoRequest Request tracking information for logs
     * @param vnfId Holder for output VF Module instance ID in the cloud
     * @param outputs Holder for Map of VNF outputs from Deployment (assigned IPs, etc)
     * @param rollback Holder for returning VnfRollback object
     */

    public void createVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vfModuleType,
            String vnfVersion, String genericVnfId, String vfModuleName, String vfModuleId, String requestType,
            String volumeGroupId, String baseVfModuleId, String modelCustomizationUuid, Map<String, Object> inputs,
            Boolean failIfExists, Boolean backout, Boolean enableBridge, MsoRequest msoRequest, Holder<String> vnfId)
            throws VnfException {
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis();

        // Require a model customization ID. Every VF Module definition must have one.
        if (modelCustomizationUuid == null || modelCustomizationUuid.isEmpty()) {
            logger.debug("Missing required input: modelCustomizationUuid");
            String error = "Create vfModule error: Missing required input: modelCustomizationUuid";
            logger.error("{} {} {} {} {}", MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                    "VF Module ModelCustomizationUuid", "VDU", ErrorCode.DataError,
                    "Create VF Module: " + "Missing required input: modelCustomizationUuid");
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

        logger.debug("requestType = {}, volumeGroupStackId = {}, baseStackId = {}", requestType, volumeGroupId,
                baseVfModuleId);

        // Get the VNF/VF Module definition from the Catalog DB first.
        // There are three relevant records: VfModule, VfModuleCustomization, VnfResource

        VfModule vfModule = null;
        VnfResource vnfResource = null;
        VfModuleCustomization vfModuleCust = null;

        try {
            vfModuleCust =
                    vfModuleCustomRepo.findFirstByModelCustomizationUUIDOrderByCreatedDesc(modelCustomizationUuid);

            if (vfModuleCust == null) {
                String error = "Create vfModule error: Unable to find vfModuleCust with modelCustomizationUuid="
                        + modelCustomizationUuid;
                logger.debug(error);
                logger.error("{} {} {} {} {} {}", MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                        "VF Module ModelCustomizationUuid", modelCustomizationUuid, "CatalogDb", ErrorCode.DataError,
                        error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found vfModuleCust entry {}", vfModuleCust.toString());
            }

            // Get the vfModule and vnfResource records
            vfModule = vfModuleCust.getVfModule();
            vnfResource = vfModuleCust.getVfModule().getVnfResources();
        } catch (Exception e) {

            logger.debug("unhandled exception in create VF - [Query]{}", e.getMessage());
            throw new VnfException("Exception during create VF " + e.getMessage());
        }

        // Perform a version check against cloudSite
        // Obtain the cloud site information where we will create the VF Module
        Optional<CloudSite> cloudSiteOp = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOp.isPresent()) {
            // If cloudSiteId is not present in the catalog DB, then default to multicloud
            logger.debug("{} is not present in cloud_site catalog DB, defaulting to Multicloud plugin adapter",
                    cloudSiteId);
        } else {
            CloudSite cloudSite = cloudSiteOp.get();
            MavenLikeVersioning aicV = new MavenLikeVersioning();
            aicV.setVersion(cloudSite.getCloudVersion());

            String vnfMin = vnfResource.getAicVersionMin();
            String vnfMax = vnfResource.getAicVersionMax();

            if ((vnfMin != null && !(aicV.isMoreRecentThan(vnfMin) || aicV.isTheSameVersion(vnfMin)))
                    || (vnfMax != null && aicV.isMoreRecentThan(vnfMax))) {
                // ERROR
                String error =
                        "VNF Resource type: " + vnfResource.getModelName() + ", ModelUuid=" + vnfResource.getModelUUID()
                                + " VersionMin=" + vnfMin + " VersionMax:" + vnfMax + " NOT supported on Cloud: "
                                + cloudSiteId + " with AIC_Version:" + cloudSite.getCloudVersion();
                logger.error("{} {} {} {} {}", MessageEnum.RA_CONFIG_EXC.toString(), error, "OpenStack",
                        ErrorCode.BusinessProcessError.getValue(), "Exception - setVersion");
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            }
        }
        // End Version check


        VduInstance vduInstance = null;
        CloudInfo cloudInfo = new CloudInfo(cloudSiteId, cloudOwner, tenantId, null);

        // Use the VduPlugin.
        VduPlugin vduPlugin = getVduPlugin(cloudSiteId, cloudOwner);

        long subStartTime1 = System.currentTimeMillis();
        try {
            vduInstance = vduPlugin.queryVdu(cloudInfo, vfModuleName);
        } catch (VduException me) {
            // Failed to query the VDU due to a plugin exception.
            String error = "Create VF Module: Query " + vfModuleName + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + me;
            logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName,
                    cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu", ErrorCode.DataError.getValue(),
                    "Exception - queryVdu", me);
            logger.debug(error);
            // Convert to a generic VnfException
            me.addContext("CreateVFModule");
            throw new VnfException(me);
        }

        // More precise handling/messaging if the Module already exists
        if (vduInstance != null && !(vduInstance.getStatus().getState() == VduStateType.NOTFOUND)) {
            VduStateType status = vduInstance.getStatus().getState();
            logger.debug("Found Existing VDU, status={}", status);

            if (status == VduStateType.INSTANTIATED) {
                if (failIfExists != null && failIfExists) {
                    // fail - it exists
                    String error = "Create VF: Deployment " + vfModuleName + " already exists in " + cloudOwner + "/"
                            + cloudSiteId + "/" + tenantId;
                    logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_VNF_ALREADY_EXIST.toString(),
                            vfModuleName, cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu",
                            ErrorCode.DataError.getValue(), "VF Module " + vfModuleName + " already exists");
                    logger.debug(error);
                    throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                            vduInstance.getVduInstanceId());
                } else {
                    // Found existing deployment and client has not requested "failIfExists".
                    // Populate the outputs from the existing deployment.

                    vnfId.value = vduInstance.getVduInstanceId();
                    return;
                }
            }
            // Check through various detailed error cases
            else if (status == VduStateType.INSTANTIATING || status == VduStateType.DELETING
                    || status == VduStateType.UPDATING) {
                // fail - it's in progress - return meaningful error
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; please wait for it to complete, or fix manually.";
                logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName,
                        cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu", ErrorCode.DataError.getValue(),
                        "VF Module " + vfModuleName + " already exists");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                        vduInstance.getVduInstanceId());
            } else if (status == VduStateType.FAILED) {
                // fail - it exists and is in a FAILED state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and is in FAILED state in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + "; requires manual intervention.";
                logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName,
                        cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu", ErrorCode.DataError.getValue(),
                        "VF Module " + vfModuleName + " already exists and is in FAILED state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                        vduInstance.getVduInstanceId());
            } else if (status == VduStateType.UNKNOWN) {
                // fail - it exists and is in a UNKNOWN state
                String error = "Create VF: Deployment " + vfModuleName + " already exists and has status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; requires manual intervention.";
                logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName,
                        cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu", ErrorCode.DataError.getValue(),
                        "VF Module " + vfModuleName + " already exists and is in " + status.toString() + " state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                        vduInstance.getVduInstanceId());
            } else {
                // Unexpected, since all known status values have been tested for
                String error = "Create VF: Deployment " + vfModuleName + " already exists with unexpected status "
                        + status.toString() + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                        + "; requires manual intervention.";
                logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_VNF_ALREADY_EXIST.toString(), vfModuleName,
                        cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu", ErrorCode.DataError.getValue(),
                        "VF Module " + vfModuleName + " already exists and is in an unknown state");
                logger.debug(error);
                throw new VnfAlreadyExists(vfModuleName, cloudSiteId, cloudOwner, tenantId,
                        vduInstance.getVduInstanceId());
            }
        }


        // Collect outputs from Base Modules and Volume Modules
        Map<String, Object> baseModuleOutputs = null;
        Map<String, Object> volumeGroupOutputs = null;

        // If a Volume Group was provided, query its outputs for inclusion in Module input parameters
        if (volumeGroupId != null) {
            long subStartTime2 = System.currentTimeMillis();
            VduInstance volumeVdu = null;
            try {
                volumeVdu = vduPlugin.queryVdu(cloudInfo, volumeGroupId);
            } catch (VduException me) {
                // Failed to query the Volume Group VDU due to a plugin exception.
                String error = "Create VF Module: Query Volume Group " + volumeGroupId + " in " + cloudOwner + "/"
                        + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), volumeGroupId,
                        cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu(volume)", ErrorCode.DataError.getValue(),
                        "Exception - queryVdu(volume)", me);
                logger.debug(error);
                // Convert to a generic VnfException
                me.addContext("CreateVFModule(QueryVolume)");
                throw new VnfException(me);
            }

            if (volumeVdu == null || volumeVdu.getStatus().getState() == VduStateType.NOTFOUND) {
                String error = "Create VFModule: Attached Volume Group DOES NOT EXIST " + volumeGroupId + " in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + " USER ERROR";
                logger.error("{} {} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), volumeGroupId,
                        cloudOwner, cloudSiteId, tenantId, error, "VDU", "queryVdu(volume)",
                        ErrorCode.BusinessProcessError.getValue(),
                        "Create VFModule: Attached Volume Group " + "DOES NOT EXIST");
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested volume group");
                volumeGroupOutputs = volumeVdu.getOutputs();
                this.sendMapToDebug(volumeGroupOutputs, "volumeGroupOutputs");
            }
        }

        // If this is an Add-On Module, query the Base Module outputs
        // Note: This will be performed whether or not the current request is for an
        // Add-On Volume Group or Add-On VF Module

        if (vfModule.getIsBase()) {
            logger.debug("This is a BASE Module request");
        } else {
            logger.debug("This is an Add-On Module request");

            // Add-On Modules should always have a Base, but just treat as a warning if not provided.
            // Add-on Volume requests may or may not specify a base.
            if (!isVolumeRequest && baseVfModuleId == null) {
                logger.debug("WARNING:  Add-on Module request - no Base Module ID provided");
            }

            if (baseVfModuleId != null) {
                long subStartTime2 = System.currentTimeMillis();
                VduInstance baseVdu = null;
                try {
                    baseVdu = vduPlugin.queryVdu(cloudInfo, baseVfModuleId);
                } catch (MsoException me) {
                    // Failed to query the Base VF Module due to a Vdu Plugin exception.
                    String error = "Create VF Module: Query Base " + baseVfModuleId + " in " + cloudOwner + "/"
                            + cloudSiteId + "/" + tenantId + ": " + me;
                    logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), baseVfModuleId,
                            cloudOwner, cloudSiteId, tenantId, "VDU", "queryVdu(Base)", ErrorCode.DataError.getValue(),
                            "Exception - queryVdu(Base)", me);
                    logger.debug(error);
                    // Convert to a generic VnfException
                    me.addContext("CreateVFModule(QueryBase)");
                    throw new VnfException(me);
                }

                if (baseVdu == null || baseVdu.getStatus().getState() == VduStateType.NOTFOUND) {
                    String error = "Create VFModule: Base Module DOES NOT EXIST " + baseVfModuleId + " in " + cloudOwner
                            + "/" + cloudSiteId + "/" + tenantId + " USER ERROR";
                    logger.error("{} {} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(),
                            baseVfModuleId, cloudOwner, cloudSiteId, tenantId, error, "VDU", "queryVdu(Base)",
                            ErrorCode.BusinessProcessError.getValue(), "Create VFModule: Base Module DOES NOT EXIST");
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                } else {
                    logger.debug("Found base module");
                    baseModuleOutputs = baseVdu.getOutputs();
                    this.sendMapToDebug(baseModuleOutputs, "baseModuleOutputs");
                }
            }
        }


        // NOTE: For this section, heatTemplate is used for all template artifacts.
        // In final implementation (post-POC), the template object would either be generic or there would
        // be a separate DB Table/Object for different sub-orchestrators.

        // NOTE: The template is fixed for the VF Module. The environment is part of the customization.

        HeatTemplate heatTemplate = null;
        HeatEnvironment heatEnvironment = null;
        if (isVolumeRequest) {
            heatTemplate = vfModule.getVolumeHeatTemplate();
            heatEnvironment = vfModuleCust.getVolumeHeatEnv();
        } else {
            heatTemplate = vfModule.getModuleHeatTemplate();
            heatEnvironment = vfModuleCust.getHeatEnvironment();
        }

        if (heatTemplate == null) {
            String error = "UpdateVF: No Heat Template ID defined in catalog database for " + vfModuleType
                    + ", modelCustomizationUuid=" + modelCustomizationUuid + ", vfModuleUuid=" + vfModule.getModelUUID()
                    + ", reqType=" + requestType;
            logger.error("{} {} {} {} {} {}", MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Template ID",
                    vfModuleType, "VNF", ErrorCode.DataError.getValue(), error);
            logger.debug(error);
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got HEAT Template from DB: {}", heatTemplate.getHeatTemplate());
        }

        if (heatEnvironment == null) {
            String error = "Update VNF: undefined Heat Environment. VF=" + vfModuleType + ", modelCustomizationUuid="
                    + modelCustomizationUuid + ", vfModuleUuid=" + vfModule.getModelUUID() + ", reqType=" + requestType;
            logger.error("{} {} {} {} {}", MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Environment ID",
                    "OpenStack", ErrorCode.DataError.getValue(), error);
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got Heat Environment from DB: {}", heatEnvironment.getEnvironment());
        }


        // Create the combined set of parameters from the incoming request, base-module outputs,
        // volume-module outputs. Also, convert all variables to their native object types.

        HashMap<String, Object> goldenInputs = new HashMap<String, Object>();
        List<String> extraInputs = new ArrayList<String>();

        Boolean skipInputChecks = false;

        if (skipInputChecks) {
            goldenInputs = new HashMap<String, Object>();
            for (String key : inputs.keySet()) {
                goldenInputs.put(key, inputs.get(key));
            }
        } else {
            // Build maps for the parameters (including aliases) to simplify checks
            HashMap<String, HeatTemplateParam> params = new HashMap<String, HeatTemplateParam>();

            Set<HeatTemplateParam> paramSet = heatTemplate.getParameters();
            logger.debug("paramSet has {} entries", paramSet.size());

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
                    } else {
                        logger.debug("Failed to convert input {}='{}' to {}", key, inputs.get(key),
                                params.get(key).getParamType());
                    }
                } else {
                    extraInputs.add(key);
                }
            }

            if (!extraInputs.isEmpty()) {
                // Add multicloud inputs
                for (String key : MsoMulticloudUtils.MULTICLOUD_INPUTS) {
                    if (extraInputs.contains(key)) {
                        goldenInputs.put(key, inputs.get(key));
                        extraInputs.remove(key);
                        if (extraInputs.isEmpty()) {
                            break;
                        }
                    }
                }
                logger.debug("Ignoring extra inputs: {}", extraInputs);
            }

            // Next add in Volume Group Outputs if there are any. Copy directly without conversions.
            if (volumeGroupOutputs != null && !volumeGroupOutputs.isEmpty()) {
                for (String key : volumeGroupOutputs.keySet()) {
                    if (params.containsKey(key) && !goldenInputs.containsKey(key)) {
                        goldenInputs.put(key, volumeGroupOutputs.get(key));
                    }
                }
            }

            // Next add in Base Module Outputs if there are any. Copy directly without conversions.
            if (baseModuleOutputs != null && !baseModuleOutputs.isEmpty()) {
                for (String key : baseModuleOutputs.keySet()) {
                    if (params.containsKey(key) && !goldenInputs.containsKey(key)) {
                        goldenInputs.put(key, baseModuleOutputs.get(key));
                    }
                }
            }

            // TODO: The model should support a mechanism to pre-assign default parameter values
            // per "customization" (i.e. usage) of a given module. In HEAT, this is specified by
            // an Environment file. There is not a general mechanism in the model to handle this.
            // For the general case, any such parameter/values can be added dynamically to the
            // inputs (only if not already specified).

            // Check that required parameters have been supplied from any of the sources
            String missingParams = null;
            boolean checkRequiredParameters = true;
            try {
                String propertyString = this.environment.getProperty(MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS);
                if ("false".equalsIgnoreCase(propertyString) || "n".equalsIgnoreCase(propertyString)) {
                    checkRequiredParameters = false;
                    logger.debug("CheckRequiredParameters is FALSE. Will still check but then skip blocking...{}",
                            MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS);
                }
            } catch (Exception e) {
                // No problem - default is true
                logger.debug("An exception occured trying to get property {}",
                        MsoVnfPluginAdapterImpl.CHECK_REQD_PARAMS, e);
            }

            // Do the actual parameter checking.
            // Include looking at the ENV file as a valid definition of a parameter value.
            // TODO: This handling of ENV applies only to Heat. A general mechanism to
            // support pre-set parameter/values does not yet exist in the model.
            //
            StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());
            MsoHeatEnvironmentEntry mhee = new MsoHeatEnvironmentEntry(sb);
            for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                if (parm.isRequired() && (!goldenInputs.containsKey(parm.getParamName()))) {
                    if (mhee != null && mhee.containsParameter(parm.getParamName())) {
                        logger.debug("Required parameter {} appears to be in environment - do not count as missing",
                                parm.getParamName());
                    } else {
                        logger.debug("adding to missing parameters list: {}", parm.getParamName());
                        if (missingParams == null) {
                            missingParams = parm.getParamName();
                        } else {
                            missingParams += "," + parm.getParamName();
                        }
                    }
                }
            }

            if (missingParams != null) {
                if (checkRequiredParameters) {
                    // Problem - missing one or more required parameters
                    String error = "Create VFModule: Missing Required inputs: " + missingParams;
                    logger.error("{} {} {} {} {}", MessageEnum.RA_MISSING_PARAM.toString(), missingParams, "VDU",
                            ErrorCode.DataError.getValue(), "Create VFModule: Missing Required inputs");
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                } else {
                    logger.debug("found missing parameters [{}] - but checkRequiredParameters is false - "
                            + "will not block", missingParams);
                }
            } else {
                logger.debug("No missing parameters found - ok to proceed");
            }

        } // NOTE: END PARAMETER CHECKING


        // Here we go... ready to deploy the VF Module.
        long instantiateVduStartTime = System.currentTimeMillis();
        if (backout == null)
            backout = true;

        try {
            // Construct the VDU Model structure to pass to the targeted VduPlugin
            VduModelInfo vduModel = null;
            if (!isVolumeRequest) {
                vduModel = vduMapper.mapVfModuleCustomizationToVdu(vfModuleCust);
            } else {
                vduModel = vduMapper.mapVfModuleCustVolumeToVdu(vfModuleCust);
            }

            // Invoke the VduPlugin to instantiate the VF Module
            vduInstance = vduPlugin.instantiateVdu(cloudInfo, vfModuleName, goldenInputs, vduModel, backout);

        } catch (VduException me) {
            // Failed to instantiate the VDU.
            me.addContext("CreateVFModule");
            String error = "Create VF Module " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + me;
            logger.error("{} {} {} {} {} {} {} {}", MessageEnum.RA_CREATE_VNF_ERR.toString(), vfModuleType, cloudOwner,
                    cloudSiteId, tenantId, "VDU", ErrorCode.DataError.getValue(), "MsoException - instantiateVdu", me);
            logger.debug(error);
            // Convert to a generic VnfException
            throw new VnfException(me);
        } catch (NullPointerException npe) {
            String error = "Create VFModule " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + npe;
            logger.error("{} {} {} {} {} {} {} {}", MessageEnum.RA_CREATE_VNF_ERR.toString(), vfModuleType, cloudOwner,
                    cloudSiteId, tenantId, "VDU", ErrorCode.DataError.getValue(),
                    "NullPointerException - instantiateVdu", npe);
            logger.debug(error);
            logger.debug("NULL POINTER EXCEPTION at vduPlugin.instantiateVdu", npe);
            throw new VnfException("NullPointerException during instantiateVdu");
        } catch (Exception e) {
            String error = "Create VFModule " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + e;
            logger.debug("Unhandled exception at vduPlugin.instantiateVdu", e);
            logger.debug(error);
            throw new VnfException("Exception during instantiateVdu: " + e.getMessage());
        }

        vnfId.value = vduInstance.getVduInstanceId();

        logger.debug("VF Module {} successfully created", vfModuleName);
        return;
    }


    public void deleteVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vfModuleId,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs) throws VnfException {

        logger.debug("Deleting VF Module {} in {}/{}/{}", vfModuleId, cloudOwner, cloudSiteId, tenantId);
        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis();

        // Capture the output parameters on a delete, so need to query first
        VduInstance vduInstance = null;
        CloudInfo cloudInfo = new CloudInfo(cloudSiteId, cloudOwner, tenantId, null);

        // Use the VduPlugin.
        VduPlugin vduPlugin = getVduPlugin(cloudSiteId, cloudOwner);

        try {
            vduInstance = vduPlugin.queryVdu(cloudInfo, vfModuleId);
        } catch (VduException e) {
            // Failed to query the VDU due to a plugin exception.
            // Convert to a generic VnfException
            e.addContext("QueryVFModule");
            String error = "Query VfModule (VDU): " + vfModuleId + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + e;
            logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleId, cloudOwner,
                    cloudSiteId, tenantId, "VDU", "QueryVFModule", ErrorCode.DataError.getValue(),
                    "Exception - queryVDU", e);
            logger.debug(error);
            throw new VnfException(e);
        }

        // call method which handles the conversion from Map<String,Object> to Map<String,String> for our expected
        // Object types
        outputs.value = convertMapStringObjectToStringString(vduInstance.getOutputs());

        // Use the VduPlugin to delete the VDU.
        // The possible outcomes of deleteVdu are
        // - a vnfInstance object with status of DELETED (success)
        // - a vnfInstance object with status of NOTFOUND (VDU did not exist, treat as success)
        // - a vnfInstance object with status of FAILED (error)
        // Also, VduException could be thrown.
        long subStartTime = System.currentTimeMillis();
        try {
            // TODO: Get an appropriate timeout value - require access to the model
            if (!vduInstance.getStatus().getState().equals(VduStateType.NOTFOUND)) {
                vduPlugin.deleteVdu(cloudInfo, vfModuleId, 5);
            }
        } catch (VduException me) {
            me.addContext("DeleteVfModule");
            // Convert to a generic VnfException
            String error =
                    "Delete VF: " + vfModuleId + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
            logger.error("{} {} {} {} {} {} {} {} {}", MessageEnum.RA_DELETE_VNF_ERR.toString(), vfModuleId, cloudOwner,
                    cloudSiteId, tenantId, "VDU", "DeleteVdu", ErrorCode.DataError.getValue(),
                    "Exception - DeleteVdu: " + me.getMessage());
            logger.debug(error);
            throw new VnfException(me);
        }

        // On success, nothing is returned.
        return;
    }

    // Update VF Module not yet implemented for generic VDU plug-in model.

    public void updateVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vnfType,
            String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId,
            String baseVfHeatStackId, String vfModuleStackId, String modelCustomizationUuid, Map<String, Object> inputs,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback)
            throws VnfException {
        // This operation is not currently supported for VduPlugin-orchestrated VF Modules.
        logger.debug("Update VF Module command attempted but not supported");
        throw new VnfException("UpdateVfModule:  Unsupported command", MsoExceptionCategory.USERDATA);
    }

    /*
     * Dynamic selection of a VduPlugin version. For initial tests, base on the "orchestrator" defined for the target
     * cloud. Should really be looking at the VNF Model (ochestration_mode) but we don't currently have access to that
     * in Query and Delete cases.
     */
    private VduPlugin getVduPlugin(String cloudSiteId, String cloudOwner) {
        Optional<CloudSite> cloudSiteOp = cloudConfig.getCloudSite(cloudSiteId);
        if (cloudSiteOp.isPresent()) {
            CloudSite cloudSite = cloudSiteOp.get();
            String orchestrator = cloudSite.getOrchestrator();

            if (orchestrator.equalsIgnoreCase("HEAT")) {
                return heatUtils;
            } else if (orchestrator.equalsIgnoreCase("MULTICLOUD")) {
                return multicloudUtils;
            } else {
                // Default if cloudSite record exists - return HEAT plugin - will fail later
                return heatUtils;
            }
        }
        // Default if no cloudSite record exists - return multicloud plugin
        return multicloudUtils;
    }
}
