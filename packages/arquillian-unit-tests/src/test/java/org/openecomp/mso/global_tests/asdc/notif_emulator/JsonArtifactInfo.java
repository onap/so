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

package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;

import org.openecomp.sdc.api.notification.IArtifactInfo;

public class JsonArtifactInfo implements IArtifactInfo {

    @JsonIgnore
    private Map<String, IArtifactInfo> artifactsMapByUUID = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> attributesMap = new HashMap<>();

    public JsonArtifactInfo() {

    }

    public synchronized void addArtifactToUUIDMap(List<JsonArtifactInfo> artifactList) {
        for (JsonArtifactInfo artifact : artifactList) {
            artifactsMapByUUID.put(artifact.getArtifactUUID(), artifact);
        }

    }

    @SuppressWarnings("unused")
    @JsonAnySetter
    public final void setAttribute(String attrName, Object attrValue) {
        if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
            this.attributesMap.put(attrName, attrValue);
        }
    }


    public Map<String, IArtifactInfo> getArtifactsMapByUUID() {
        return artifactsMapByUUID;
    }

    @Override
    public String getArtifactChecksum() {
        return (String) attributesMap.get("artifactCheckSum");
    }

    @Override
    public String getArtifactDescription() {
        return (String) attributesMap.get("artifactDescription");
    }

    @Override
    public String getArtifactName() {
        return (String) attributesMap.get("artifactName");
    }

    @Override
    public Integer getArtifactTimeout() {
        return (Integer) attributesMap.get("artifactTimeout");
    }

    @Override
    public String getArtifactType() {
        return (String) attributesMap.get("artifactType");
    }

    @Override
    public String getArtifactURL() {
        return (String) attributesMap.get("artifactURL");
    }

    @Override
    public String getArtifactUUID() {
        return (String) attributesMap.get("artifactUUID");
    }

    @Override
    public String getArtifactVersion() {
        return (String) attributesMap.get("artifactVersion");
    }

    @Override
    public IArtifactInfo getGeneratedArtifact() {
        return artifactsMapByUUID.get(attributesMap.get("generatedArtifact"));
    }

    @Override
    public List<IArtifactInfo> getRelatedArtifacts() {
        List<IArtifactInfo> listArtifacts = new LinkedList<>();
        List<String> uuidList = (List<String>) attributesMap.get("relatedArtifact");
        if (uuidList != null) {
            for (String uuid : uuidList) {
                listArtifacts.add(artifactsMapByUUID.get(uuid));
            }
        }
        return listArtifacts;
    }

}
