/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.request.beans;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "site_status")
public class SiteStatus {

    @Column(name = "STATUS")
    private boolean status;
    @Id
    @Column(name = "SITE_NAME")
    private String siteName;
    @Column(name = "CREATION_TIMESTAMP", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public SiteStatus() {}

    public SiteStatus(String siteName) {
        this.siteName = siteName;
    }

    public Date getCreated() {
        return created;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    @PrePersist
    protected void createdAt() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SiteStatus)) {
            return false;
        }
        SiteStatus castOther = (SiteStatus) other;
        return Objects.equals(getSiteName(), castOther.getSiteName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSiteName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", getStatus()).append("siteName", getSiteName())
                .append("created", getCreated()).toString();
    }
}
