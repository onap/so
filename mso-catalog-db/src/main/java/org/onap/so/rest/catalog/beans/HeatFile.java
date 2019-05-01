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

package org.onap.so.rest.catalog.beans;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class HeatFile implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3280125018687060890L;

    private String artifactUuid;

    private String description = null;

    private String fileName;

    private String fileBody;

    private Date created;

    private String artifactChecksum;

    private String version;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("artifactUuid", artifactUuid).append("description", description)
                .append("fileName", fileName).append("fileBody", fileBody).append("created", created)
                .append("artifactChecksum", artifactChecksum).toString();
    }

    public String getArtifactUuid() {
        return this.artifactUuid;
    }

    public void setArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileBody() {
        return this.fileBody;
    }

    public void setFileBody(String fileBody) {
        this.fileBody = fileBody;
    }

    public Date getCreated() {
        return created;
    }

    public String getAsdcUuid() {
        return this.artifactUuid;
    }

    public void setAsdcUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getArtifactChecksum() {
        return artifactChecksum;
    }

    public void setArtifactChecksum(String artifactChecksum) {
        this.artifactChecksum = artifactChecksum;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
