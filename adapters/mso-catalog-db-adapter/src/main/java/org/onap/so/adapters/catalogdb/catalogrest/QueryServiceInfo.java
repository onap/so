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

import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceInfo")
public class QueryServiceInfo extends CatalogQuery {

    protected static Logger logger = LoggerFactory.getLogger(QueryServiceInfo.class);

    private ServiceInfo serviceInfo;

    private static final String TEMPLATE =
            "\n" + "\t{" + "\t\t\"id\"              : <ID>,\n" + "\t\t\"serviceInput\"     : <SERVICE_INPUT>,\n"
                    + "\t\"serviceProperties\"            : <SERVICE_PROPERTIES>,\n" + "<_SERVICEARTIFACT_>\n";


    public QueryServiceInfo() {
        super();
        this.serviceInfo = new ServiceInfo();
    }

    public QueryServiceInfo(List<ServiceInfo> serviceInfos) {
        if (!CollectionUtils.isEmpty(serviceInfos)) {
            this.serviceInfo = serviceInfos.get(0);
        }
    }

    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public String toString() {

        return serviceInfo.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        if (serviceInfo == null) {
            return "\"serviceInfo\": null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"serviceInfo\": ");
        sb.append("\n");
        Map<String, String> valueMap = new HashMap<>();
        Service service = serviceInfo.getService();
        put(valueMap, "ID", null == serviceInfo ? null : serviceInfo.getId());
        put(valueMap, "SERVICE_INPUT", null == serviceInfo ? null : serviceInfo.getServiceInput());
        put(valueMap, "SERVICE_PROPERTIES", null == serviceInfo ? null : serviceInfo.getServiceProperties());
        String subitem = new QueryServiceArtifact(service.getServiceArtifactList()).JSON2(true, true);
        valueMap.put("_SERVICEARTIFACT_", subitem.replaceAll("(?m)^", "\t\t"));
        sb.append(this.setTemplate(TEMPLATE, valueMap));
        sb.append("}");
        return sb.toString();
    }
}
