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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.tasks;

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.Tenant;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class CreateInstantiateRequest implements Serializable {
    private static final long serialVersionUID = -4371264952509580468L;
    private String nsInstId;
    private String vnfdId;
    private String vnfName;
    private String vnfPkgId;
    private String nfType;
    private Tenant tenant;
    private Map<String, String> additionalParams;

    public String getNsInstId() {
        return nsInstId;
    }

    public void setNsInstId(final String nsInstId) {
        this.nsInstId = nsInstId;
    }

    public CreateInstantiateRequest nsInstId(final String nsInstId) {
        this.nsInstId = nsInstId;
        return this;
    }

    public final String getVnfdId() {
        return vnfdId;
    }

    public final void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public CreateInstantiateRequest vnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
        return this;
    }

    public final String getVnfName() {
        return vnfName;
    }

    public final void setVnfName(final String vnfName) {
        this.vnfName = vnfName;
    }

    public CreateInstantiateRequest vnfName(final String vnfName) {
        this.vnfName = vnfName;
        return this;
    }

    public final String getVnfPkgId() {
        return vnfPkgId;
    }

    public final void setVnfPkgId(final String vnfPkgId) {
        this.vnfPkgId = vnfPkgId;
    }

    public CreateInstantiateRequest vnfPkgId(final String vnfPkgId) {
        this.vnfPkgId = vnfPkgId;
        return this;
    }

    public final String getNfType() {
        return nfType;
    }

    public final void setNfType(final String nfType) {
        this.nfType = nfType;
    }

    public CreateInstantiateRequest nfType(final String nfType) {
        this.nfType = nfType;
        return this;
    }

    public final Tenant getTenant() {
        return tenant;
    }

    public final void setTenant(final Tenant tenant) {
        this.tenant = tenant;
    }

    public CreateInstantiateRequest tenant(final Tenant tenant) {
        this.tenant = tenant;
        return this;
    }

    public final Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public final void setAdditionalParams(final Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public CreateInstantiateRequest additionalParams(final Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nsInstId, vnfdId, vnfName, vnfPkgId, nfType, tenant, additionalParams);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CreateInstantiateRequest) {
            final CreateInstantiateRequest other = (CreateInstantiateRequest) obj;
            return Objects.equals(nsInstId, other.nsInstId) && Objects.equals(vnfdId, other.vnfdId)
                    && Objects.equals(vnfName, other.vnfName) && Objects.equals(vnfPkgId, other.vnfPkgId)
                    && Objects.equals(nfType, other.nfType) && Objects.equals(tenant, other.tenant)
                    && Objects.equals(additionalParams, other.additionalParams);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class CreateInstantiateRequest {\n");
        sb.append("    nsInstId: ").append(toIndentedString(nsInstId)).append("\n");
        sb.append("    vnfdId: ").append(toIndentedString(vnfdId)).append("\n");
        sb.append("    vnfName: ").append(toIndentedString(vnfName)).append("\n");
        sb.append("    vnfPkgId: ").append(toIndentedString(vnfPkgId)).append("\n");
        sb.append("    nfType: ").append(toIndentedString(nfType)).append("\n");
        sb.append("    tenant: ").append(toIndentedString(tenant)).append("\n");
        sb.append("    additionalParams: ").append(toIndentedString(additionalParams)).append("\n");

        sb.append("}");
        return sb.toString();
    }


}
