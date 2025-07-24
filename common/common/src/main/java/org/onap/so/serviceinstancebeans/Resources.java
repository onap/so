/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Nokia
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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "resources")
@JsonInclude(Include.NON_DEFAULT)
public class Resources implements Serializable {

    private static final long serialVersionUID = 2194797231782624520L;
    @JsonProperty("vnfs")
    private List<Vnfs> vnfs = new ArrayList<>();
    @JsonProperty("pnfs")
    private List<Pnfs> pnfs = new ArrayList<>();
    @JsonProperty("networks")
    private List<Networks> networks = new ArrayList<>();
    @JsonProperty("services")
    private List<Service> services = new ArrayList<>();

    public List<Vnfs> getVnfs() {
        return vnfs;
    }

    public void setVnfs(List<Vnfs> vnfs) {
        this.vnfs = vnfs;
    }

    public List<Pnfs> getPnfs() {
        return pnfs;
    }

    public void setPnfs(List<Pnfs> pnfs) {
        this.pnfs = pnfs;
    }

    public List<Networks> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Networks> networks) {
        this.networks = networks;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Resources [vnfs=" + vnfs + ", networks=" + networks + ", services=" + services + "]";
    }
}
