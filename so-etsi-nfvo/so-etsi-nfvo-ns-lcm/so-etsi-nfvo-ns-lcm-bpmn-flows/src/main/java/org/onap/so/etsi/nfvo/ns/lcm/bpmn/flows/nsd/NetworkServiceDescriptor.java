/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class NetworkServiceDescriptor implements Serializable {

    private static final long serialVersionUID = -1739293595041180242L;

    private String type;

    private Map<String, Object> properties = new HashMap<>();

    private List<VirtualNetworkFunction> vnfs = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public NetworkServiceDescriptor type(final String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public NetworkServiceDescriptor properties(final Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public List<VirtualNetworkFunction> getVnfs() {
        return vnfs;
    }

    public void setVnfs(final List<VirtualNetworkFunction> vnfs) {
        if (vnfs != null) {
            this.vnfs = vnfs;
        } else {
            this.vnfs = new ArrayList<>();
        }
    }

    public NetworkServiceDescriptor addVnfPkgIdsItem(final VirtualNetworkFunction vnf) {
        if (this.vnfs == null) {
            this.vnfs = new ArrayList<>();
        }
        this.vnfs.add(vnf);
        return this;
    }

    public NetworkServiceDescriptor vnfs(final List<VirtualNetworkFunction> vnfs) {
        this.vnfs = vnfs;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties, vnfs);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof NetworkServiceDescriptor) {
            final NetworkServiceDescriptor other = (NetworkServiceDescriptor) obj;
            return Objects.equals(type, other.type) && Objects.equals(properties, other.properties)
                    && Objects.equals(vnfs, other.vnfs);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NetworkServiceDescriptor {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    vnfs: ").append(toIndentedString(vnfs)).append("\n");
        sb.append("}");
        return sb.toString();
    }

}
