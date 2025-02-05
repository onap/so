/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceArtifacts")
public class QueryServiceArtifact extends CatalogQuery {

    protected static Logger logger = LoggerFactory.getLogger(QueryServiceArtifact.class);

    private List<ServiceArtifact> serviceArtifactList;

    private static final String TEMPLATE = "\t{\n" + "\t\t\"artifactUUID\"         : <ARTIFACT_UUID>,\n"
            + "\t\t\"name\"                 : <NAME>,\n" + "\t\t\"version\"              : <VERSION>,\n"
            + "\t\t\"checksum\"     : <CHECKSUM>,\n" + "\t\t\"type\"                  : <TYPE>,\n"
            + "\t\t\"content\"     : <CONTENT>,\n" + "\t\t\"description\"          : <DESCRIPTION>\n" + "\t}";

    public QueryServiceArtifact() {
        super();
        serviceArtifactList = new ArrayList<>();
    }

    public QueryServiceArtifact(List<ServiceArtifact> alist) {
        serviceArtifactList = new ArrayList<>();
        for (ServiceArtifact o : alist) {
            if (logger.isDebugEnabled())
                logger.debug(o.toString());
            serviceArtifactList.add(o);
        }
    }

    public List<ServiceArtifact> getServiceArtifact() {
        return this.serviceArtifactList;
    }

    public void setServiceArtifact(List<ServiceArtifact> a) {
        this.serviceArtifactList = a;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (ServiceArtifact o : serviceArtifactList) {
            sb.append(i).append("\t");
            if (!first)
                sb.append("\n");
            first = false;
            sb.append(o);
        }
        return sb.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray)
            sb.append("{ ");
        if (isArray)
            sb.append("\"serviceArtifact\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (ServiceArtifact o : serviceArtifactList) {
            if (first)
                sb.append("\n");
            first = false;

            boolean vrNull = o == null;

            put(valueMap, "ARTIFACT_UUID", vrNull ? null : o.getArtifactUUID());
            put(valueMap, "TYPE", vrNull ? null : o.getType());
            put(valueMap, "NAME", vrNull ? null : o.getName());
            put(valueMap, "VERSION", vrNull ? null : o.getVersion());
            put(valueMap, "DESCRIPTION", vrNull ? null : o.getDescription());
            put(valueMap, "CONTENT", vrNull ? null : o.getContent());
            put(valueMap, "CHECKSUM", vrNull ? null : o.getChecksum());
            sb.append(sep).append(this.setTemplate(TEMPLATE, valueMap));
            sep = ",\n";
        }
        if (!first)
            sb.append("\n");
        if (isArray)
            sb.append("]");
        if (!isEmbed && isArray)
            sb.append("}");
        return sb.toString();
    }
}
