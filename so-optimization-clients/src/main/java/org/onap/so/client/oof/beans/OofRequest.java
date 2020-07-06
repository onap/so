/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;


public class OofRequest implements Serializable {

    private static final long serialVersionUID = -1541132882892163132L;
    private static final Logger logger = LoggerFactory.getLogger(OofRequest.class);


    @JsonProperty("requestInfo")
    private RequestInfo requestInformation;

    @JsonProperty("serviceInfo")
    private ServiceInfo serviceInformation;

    @JsonProperty("placementInfo")
    private PlacementInfo placementInformation;

    @JsonProperty("licenseInfo")
    private LicenseInfo licenseInformation;


    public RequestInfo getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(RequestInfo requestInformation) {
        this.requestInformation = requestInformation;
    }

    public ServiceInfo getServiceInformation() {
        return serviceInformation;
    }

    public void setServiceInformation(ServiceInfo serviceInformation) {
        this.serviceInformation = serviceInformation;
    }

    public PlacementInfo getPlacementInformation() {
        return placementInformation;
    }

    public void setPlacementInformation(PlacementInfo placementInformation) {
        this.placementInformation = placementInformation;
    }

    public LicenseInfo getLicenseInformation() {
        return licenseInformation;
    }

    public void setLicenseInformation(LicenseInfo licenseInformation) {
        this.licenseInformation = licenseInformation;
    }


    @JsonInclude(Include.NON_NULL)
    public String toJsonString() {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        try {
            json = ow.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("Unable to convert oofRequest to string", e);
        }
        return json.replaceAll("\\\\", "");
    }


}
