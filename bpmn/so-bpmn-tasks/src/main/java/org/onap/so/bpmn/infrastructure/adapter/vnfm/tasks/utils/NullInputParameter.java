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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;

/**
 * @author waqas.ikram@est.tech
 */
public class NullInputParameter extends InputParameter {

    private static final String ERROR =
            "method should not be called for null object " + NullInputParameter.class.getSimpleName();

    private static final long serialVersionUID = -7261286746726871696L;

    public static final NullInputParameter NULL_INSTANCE = new NullInputParameter();

    private NullInputParameter() {
        super(Collections.emptyMap(), Collections.emptyList());
    }

    @Override
    public void setAdditionalParams(final Map<String, String> additionalParams) {
        throw new UnsupportedOperationException("setAdditionalParams() " + ERROR);
    }

    @Override
    public void setExtVirtualLinks(final List<ExternalVirtualLink> extVirtualLinks) {
        throw new UnsupportedOperationException("setExtVirtualLinks() " + ERROR);
    }

    @Override
    public void addExtVirtualLinks(final List<ExternalVirtualLink> extVirtualLinks) {
        throw new UnsupportedOperationException("addExtVirtualLinks() " + ERROR);
    }

    @Override
    public void putAdditionalParams(final Map<String, String> additionalParams) {
        throw new UnsupportedOperationException("putAdditionalParams() " + ERROR);
    }
}
