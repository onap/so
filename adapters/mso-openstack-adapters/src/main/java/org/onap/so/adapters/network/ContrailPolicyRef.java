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

package org.onap.so.adapters.network;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContrailPolicyRef {

    private static final Logger logger = LoggerFactory.getLogger(ContrailPolicyRef.class);

    @JsonProperty("network_policy_refs_data_sequence")
    private ContrailPolicyRefSeq seq;

    public JsonNode toJsonNode() {
        JsonNode node = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            node = mapper.convertValue(this, JsonNode.class);
        } catch (Exception e) {
            logger.error("{} {} Error creating JsonString for Contrail Policy Ref: ", MessageEnum.RA_MARSHING_ERROR,
                    ErrorCode.SchemaError.getValue(), e);
        }

        return node;
    }

    public String toJsonString() {
        String jsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonString = mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("{} {} Error creating JsonString for Contrail Policy Ref: ", MessageEnum.RA_MARSHING_ERROR,
                    ErrorCode.SchemaError.getValue(), e);
        }

        return jsonString;
    }

    public void populate(String major, String minor) {
        seq = new ContrailPolicyRefSeq(major, minor);
        return;
    }

}
