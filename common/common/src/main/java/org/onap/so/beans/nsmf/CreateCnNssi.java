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
public class CreateCnNssi {

    public final static String URL = "/api/rest/provMns/v1/NSS/nssi";

    private String nsstId;

    private String flavorId;

    private String nssiName;

    private String scriptName;

    private String extension;

    private NsiInfo nsiInfo;

    public String getNsstId() {
        return nsstId;
    }

    public void setNsstId(String nsstId) {
        this.nsstId = nsstId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getNssiName() {
        return nssiName;
    }

    public void setNssiName(String nssiName) {
        this.nssiName = nssiName;
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

    public NsiInfo getNsiInfo() {
        return nsiInfo;
    }

    public void setNsiInfo(NsiInfo nsiInfo) {
        this.nsiInfo = nsiInfo;
    }
}
