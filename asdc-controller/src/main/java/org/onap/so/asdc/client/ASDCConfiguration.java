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

package org.onap.so.asdc.client;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.onap.sdc.api.consumer.IConfiguration;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ASDCConfiguration implements IConfiguration {

    // SHell command to obtain the same encryption, 128 bits key, key must be HEX
    // echo -n "This is a test string" | openssl aes-128-ecb -e -K 546573746F736973546573746F736973
    // -nosalt | xxd

    private static Logger logger = LoggerFactory.getLogger(ASDCConfiguration.class);

    private String asdcControllerName;

    public static final String HEAT = "HEAT";
    public static final String HEAT_ARTIFACT = "HEAT_ARTIFACT";
    public static final String HEAT_ENV = "HEAT_ENV";
    public static final String HEAT_NESTED = "HEAT_NESTED";
    public static final String HEAT_NET = "HEAT_NET";
    public static final String HEAT_VOL = "HEAT_VOL";
    public static final String OTHER = "OTHER";
    public static final String TOSCA_CSAR = "TOSCA_CSAR";
    public static final String WORKFLOW = "WORKFLOW";
    public static final String VF_MODULES_METADATA = "VF_MODULES_METADATA";
    public static final String CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT = "CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT";

    private static final String[] SUPPORTED_ARTIFACT_TYPES = {HEAT, HEAT_ARTIFACT, HEAT_ENV, HEAT_NESTED, HEAT_NET,
            HEAT_VOL, OTHER, TOSCA_CSAR, VF_MODULES_METADATA, WORKFLOW, CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT};


    public static final List<String> SUPPORTED_ARTIFACT_TYPES_LIST =
            Collections.unmodifiableList(Arrays.asList(SUPPORTED_ARTIFACT_TYPES));

    @Autowired
    private Environment env;

    @Value("${mso.asdc.config.key}")
    private String configKey;

    @Value("${mso.asdc-connections.asdc-controller1.messageBusAddress}")
    private String[] messageBusAddress;

    public void setAsdcControllerName(String asdcControllerName) {
        this.asdcControllerName = asdcControllerName;
    }

    @Override
    public java.lang.Boolean isUseHttpsWithDmaap() {
        return getBooleanPropertyWithDefault("mso.asdc-connections.asdc-controller1.useHttpsWithDmaap", true);
    }

    @Override
    public java.lang.Boolean isUseHttpsWithSDC() {
        return getBooleanPropertyWithDefault("mso.asdc-connections.asdc-controller1.useHttpsWithSdc", true);
    }

    @Override
    public boolean isConsumeProduceStatusTopic() {
        return true;
    }

    @Override
    public List<String> getMsgBusAddress() {
        if (messageBusAddress.length > 0) {
            return Arrays.asList(messageBusAddress);
        } else {
            return Collections.emptyList();
        }
    }

    public String getAsdcControllerName() {
        return asdcControllerName;
    }

    @Override
    public String getConsumerGroup() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.consumerGroup");
    }

    public int getWatchDogTimeout() {
        return getIntegerPropertyOrZero("mso.asdc-connections.asdc-controller1.watchDogTimeout");

    }

    @Override
    public String getConsumerID() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.consumerId");
    }

    public int getIntegerPropertyOrZero(String propertyName) {
        String property = env.getProperty(propertyName);
        if (property == null || "NULL".equals(property) || property.isEmpty()) {
            return 0;
        } else {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public String getPropertyOrNull(String propertyName) {
        String config = env.getProperty(propertyName);
        if (config == null || "NULL".equals(config) || config.isEmpty()) {
            return null;
        } else {
            return config;
        }
    }

    public String getEncryptedPropertyOrNull(String propertyName) {
        String decryptedKey;
        String config = env.getProperty(propertyName);

        if (config == null || "NULL".equals(config) || config.isEmpty()) {
            return null;
        }

        try {
            decryptedKey = CryptoUtils.decrypt(config, this.configKey);
        } catch (GeneralSecurityException e) {
            logger.debug("Exception while decrypting property: {}", propertyName, e);
            return null;
        }

        if (decryptedKey.isEmpty()) {
            return null;
        } else {
            return decryptedKey;
        }
    }

    public boolean getBooleanPropertyWithDefault(String propertyName, boolean defaultValue) {
        String config = env.getProperty(propertyName);
        if (config == null || "NULL".equals(config) || config.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Boolean.valueOf(config);
            } catch (Exception e) {
                logger.warn("Exception while getting boolean property with default property", e);
                return defaultValue;
            }
        }
    }

    @Override
    public String getEnvironmentName() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.environmentName");
    }

    @Override
    public String getPassword() {
        return getEncryptedPropertyOrNull("mso.asdc-connections.asdc-controller1.password");
    }

    @Override
    public int getPollingInterval() {
        return getIntegerPropertyOrZero("mso.asdc-connections.asdc-controller1.pollingInterval");
    }

    @Override
    public List<String> getRelevantArtifactTypes() {
        // DO not return the Static List SUPPORTED_ARTIFACT_TYPES_LIST because the ASDC Client will
        // try to modify it !!!
        return Arrays.asList(SUPPORTED_ARTIFACT_TYPES);
    }

    @Override
    public String getUser() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.user");
    }

    @Override
    public String getAsdcAddress() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.asdcAddress");
    }

    @Override
    public int getPollingTimeout() {
        return getIntegerPropertyOrZero("mso.asdc-connections.asdc-controller1.pollingTimeout");
    }

    @Override
    public boolean activateServerTLSAuth() {
        return getBooleanPropertyWithDefault("mso.asdc-connections.asdc-controller1.activateServerTLSAuth", true);
    }

    @Override
    public String getKeyStorePassword() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.keyStorePassword");
    }

    @Override
    public String getKeyStorePath() {
        return getPropertyOrNull("mso.asdc-connections.asdc-controller1.keyStorePath");
    }

    /**
     * The flag allows the client to receive metadata for all resources of the service regardless of the artifacts
     * associated to them. Setting the flag to false will preserve legacy behavior.
     */
    @Override
    public boolean isFilterInEmptyResources() {
        return getBooleanPropertyWithDefault("mso.asdc-connections.asdc-controller1.isFilterInEmptyResources", true);
    }

}
