/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vim.model;

import java.util.Objects;

public class AccessInfo {

    protected String projectId;
    protected String projectName;
    protected String domainName;
    protected VimCredentials credentials;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String value) {
        projectId = value;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String value) {
        projectName = value;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String value) {
        domainName = value;
    }

    public VimCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(final VimCredentials value) {
        credentials = value;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AccessInfo accessInfo = (AccessInfo) o;
        return Objects.equals(this.projectId, accessInfo.projectId)
                && Objects.equals(this.projectName, accessInfo.projectName)
                && Objects.equals(this.domainName, accessInfo.domainName)
                && Objects.equals(this.credentials, accessInfo.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, projectName, domainName, credentials);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class AccessInfo {\n");

        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
        sb.append("    domainName: ").append(toIndentedString(domainName)).append("\n");
        sb.append("    credentials: ").append(toIndentedString(credentials)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(final java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
