/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("esr-thirdparty-sdnc")
public class EsrThirdpartySdnc extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = -7493461787172382640L;

    @Id
    @JsonProperty("thirdparty-sdnc-id")
    private String thirdpartySdncId;
    @JsonProperty("location")
    private String location;
    @JsonProperty("product-name")
    private String productName;
    @JsonProperty("resource-version")
    private String resourceVersion;
    @JsonProperty("domain-type")
    private String domainType;
    @JsonProperty("esr-system-info-list")
    private EsrSystemInfoList esrSystemInfoList;

    public String getThirdpartySdncId() {
        return thirdpartySdncId;
    }

    public void setThirdpartySdncId(String name) {
        this.thirdpartySdncId = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String name) {
        this.productName = name;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String version) {
        this.resourceVersion = version;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String type) {
        this.domainType = type;
    }

    public EsrSystemInfoList getEsrSystemInfoList() {
        return esrSystemInfoList;
    }

    public void setEsrSystemInfoList(EsrSystemInfoList esrList) {
        this.esrSystemInfoList = esrList;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((thirdpartySdncId == null) ? 0 : thirdpartySdncId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EsrThirdpartySdnc other = (EsrThirdpartySdnc) obj;
        if (thirdpartySdncId == null) {
            if (other.thirdpartySdncId != null)
                return false;
        } else if (!thirdpartySdncId.equals(other.thirdpartySdncId))
            return false;
        return true;
    }


}
