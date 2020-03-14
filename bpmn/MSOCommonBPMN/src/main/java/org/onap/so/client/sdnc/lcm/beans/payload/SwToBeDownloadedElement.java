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

package org.onap.so.client.sdnc.lcm.beans.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"swLocation", "swFileSize", "swFileCompression", "swFileFormat"})
public class SwToBeDownloadedElement {

    @JsonProperty(value = "swLocation", required = true)
    private String swLocation;

    @JsonProperty(value = "swFileSize")
    private Long swFileSize;

    @JsonProperty(value = "swFileCompression")
    private String swFileCompression;

    @JsonProperty(value = "swFileFormat")
    private String swFileFormat;

    public String getSwLocation() {
        return swLocation;
    }

    public void setSwLocation(String value) {
        this.swLocation = value;
    }

    public Long getSwFileSize() {
        return swFileSize;
    }

    public void setSwFileSize(Long value) {
        this.swFileSize = value;
    }

    public String getSwFileCompression() {
        return swFileCompression;
    }

    public void setSwFileCompression(String value) {
        this.swFileCompression = value;
    }

    public String getSwFileFormat() {
        return swFileFormat;
    }

    public void setSwFileFormat(String value) {
        this.swFileFormat = value;
    }

}
