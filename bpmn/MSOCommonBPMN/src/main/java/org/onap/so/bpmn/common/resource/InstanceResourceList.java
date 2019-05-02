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
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ResourceType;
import org.onap.so.bpmn.core.domain.VnfResource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InstanceResourceList {

    private static Map<String, List<List<GroupResource>>> convertUUIReqTOStd(final JsonObject reqInputJsonObj,
            List<Resource> seqResourceList) {

        Map<String, List<List<GroupResource>>> normalizedRequest = new HashMap<>();
        for (Resource r : seqResourceList) {

            if (r.getResourceType() == ResourceType.VNF) {
                String pk = getPrimaryKey(r);

                JsonElement vfNode = reqInputJsonObj.get(pk);

                // if the service property is type of array then it
                // means it is a VF resource
                if (vfNode instanceof JsonArray) {

                    for (int i = 0; i < ((JsonArray) vfNode).size(); i++) {
                        List<List<GroupResource>> groupsList = normalizedRequest.get(pk);
                        if (groupsList == null) {
                            groupsList = new ArrayList<>();
                            normalizedRequest.put(pk, groupsList);
                        }

                        groupsList.add(new ArrayList<>());
                    }
                }

            } else if (r.getResourceType() == ResourceType.GROUP) {
                String sk = getPrimaryKey(r);

                for (String pk : normalizedRequest.keySet()) {
                    JsonArray vfs = reqInputJsonObj.getAsJsonArray(pk);

                    for (int i = 0; i < vfs.size(); i++) {

                        JsonElement vfcsNode = vfs.get(i).getAsJsonObject().get(sk);
                        if (vfcsNode instanceof JsonArray) {

                            List<GroupResource> groupResources = normalizedRequest.get(pk).get(i);

                            if (groupResources == null) {
                                groupResources = new ArrayList<>();
                                normalizedRequest.get(pk).add(i, groupResources);
                            }

                            for (int j = 0; j < ((JsonArray) vfcsNode).size(); j++) {
                                groupResources.add((GroupResource) r);
                            }
                        }
                    }
                }
            }
        }
        return normalizedRequest;
    }

    // this method returns key from resource input
    // e.g. {\"sdwansite_emails\" : \"[sdwansiteresource_list(PK), INDEX, sdwansite_emails]|default\",
    // ....}
    // it will return sdwansiteresource_list
    private static String getPrimaryKey(Resource resource) {
        String pk = "";

        String resourceInput = "";
        if (resource instanceof VnfResource) {
            resourceInput = ((VnfResource) resource).getResourceInput();
        } else if (resource instanceof GroupResource) {
            resourceInput = ((GroupResource) resource).getVnfcs().get(0).getResourceInput();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> map = gson.fromJson(resourceInput, type);

        Optional<String> pkOpt = map.values().stream().filter(e -> e.contains("[")).map(e -> e.replace("[", ""))
                .map(e -> e.split(",")[0]).findFirst();

        return pkOpt.isPresent() ? pkOpt.get() : "";
    }

    private static List<Resource> convertToInstanceResourceList(Map<String, List<List<GroupResource>>> normalizedReq,
            List<Resource> seqResourceList) {
        List<Resource> flatResourceList = new ArrayList<>();
        for (Resource r : seqResourceList) {
            if (r.getResourceType() == ResourceType.VNF) {
                String primaryKey = getPrimaryKey(r);
                for (String pk : normalizedReq.keySet()) {

                    if (primaryKey.equalsIgnoreCase(pk)) {

                        List<List<GroupResource>> vfs = normalizedReq.get(pk);

                        vfs.stream().forEach(e -> {
                            flatResourceList.add(r);
                            flatResourceList.addAll(e);
                        });
                    }
                }
            }
        }
        return flatResourceList;
    }

    public static List<Resource> getInstanceResourceList(final List<Resource> seqResourceList,
            final String uuiRequest) {

        Gson gson = new Gson();
        JsonObject servJsonObject = gson.fromJson(uuiRequest, JsonObject.class);
        JsonObject reqInputJsonObj = servJsonObject.getAsJsonObject("service").getAsJsonObject("parameters")
                .getAsJsonObject("requestInputs");

        // this will convert UUI request to normalized form
        Map<String, List<List<GroupResource>>> normalizedRequest = convertUUIReqTOStd(reqInputJsonObj, seqResourceList);
        return convertToInstanceResourceList(normalizedRequest, seqResourceList);
    }
}
