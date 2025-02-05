/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.db.request.beans;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "instance_nfvo_mapping")
public class InstanceNfvoMapping implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "INSTANCE_ID")
    private String instanceId;
    @Column(name = "NFVO_NAME", length = 256)
    private String nfvoName;
    @Column(name = "ENDPOINT", length = 256)
    private String endpoint;
    @Column(name = "USERNAME", length = 256)
    private String username;
    @Column(name = "PASSWORD", length = 256)
    private String password;
    @Column(name = "API_ROOT", length = 256)
    private String apiRoot;
    @Column(name = "JOB_ID", length = 256)
    private String jobId;

    public InstanceNfvoMapping() {}

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InstanceNfvoMapping)) {
            return false;
        }
        final InstanceNfvoMapping castOther = (InstanceNfvoMapping) other;
        return Objects.equals(getInstanceId(), castOther.getInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceId());
    }


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNfvoName() {
        return nfvoName;
    }

    public void setNfvoName(String nfvoName) {
        this.nfvoName = nfvoName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiRoot() {
        return apiRoot;
    }

    public void setApiRoot(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public String toString() {
        return "InstanceNfvoMapping{" + "instanceId='" + instanceId + '\'' + ", nfvoName='" + nfvoName + '\''
                + ", endpoint='" + endpoint + '\'' + ", username='" + username + '\'' + ", password='" + password + '\''
                + ", apiRoot='" + apiRoot + '\'' + '}';
    }
}
