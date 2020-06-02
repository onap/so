/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.common.resource;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.VnfResource;

public class InstanceResourceList {

    private InstanceResourceList() {
        throw new IllegalStateException("Utility class");
    }

    // this method returns key from resource input
    // e.g. {\"sdwansite_emails\" : \"[sdwansiteresource_list(PK), INDEX, sdwansite_emails]|default\",
    // ....}
    // it will return sdwansiteresource_list
    private static String getPrimaryKey(Resource resource) {
        String resourceInput = "";
        if (resource instanceof VnfResource) {
            resourceInput = ((VnfResource) resource).getResourceInput();
        } else if (resource instanceof GroupResource) {
            resourceInput = ((GroupResource) resource).getVnfcs().get(0).getResourceInput();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> map = gson.fromJson(resourceInput, type);

        if (map != null) {
            Optional<String> pkOpt = map.values().stream().filter(e -> e.contains("[")).map(e -> e.replace("[", ""))
                    .map(e -> e.split(",")[0]).findFirst();

            return pkOpt.isPresent() ? pkOpt.get() : "";
        } else {
            return "";
        }
    }


    public static List<Resource> getInstanceResourceList(final VnfResource vnfResource, final String uuiRequest) {
        List<Resource> sequencedResourceList = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject servJsonObject = gson.fromJson(uuiRequest, JsonObject.class);
        JsonObject reqInputJsonObj = servJsonObject.getAsJsonObject("service").getAsJsonObject("parameters")
                .getAsJsonObject("requestInputs");

        String pk = getPrimaryKey(vnfResource);
        // if pk is not empty that means it can contain list of VNF
        if (!pk.isEmpty()) {
            JsonElement vfNode = reqInputJsonObj.get(pk);
            if (vfNode.isJsonArray()) {
                // multiple instance of VNF
                JsonArray vfNodeList = vfNode.getAsJsonArray();
                for (JsonElement vf : vfNodeList) {
                    JsonObject vfObj = vf.getAsJsonObject();

                    // Add VF first before adding groups
                    sequencedResourceList.add(vnfResource);
                    List<Resource> sequencedGroupResourceList = getGroupResourceInstanceList(vnfResource, vfObj);
                    if (!sequencedGroupResourceList.isEmpty()) {
                        sequencedResourceList.addAll(sequencedGroupResourceList);
                    }
                }
            }
        } else {
            // if pk is empty that means it has only one VNF Node
            // Add VF first before adding groups
            sequencedResourceList.add(vnfResource);
            // check the groups for this VNF and add into resource list
            List<Resource> sequencedGroupResourceList = getGroupResourceInstanceList(vnfResource, reqInputJsonObj);
            if (!sequencedGroupResourceList.isEmpty()) {
                sequencedResourceList.addAll(sequencedGroupResourceList);
            }
        }

        // In negative case consider only VNF resource only
        if (sequencedResourceList.isEmpty()) {
            sequencedResourceList.add(vnfResource);
        }

        // check if the resource contains vf-module
        if (isVnfResourceWithVfModule(vnfResource)) {
            sequencedResourceList.addAll(vnfResource.getVfModules());
        }

        return sequencedResourceList;
    }

    private static boolean isVnfResourceWithVfModule(VnfResource vnfResource) {
        return vnfResource != null && vnfResource.getVfModules() != null;
    }

    private static List<Resource> getGroupResourceInstanceList(VnfResource vnfResource, JsonObject vfObj) {
        List<Resource> sequencedResourceList = new ArrayList<>();
        if (isVnfGroupOrderFilled(vnfResource)) {
            String[] grpSequence = vnfResource.getGroupOrder().split(",");
            for (String grpType : grpSequence) {
                for (GroupResource gResource : vnfResource.getGroups()) {
                    if (StringUtils.containsIgnoreCase(gResource.getModelInfo().getModelName(), grpType)) {
                        // check the number of group instances from UUI to be added
                        String sk = getPrimaryKey(gResource);

                        // if sk is empty that means it is not list type
                        // only one group / vnfc to be considered
                        if (sk.isEmpty()) {
                            sequencedResourceList.add(gResource);
                        } else {
                            // check the number of list size of VNFC of a group
                            JsonElement vfcNode = vfObj.get(sk);
                            if (vfcNode.isJsonArray()) {
                                JsonArray vfcList = vfcNode.getAsJsonArray();
                                for (JsonElement vfc : vfcList) {
                                    sequencedResourceList.add(gResource);
                                }
                            } else {
                                // consider only one vnfc/group if not an array
                                sequencedResourceList.add(gResource);
                            }
                        }

                    }
                }
            }
        }
        return sequencedResourceList;
    }

    private static boolean isVnfGroupOrderFilled(VnfResource vnfResource) {
        return vnfResource.getGroupOrder() != null && !StringUtils.isEmpty(vnfResource.getGroupOrder());
    }
}
