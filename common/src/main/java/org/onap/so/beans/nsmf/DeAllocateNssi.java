/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeAllocateNssi implements Serializable {
    @Deprecated
    public final static String URL = "/api/rest/provMns/v1/NSS" + "/SliceProfiles/%s";
    private static final long serialVersionUID = 8198187346132154044L;

    private String nsiId;

    private String nssiId;

    private List<String> snssaiList;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int terminateNssiOption;

    private String scriptName;

    private String extension;

    private String sliceProfileId;

    public String getSliceProfileId() {
        return sliceProfileId;
    }

    public void setSliceProfileId(String sliceProfileId) {
        this.sliceProfileId = sliceProfileId;
    }

    public String getNsiId() {
        return nsiId;
    }

    public void setNsiId(String nsiId) {
        this.nsiId = nsiId;
    }

    public String getNssiId() {
        return nssiId;
    }

    public void setNssiId(String nssiId) {
        this.nssiId = nssiId;
    }

    public List<String> getSnssaiList() {
        return snssaiList;
    }

    public void setSnssaiList(List<String> snssaiList) {
        this.snssaiList = snssaiList;
    }

    public int getTerminateNssiOption() {
        return terminateNssiOption;
    }

    public void setTerminateNssiOption(int terminateNssiOption) {
        this.terminateNssiOption = terminateNssiOption;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
