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

package org.onap.aaiclient.client.aai;

import java.io.Serializable;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectPlurals;
import com.google.common.base.CaseFormat;

public class AAIObjectPlurals implements AAIObjectBase, GraphInventoryObjectPlurals, Serializable {

    private static final long serialVersionUID = 5312713297525740746L;

    private final String uriTemplate;
    private final String partialUri;
    private final String name;

    protected AAIObjectPlurals(AAIObjectType type, String parentUri, String partialUri) {
        this.uriTemplate = parentUri + partialUri;
        this.partialUri = partialUri;
        this.name = type.typeName();
    }

    public AAIObjectPlurals(String name, String parentUri, String partialUri) {
        this.uriTemplate = parentUri + partialUri;
        this.partialUri = partialUri;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.uriTemplate();
    }

    @Override
    public String uriTemplate() {
        return this.uriTemplate;
    }

    @Override
    public String partialUri() {
        return this.partialUri;
    }

    @Override
    public String typeName() {
        return this.typeName(CaseFormat.LOWER_HYPHEN);
    }

    @Override
    public String typeName(CaseFormat format) {
        return CaseFormat.LOWER_HYPHEN.to(format, this.name.replace("default-", ""));
    }

    @Override
    public int hashCode() {
        return this.typeName().hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof AAIObjectBase) {
            return this.typeName().equals(((AAIObjectBase) o).typeName());
        }

        return false;
    }
}
