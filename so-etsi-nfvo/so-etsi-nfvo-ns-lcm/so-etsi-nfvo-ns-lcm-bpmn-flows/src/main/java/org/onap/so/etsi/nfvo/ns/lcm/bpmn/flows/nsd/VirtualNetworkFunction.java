/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd;

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class VirtualNetworkFunction implements Serializable {

    private static final long serialVersionUID = 3164293220359211834L;

    private String vnfdId;
    private String vnfName;
    private List<String> vnfmInfoList;
    private Map<String, Object> properties = new HashMap<>();

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public VirtualNetworkFunction vnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
        return this;
    }

    public String getVnfName() {
        return vnfName;
    }

    public void setVnfName(final String vnfName) {
        this.vnfName = vnfName;
    }

    public VirtualNetworkFunction vnfName(final String vnfName) {
        this.vnfName = vnfName;
        return this;
    }

    public List<String> getVnfmInfoList() {
        return vnfmInfoList;
    }

    public void setVnfmInfoList(final List<String> vnfmInfoList) {
        this.vnfmInfoList = vnfmInfoList;
    }

    public VirtualNetworkFunction vnfmInfoList(final List<String> vnfmInfoList) {
        this.vnfmInfoList = vnfmInfoList;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public VirtualNetworkFunction properties(final Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vnfdId, vnfName, vnfmInfoList, properties);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VirtualNetworkFunction) {
            final VirtualNetworkFunction other = (VirtualNetworkFunction) obj;
            return Objects.equals(vnfdId, other.vnfdId) && Objects.equals(vnfName, other.vnfName)
                    && Objects.equals(vnfmInfoList, other.vnfmInfoList) && Objects.equals(properties, other.properties);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class VirtualNetworkFunction {\n");
        sb.append("    vnfdId: ").append(toIndentedString(vnfdId)).append("\n");
        sb.append("    vnfName: ").append(toIndentedString(vnfName)).append("\n");
        sb.append("    vnfmInfo: ").append(toIndentedString(vnfmInfoList)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");

        sb.append("}");
        return sb.toString();
    }


}
