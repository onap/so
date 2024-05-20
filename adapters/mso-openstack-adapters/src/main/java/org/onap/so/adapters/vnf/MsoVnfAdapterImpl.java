/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * Modifications Copyright (c) 2019 IBM
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
import jakarta.xml.ws.Holder;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.adapters.vnf.exceptions.VnfNotFound;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.exceptions.MsoHeatNotFoundException;
import org.onap.so.openstack.utils.MsoHeatEnvironmentEntry;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoHeatUtilsWithUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Transactional
public class MsoVnfAdapterImpl {

    @Autowired
    private CloudConfig cloudConfig;

    @Autowired
    private Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(MsoVnfAdapterImpl.class);


    private static final String CHECK_REQD_PARAMS = "org.onap.so.adapters.vnf.checkRequiredParameters";
    private static final String ADD_GET_FILES_ON_VOLUME_REQ = "org.onap.so.adapters.vnf.addGetFilesOnVolumeReq";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String OPENSTACK = "OpenStack";
    private static final String DELETE_VNF = "DeleteVNF";
    private static final String QUERY_STACK = "QueryStack";
    private static final String CREATE_VFM_MODULE = "CreateVFModule";
    private static final String USER_ERROR = "USER ERROR";
    private static final String VERSION_MIN = "VersionMin";
    private static final String VERSION_MAX = "VersionMax";
    private static final String RESOURCE_EXIST_STATUS_MESSAGE =
            "The %s was found to already exist, thus no new %s was created in the cloud via this request";
    private static final String RESOURCE_CREATED_STATUS_MESSAGE = "The new %s was successfully created in the cloud";
    private static final String RESOURCE_NOT_EXIST_STATUS_MESSAGE =
            "The %s was not found, thus no %s was deleted in the cloud via this request";
    private static final String RESOURCE_DELETED_STATUS_MESSAGE = "The %s was successfully deleted in the cloud";
    private static final String VF_MODULE = "vf module";
    private static final String VOLUME_GROUP = "volume group";


    @Autowired
    private VFModuleCustomizationRepository vfModuleCustomRepo;
    @Autowired
    private VnfResourceRepository vnfResourceRepo;
    @Autowired
    private MsoHeatUtilsWithUpdate heatU;
    @Autowired
    private MsoHeatUtils msoHeatUtils;

    /**
     * DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
     *
     * @see MsoVnfAdapterImpl#MsoVnfAdapterImpl(MsoPropertiesFactory, CloudConfigFactory)
     */
    public MsoVnfAdapterImpl() {
        // Do nothing
        // DO NOT use that constructor to instantiate this class, the msoPropertiesfactory will be NULL.
    }

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    public void healthCheck() {
        logger.debug("Health check call in VNF Adapter");
    }

    /**
     * This is the "Create VNF" web service implementation. It will create a new VNF of the requested type in the
     * specified cloud and tenant. The tenant must exist before this service is called.
     *
     * If a VNF with the same name already exists, this can be considered a success or failure, depending on the value
     * of the 'failIfExists' parameter.
     *
     * All VNF types will be defined in the MSO catalog. The caller must request one of these pre-defined types or an
     * error will be returned. Within the catalog, each VNF type references (among other things) a Heat template which
     * is used to deploy the required VNF artifacts (VMs, networks, etc.) to the cloud.
     *
     * Depending on the Heat template, a variable set of input parameters will be defined, some of which are required.
     * The caller is responsible to pass the necessary input data for the VNF or an error will be thrown.
     *
     * The method returns the vnfId (the canonical name), a Map of VNF output attributes, and a VnfRollback object. This
     * last object can be passed as-is to the rollbackVnf operation to undo everything that was created for the VNF.
     * This is useful if a VNF is successfully created but the orchestrator fails on a subsequent operation.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param cloudOwner cloud owner of the cloud region in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vnfType VNF type key, should match a VNF definition in catalog DB
     * @param vnfVersion VNF version key, should match a VNF definition in catalog DB
     * @param vnfName Name to be assigned to the new VNF
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered a success or failure
     * @param msoRequest Request tracking information for logs
     * @param vnfId Holder for output VNF Openstack ID
     * @param outputs Holder for Map of VNF outputs from heat (assigned IPs, etc)
     * @param rollback Holder for returning VnfRollback object
     */
    @Deprecated
    public void createVnf(String cloudSiteId, String cloudOwner, String tenantId, String vnfType, String vnfVersion,
            String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, Object> inputs,
            Boolean failIfExists, Boolean backout, Boolean enableBridge, MsoRequest msoRequest, Holder<String> vnfId,
            Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback) throws VnfException {
        // parameters used for multicloud adapter
        String genericVnfId = "";
        String vfModuleId = "";
        // Create a hook here to catch shortcut createVf requests:
        if (requestType != null && requestType.startsWith("VFMOD")) {
            logger.debug("Calling createVfModule from createVnf -- requestType={}", requestType);
            String newRequestType = requestType.substring(5);
            String vfVolGroupHeatStackId = "";
            String vfBaseHeatStackId = "";
            try {
                if (volumeGroupHeatStackId != null) {
                    vfVolGroupHeatStackId =
                            volumeGroupHeatStackId.substring(0, volumeGroupHeatStackId.lastIndexOf('|'));
                    vfBaseHeatStackId = volumeGroupHeatStackId.substring(volumeGroupHeatStackId.lastIndexOf('|') + 1);
                }
            } catch (Exception e) {
                // might be ok - both are just blank
                logger.debug("ERROR trying to parse the volumeGroupHeatStackId {}", volumeGroupHeatStackId, e);
            }
            this.createVfModule(cloudSiteId, cloudOwner, tenantId, vnfType, vnfVersion, genericVnfId, vnfName,
                    vfModuleId, newRequestType, vfVolGroupHeatStackId, vfBaseHeatStackId, null, inputs, failIfExists,
                    backout, enableBridge, msoRequest, vnfId);
            return;
        }
        // createVf will know if the requestType starts with "X" that it's the "old" way
        StringBuilder newRequestTypeSb = new StringBuilder("X");
        String vfVolGroupHeatStackId = "";
        String vfBaseHeatStackId = "";
        if (requestType != null) {
            newRequestTypeSb.append(requestType);
        }
        this.createVfModule(cloudSiteId, cloudOwner, tenantId, vnfType, vnfVersion, genericVnfId, vnfName, vfModuleId,
                newRequestTypeSb.toString(), vfVolGroupHeatStackId, vfBaseHeatStackId, null, inputs, failIfExists,
                backout, enableBridge, msoRequest, vnfId);
        return;
        // End createVf shortcut
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation. A rollback object is returned to the
     * client in a successful creation response. The client can pass that object as-is back to the rollbackVnf operation
     * to undo the creation.
     */
    public void rollbackVnf(VnfRollback rollback) throws VnfException {
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            logger.info(MessageEnum.RA_ROLLBACK_NULL.toString(), OPENSTACK, "rollbackVnf");
            return;
        }

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudSiteId();
        String cloudOwner = rollback.getCloudOwner();
        String tenantId = rollback.getTenantId();
        String vnfId = rollback.getVnfId();

        logger.debug("Rolling Back VNF {} in {}", vnfId, cloudOwner + "/" + cloudSiteId + "/" + tenantId);

        // Use the MsoHeatUtils to delete the stack. Set the polling flag to true.
        // The possible outcomes of deleteStack are a StackInfo object with status
        // of NOTFOUND (on success) or FAILED (on error). Also, MsoOpenstackException
        // could be thrown.
        try {
            msoHeatUtils.deleteStack(tenantId, cloudOwner, cloudSiteId, vnfId, true, 118);
        } catch (MsoException me) {
            // Failed to rollback the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext("RollbackVNF");
            String error =
                    "Rollback VNF: " + vnfId + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
            logger.error(LoggingAnchor.NINE, MessageEnum.RA_DELETE_VNF_ERR.toString(), vnfId, cloudOwner, cloudSiteId,
                    tenantId, OPENSTACK, "DeleteStack", ErrorCode.DataError.getValue(), "Exception - DeleteStack", me);
            logger.debug(error);
            throw new VnfException(me);
        }
        return;
    }

    public Map<String, String> copyStringOutputs(Map<String, Object> stackOutputs) {
        Map<String, String> stringOutputs = new HashMap<>();
        for (Map.Entry<String, Object> entry : stackOutputs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                stringOutputs.put(key, value.toString());
            } catch (Exception e) {
                StringBuilder msg = new StringBuilder("Unable to add " + key + " to outputs");
                if (value instanceof Integer) { // nothing to add to the message
                } else if (value instanceof JsonNode) {
                    msg.append(" - exception converting JsonNode");
                } else if (value instanceof java.util.LinkedHashMap) {
                    msg.append(" exception converting LinkedHashMap");
                } else {
                    msg.append(" - unable to call .toString() " + e.getMessage());
                }
                logger.debug(msg.toString(), e);
            }
        }
        return stringOutputs;
    }

    private Map<String, Object> copyStringInputs(Map<String, Object> stringInputs) {
        return new HashMap<>(stringInputs);
    }

    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (JsonParseException jpe) {
            logger.debug("Error converting json to string: {}", jpe.getMessage(), jpe);
        } catch (Exception e) {
            logger.debug("Error converting json to string: {}", e.getMessage(), e);
        }
        return "[Error converting json to string]";
    }

    private Map<String, String> convertMapStringObjectToStringString(Map<String, Object> objectMap) {
        if (objectMap == null) {
            return null;
        }
        Map<String, String> stringMap = new HashMap<>();
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
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for JsonNode " + key, e);
                        // okay in this instance - only string values (fqdn) are expected to be needed
                    }
                } else if (obj instanceof java.util.LinkedHashMap) {
                    logger.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                    try {
                        String str = JSON_MAPPER.writeValueAsString(obj);
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for LinkedHashMap " + key, e);
                    }
                } else if (obj instanceof Integer) {
                    try {
                        String str = "" + obj;
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug("DANGER WILL ROBINSON: unable to convert value for Integer " + key, e);
                    }
                } else {
                    try {
                        String str = obj.toString();
                        stringMap.put(key, str);
                    } catch (Exception e) {
                        logger.debug(
                                "DANGER WILL ROBINSON: unable to convert value " + key + " (" + e.getMessage() + ")",
                                e);
                    }
                }
            }
        }

        return stringMap;
    }

    // TODO remove polling
    public void createVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vnfType,
            String vnfVersion, String genericVnfName, String vnfName, String vfModuleId, String requestType,
            String volumeGroupHeatStackId, String baseVfHeatStackId, String modelCustomizationUuid,
            Map<String, Object> inputs, Boolean failIfExists, Boolean backout, Boolean enableBridge,
            MsoRequest msoRequest, Holder<String> stackId) throws VnfException {
        String vfModuleName = vnfName;
        String vfModuleType = vnfType;
        String vfVersion = vnfVersion;
        String mcu = modelCustomizationUuid;
        boolean useMCUuid = false;
        if (mcu != null && !mcu.isEmpty()) {
            if ("null".equalsIgnoreCase(mcu)) {
                logger.debug("modelCustomizationUuid: passed in as the string 'null' - will ignore: "
                        + modelCustomizationUuid);
                useMCUuid = false;
                mcu = "";
            } else {
                logger.debug("Found modelCustomizationUuid! Will use that: {}", mcu);
                useMCUuid = true;
            }
        }

        String requestTypeString = "";
        if (requestType != null && !"".equals(requestType)) {
            requestTypeString = requestType;
        }
        String nestedStackId = null;
        if (volumeGroupHeatStackId != null && !"".equals(volumeGroupHeatStackId)
                && !"null".equalsIgnoreCase(volumeGroupHeatStackId)) {
            nestedStackId = volumeGroupHeatStackId;
        }
        String nestedBaseStackId = null;
        if (baseVfHeatStackId != null && !"".equals(baseVfHeatStackId) && !"null".equalsIgnoreCase(baseVfHeatStackId)) {
            nestedBaseStackId = baseVfHeatStackId;
        }

        // This method will also handle doing things the "old" way - i.e., just orchestrate a VNF
        boolean oldWay = false;
        if (requestTypeString.startsWith("X")) {
            oldWay = true;
            logger.debug("orchestrating a VNF - *NOT* a module!");
            requestTypeString = requestTypeString.substring(1);
        }

        // let's parse out the request type we're being sent
        boolean isBaseRequest = false;
        boolean isVolumeRequest = false;
        if (requestTypeString.startsWith("VOLUME")) {
            isVolumeRequest = true;
        }

        logger.debug("requestTypeString = " + requestTypeString + ", nestedStackId = " + nestedStackId
                + ", nestedBaseStackId = " + nestedBaseStackId);

        // handle a nestedStackId if sent- this one would be for the volume - so applies to both Vf and Vnf
        StackInfo nestedHeatStack = null;
        Map<String, Object> nestedVolumeOutputs = null;
        if (nestedStackId != null) {
            try {
                logger.debug("Querying for nestedStackId = {}", nestedStackId);
                nestedHeatStack = msoHeatUtils.queryStack(cloudSiteId, cloudOwner, tenantId, nestedStackId);
            } catch (MsoException me) {
                // Failed to query the Stack due to an openstack exception.
                // Convert to a generic VnfException
                me.addContext(CREATE_VFM_MODULE);
                String error = "Create VFModule: Attached heatStack ID Query " + nestedStackId + " in " + cloudOwner
                        + "/" + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.BusinessProcessError.getValue(),
                        "MsoException trying to query nested stack", me);
                logger.debug("ERROR trying to query nested stack= {}", error);
                throw new VnfException(me);
            }
            if (nestedHeatStack == null || nestedHeatStack.getStatus() == HeatStatus.NOTFOUND) {
                String error = "Create VFModule: Attached heatStack ID DOES NOT EXIST " + nestedStackId + " in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + " " + USER_ERROR;
                logger.error(LoggingAnchor.TEN, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, error, OPENSTACK, QUERY_STACK, ErrorCode.BusinessProcessError.getValue(),
                        "Create VFModule: Attached heatStack ID " + "DOES NOT EXIST");
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested volume heat stack - copying values to inputs *later*");
                nestedVolumeOutputs = nestedHeatStack.getOutputs();
            }
        }

        // handle a nestedBaseStackId if sent- this is the stack ID of the base. Should be null for VNF requests
        StackInfo nestedBaseHeatStack = null;
        Map<String, Object> baseStackOutputs = null;
        if (nestedBaseStackId != null) {
            try {
                logger.debug("Querying for nestedBaseStackId = {}", nestedBaseStackId);
                nestedBaseHeatStack = msoHeatUtils.queryStack(cloudSiteId, cloudOwner, tenantId, nestedBaseStackId);
            } catch (MsoException me) {
                // Failed to query the Stack due to an openstack exception.
                // Convert to a generic VnfException
                me.addContext(CREATE_VFM_MODULE);
                String error = "Create VFModule: Attached baseHeatStack ID Query " + nestedBaseStackId + " in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.BusinessProcessError.getValue(),
                        "MsoException trying to query nested base stack", me);
                logger.debug("ERROR trying to query nested base stack= {}", error);
                throw new VnfException(me);
            }
            if (nestedBaseHeatStack == null || nestedBaseHeatStack.getStatus() == HeatStatus.NOTFOUND) {
                String error = "Create VFModule: Attached base heatStack ID DOES NOT EXIST " + nestedBaseStackId
                        + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + " " + USER_ERROR;
                logger.error(LoggingAnchor.TEN, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, error, OPENSTACK, QUERY_STACK, ErrorCode.BusinessProcessError.getValue(),
                        "Create VFModule: Attached base heatStack ID DOES NOT EXIST");
                logger.debug("Exception occurred", error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested base heat stack - these values will be copied to inputs *later*");
                baseStackOutputs = nestedBaseHeatStack.getOutputs();
            }
        }

        try {
            VfModule vf = null;
            VnfResource vnfResource = null;
            VfModuleCustomization vfmc = null;
            if (useMCUuid) {
                vfmc = vfModuleCustomRepo.findFirstByModelCustomizationUUIDOrderByCreatedDesc(mcu);
                if (vfmc != null)
                    vf = vfmc.getVfModule();
                else
                    vf = null;

                // this will be the new way going forward. We find the vf by mcu - otherwise, code is the same.
                if (vf == null) {
                    logger.debug("Unable to find vfModuleCust with modelCustomizationUuid={}", mcu);
                    String error =
                            "Create vfModule error: Unable to find vfModuleCust with modelCustomizationUuid=" + mcu;
                    logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                            "VF Module ModelCustomizationUuid", modelCustomizationUuid, OPENSTACK,
                            ErrorCode.DataError.getValue(),
                            "Create VF Module: Unable to find vfModule with " + "modelCustomizationUuid=" + mcu);
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                } else {
                    logger.trace("Found vfModuleCust entry {}", vfmc.toString());
                }
                if (vf.getIsBase()) {
                    isBaseRequest = true;
                    logger.debug("This is a BASE VF request!");
                } else {
                    logger.debug("This is *not* a BASE VF request!");
                    if (!isVolumeRequest && nestedBaseStackId == null) {
                        logger.debug(
                                "DANGER WILL ROBINSON! This is unexpected - no nestedBaseStackId with this non-base request");
                    }
                }
            }

            else { // This is to support gamma only - get info from vnf_resource table
                if (vfVersion != null && !vfVersion.isEmpty()) {
                    vnfResource = vnfResourceRepo.findByModelNameAndModelVersion(vnfType, vnfVersion);
                } else {
                    vnfResource = vnfResourceRepo.findByModelName(vnfType);
                }
                if (vnfResource == null) {
                    String error = "Create VNF: Unknown VNF Type: " + vnfType;
                    logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "VNF Type", vnfType,
                            OPENSTACK, ErrorCode.DataError.getValue(), "Create VNF: Unknown VNF Type");
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                }
                logger.debug("Got VNF module definition from Catalog: {}", vnfResource.toString());
            }
            // By here - we have either a vf or vnfResource

            // Add version check
            // First - see if it's in the VnfResource record
            // if we have a vf Module - then we have to query to get the VnfResource record.
            if (!oldWay) {
                if (vf != null) {
                    vnfResource = vf.getVnfResources();
                }
                if (vnfResource == null) {
                    logger.debug("Unable to find vnfResource will not error for now...");
                }
            }
            String minVersionVnf = null;
            String maxVersionVnf = null;
            if (vnfResource != null) {
                try {
                    minVersionVnf = vnfResource.getAicVersionMin();
                    maxVersionVnf = vnfResource.getAicVersionMax();
                } catch (Exception e) {
                    logger.debug("Unable to pull min/max version for this VNF Resource entry", e);
                    minVersionVnf = null;
                    maxVersionVnf = null;
                }
                if (minVersionVnf != null && "".equals(minVersionVnf)) {
                    minVersionVnf = null;
                }
                if (maxVersionVnf != null && "".equals(maxVersionVnf)) {
                    maxVersionVnf = null;
                }
            }
            if (minVersionVnf != null && maxVersionVnf != null) {
                MavenLikeVersioning aicV = new MavenLikeVersioning();

                // double check
                if (this.cloudConfig != null) {
                    Optional<CloudSite> cloudSiteOpt = this.cloudConfig.getCloudSite(cloudSiteId);
                    if (cloudSiteOpt.isPresent()) {
                        aicV.setVersion(cloudSiteOpt.get().getCloudVersion());
                        // Add code to handle unexpected values in here
                        boolean moreThanMin = true;
                        boolean equalToMin = true;
                        boolean moreThanMax = true;
                        boolean equalToMax = true;
                        boolean doNotTest = false;
                        try {
                            moreThanMin = aicV.isMoreRecentThan(minVersionVnf);
                            equalToMin = aicV.isTheSameVersion(minVersionVnf);
                            moreThanMax = aicV.isMoreRecentThan(maxVersionVnf);
                            equalToMax = aicV.isTheSameVersion(maxVersionVnf);
                        } catch (Exception e) {
                            logger.debug(
                                    "An exception occurred while trying to test Cloud Version {} - will default to not check",
                                    e.getMessage(), e);
                            doNotTest = true;
                        }
                        if (!doNotTest) {
                            if ((moreThanMin || equalToMin) // aic >= min
                                    && (equalToMax || !(moreThanMax))) { // aic <= max
                                logger.debug("VNF Resource " + vnfResource.getModelName() + ", ModelUuid="
                                        + vnfResource.getModelUUID() + " " + VERSION_MIN + " =" + minVersionVnf + " "
                                        + VERSION_MAX + " :" + maxVersionVnf + " supported on Cloud: " + cloudSiteId
                                        + " with AIC_Version:" + cloudSiteOpt.get().getCloudVersion());
                            } else {
                                // ERROR
                                String error = "VNF Resource type: " + vnfResource.getModelName() + ", ModelUuid="
                                        + vnfResource.getModelUUID() + " " + VERSION_MIN + " =" + minVersionVnf + " "
                                        + VERSION_MAX + " :" + maxVersionVnf + " NOT supported on Cloud: " + cloudSiteId
                                        + " with AIC_Version:" + cloudSiteOpt.get().getCloudVersion();
                                logger.error(LoggingAnchor.FIVE, MessageEnum.RA_CONFIG_EXC.toString(), error, OPENSTACK,
                                        ErrorCode.BusinessProcessError.getValue(), "Exception - setVersion");
                                logger.debug(error);
                                throw new VnfException(error, MsoExceptionCategory.USERDATA);
                            }
                        } else {
                            logger.debug("bypassing testing Cloud version...");
                        }
                    } // let this error out downstream to avoid introducing uncertainty at this stage
                } else {
                    logger.debug("cloudConfig is NULL - cannot check cloud site version");
                }
            }

            // By the time we get here - heatTemplateId and heatEnvtId should be populated (or null)
            HeatTemplate heatTemplate = null;
            HeatEnvironment heatEnvironment = null;
            if (oldWay) {
                // This will handle old Gamma BrocadeVCE VNF
                heatTemplate = vnfResource.getHeatTemplates();
            } else {
                if (vf != null) {
                    if (isVolumeRequest) {
                        heatTemplate = vf.getVolumeHeatTemplate();
                        heatEnvironment = vfmc.getVolumeHeatEnv();
                    } else {
                        heatTemplate = vf.getModuleHeatTemplate();
                        heatEnvironment = vfmc.getHeatEnvironment();
                    }
                }
            }

            if (heatTemplate == null) {
                String error = "UpdateVF: No Heat Template ID defined in catalog database for " + vfModuleType
                        + ", modelCustomizationUuid=" + mcu + ", vfModuleUuid="
                        + (vf != null ? vf.getModelUUID() : "null") + ", vnfResourceModelUuid="
                        + vnfResource.getModelUUID() + ", reqType=" + requestTypeString;
                logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Template " + "ID",
                        vfModuleType, OPENSTACK, ErrorCode.DataError.getValue(), error);
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.INTERNAL);
            } else {
                logger.debug("Got HEAT Template from DB: {}", heatTemplate.getHeatTemplate());
            }

            if (oldWay) {
                // This will handle old Gamma BrocadeVCE VNF
                logger.debug("No environment parameter found for this Type " + vfModuleType);
            } else {
                if (heatEnvironment == null) {
                    String error = "Update VNF: undefined Heat Environment. VF=" + vfModuleType
                            + ", modelCustomizationUuid=" + mcu + ", vfModuleUuid="
                            + (vf != null ? vf.getModelUUID() : "null") + ", vnfResourceModelUuid="
                            + vnfResource.getModelUUID() + ", reqType=" + requestTypeString;
                    logger.error(LoggingAnchor.FIVE, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(),
                            "Heat " + "Environment ID", OPENSTACK, ErrorCode.DataError.getValue(), error);
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.INTERNAL);
                } else {
                    logger.debug("Got Heat Environment from DB: {}", heatEnvironment.getEnvironment());
                }
            }

            logger.debug("In MsoVnfAdapterImpl, about to call db.getNestedTemplates avec templateId="
                    + heatTemplate.getArtifactUuid());


            List<HeatTemplate> nestedTemplates = heatTemplate.getChildTemplates();
            Map<String, Object> nestedTemplatesChecked = new HashMap<>();
            if (nestedTemplates != null && !nestedTemplates.isEmpty()) {
                // for debugging print them out
                logger.debug("Contents of nestedTemplates - to be added to files: on stack:");
                for (HeatTemplate entry : nestedTemplates) {
                    nestedTemplatesChecked.put(entry.getTemplateName(), entry.getTemplateBody());
                    logger.debug("Adding Nested Template", entry.getTemplateName());
                }
            } else {
                logger.debug("No nested templates found - nothing to do here");
                nestedTemplatesChecked = null;
            }

            // Also add the files: for any get_files associated with this vnf_resource_id
            // *if* there are any
            List<HeatFiles> heatFiles = null;

            Map<String, Object> heatFilesObjects = new HashMap<>();

            // Add ability to turn on adding get_files with volume requests (by property).
            boolean addGetFilesOnVolumeReq = false;
            try {
                String propertyString = this.environment.getProperty(MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ);
                if ("true".equalsIgnoreCase(propertyString) || "y".equalsIgnoreCase(propertyString)) {
                    addGetFilesOnVolumeReq = true;
                    logger.debug("AddGetFilesOnVolumeReq - setting to true! {}", propertyString);
                }
            } catch (Exception e) {
                logger.debug("An error occured trying to get property " + MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ
                        + " - default to false", e);
            }

            if (!isVolumeRequest || addGetFilesOnVolumeReq) {
                if (oldWay) {
                    logger.debug("In MsoVnfAdapterImpl createVfModule, this should not happen, no heat files!");
                } else {
                    // now use VF_MODULE_TO_HEAT_FILES table
                    logger.debug(
                            "In MsoVnfAdapterImpl createVfModule, about to call db.getHeatFilesForVfModule avec vfModuleId="
                                    + vf.getModelUUID());
                    heatFiles = vf.getHeatFiles();
                }
                if (heatFiles != null && !heatFiles.isEmpty()) {
                    // add these to stack - to be done in createStack
                    // here, we will map them to Map<String, Object> from
                    // Map<String, HeatFiles>
                    // this will match the nested templates format
                    logger.debug("Contents of heatFiles - to be added to files: on stack");

                    for (HeatFiles heatfile : heatFiles) {
                        logger.debug(heatfile.getFileName() + " -> " + heatfile.getFileBody());
                        heatFilesObjects.put(heatfile.getFileName(), heatfile.getFileBody());
                    }
                } else {
                    logger.debug("No heat files found -nothing to do here");
                    heatFilesObjects = null;
                }
            }

            // Check that required parameters have been supplied
            String missingParams = null;
            List<String> paramList = new ArrayList<>();

            // consult the PARAM_ALIAS field to see if we've been
            // supplied an alias. Only check if we don't find it initially.
            // don't flag missing parameters if there's an environment - because they might be there.
            // And also new - add parameter to turn off checking all together if we find we're blocking orders we
            // shouldn't
            boolean checkRequiredParameters = true;
            try {
                String propertyString = this.environment.getProperty(MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
                if ("false".equalsIgnoreCase(propertyString) || "n".equalsIgnoreCase(propertyString)) {
                    checkRequiredParameters = false;
                    logger.debug("CheckRequiredParameters is FALSE. Will still check but then skip blocking..."
                            + MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
                }
            } catch (Exception e) {
                // No problem - default is true
                logger.debug("An exception occured trying to get property {}", MsoVnfAdapterImpl.CHECK_REQD_PARAMS, e);
            }
            // Part 1: parse envt entries to see if reqd parameter is there (before used a simple grep
            // Part 2: only submit to openstack the parameters in the envt that are in the heat template
            // Note this also removes any comments
            MsoHeatEnvironmentEntry mhee = null;
            if (heatEnvironment != null && heatEnvironment.getEnvironment() != null
                    && heatEnvironment.getEnvironment().contains("parameters:")) {

                StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());

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
                logger.debug(sb2.toString());
            } else {
                logger.debug("NO ENVIRONMENT for this entry");
            }
            // all variables converted to their native object types
            Map<String, Object> goldenInputs = null;

            ArrayList<String> parameterNames = new ArrayList<>();
            HashMap<String, String> aliasToParam = new HashMap<>();
            StringBuilder sb = new StringBuilder("\nTemplate Parameters:\n");
            int cntr = 0;
            try {
                for (HeatTemplateParam htp : heatTemplate.getParameters()) {
                    sb.append("param[" + cntr++ + "]=" + htp.getParamName());
                    parameterNames.add(htp.getParamName());
                    if (htp.getParamAlias() != null && !"".equals(htp.getParamAlias())) {
                        aliasToParam.put(htp.getParamAlias(), htp.getParamName());
                        sb.append(" ** (alias=" + htp.getParamAlias() + ")");
                    }
                    sb.append("\n");
                }
                logger.debug(sb.toString());
            } catch (Exception e) {
                logger.debug("??An exception occurred trying to go through Parameter Names {}", e.getMessage(), e);
            }
            // Step 1 - convert what we got as inputs (Map<String, String>) to a
            // Map<String, Object> - where the object matches the param type identified in the template
            // This will also not copy over params that aren't identified in the template
            goldenInputs = msoHeatUtils.convertInputMap(inputs, heatTemplate);
            // Step 2 - now simply add the outputs as we received them - no need to convert to string
            logger.debug("Now add in the base stack outputs if applicable");
            msoHeatUtils.copyBaseOutputsToInputs(goldenInputs, baseStackOutputs, parameterNames, aliasToParam);
            // Step 3 - add the volume inputs if any
            logger.debug("Now add in the volume stack outputs if applicable");
            msoHeatUtils.copyBaseOutputsToInputs(goldenInputs, nestedVolumeOutputs, parameterNames, aliasToParam);

            for (HeatTemplateParam parm : heatTemplate.getParameters()) {
                logger.debug("Parameter:'" + parm.getParamName() + "', isRequired=" + parm.isRequired() + ", alias="
                        + parm.getParamAlias());

                if (parm.isRequired() && (goldenInputs == null || !goldenInputs.containsKey(parm.getParamName()))) {
                    // The check for an alias was moved to the method in MsoHeatUtils - when we converted the
                    // Map<String, String> to Map<String, Object>
                    logger.debug("**Parameter " + parm.getParamName() + " is required and not in the inputs...check "
                            + "environment");
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
                paramList.add(parm.getParamName());
            }
            if (missingParams != null) {
                if (checkRequiredParameters) {
                    // Problem - missing one or more required parameters
                    String error = "Create VFModule: Missing Required inputs: " + missingParams;
                    logger.error(LoggingAnchor.FIVE, MessageEnum.RA_MISSING_PARAM.toString(), missingParams, OPENSTACK,
                            ErrorCode.DataError.getValue(), "Create VFModule: Missing Required inputs");
                    logger.debug(error);
                    throw new VnfException(error, MsoExceptionCategory.USERDATA);
                } else {
                    logger.debug("found missing parameters - but checkRequiredParameters is false - will not block");
                }
            } else {
                logger.debug("No missing parameters found - ok to proceed");
            }
            // We can now remove the recreating of the ENV with only legit params - that check is done for us,
            // and it causes problems with json that has arrays
            String newEnvironmentString = null;
            if (mhee != null) {
                newEnvironmentString = mhee.getRawEntry().toString();
            }

            // "Fix" the template if it has CR/LF (getting this from Oracle)
            String template = heatTemplate.getHeatTemplate();
            template = template.replaceAll("\r\n", "\n");

            // Have the tenant. Now deploy the stack itself
            // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
            // because we already checked for those.

            StackInfo heatStack = null;
            try {
                if (backout == null) {
                    backout = true;
                }
                if (failIfExists == null) {
                    failIfExists = false;
                }
                if (msoHeatUtils != null) {
                    heatStack = msoHeatUtils.createStack(cloudSiteId, cloudOwner, tenantId, vfModuleName, null,
                            template, goldenInputs, false, heatTemplate.getTimeoutMinutes(), newEnvironmentString,
                            nestedTemplatesChecked, heatFilesObjects, backout.booleanValue(), failIfExists);
                    String resource = VF_MODULE;
                    if (isVolumeRequest) {
                        resource = VOLUME_GROUP;
                    }
                    if (msoRequest.getRequestId() != null) {
                        msoHeatUtils.updateResourceStatus(msoRequest.getRequestId(),
                                heatStack.isOperationPerformed()
                                        ? String.format(RESOURCE_CREATED_STATUS_MESSAGE, resource)
                                        : String.format(RESOURCE_EXIST_STATUS_MESSAGE, resource, resource));
                    }
                } else {
                    throw new MsoHeatNotFoundException();
                }
            } catch (MsoException me) {
                me.addContext(CREATE_VFM_MODULE);
                logger.error("Error creating Stack", me);
                throw new VnfException(me);
            } catch (NullPointerException npe) {
                logger.error("Error creating Stack", npe);
                throw new VnfException("NullPointerException during heat.createStack");
            } catch (Exception e) {
                logger.error("Error creating Stack", e);
                throw new VnfException("Exception during heat.createStack! " + e.getMessage());
            }

            stackId.value = heatStack.getCanonicalName();
            logger.debug("VF Module {} successfully created", vfModuleName);
        } catch (Exception e) {
            logger.debug("unhandled exception in create VF", e);
            throw new VnfException("Exception during create VF " + e.getMessage());
        }
    }

    public void deleteVfModule(String cloudSiteId, String cloudOwner, String tenantId, String stackId,
            String modelCustomizationUuid, MsoRequest msoRequest, Holder<Map<String, String>> outputs)
            throws VnfException {
        Map<String, Object> stackOutputs;
        try {
            stackOutputs = msoHeatUtils.queryStackForOutputs(cloudSiteId, cloudOwner, tenantId, stackId);
        } catch (MsoException me) {
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext("DeleteVFModule");
            String error = "Delete VFModule: Query to get outputs: " + stackId + " in " + cloudOwner + "/" + cloudSiteId
                    + "/" + tenantId + ": " + me;
            logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), stackId, cloudOwner, cloudSiteId,
                    tenantId, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(), "Exception - " + QUERY_STACK, me);
            logger.debug(error);
            throw new VnfException(me);
        }
        // call method which handles the conversion from Map<String,Object> to Map<String,String> for our expected
        // Object types
        outputs.value = this.convertMapStringObjectToStringString(stackOutputs);
        int timeoutMinutes = 118;
        if (modelCustomizationUuid != null) {
            timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(modelCustomizationUuid, false);
        }

        try {
            StackInfo currentStack =
                    msoHeatUtils.deleteStack(tenantId, cloudOwner, cloudSiteId, stackId, false, timeoutMinutes);
            if (currentStack != null && outputs != null && outputs.value != null) {
                logger.debug("Adding canonical stack id to outputs " + currentStack.getCanonicalName());
                outputs.value.put("canonicalStackId", currentStack.getCanonicalName());
            }
            msoHeatUtils.updateResourceStatus(msoRequest.getRequestId(),
                    currentStack.isOperationPerformed() ? String.format(RESOURCE_DELETED_STATUS_MESSAGE, VF_MODULE)
                            : String.format(RESOURCE_NOT_EXIST_STATUS_MESSAGE, VF_MODULE, VF_MODULE));
        } catch (MsoException me) {
            me.addContext(DELETE_VNF);
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            String error =
                    "Delete VF: " + stackId + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
            logger.error(LoggingAnchor.NINE, MessageEnum.RA_DELETE_VNF_ERR.toString(), stackId, cloudOwner, cloudSiteId,
                    tenantId, OPENSTACK, "DeleteStack", ErrorCode.DataError.getValue(), "Exception - deleteStack", me);
            logger.error(error);
            throw new VnfException(me);
        }
    }

    public void updateVfModule(String cloudSiteId, String cloudOwner, String tenantId, String vnfType,
            String vnfVersion, String vnfName, String requestType, String volumeGroupHeatStackId,
            String baseVfHeatStackId, String vfModuleStackId, String modelCustomizationUuid, Map<String, Object> inputs,
            MsoRequest msoRequest, Holder<Map<String, String>> outputs, Holder<VnfRollback> rollback)
            throws VnfException {
        String vfModuleName = vnfName;
        String vfModuleType = vnfType;

        StringBuilder sbInit = new StringBuilder();
        sbInit.append("updateVfModule: \n");
        sbInit.append("cloudOwner=" + cloudOwner + "\n");
        sbInit.append("cloudSiteId=" + cloudSiteId + "\n");
        sbInit.append("tenantId=" + tenantId + "\n");
        sbInit.append("vnfType=" + vnfType + "\n");
        sbInit.append("vnfVersion=" + vnfVersion + "\n");
        sbInit.append("vnfName=" + vnfName + "\n");
        sbInit.append("requestType=" + requestType + "\n");
        sbInit.append("volumeGroupHeatStackId=" + volumeGroupHeatStackId + "\n");
        sbInit.append("baseVfHeatStackId=" + baseVfHeatStackId + "\n");
        sbInit.append("vfModuleStackId=" + vfModuleStackId + "\n");
        sbInit.append("modelCustomizationUuid=" + modelCustomizationUuid + "\n");
        logger.debug(sbInit.toString());

        String mcu = modelCustomizationUuid;
        boolean useMCUuid = false;
        if (mcu != null && !mcu.isEmpty()) {
            if ("null".equalsIgnoreCase(mcu)) {
                logger.debug("modelCustomizationUuid: passed in as the string 'null' - will ignore: {}",
                        modelCustomizationUuid);
                useMCUuid = false;
                mcu = "";
            } else {
                logger.debug("Found modelCustomizationUuid! Will use that: {}", mcu);
                useMCUuid = true;
            }
        }

        String requestTypeString = "";
        if (requestType != null && !"".equals(requestType)) {
            requestTypeString = requestType;
        }

        String nestedStackId = null;
        if (volumeGroupHeatStackId != null && !"".equals(volumeGroupHeatStackId)
                && !"null".equalsIgnoreCase(volumeGroupHeatStackId)) {
            nestedStackId = volumeGroupHeatStackId;
        }
        String nestedBaseStackId = null;
        if (baseVfHeatStackId != null && !"".equals(baseVfHeatStackId) && !"null".equalsIgnoreCase(baseVfHeatStackId)) {
            nestedBaseStackId = baseVfHeatStackId;
        }

        if (inputs == null) {
            // Create an empty set of inputs
            inputs = new HashMap<>();
            logger.debug("inputs == null - setting to empty");
        }

        boolean isBaseRequest = false;
        boolean isVolumeRequest = false;
        if (requestTypeString.startsWith("VOLUME")) {
            isVolumeRequest = true;
        }
        if ((vfModuleName == null || "".equals(vfModuleName.trim())) && vfModuleStackId != null) {
            vfModuleName = this.getVfModuleNameFromModuleStackId(vfModuleStackId);
        }

        logger.debug("Updating VFModule: " + vfModuleName + " of type " + vfModuleType + "in " + cloudOwner + "/"
                + cloudSiteId + "/" + tenantId);
        logger.debug("requestTypeString = " + requestTypeString + ", nestedVolumeStackId = " + nestedStackId
                + ", nestedBaseStackId = " + nestedBaseStackId);

        // Build a default rollback object (no actions performed)
        VnfRollback vfRollback = new VnfRollback();
        vfRollback.setCloudSiteId(cloudSiteId);
        vfRollback.setCloudOwner(cloudOwner);
        vfRollback.setTenantId(tenantId);
        vfRollback.setMsoRequest(msoRequest);
        vfRollback.setRequestType(requestTypeString);
        vfRollback.setVolumeGroupHeatStackId(volumeGroupHeatStackId);
        vfRollback.setBaseGroupHeatStackId(baseVfHeatStackId);
        vfRollback.setIsBase(isBaseRequest);
        vfRollback.setVfModuleStackId(vfModuleStackId);
        vfRollback.setModelCustomizationUuid(mcu);

        StackInfo heatStack;
        logger.debug("UpdateVfModule - querying for {}", vfModuleName);
        try {
            heatStack = msoHeatUtils.queryStack(cloudSiteId, cloudOwner, tenantId, vfModuleName);
        } catch (MsoException me) {
            // Failed to query the Stack due to an openstack exception.
            // Convert to a generic VnfException
            me.addContext("UpdateVFModule");
            String error = "Update VFModule: Query " + vfModuleName + " in " + cloudOwner + "/" + cloudSiteId + "/"
                    + tenantId + ": " + me;
            logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                    cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(),
                    "Exception - " + QUERY_STACK, me);
            logger.debug(error);
            throw new VnfException(me);
        }

        // TODO - do we need to check for the other status possibilities?
        if (heatStack == null || heatStack.getStatus() == HeatStatus.NOTFOUND) {
            // Not Found
            String error = "Update VF: Stack " + vfModuleName + " does not exist in " + cloudOwner + "/" + cloudSiteId
                    + "/" + tenantId;
            logger.error(LoggingAnchor.NINE, MessageEnum.RA_VNF_NOT_EXIST.toString(), vfModuleName, cloudOwner,
                    cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(), error);
            throw new VnfNotFound(cloudSiteId, cloudOwner, tenantId, vfModuleName);
        } else {
            logger.debug("Found Existing stack, status={}", heatStack.getStatus());
            // Populate the outputs from the existing stack.
            outputs.value = copyStringOutputs(heatStack.getOutputs());
            rollback.value = vfRollback; // Default rollback - no updates performed
        }

        // 1604 Cinder Volume support - handle a nestedStackId if sent (volumeGroupHeatStackId):
        StackInfo nestedHeatStack = null;
        if (nestedStackId != null) {
            try {
                logger.debug("Querying for nestedStackId = {}", nestedStackId);
                nestedHeatStack = msoHeatUtils.queryStack(cloudSiteId, cloudOwner, tenantId, nestedStackId);
            } catch (MsoException me) {
                // Failed to query the Stack due to an openstack exception.
                // Convert to a generic VnfException
                me.addContext("UpdateVFModule");
                String error = "Update VF: Attached heatStack ID Query " + nestedStackId + " in " + cloudOwner + "/"
                        + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), vnfName, cloudOwner,
                        cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(),
                        "Exception - " + error, me);
                logger.debug("ERROR trying to query nested stack= {}", error);
                throw new VnfException(me);
            }
            if (nestedHeatStack == null || nestedHeatStack.getStatus() == HeatStatus.NOTFOUND) {
                String error = "Update VFModule: Attached volume heatStack ID DOES NOT EXIST " + nestedStackId + " in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + " " + USER_ERROR;
                logger.error(LoggingAnchor.TEN, MessageEnum.RA_QUERY_VNF_ERR.toString(), vnfName, cloudOwner,
                        cloudSiteId, tenantId, error, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(), error);
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested heat stack - copying values to inputs *later*");
                msoHeatUtils.copyStringOutputsToInputs(inputs, nestedHeatStack.getOutputs(), false);
            }
        }
        // handle a nestedBaseStackId if sent - this is the stack ID of the base.
        StackInfo nestedBaseHeatStack = null;
        if (nestedBaseStackId != null) {
            try {
                logger.debug("Querying for nestedBaseStackId = {}", nestedBaseStackId);
                nestedBaseHeatStack = msoHeatUtils.queryStack(cloudSiteId, cloudOwner, tenantId, nestedBaseStackId);
            } catch (MsoException me) {
                // Failed to query the Stack due to an openstack exception.
                // Convert to a generic VnfException
                me.addContext("UpdateVfModule");
                String error = "Update VFModule: Attached baseHeatStack ID Query " + nestedBaseStackId + " in "
                        + cloudOwner + "/" + cloudSiteId + "/" + tenantId + ": " + me;
                logger.error(LoggingAnchor.NINE, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(),
                        "Exception - " + error, me);
                logger.debug("ERROR trying to query nested base stack= {}", error);
                throw new VnfException(me);
            }
            if (nestedBaseHeatStack == null || nestedBaseHeatStack.getStatus() == HeatStatus.NOTFOUND) {
                String error = "Update VFModule: Attached base heatStack ID DOES NOT EXIST " + nestedBaseStackId
                        + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId + " " + USER_ERROR;
                logger.error(LoggingAnchor.TEN, MessageEnum.RA_QUERY_VNF_ERR.toString(), vfModuleName, cloudOwner,
                        cloudSiteId, tenantId, error, OPENSTACK, QUERY_STACK, ErrorCode.DataError.getValue(), error);
                logger.debug(error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("Found nested base heat stack - copying values to inputs *later*");
                msoHeatUtils.copyStringOutputsToInputs(inputs, nestedBaseHeatStack.getOutputs(), false);
            }
        }

        // Retrieve the VF definition
        VnfResource vnfResource = null;
        VfModule vf = null;
        VfModuleCustomization vfmc = null;
        if (useMCUuid) {
            vfmc = vfModuleCustomRepo.findFirstByModelCustomizationUUIDOrderByCreatedDesc(modelCustomizationUuid);
            vf = vfmc != null ? vfmc.getVfModule() : null;
            if (vf == null) {
                logger.debug("Unable to find a vfModule matching modelCustomizationUuid={}", mcu);
            }
        } else {
            logger.debug("1707 and later - MUST PROVIDE Model Customization UUID!");
        }
        if (vf == null) {
            String error = "Update VfModule: unable to find vfModule with modelCustomizationUuid=" + mcu;
            logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "VF Module Type", vfModuleType,
                    OPENSTACK, ErrorCode.DataError.getValue(), error);
            throw new VnfException(error, MsoExceptionCategory.USERDATA);
        }
        logger.debug("Got VF module definition from Catalog: {}", vf.toString());
        if (vf.getIsBase()) {
            isBaseRequest = true;
            logger.debug("This a BASE update request");
        } else {
            logger.debug("This is *not* a BASE VF update request");
            if (!isVolumeRequest && nestedBaseStackId == null) {
                logger.debug(
                        "DANGER WILL ROBINSON! This is unexpected - no nestedBaseStackId with this non-base request");
            }
        }

        // 1607 - Add version check
        // First - see if it's in the VnfResource record
        // if we have a vf Module - then we have to query to get the VnfResource record.
        if (vf.getModelUUID() != null) {
            String vnfResourceModelUuid = vf.getModelUUID();

            vnfResource = vf.getVnfResources();
            if (vnfResource == null) {
                logger.debug("Unable to find vnfResource at ? will not error for now...", vnfResourceModelUuid);
            }
        }

        String minVersionVnf = null;
        String maxVersionVnf = null;
        if (vnfResource != null) {
            try {
                minVersionVnf = vnfResource.getAicVersionMin();
                maxVersionVnf = vnfResource.getAicVersionMax();
            } catch (Exception e) {
                logger.debug("Unable to pull min/max version for this VNF Resource entry", e);
                minVersionVnf = null;
                maxVersionVnf = null;
            }
            if (minVersionVnf != null && "".equals(minVersionVnf)) {
                minVersionVnf = null;
            }
            if (maxVersionVnf != null && "".equals(maxVersionVnf)) {
                maxVersionVnf = null;
            }
        }
        if (minVersionVnf != null && maxVersionVnf != null) {
            MavenLikeVersioning aicV = new MavenLikeVersioning();

            // double check
            if (this.cloudConfig != null) {
                Optional<CloudSite> cloudSiteOpt = this.cloudConfig.getCloudSite(cloudSiteId);
                if (cloudSiteOpt.isPresent()) {
                    aicV.setVersion(cloudSiteOpt.get().getCloudVersion());
                    boolean moreThanMin = true;
                    boolean equalToMin = true;
                    boolean moreThanMax = true;
                    boolean equalToMax = true;
                    boolean doNotTest = false;
                    try {
                        moreThanMin = aicV.isMoreRecentThan(minVersionVnf);
                        equalToMin = aicV.isTheSameVersion(minVersionVnf);
                        moreThanMax = aicV.isMoreRecentThan(maxVersionVnf);
                        equalToMax = aicV.isTheSameVersion(maxVersionVnf);
                    } catch (Exception e) {
                        logger.debug(
                                "An exception occured while trying to test AIC Version {} - will default to not check",
                                e.getMessage(), e);
                        doNotTest = true;
                    }
                    if (!doNotTest) {
                        if ((moreThanMin || equalToMin) // aic >= min
                                && ((equalToMax) || !(moreThanMax))) { // aic <= max
                            logger.debug("VNF Resource " + vnfResource.getModelName() + " " + VERSION_MIN + " ="
                                    + minVersionVnf + " " + VERSION_MAX + " :" + maxVersionVnf + " supported on Cloud: "
                                    + cloudSiteId + " with AIC_Version:" + aicV);
                        } else {
                            // ERROR
                            String error = "VNF Resource type: " + vnfResource.getModelName() + " " + VERSION_MIN + " ="
                                    + minVersionVnf + " " + VERSION_MAX + " :" + maxVersionVnf
                                    + " NOT supported on Cloud: " + cloudSiteId + " with AIC_Version:" + aicV;
                            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_CONFIG_EXC.toString(), error, OPENSTACK,
                                    ErrorCode.BusinessProcessError.getValue(), "Exception - setVersion");
                            logger.debug(error);
                            throw new VnfException(error, MsoExceptionCategory.USERDATA);
                        }
                    } else {
                        logger.debug("bypassing testing AIC version...");
                    }
                } // let this error out downstream to avoid introducing uncertainty at this stage
            } else {
                logger.debug("cloudConfig is NULL - cannot check cloud site version");
            }

        } else {
            logger.debug("AIC Version not set in VNF_Resource - do not error for now - not checked.");
        }
        // End Version check 1607

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
                    + ", modelCustomizationUuid=" + mcu + ", vfModuleUuid=" + vf.getModelUUID() + ", reqType="
                    + requestTypeString;
            logger.error(LoggingAnchor.SIX, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Template ID",
                    vfModuleType, OPENSTACK, ErrorCode.DataError.getValue(), error);
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got HEAT Template from DB: {}", heatTemplate.getHeatTemplate());
        }

        if (heatEnvironment == null) {
            String error = "Update VNF: undefined Heat Environment. VF=" + vfModuleType + ", modelCustomizationUuid="
                    + mcu + ", vfModuleUuid=" + vf.getModelUUID() + ", reqType=" + requestTypeString;
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_VNF_UNKNOWN_PARAM.toString(), "Heat Environment ID",
                    OPENSTACK, ErrorCode.DataError.getValue(), error);
            throw new VnfException(error, MsoExceptionCategory.INTERNAL);
        } else {
            logger.debug("Got Heat Environment from DB: {}", heatEnvironment.getEnvironment());
        }

        logger.debug("In MsoVnfAdapterImpl, about to call db.getNestedTemplates avec templateId={}",
                heatTemplate.getArtifactUuid());


        List<HeatTemplate> nestedTemplates = heatTemplate.getChildTemplates();
        Map<String, Object> nestedTemplatesChecked = new HashMap<>();
        if (nestedTemplates != null && !nestedTemplates.isEmpty()) {
            // for debugging print them out
            logger.debug("Contents of nestedTemplates - to be added to files: on stack:");
            for (HeatTemplate entry : nestedTemplates) {

                nestedTemplatesChecked.put(entry.getTemplateName(), entry.getTemplateBody());
                logger.debug(entry.getTemplateName() + " -> " + entry.getTemplateBody());
            }
        } else {
            logger.debug("No nested templates found - nothing to do here");
            nestedTemplatesChecked = null;
        }

        // Also add the files: for any get_files associated with this VfModule
        // *if* there are any
        logger.debug("In MsoVnfAdapterImpl.updateVfModule, about to call db.getHeatFiles avec vfModuleId={}",
                vf.getModelUUID());

        List<HeatFiles> heatFiles = null;
        Map<String, Object> heatFilesObjects = new HashMap<>();

        // Add ability to turn on adding get_files with volume requests (by property).
        boolean addGetFilesOnVolumeReq = false;
        try {
            String propertyString = this.environment.getProperty(MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ);
            if ("true".equalsIgnoreCase(propertyString) || "y".equalsIgnoreCase(propertyString)) {
                addGetFilesOnVolumeReq = true;
                logger.debug("AddGetFilesOnVolumeReq - setting to true! {}", propertyString);
            }
        } catch (Exception e) {
            logger.debug("An error occured trying to get property {} - default to false",
                    MsoVnfAdapterImpl.ADD_GET_FILES_ON_VOLUME_REQ, e);
        }
        if (!isVolumeRequest || addGetFilesOnVolumeReq) {
            logger.debug("In MsoVnfAdapterImpl updateVfModule, about to call db.getHeatFilesForVfModule avec "
                    + "vfModuleId={}", vf.getModelUUID());

            heatFiles = vf.getHeatFiles();
            if (heatFiles != null && !heatFiles.isEmpty()) {
                // add these to stack - to be done in createStack
                // here, we will map them to Map<String, Object> from Map<String, HeatFiles>
                // this will match the nested templates format
                logger.debug("Contents of heatFiles - to be added to files: on stack:");
                for (HeatFiles heatfile : heatFiles) {
                    logger.debug(heatfile.getFileName() + " -> " + heatfile.getFileBody());
                    heatFilesObjects.put(heatfile.getFileName(), heatfile.getFileBody());
                }
            } else {
                logger.debug("No heat files found -nothing to do here");
                heatFilesObjects = null;
            }
        }

        // Check that required parameters have been supplied
        String missingParams = null;
        List<String> paramList = new ArrayList<>();

        // New for 1510 - consult the PARAM_ALIAS field to see if we've been
        // supplied an alias. Only check if we don't find it initially.
        // Also new in 1510 - don't flag missing parameters if there's an environment - because they might be there.
        // And also new - add parameter to turn off checking all together if we find we're blocking orders we
        // shouldn't
        boolean checkRequiredParameters = true;
        try {
            String propertyString = this.environment.getProperty(MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
            if ("false".equalsIgnoreCase(propertyString) || "n".equalsIgnoreCase(propertyString)) {
                checkRequiredParameters = false;
                logger.debug("CheckRequiredParameters is FALSE. Will still check but then skip blocking...",
                        MsoVnfAdapterImpl.CHECK_REQD_PARAMS);
            }
        } catch (Exception e) {
            // No problem - default is true
            logger.debug("An exception occured trying to get property {}", MsoVnfAdapterImpl.CHECK_REQD_PARAMS, e);
        }
        // 1604 - Add enhanced environment & parameter checking
        // Part 1: parse envt entries to see if reqd parameter is there (before used a simple grep
        // Part 2: only submit to openstack the parameters in the envt that are in the heat template
        // Note this also removes any comments
        MsoHeatEnvironmentEntry mhee = null;
        if (heatEnvironment != null && heatEnvironment.getEnvironment().toLowerCase().contains("parameters:")) {
            logger.debug("Enhanced environment checking enabled - 1604");
            StringBuilder sb = new StringBuilder(heatEnvironment.getEnvironment());
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
            logger.debug(sb2.toString());
        } else {
            logger.debug("NO ENVIRONMENT for this entry");
        }
        // New for 1607 - support params of json type
        HashMap<String, JsonNode> jsonParams = new HashMap<>();
        boolean hasJson = false;

        for (HeatTemplateParam parm : heatTemplate.getParameters()) {
            logger.debug("Parameter:'" + parm.getParamName() + "', isRequired=" + parm.isRequired() + ", alias="
                    + parm.getParamAlias());
            // handle json
            String parameterType = parm.getParamType();
            if (parameterType == null || "".equals(parameterType.trim())) {
                parameterType = "String";
            }
            JsonNode jsonNode = null;
            if ("json".equalsIgnoreCase(parameterType) && inputs != null) {
                if (inputs.containsKey(parm.getParamName())) {
                    hasJson = true;
                    String jsonString = null;
                    try {
                        jsonString = JSON_MAPPER.writeValueAsString(inputs.get(parm.getParamName()));
                        jsonNode = JSON_MAPPER.readTree(jsonString);
                    } catch (JsonParseException jpe) {
                        // TODO - what to do here?
                        // for now - send the error to debug
                        logger.debug("Json Error Converting {} - {}", parm.getParamName(), jpe.getMessage(), jpe);
                        hasJson = false;
                        jsonNode = null;
                    } catch (Exception e) {
                        // or here?
                        logger.debug("Json Error Converting {} {}", parm.getParamName(), e.getMessage(), e);
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
                        jsonString = (String) inputs.get(parm.getParamAlias());
                        jsonNode = JSON_MAPPER.readTree(jsonString);
                    } catch (JsonParseException jpe) {
                        // TODO - what to do here?
                        // for now - send the error to debug, but just leave it as a String
                        String errorMessage = jpe.getMessage();
                        logger.debug("Json Error Converting " + parm.getParamName() + " - " + errorMessage, jpe);
                        hasJson = false;
                        jsonNode = null;
                    } catch (Exception e) {
                        // or here?
                        logger.debug("Json Error Converting " + parm.getParamName() + " " + e.getMessage(), e);
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
                } // TODO add a check for the parameter in the env file
            }

            if (parm.isRequired() && (inputs == null || !inputs.containsKey(parm.getParamName()))) {
                if (inputs.containsKey(parm.getParamAlias())) {
                    // They've submitted using an alias name. Remove that from inputs, and add back using real name.
                    String realParamName = parm.getParamName();
                    String alias = parm.getParamAlias();
                    Object value = inputs.get(alias);
                    logger.debug("*Found an Alias: paramName=" + realParamName + ",alias=" + alias + ",value=" + value);
                    inputs.remove(alias);
                    inputs.put(realParamName, value);
                    logger.debug("{} entry removed from inputs, added back using {}", alias, realParamName);
                }
                // enhanced - check if it's in the Environment (note: that method
                else if (mhee != null && mhee.containsParameter(parm.getParamName())) {

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
            paramList.add(parm.getParamName());
        }


        if (missingParams != null) {
            // Problem - missing one or more required parameters
            if (checkRequiredParameters) {
                String error = "Update VNF: Missing Required inputs: " + missingParams;
                logger.error(LoggingAnchor.FIVE, MessageEnum.RA_MISSING_PARAM.toString(), missingParams, OPENSTACK,
                        ErrorCode.DataError.getValue(), error);
                throw new VnfException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.debug("found missing parameters - but checkRequiredParameters is false - will not block");
            }
        }

        // Just submit the envt entry as received from the database
        String newEnvironmentString = null;
        if (mhee != null) {
            newEnvironmentString = mhee.getRawEntry().toString();
        }
        // Remove any extraneous parameters (don't throw an error)
        if (inputs != null) {
            List<String> extraParams = new ArrayList<>();
            extraParams.addAll(inputs.keySet());
            // This is not a valid parameter for this template
            extraParams.removeAll(paramList);
            if (!extraParams.isEmpty()) {
                logger.warn(LoggingAnchor.SIX, MessageEnum.RA_VNF_EXTRA_PARAM.toString(), vnfType,
                        extraParams.toString(), OPENSTACK, ErrorCode.DataError.getValue(), "Extra params");
                inputs.keySet().removeAll(extraParams);
            }
        }
        Map<String, Object> goldenInputs = copyStringInputs(inputs);
        // 1607 - when we get here - we have clean inputs. Create inputsTwo in case we have json
        Map<String, Object> inputsTwo = null;
        if (hasJson && jsonParams.size() > 0) {
            inputsTwo = new HashMap<>();
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                String keyParamName = entry.getKey();
                Object value = entry.getValue();
                if (jsonParams.containsKey(keyParamName)) {
                    inputsTwo.put(keyParamName, jsonParams.get(keyParamName));
                } else {
                    inputsTwo.put(keyParamName, value);
                }
            }
            goldenInputs = inputsTwo;
        }

        // "Fix" the template if it has CR/LF (getting this from Oracle)
        String template = heatTemplate.getHeatTemplate();
        template = template.replaceAll("\r\n", "\n");

        // Have the tenant. Now deploy the stack itself
        // Ignore MsoTenantNotFound and MsoStackAlreadyExists exceptions
        // because we already checked for those.
        try {
            heatStack = heatU.updateStack(cloudSiteId, cloudOwner, tenantId, vfModuleName, template, goldenInputs, true,
                    heatTemplate.getTimeoutMinutes(), newEnvironmentString,
                    // heatEnvironmentString,
                    nestedTemplatesChecked, heatFilesObjects);
        } catch (MsoException me) {
            me.addContext("UpdateVFModule");
            String error = "Update VFModule " + vfModuleType + " in " + cloudOwner + "/" + cloudSiteId + "/" + tenantId
                    + ": " + me;
            logger.error(LoggingAnchor.EIGHT, MessageEnum.RA_UPDATE_VNF_ERR.toString(), vfModuleType, cloudOwner,
                    cloudSiteId, tenantId, OPENSTACK, ErrorCode.DataError.getValue(), "Exception - " + error, me);
            throw new VnfException(me);
        }

        // Reach this point if updateStack is successful.
        // Populate remaining rollback info and response parameters.
        vfRollback.setVnfId(heatStack.getCanonicalName());
        vfRollback.setVnfCreated(true);
        outputs.value = copyStringOutputs(heatStack.getOutputs());
        rollback.value = vfRollback;
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
            logger.debug("Exception", e);
            vfModuleName = null;
        }
        return vfModuleName;
    }

}
