/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.onap.so.bpmn.servicedecomposition.generalobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonRootName(value = "requestParameters")
@JsonInclude(Include.NON_DEFAULT)
public class RequestParameters implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RequestParameters.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    private static final long serialVersionUID = -5979049912538894930L;
    @JsonProperty("subscriptionServiceType")
    private String subscriptionServiceType;
    @JsonProperty("userParams")
    private List<Map<String, Object>> userParams = new ArrayList<>();
    @JsonProperty("aLaCarte")
    private Boolean aLaCarte;
    @JsonProperty("payload")
    private String payload;
    @JsonProperty("usePreload")
    private Boolean usePreload;

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    @JsonProperty("aLaCarte")
    public Boolean getALaCarte() {
        return aLaCarte;
    }

    @JsonProperty("aLaCarte")
    public void setaLaCarte(Boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    public Boolean isaLaCarte() {
        return aLaCarte;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String value) {
        this.payload = value;
    }

    public List<Map<String, Object>> getUserParams() {
        return userParams;
    }

    public void setUserParams(List<Map<String, Object>> userParams) {
        this.userParams = userParams;
    }

    public Object getUserParamValue(String name) {
        if (userParams != null) {
            for (Map<String, Object> param : userParams) {
                if (param.get(name) != null) {
                    return param.get(name);
                }
            }
        }
        return null;
    }

    public Boolean isUsePreload() {
        return usePreload;
    }

    @JsonProperty("usePreload")
    public Boolean getUsePreload() {
        return usePreload;
    }

    @JsonProperty("usePreload")
    public void setUsePreload(Boolean usePreload) {
        this.usePreload = usePreload;
    }


    @JsonInclude(Include.NON_NULL)
    public String toJsonString() {
        String json = "";
        ObjectWriter ow = mapper.writer();
        try {
            json = ow.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("Unable to convert Sniro Manager Request to string", e);
        }
        return json;
    }

    @Override
    public String toString() {
        return "RequestParameters [subscriptionServiceType=" + subscriptionServiceType + ", userParams=" + userParams
                + ", aLaCarte=" + aLaCarte + "]";
    }
}
