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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocateAnNssi {

    @Deprecated
    public final static String URL = "/api/rest/provMns/v1/an/NSS" + "/SliceProfiles";

    private String nsstId;

    private String nssiId;

    private String nssiName;

    private AnSliceProfile sliceProfile;

    private String scriptName;

    private Object extension;

    private NsiInfo nsiInfo;

    public String getNsstId() {
        return nsstId;
    }

    public void setNsstId(String nsstId) {
        this.nsstId = nsstId;
    }

    public String getNssiId() {
        return nssiId;
    }

    public void setNssiId(String nssiId) {
        this.nssiId = nssiId;
    }

    public String getNssiName() {
        return nssiName;
    }

    public void setNssiName(String nssiName) {
        this.nssiName = nssiName;
    }

    public AnSliceProfile getSliceProfile() {
        return sliceProfile;
    }

    public void setSliceProfile(AnSliceProfile sliceProfile) {
        this.sliceProfile = sliceProfile;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public Object getExtension() {
        return extension;
    }

    public void setExtension(Object extension) {
        this.extension = extension;
    }

    public NsiInfo getNsiInfo() {
        return nsiInfo;
    }

    public void setNsiInfo(NsiInfo nsiInfo) {
        this.nsiInfo = nsiInfo;
    }
}
