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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd;

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class ToscaMetadata {

    private Map<String, String> entries = new HashMap<>();

    public Map<String, String> getEntries() {
        return entries;
    }

    public void addEntry(final String name, final String value) {
        this.entries.put(name, value);
    }

    public boolean hasEntry(final String name) {
        return this.entries.containsKey(name);
    }

    public String getEntry(final String name) {
        return this.entries.get(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ToscaMetadata) {
            final ToscaMetadata other = (ToscaMetadata) obj;
            return Objects.equals(entries, other.entries);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class ToscaMetadata {\n");
        sb.append("    entries: ").append(toIndentedString(entries)).append("\n");
        sb.append("}");
        return sb.toString();
    }


}
