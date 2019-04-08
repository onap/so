/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.vdu;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class VduArtifact {

    // Enumerate the types of artifacts permitted. This may need to be a variable string
    // value if arbitrary (cloud-specific) artifacts may be attached to VDUs in ASDC.
    public enum ArtifactType {
        MAIN_TEMPLATE, NESTED_TEMPLATE, CONFIG_FILE, SCRIPT_FILE, TEXT_FILE, ENVIRONMENT
    }

    private String name;
    private byte[] content;
    private ArtifactType type;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VduArtifact)) {
            return false;
        }
        VduArtifact castOther = (VduArtifact) other;
        return new EqualsBuilder().append(name, castOther.name).append(content, castOther.content)
                .append(type, castOther.type).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(content).append(type).toHashCode();
    }

    // Default constructor
    public VduArtifact() {}

    // Fully specified constructor
    public VduArtifact(String name, byte[] content, ArtifactType type) {
        this.name = name;
        this.content = content;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public ArtifactType getType() {
        return type;
    }

    public void setType(ArtifactType type) {
        this.type = type;
    }


}
