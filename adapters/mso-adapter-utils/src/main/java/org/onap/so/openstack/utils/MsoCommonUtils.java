/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.openstack.utils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloud.authentication.AuthenticationMethodFactory;
import org.onap.so.cloud.authentication.KeystoneAuthHolder;
import org.onap.so.cloud.authentication.KeystoneV3Authentication;
import org.onap.so.cloud.authentication.ServiceEndpointNotFoundException;
import org.onap.so.config.beans.PoConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Explanation;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.Error;
import com.woorea.openstack.keystone.utils.KeystoneUtils;
import com.woorea.openstack.quantum.model.NeutronError;

@Component("CommonUtils")
public class MsoCommonUtils {

    private static Logger logger = LoggerFactory.getLogger(MsoCommonUtils.class);

    /** The Constant TOKEN_AUTH. */
    protected static final String TOKEN_AUTH = "TokenAuth";

    /** The cloud config. */
    @Autowired
    protected CloudConfig cloudConfig;

    /** The authentication method factory. */
    @Autowired
    protected AuthenticationMethodFactory authenticationMethodFactory;

    /** The tenant utils factory. */
    @Autowired
    protected MsoTenantUtilsFactory tenantUtilsFactory;

    /** The keystone V 3 authentication. */
    @Autowired
    protected KeystoneV3Authentication keystoneV3Authentication;

    @Autowired
    protected PoConfig poConfig;

    /*
     * Method to execute an Openstack command and track its execution time. For the metrics log, a category of
     * "Openstack" is used along with a sub-category that identifies the specific call (using the real
     * openstack-java-sdk classname of the OpenStackRequest<T> parameter).
     */

    public <T> T executeAndRecordOpenstackRequest(OpenStackRequest<T> request) {
        return executeAndRecordOpenstackRequest(request, true);
    }

    /*
     * Method to execute an Openstack command and track its execution time. For the metrics log, a category of
     * "Openstack" is used along with a sub-category that identifies the specific call (using the real
     * openstack-java-sdk classname of the OpenStackRequest<T> parameter). boolean isNoRetry - true if No retry; and
     * false if Retry.
     */

    protected <T> T executeAndRecordOpenstackRequest(OpenStackRequest<T> request, boolean shouldRetry) {
        int retryDelay = poConfig.getRetryDelay();
        int retryCount = poConfig.getRetryCount();
        String retryCodes = poConfig.getRetryCodes();
        if (!shouldRetry) {
            retryCodes = null;
        }
        // Run the actual command. All exceptions will be propagated
        while (true) {
            try {
                return request.execute();
            } catch (OpenStackResponseException e) {
                boolean retry = false;
                if (retryCodes != null) {
                    int code = e.getStatus();
                    logger.debug("Config values RetryDelay:{} RetryCount:{}  RetryCodes:{} ResponseCode:{}", retryDelay,
                            retryCount, retryCodes, code);
                    for (String rCode : retryCodes.split(",")) {
                        try {
                            if (retryCount > 0 && code == Integer.parseInt(rCode)) {
                                retryCount--;
                                retry = true;
                                logger.debug(
                                        "OpenStackResponseException ResponseCode: {} Retry indicated. Attempts remaining:{}",
                                        code, retryCount);
                                break;
                            }
                        } catch (NumberFormatException e1) {
                            logger.error("{} No retries. Exception in parsing retry code in config:{} {} {}",
                                    MessageEnum.RA_CONFIG_EXC, rCode, ErrorCode.SchemaError.getValue(),
                                    "Exception in parsing retry code in config");
                            throw e;
                        }
                    }
                }
                if (retry) {
                    try {
                        Thread.sleep(retryDelay * 1000L);
                    } catch (InterruptedException e1) {
                        logger.debug("Thread interrupted while sleeping", e1);
                        Thread.currentThread().interrupt();
                    }
                } else
                    throw e; // exceeded retryCount or code is not retryable
            } catch (OpenStackConnectException e) {
                // Connection to Openstack failed
                if (retryCount > 0) {
                    retryCount--;
                    logger.debug("Retry indicated. Attempts remaining:{}", retryCount);
                    try {
                        Thread.sleep(retryDelay * 1000L);
                    } catch (InterruptedException e1) {
                        logger.debug("Thread interrupted while sleeping", e1);
                        Thread.currentThread().interrupt();
                    }
                } else
                    throw e;

            }
        }
    }

    /*
     * Convert an Openstack Exception on a Keystone call to an MsoException. This method supports both
     * OpenstackResponseException and OpenStackConnectException.
     */
    public MsoException keystoneErrorToMsoException(OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Keystone calls return an Error entity body.
                Error error = re.getResponse().getErrorEntity(Error.class);
                logger.error("{} {} Openstack Keystone Error on {}: {}", MessageEnum.RA_CONNECTION_EXCEPTION,
                        ErrorCode.DataError.getValue(), context, error);
                me = new MsoOpenstackException(error.getCode(), error.getTitle(), error.getMessage());
            } catch (Exception e2) {
                // Can't parse the body as an "Error". Report the HTTP error
                logger.error("{} {} HTTP Error on {}: {}, {}", MessageEnum.RA_CONNECTION_EXCEPTION,
                        ErrorCode.DataError.getValue(), context, re.getStatus(), re.getMessage(), e2);
                me = new MsoOpenstackException(re.getStatus(), re.getMessage(), "");
            }

            // Add the context of the error
            me.addContext(context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus() >= 500) {
                logger.error("{} {} OpenStackResponseException with response code {} on {}: ",
                        MessageEnum.RA_CONNECTION_EXCEPTION, ErrorCode.DataError.getValue(), re.getStatus(), context,
                        e);
            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException(ce.getMessage());
            me.addContext(context);

            // Generate an alarm for all connection errors.
            logger.error("{} {} Openstack Keystone connection error on {}: ", MessageEnum.RA_GENERAL_EXCEPTION_ARG,
                    ErrorCode.DataError.getValue(), context, e);
        }

        return me;
    }

    /*
     * Convert an Openstack Exception on a Heat call to an MsoOpenstackException. This method supports both
     * OpenstackResponseException and OpenStackConnectException.
     */
    protected MsoException heatExceptionToMsoException(OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Heat calls return an Explanation entity body.
                Explanation explanation = re.getResponse().getErrorEntity(Explanation.class);
                logger.error("Exception - Openstack Error on {} : {}", context, explanation);
                String fullError = explanation.getExplanation() + ", error.type=" + explanation.getError().getType()
                        + ", error.message=" + explanation.getError().getMessage();
                logger.error(fullError);
                me = new MsoOpenstackException(explanation.getCode(), explanation.getTitle(), fullError);
            } catch (Exception e2) {
                // Couldn't parse the body as an "Explanation". Report the original HTTP error.
                logger.error("{} {} Exception - HTTP Error on {}: {}, ", MessageEnum.RA_CONNECTION_EXCEPTION,
                        ErrorCode.DataError.getValue(), context, re.getStatus(), e.getMessage(), e2);
                me = new MsoOpenstackException(re.getStatus(), re.getMessage(), re.getMessage());
            }

            // Add the context of the error
            me.addContext(context);

        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;
            me = new MsoIOException(ce.getMessage());
            me.addContext(context);
            // Generate an alarm for all connection errors.
            logger.error("{} {} Openstack Heat connection error on {}: ", MessageEnum.RA_CONNECTION_EXCEPTION,
                    ErrorCode.DataError.getValue(), context, e);
        }
        return me;
    }

    /*
     * Convert an Openstack Exception on a Neutron call to an MsoOpenstackException. This method supports both
     * OpenstackResponseException and OpenStackConnectException.
     */
    protected MsoException neutronExceptionToMsoException(OpenStackBaseException e, String context) {
        MsoException me = null;

        if (e instanceof OpenStackResponseException) {
            OpenStackResponseException re = (OpenStackResponseException) e;

            try {
                // Failed Neutron calls return an NeutronError entity body
                NeutronError error = re.getResponse().getErrorEntity(NeutronError.class);
                logger.error("{} {} Openstack Neutron Error on {} {}", MessageEnum.RA_CONNECTION_EXCEPTION,
                        ErrorCode.DataError.getValue(), context, error);
                me = new MsoOpenstackException(re.getStatus(), error.getType(), error.getMessage());
            } catch (Exception e2) {
                // Couldn't parse body as a NeutronError. Report the HTTP error.
                logger.error("{} {} Openstack HTTP Error on {}: {}, {}", MessageEnum.RA_CONNECTION_EXCEPTION,
                        ErrorCode.DataError.getValue(), context, re.getStatus(), e.getMessage(), e2);
                me = new MsoOpenstackException(re.getStatus(), re.getMessage(), null);
            }

            // Add the context of the error
            me.addContext(context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus() >= 500) {
                logger.error("{} {} OpenStackBaseException with response code {} on {}: ",
                        MessageEnum.RA_CONNECTION_EXCEPTION, ErrorCode.DataError.getValue(), re.getStatus(), context,
                        e);
            }
        } else if (e instanceof OpenStackConnectException) {
            OpenStackConnectException ce = (OpenStackConnectException) e;

            me = new MsoIOException(ce.getMessage());
            me.addContext(context);

            // Generate an alarm for all connection errors.

            logger.error("{} {} Openstack Neutron Connection error on {}: ", MessageEnum.RA_CONNECTION_EXCEPTION,
                    ErrorCode.DataError.getValue(), context, e);
        }

        return me;
    }

    /*
     * Convert a Java Runtime Exception to an MsoException. All Runtime exceptions will be translated into an
     * MsoAdapterException, which captures internal errors. Alarms will be generated on all such exceptions.
     */
    protected MsoException runtimeExceptionToMsoException(RuntimeException e, String context) {
        MsoAdapterException me = new MsoAdapterException(e.getMessage(), e);
        me.addContext(context);
        me.setCategory(MsoExceptionCategory.INTERNAL);

        // Always generate an alarm for internal exceptions
        logger.error("{} {} An exception occured on {}: ", MessageEnum.RA_GENERAL_EXCEPTION_ARG,
                ErrorCode.DataError.getValue(), context, e);

        return me;
    }

    protected MsoException ioExceptionToMsoException(IOException e, String context) {
        MsoAdapterException me = new MsoAdapterException(e.getMessage(), e);
        me.addContext(context);
        me.setCategory(MsoExceptionCategory.INTERNAL);

        // Always generate an alarm for internal exceptions
        logger.error("{} {} An exception occured on {}: ", MessageEnum.RA_GENERAL_EXCEPTION_ARG,
                ErrorCode.DataError.getValue(), context, e);

        return me;
    }

    public boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }


    protected CreateStackParam createStackParam(String stackName, String heatTemplate, Map<String, ?> stackInputs,
            int timeoutMinutes, String environment, Map<String, Object> files, Map<String, Object> heatFiles) {

        // force entire stackInput object to generic Map<String, Object> for openstack compatibility
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> normalized = new HashMap<>();
        try {
            normalized = mapper.readValue(mapper.writeValueAsString(stackInputs),
                    new TypeReference<HashMap<String, Object>>() {});
        } catch (IOException e1) {
            logger.debug("could not map json", e1);
        }

        CreateStackParam stack =
                createStack(stackName, heatTemplate, timeoutMinutes, environment, files, heatFiles, normalized);

        // 1802 - attempt to add better formatted printout of request to openstack
        try {
            Map<String, Object> inputs = new HashMap<>();
            for (Entry<String, ?> entry : stackInputs.entrySet()) {
                if (entry.getValue() != null) {
                    inputs.put(entry.getKey(), entry.getValue());
                }
            }
            logger.debug("stack request: {}", stack);
        } catch (Exception e) {
            // that's okay - this is a nice-to-have
            logger.debug("(had an issue printing nicely formatted request to debuglog) {}", e);
        }

        return stack;
    }

    private CreateStackParam createStack(String stackName, String heatTemplate, int timeoutMinutes, String environment,
            Map<String, Object> files, Map<String, Object> heatFiles, Map<String, Object> normalized) {
        // Build up the stack to create
        // Disable auto-rollback, because error reason is lost. Always rollback in the code.
        CreateStackParam stack = new CreateStackParam();
        stack.setStackName(stackName);
        stack.setTimeoutMinutes(timeoutMinutes);
        stack.setParameters(normalized);
        stack.setTemplate(heatTemplate);
        stack.setDisableRollback(true);
        // TJM New for PO Adapter - add envt variable
        if (isEnvVariablePresent(environment)) {
            logger.debug("Found an environment variable - value: {}", environment);
            stack.setEnvironment(environment);
        }
        // Now handle nested templates or get_files - have to combine if we have both
        // as they're both treated as "files:" on the stack.
        if (isFilesPresent(files) && isHeatFilesPresent(heatFiles)) {
            // Let's do this here - not in the bean
            logger.debug("Found files AND heatFiles - combine and add!");
            Map<String, Object> combinedFiles = new HashMap<>();
            for (Entry<String, Object> entry : files.entrySet()) {
                combinedFiles.put(entry.getKey(), entry.getValue());
            }
            for (Entry<String, Object> entry : heatFiles.entrySet()) {
                combinedFiles.put(entry.getKey(), entry.getValue());
            }
            stack.setFiles(combinedFiles);
        } else {
            // Handle if we only have one or neither:
            if (isFilesPresent(files)) {
                logger.debug("Found files - adding to stack");
                stack.setFiles(files);
            }
            if (isHeatFilesPresent(heatFiles)) {
                logger.debug("Found heatFiles - adding to stack");
                // the setFiles was modified to handle adding the entries
                stack.setFiles(heatFiles);
            }
        }
        return stack;
    }

    private boolean isEnvVariablePresent(String environment) {
        boolean haveEnvVariable = true;
        if (environment == null || "".equalsIgnoreCase(environment.trim())) {
            haveEnvVariable = false;
            logger.debug("createStackParam called with no environment variable");
        } else {
            logger.debug("createStackParam called with an environment variable: {}", environment);
        }
        return haveEnvVariable;
    }

    private boolean isFilesPresent(Map<String, Object> files) {
        boolean haveFile = true;
        if (files == null || files.isEmpty()) {
            haveFile = false;
            logger.debug("createStackParam called with no files / child template ids");
        } else {
            logger.debug("createStackParam called with {} files / child template ids", files.size());
        }
        return haveFile;
    }

    private boolean isHeatFilesPresent(Map<String, Object> heatFiles) {
        boolean haveHeatFile = true;
        if (heatFiles == null || heatFiles.isEmpty()) {
            haveHeatFile = false;
            logger.debug("createStackParam called with no heatFiles");
        } else {
            logger.debug("createStackParam called with {} heatFiles", heatFiles.size());
        }
        return haveHeatFile;
    }


    /**
     * Gets the Keystone Authorization
     *
     * @param cloudSite the cloud site
     * @param tenantId the tenant id
     * @return the Neutron client
     * @throws MsoException the mso exception
     */
    protected KeystoneAuthHolder getKeystoneAuthHolder(String cloudSiteId, String tenantId, String serviceName)
            throws MsoException {
        CloudIdentity cloudIdentity = null;
        try {
            CloudSite cloudSite =
                    cloudConfig.getCloudSite(cloudSiteId).orElseThrow(() -> new MsoCloudSiteNotFound(cloudSiteId));
            String cloudId = cloudSite.getId();
            String region = cloudSite.getRegionId();
            cloudIdentity = cloudSite.getIdentityService();
            MsoTenantUtils tenantUtils =
                    tenantUtilsFactory.getTenantUtilsByServerType(cloudIdentity.getIdentityServerType());
            String keystoneUrl = tenantUtils.getKeystoneUrl(cloudId, cloudIdentity);
            if (ServerType.KEYSTONE.equals(cloudIdentity.getIdentityServerType())) {
                Access access = getKeystone(tenantId, cloudIdentity, keystoneUrl);
                try {
                    KeystoneAuthHolder keystoneAuthV2 = new KeystoneAuthHolder();
                    keystoneAuthV2.setServiceUrl(
                            KeystoneUtils.findEndpointURL(access.getServiceCatalog(), serviceName, region, "public"));
                    keystoneAuthV2.setId(access.getToken().getId());
                    return keystoneAuthV2;
                } catch (RuntimeException e) {
                    String error = "Openstack did not match an orchestration service for: region=" + region + ",cloud="
                            + cloudIdentity.getIdentityUrl();
                    throw new MsoAdapterException(error, e);
                }
            } else if (ServerType.KEYSTONE_V3.equals(cloudIdentity.getIdentityServerType())) {
                try {
                    return keystoneV3Authentication.getToken(cloudSite, tenantId, serviceName);
                } catch (ServiceEndpointNotFoundException e) {
                    String error = "cloud did not match an orchestration service for: region=" + region + ",cloud="
                            + cloudIdentity.getIdentityUrl();
                    throw new MsoAdapterException(error, e);
                }
            } else {
                throw new MsoAdapterException("Unknown Keystone Server Type");
            }
        } catch (OpenStackResponseException e) {
            if (e.getStatus() == 401) {
                String error = "Authentication Failure: tenant=" + tenantId + ",cloud=" + cloudIdentity.getId();
                throw new MsoAdapterException(error);
            } else {
                throw keystoneErrorToMsoException(e, TOKEN_AUTH);
            }
        } catch (OpenStackConnectException e) {
            MsoIOException me = new MsoIOException(e.getMessage(), e);
            me.addContext(TOKEN_AUTH);
            throw me;
        } catch (RuntimeException e) {
            throw runtimeExceptionToMsoException(e, TOKEN_AUTH);
        }
    }

    /**
     * @param tenantId
     * @param cloudIdentity
     * @param keystoneUrl
     * @return
     */
    protected Access getKeystone(String tenantId, CloudIdentity cloudIdentity, String keystoneUrl) {
        Keystone keystoneTenantClient = new Keystone(keystoneUrl);
        Access access = null;
        Authentication credentials = authenticationMethodFactory.getAuthenticationFor(cloudIdentity);
        OpenStackRequest<Access> request =
                keystoneTenantClient.tokens().authenticate(credentials).withTenantId(tenantId);
        access = executeAndRecordOpenstackRequest(request);
        return access;
    }
}
