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
public class NssiActDeActRequest {

    private EsrInfo esrInfo;

    private ActDeActNssi actDeActNssi;

    public ActDeActNssi getActDeActNssi() {
        return actDeActNssi;
    }

    public void setActDeActNssi(ActDeActNssi actDeActNssi) {
        this.actDeActNssi = actDeActNssi;
    }

    public EsrInfo getEsrInfo() {
        return esrInfo;
    }

    public void setEsrInfo(EsrInfo esrInfo) {
        this.esrInfo = esrInfo;
    }
}
