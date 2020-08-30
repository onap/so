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
package org.onap.so.etsi.nfvo.ns.lcm.database.beans;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Entity
@Table(name = "NS_INST")
public class NfvoNsInst {

    @Id
    @Column(name = "NS_INST_ID")
    private String nsInstId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "NS_PACKAGE_ID")
    private String nsPackageId;

    @Column(name = "NSD_ID")
    private String nsdId;

    @Column(name = "NSD_INVARIANT_ID")
    private String nsdInvariantId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private State status;

    @Column(name = "STATUS_UPDATED_TIME")
    private LocalDateTime statusUpdatedTime;

    @Column(name = "GLOBAL_CUSTOMER_ID")
    private String globalCustomerId;

    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @OneToMany(mappedBy = "nsInst", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NfvoNfInst> nfvoNfInsts = new ArrayList<>();

    @OneToMany(mappedBy = "nfvoNsInst", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NsLcmOpOcc> nsLcmOpOccs = new ArrayList<>();

    public NfvoNsInst() {
        this.nsInstId = UUID.randomUUID().toString();
    }

    public String getNsInstId() {
        return nsInstId;
    }

    public void setNsInstId(final String nsInstId) {
        this.nsInstId = nsInstId;
    }

    public NfvoNsInst nsInstId(final String nsInstId) {
        this.nsInstId = nsInstId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NfvoNsInst name(final String name) {
        this.name = name;
        return this;
    }

    public String getNsPackageId() {
        return nsPackageId;
    }

    public void setNsPackageId(final String nsPackageId) {
        this.nsPackageId = nsPackageId;
    }

    public NfvoNsInst nsPackageId(final String nsPackageId) {
        this.nsPackageId = nsPackageId;
        return this;
    }

    public String getNsdId() {
        return nsdId;
    }

    public void setNsdId(final String nsdId) {
        this.nsdId = nsdId;
    }

    public NfvoNsInst nsdId(final String nsdId) {
        this.nsdId = nsdId;
        return this;
    }

    public String getNsdInvariantId() {
        return nsdInvariantId;
    }

    public void setNsdInvariantId(final String nsdInvariantId) {
        this.nsdInvariantId = nsdInvariantId;
    }

    public NfvoNsInst nsdInvariantId(final String nsdInvariantId) {
        this.nsdInvariantId = nsdInvariantId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public NfvoNsInst description(final String description) {
        this.description = description;
        return this;
    }

    public State getStatus() {
        return status;
    }

    public void setStatus(final State status) {
        this.status = status;
    }

    public NfvoNsInst status(final State status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(final LocalDateTime statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public NfvoNsInst statusUpdatedTime(final LocalDateTime statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
        return this;
    }

    public String getGlobalCustomerId() {
        return globalCustomerId;
    }

    public void setGlobalCustomerId(final String globalCustomerId) {
        this.globalCustomerId = globalCustomerId;
    }

    public NfvoNsInst globalCustomerId(final String globalCustomerId) {
        this.globalCustomerId = globalCustomerId;
        return this;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    public NfvoNsInst serviceType(final String serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public List<NfvoNfInst> getNfvoNfInsts() {
        return nfvoNfInsts;
    }

    public void setNfvoNfInsts(final List<NfvoNfInst> nfvoNfInsts) {
        this.nfvoNfInsts = nfvoNfInsts;
    }

    public NfvoNsInst nfvoNfInsts(final NfvoNfInst nfvoNfInsts) {
        nfvoNfInsts.nfvoNsInst(this);
        this.nfvoNfInsts.add(nfvoNfInsts);
        return this;
    }

    public List<NsLcmOpOcc> getNsLcmOpOccs() {
        return nsLcmOpOccs;
    }

    public void setNsLcmOpOccs(final List<NsLcmOpOcc> nsLcmOpOccs) {
        this.nsLcmOpOccs = nsLcmOpOccs;
    }

    public NfvoNsInst nsLcmOpOccs(final NsLcmOpOcc nsLcmOpOcc) {
        nsLcmOpOcc.nfvoNsInst(this);
        this.nsLcmOpOccs.add(nsLcmOpOcc);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nsInstId, name, nsPackageId, nsdId, nsdInvariantId, description, status, statusUpdatedTime,
                globalCustomerId, serviceType, nfvoNfInsts, nsLcmOpOccs);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof NfvoNsInst) {
            final NfvoNsInst other = (NfvoNsInst) obj;
            return Objects.equals(nsInstId, other.nsInstId) && Objects.equals(name, other.name)
                    && Objects.equals(nsPackageId, other.nsPackageId) && Objects.equals(nsdId, other.nsdId)
                    && Objects.equals(nsdInvariantId, other.nsdInvariantId)
                    && Objects.equals(description, other.description) && Objects.equals(status, other.status)
                    && Objects.equals(statusUpdatedTime, other.statusUpdatedTime)
                    && Objects.equals(globalCustomerId, other.globalCustomerId)
                    && Objects.equals(serviceType, other.serviceType) && Objects.equals(nfvoNfInsts, other.nfvoNfInsts)
                    && Objects.equals(nsLcmOpOccs, other.nsLcmOpOccs);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NfvoNsInst {\n");
        sb.append("    nsInstId: ").append(toIndentedString(nsInstId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nsPackageId: ").append(toIndentedString(nsPackageId)).append("\n");
        sb.append("    nsdId: ").append(toIndentedString(nsdId)).append("\n");
        sb.append("    nsdInvariantId: ").append(toIndentedString(nsdInvariantId)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    statusUpdatedTime: ").append(toIndentedString(statusUpdatedTime)).append("\n");
        sb.append("    globalCustomerId: ").append(toIndentedString(globalCustomerId)).append("\n");
        sb.append("    serviceType: ").append(toIndentedString(serviceType)).append("\n");
        sb.append("    nfvoNfInsts: ").append(toIndentedString(nfvoNfInsts)).append("\n");
        sb.append("    nsLcmOpOccs: ").append(toIndentedString(nsLcmOpOccs)).append("\n");

        sb.append("}");
        return sb.toString();
    }

}
