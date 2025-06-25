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

package org.onap.so.bpmn.core.json;

import java.io.IOException;
import java.io.Serializable;
import org.onap.so.bpmn.core.domain.AllottedResource;
import org.onap.so.bpmn.core.domain.ConfigResource;
import org.onap.so.bpmn.core.domain.NetworkResource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.domain.ServiceInstance;
import org.onap.so.bpmn.core.domain.VnfResource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DecomposeJsonUtil implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DecomposeJsonUtil.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    private static final ObjectMapper mapperUnknown;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapperUnknown = new ObjectMapper();
        mapperUnknown.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapperUnknown.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private DecomposeJsonUtil() {}

    /**
     * Method to construct Service Decomposition object converting JSON structure
     *
     * @param jsonString input in JSON format confirming ServiceDecomposition
     * @return decoded object
     * @throws JsonDecomposingException thrown when decoding json fails
     */
    public static ServiceDecomposition jsonToServiceDecomposition(String jsonString) throws JsonDecomposingException {
        try {
            return mapperUnknown.readValue(jsonString, ServiceDecomposition.class);
        } catch (IOException e) {
            throw new JsonDecomposingException("Exception while converting json to service decomposition", e);
        }
    }

    /**
     * Method to construct Service Decomposition object converting JSON structure
     *
     * @param jsonString input in JSON format confirming ServiceDecomposition
     * @param serviceInstanceId service instance id to be put in decoded ServiceDecomposition
     * @return decoded object
     * @throws JsonDecomposingException thrown when decoding json fails
     */
    public static ServiceDecomposition jsonToServiceDecomposition(String jsonString, String serviceInstanceId)
            throws JsonDecomposingException {
        ServiceDecomposition serviceDecomposition = jsonToServiceDecomposition(jsonString);
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setInstanceId(serviceInstanceId);
        serviceDecomposition.setServiceInstance(serviceInstance);
        return serviceDecomposition;
    }

    /**
     * Method to construct Resource Decomposition object converting JSON structure
     *
     * @param jsonString input in JSON format confirming ResourceDecomposition
     * @return decoded object
     * @throws JsonDecomposingException thrown when decoding json fails
     */
    public static VnfResource jsonToVnfResource(String jsonString) throws JsonDecomposingException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, VnfResource.class);
        } catch (IOException e) {
            throw new JsonDecomposingException("Exception while converting json to vnf resource", e);
        }
    }

    /**
     * Method to construct Resource Decomposition object converting JSON structure
     *
     * @param jsonString input in JSON format confirming ResourceDecomposition
     * @return decoded object
     * @throws JsonDecomposingException thrown when decoding json fails
     */
    public static NetworkResource jsonToNetworkResource(String jsonString) throws JsonDecomposingException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, NetworkResource.class);
        } catch (IOException e) {
            throw new JsonDecomposingException("Exception while converting json to network resource", e);
        }
    }

    /**
     * Method to construct Resource Decomposition object converting JSON structure
     *
     * @param jsonString - input in JSON format confirming ResourceDecomposition
     * @return decoded object
     * @throws JsonDecomposingException thrown when decoding json fails
     */
    public static AllottedResource jsonToAllottedResource(String jsonString) throws JsonDecomposingException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, AllottedResource.class);
        } catch (IOException e) {
            throw new JsonDecomposingException("Exception while converting json to allotted resource", e);
        }
    }

    public static ConfigResource jsonToConfigResource(String jsonString) throws JsonDecomposingException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, ConfigResource.class);
        } catch (IOException e) {
            throw new JsonDecomposingException("Exception while converting json to allotted resource", e);
        }
    }
}
