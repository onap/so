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

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author mukeshsharma (mukeshsharma@est.tech)
 */
@Entity
@Table(name = "NF_INST")
public class NfvoNfInst {

    @Id
    @Column(name = "NF_INST_ID")
    private String nfInstId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "VNFD_ID")
    private String vnfdId;

    @Column(name = "PACKAGE_ID")
    private String packageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NS_INST_ID")
    private NfvoNsInst nsInst;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private State status;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;

    @Column(name = "LAST_UPDATE_TIME")
    private LocalDateTime lastUpdateTime;

    public NfvoNfInst() {
        this.nfInstId = UUID.randomUUID().toString();
    }

    public String getNfInstId() {
        return nfInstId;
    }

    public void setNfInstId(final String nfInstId) {
        this.nfInstId = nfInstId;
    }

    public NfvoNfInst nfInstId(final String nfInstId) {
        this.nfInstId = nfInstId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NfvoNfInst name(final String name) {
        this.name = name;
        return this;
    }

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public NfvoNfInst vnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
        return this;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(final String packageId) {
        this.packageId = packageId;
    }

    public NfvoNfInst packageId(final String packageId) {
        this.packageId = packageId;
        return this;
    }

    public NfvoNsInst getNsInst() {
        return nsInst;
    }

    public void setNsInst(final NfvoNsInst nsInst) {
        this.nsInst = nsInst;
    }

    public NfvoNfInst nfvoNsInst(final NfvoNsInst nfvoNsInst) {
        this.nsInst = nfvoNsInst;
        return this;
    }

    public State getStatus() {
        return status;
    }

    public void setStatus(final State status) {
        this.status = status;
    }

    public NfvoNfInst status(final State status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public NfvoNfInst createTime(final LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public NfvoNfInst lastUpdateTime(final LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        final NfvoNfInst that = (NfvoNfInst) object;
        return Objects.equals(nfInstId, that.nfInstId) && Objects.equals(name, that.name)
                && Objects.equals(vnfdId, that.vnfdId) && Objects.equals(packageId, that.packageId)
                && Objects.equals(nsInst, that.nsInst) && Objects.equals(status, that.status)
                && Objects.equals(createTime, that.createTime) && Objects.equals(lastUpdateTime, that.lastUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nfInstId, name, vnfdId, packageId, nsInst, status, createTime, lastUpdateTime);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NfvoNfInst {\n");
        sb.append("    nfInstId: ").append(toIndentedString(nfInstId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    vnfdId: ").append(toIndentedString(vnfdId)).append("\n");
        sb.append("    packageId: ").append(toIndentedString(packageId)).append("\n");
        sb.append("    nsInst: ").append(nsInst != null ? toIndentedString(nsInst.getNsInstId()) : null).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    lastUpdateTime: ").append(toIndentedString(lastUpdateTime)).append("\n");

        sb.append("}");
        return sb.toString();
    }

}
