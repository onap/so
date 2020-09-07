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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class FileEntry {

    private boolean isDirectory;
    private String filePath;
    private byte[] fileContent;

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(final boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public FileEntry isDirectory(final boolean isDirectory) {
        this.isDirectory = isDirectory;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilename(final String filePath) {
        this.filePath = filePath;
    }

    public FileEntry filePath(final String filePath) {
        this.filePath = filePath;
        return this;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(final byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public FileEntry fileContent(final byte[] fileContent) {
        this.fileContent = fileContent;
        return this;
    }

    public InputStream getFileContentAsStream() {
        if (fileContent == null || fileContent.length == 0) {
            return null;
        }
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDirectory, filePath, fileContent);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FileEntry) {
            final FileEntry other = (FileEntry) obj;
            return Objects.equals(isDirectory, other.isDirectory) && Objects.equals(filePath, other.filePath)
                    && Objects.equals(fileContent, other.fileContent);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class FileEntry {\n");
        sb.append("    isDirectory: ").append(toIndentedString(isDirectory)).append("\n");
        sb.append("    filePath: ").append(toIndentedString(filePath)).append("\n");
        sb.append("    fileContent size: ").append(toIndentedString(fileContent != null ? fileContent.length : 0))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }


}
