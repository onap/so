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

package org.onap.so.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"vnf_name", "book_name", "node_list", "file_parameter_content"})
public class ConfigurationParametersDistributeTraffic {
    @JsonProperty("vnf_name")
    private String vnfName;
    @JsonProperty("book_name")
    private String bookName;
    @JsonProperty("node_list")
    private String nodeList;
    @JsonProperty("file_parameter_content")
    private String fileParameterContent;

    @JsonProperty("vnf_name")
    public String getVnfName() { return vnfName; }

    @JsonProperty("vnf_name")
    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    @JsonProperty("book_name")
    public String getBookName() {
        return bookName;
    }

    @JsonProperty("book_name")
    public void setBookName(String bookName) { this.bookName = bookName; }

    @JsonProperty("node_list")
    public String getNodeList() {
        return nodeList;
    }

    @JsonProperty("node_list")
    public void setNodeList(String nodeList) {
        this.nodeList = nodeList;
    }

    @JsonProperty("file_parameter_content")
    public String getFileParameterContent() {
        return fileParameterContent;
    }

    @JsonProperty("file_parameter_content")
    public void setFileParameterContent(String fileParameterContent) {
        this.fileParameterContent = fileParameterContent;
    }
}
