/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.rest.beans.ServiceMacroHolder;

@XmlRootElement(name = "serviceResources")
public class QueryServiceMacroHolder extends CatalogQuery {
    private ServiceMacroHolder serviceMacroHolder;
    private static final String LINE_BEGINNING = "(?m)^";
    private static final String TEMPLATE = "{ \"serviceResources\"    : {\n" + "\t\"modelInfo\"       : {\n"
            + "\t\t\"modelName\"          : <SERVICE_MODEL_NAME>,\n"
            + "\t\t\"modelUuid\"          : <SERVICE_MODEL_UUID>,\n"
            + "\t\t\"modelInvariantUuid\" : <SERVICE_MODEL_INVARIANT_ID>,\n"
            + "\t\t\"modelVersion\"       : <SERVICE_MODEL_VERSION>\n" + "\t},\n"
            + "\t\"serviceCategory\"    : <SERVICE_CATEGORY>,\n" + "\t\"serviceType\"        : <SERVICE_TYPE>,\n"
            + "\t\"serviceRole\"        : <SERVICE_ROLE>,\n" + "\t\"environmentContext\" : <ENVIRONMENT_CONTEXT>,\n"
            + "\t\"resourceOrder\"      : <RESOURCE_ORDER>,\n" + "\t\"workloadContext\"    : <WORKLOAD_CONTEXT>,\n"
            + "<_SERVICEPNFS_>,\n" + "<_SERVICEVNFS_>,\n" + "<_SERVICENETWORKS_>,\n" + "<_SERVICEINFO_>,\n"
            + "<_SERVICEPROXY_>,\n" + "<_SERVICEALLOTTEDRESOURCES_>\n" + "\t}}";

    public QueryServiceMacroHolder() {
        super();
        serviceMacroHolder = new ServiceMacroHolder();
    }

    public QueryServiceMacroHolder(ServiceMacroHolder vlist) {
        serviceMacroHolder = vlist;
    }

    public ServiceMacroHolder getServiceResources() {
        return this.serviceMacroHolder;
    }

    public void setServiceResources(ServiceMacroHolder v) {
        this.serviceMacroHolder = v;
    }

    @Override
    public String toString() {
        return serviceMacroHolder.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean x) {
        Service service = serviceMacroHolder.getService();
        if (service == null) {
            return "\"serviceResources\": null";
        }

        StringBuilder buf = new StringBuilder();
        Map<String, String> valueMap = new HashMap<>();

        put(valueMap, "SERVICE_MODEL_NAME", service.getModelName());
        put(valueMap, "SERVICE_MODEL_UUID", service.getModelUUID());
        put(valueMap, "SERVICE_MODEL_INVARIANT_ID", service.getModelInvariantUUID());
        put(valueMap, "SERVICE_MODEL_VERSION", service.getModelVersion());
        put(valueMap, "SERVICE_TYPE", service.getServiceType());
        put(valueMap, "SERVICE_ROLE", service.getServiceRole());
        put(valueMap, "SERVICE_CATEGORY", service.getCategory());
        put(valueMap, "ENVIRONMENT_CONTEXT", service.getEnvironmentContext());
        put(valueMap, "WORKLOAD_CONTEXT", service.getWorkloadContext());
        put(valueMap, "RESOURCE_ORDER", service.getResourceOrder());

        String subitem;
        subitem = new QueryServicePnfs(service.getPnfCustomizations()).JSON2(true, true);
        valueMap.put("_SERVICEPNFS_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        subitem = new QueryServiceVnfs(service.getVnfCustomizations()).JSON2(true, true);
        valueMap.put("_SERVICEVNFS_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        subitem = new QueryServiceNetworks(service.getNetworkCustomizations()).JSON2(true, true);
        valueMap.put("_SERVICENETWORKS_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        subitem = new QueryAllottedResourceCustomization(service.getAllottedCustomizations()).JSON2(true, true);
        valueMap.put("_SERVICEALLOTTEDRESOURCES_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        subitem = new QueryServiceInfo(service.getServiceInfos()).JSON2(true, true);
        valueMap.put("_SERVICEINFO_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        subitem = new QueryServiceProxyCustomization(service.getServiceProxyCustomizations()).JSON2(true, true);
        valueMap.put("_SERVICEPROXY_", subitem.replaceAll(LINE_BEGINNING, "\t"));

        buf.append(this.setTemplate(TEMPLATE, valueMap));
        return buf.toString();
    }

}
