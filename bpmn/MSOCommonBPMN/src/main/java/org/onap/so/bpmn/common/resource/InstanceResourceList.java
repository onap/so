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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ResourceType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InstanceResourceList {

    private static List<Map<String, List<GroupResource>>> convertUUIReqTOStd(final String uuiRequest,
            List<Resource> seqResourceList) {

        List<Map<String, List<GroupResource>>> normalizedList = new ArrayList<>();

        Gson gson = new Gson();
        JsonObject servJsonObject = gson.fromJson(uuiRequest, JsonObject.class);

        JsonObject reqInputJsonObj = servJsonObject.getAsJsonObject("service").getAsJsonObject("parameters")
                .getAsJsonObject("requestInputs");

        // iterate all node in requestInputs
        Iterator<Map.Entry<String, JsonElement>> iterator = reqInputJsonObj.entrySet().iterator();

        while (iterator.hasNext()) { // iterate all <vf>_list
            Map.Entry<String, JsonElement> entry = iterator.next();

            // truncate "_list" from key and keep only the <VF_NAME>
            String key = entry.getKey().substring(0, entry.getKey().indexOf("_list"));

            // all the element represent VF will contain "<VF_NAME>_list".
            if (key.contains("_list")) {
                // this will return list of vf of same type
                // e.g. vf_list [{vf1}, {vf2}]
                Iterator<JsonElement> vfsIterator = entry.getValue().getAsJsonArray().iterator();

                while (vfsIterator.hasNext()) { // iterate all [] inside vf_list
                    JsonObject vfObject = vfsIterator.next().getAsJsonObject();
                    List<GroupResource> tmpGrpsHolder = new ArrayList<>();

                    // iterate vfObject to get groups(vfc)
                    // currently each vfc represented by one group.
                    Iterator<Map.Entry<String, JsonElement>> vfIterator = vfObject.entrySet().iterator();
                    while (vfIterator.hasNext()) { // iterate all property inside a VF
                        Map.Entry<String, JsonElement> vfEntry = vfIterator.next();

                        // property name for vfc input will always carry "<VFC_NAME>_list"
                        if (vfEntry.getKey().contains("_list")) {
                            // truncate "_list" from key and keep only the <VFC_NAME>
                            String vfcName = vfEntry.getKey().substring(0, vfEntry.getKey().indexOf("_list"));
                            GroupResource grpRes = getGroupResource(vfcName, seqResourceList);
                            // A <vfc>_list can contain more than one vfc of same type
                            Iterator<JsonElement> vfcsIterator = vfEntry.getValue().getAsJsonArray().iterator();

                            while (vfcsIterator.hasNext()) { // iterate all the vfcs inside <vfc>_list
                                tmpGrpsHolder.add(grpRes);
                            }
                        }
                    }
                    List<GroupResource> seqGrpResourceList = seqGrpResource(tmpGrpsHolder, seqResourceList);
                    HashMap<String, List<GroupResource>> entryNormList = new HashMap<>();
                    entryNormList.put(key, seqGrpResourceList);
                    normalizedList.add(entryNormList);
                }
            }
        }

        return normalizedList;
    }

    private static List<GroupResource> seqGrpResource(List<GroupResource> grpResources, List<Resource> resourceList) {
        List<GroupResource> seqGrpResList = new ArrayList<>();
        for (Resource r : resourceList) {
            if (r.getResourceType() != ResourceType.GROUP) {
                continue;
            }
            for (GroupResource g : grpResources) {
                if (r.getModelInfo().getModelName().equalsIgnoreCase(g.getModelInfo().getModelName())) {
                    seqGrpResList.add(g);
                }
            }
        }
        return seqGrpResList;
    }

    private static GroupResource getGroupResource(String vfcName, List<Resource> seqRessourceList) {
        for (Resource r : seqRessourceList) {
            if (r.getResourceType() == ResourceType.GROUP) {
                // Currently only once vnfc is added to group
                return ((GroupResource) r).getVnfcs().get(0).getModelInfo().getModelName().contains(vfcName)
                        ? (GroupResource) r
                        : null;
            }
        }
        return null;
    }

    private static List<Resource> convertToInstanceResourceList(List<Map<String, List<GroupResource>>> normalizedReq,
            List<Resource> seqResourceList) {
        List<Resource> flatResourceList = new ArrayList<>();
        for (Resource r : seqResourceList) {
            if (r.getResourceType() == ResourceType.VNF) {
                for (Map<String, List<GroupResource>> entry : normalizedReq) {
                    if (r.getModelInfo().getModelName().equalsIgnoreCase(entry.keySet().iterator().next())) {
                        flatResourceList.add(r);
                        flatResourceList.addAll(entry.get(entry.keySet().iterator().next()));
                    }
                }
            }
        }
        return flatResourceList;
    }

    public static List<Resource> getInstanceResourceList(final List<Resource> seqResourceList,
            final String uuiRequest) {

        // this will convert UUI request to normalized form
        List<Map<String, List<GroupResource>>> normalizedReq = convertUUIReqTOStd(uuiRequest, seqResourceList);

        // now UUI json req is normalized to
        // [
        // { VFB1 : [GrpA1, GrA2, GrB1]},
        // { VFB2 : [GrpA1, GrB1]},
        // { VFA1 : [GrpC1]}
        // ]
        // now sequence according to VF order (Group is already sequenced).
        // After sequence it will look like :
        // [
        // { VFA1 : [GrpA1, GrA2, GrB1]},
        // { VFA2 : [GrpA1, GrB1]},
        // { VFB1 : [GrpC1]}
        // ]
        return convertToInstanceResourceList(normalizedReq, seqResourceList);
    }
}
