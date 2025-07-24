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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PnfErrorList implements Serializable {

    private static final long serialVersionUID = 1506455363755909867L;

    private String pnfId;

    private String pnfErrorDesc;

    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    public String getPnfErrorDesc() {
        return pnfErrorDesc;
    }

    public void setPnfErrorDesc(String pnfErrorDesc) {
        this.pnfErrorDesc = pnfErrorDesc;
    }
}
