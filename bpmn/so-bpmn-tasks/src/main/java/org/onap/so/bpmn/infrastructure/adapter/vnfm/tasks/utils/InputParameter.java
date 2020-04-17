/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.onap.etsi.sol003.adapter.lcm.v1.model.ExternalVirtualLink;

/**
 * Wrapper class for instance parameters which are based on SOL003
 * 
 * @author waqas.ikram@est.tech
 */
public class InputParameter implements Serializable {

    private static final long serialVersionUID = 42034634585595304L;

    private Map<String, String> additionalParams = new HashMap<>();

    private List<ExternalVirtualLink> extVirtualLinks = new ArrayList<>();

    public InputParameter() {}

    public InputParameter(final Map<String, String> additionalParams, final List<ExternalVirtualLink> extVirtualLinks) {
        this.additionalParams = additionalParams;
        this.extVirtualLinks = extVirtualLinks;
    }

    /**
     * @return the additionalParams
     */
    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    /**
     * @return the extVirtualLinks
     */
    public List<ExternalVirtualLink> getExtVirtualLinks() {
        return extVirtualLinks;
    }

    /**
     * @param additionalParams the additionalParams to set
     */
    public void setAdditionalParams(final Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void putAdditionalParams(final Map<String, String> additionalParams) {
        if (additionalParams != null) {
            this.additionalParams.putAll(additionalParams);
        }
    }

    /**
     * @param extVirtualLinks the extVirtualLinks to set
     */
    public void setExtVirtualLinks(final List<ExternalVirtualLink> extVirtualLinks) {
        this.extVirtualLinks = extVirtualLinks;
    }

    public void addExtVirtualLinks(final List<ExternalVirtualLink> extVirtualLinks) {
        if (extVirtualLinks != null) {
            this.extVirtualLinks = Stream.concat(this.extVirtualLinks.stream(), extVirtualLinks.stream()).distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [additionalParams=" + additionalParams + ", extVirtualLinks="
                + extVirtualLinks + "]";
    }

}
