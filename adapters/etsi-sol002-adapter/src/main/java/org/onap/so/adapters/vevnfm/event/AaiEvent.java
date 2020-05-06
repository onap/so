/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AaiEvent {

    private final boolean vserverIsClosedLoopDisabled;
    private final String vserverName;
    private final String genericVnfVnfId;

    public AaiEvent(final boolean cld, final String name, final String id) {
        vserverIsClosedLoopDisabled = cld;
        vserverName = name;
        genericVnfVnfId = id;
    }

    @JsonProperty("vserver.is-closed-loop-disabled")
    public boolean isVserverIsClosedLoopDisabled() {
        return vserverIsClosedLoopDisabled;
    }

    @JsonProperty("vserver.vserver-name")
    public String getVserverName() {
        return vserverName;
    }

    @JsonProperty("generic-vnf.vnf-id")
    public String getGenericVnfVnfId() {
        return genericVnfVnfId;
    }
}
