/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import javax.validation.constraints.NotNull;
import java.util.List;

public class NsLinkPortInfo {
    @NotNull
    private String id;
    @NotNull
    private ResourceHandle resourceHandle;
    private NsCpHandle nsCpHandle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ResourceHandle getResourceHandle() {
        return resourceHandle;
    }

    public void setResourceHandle(ResourceHandle resourceHandle) {
        this.resourceHandle = resourceHandle;
    }

    public NsCpHandle getNsCpHandle() {
        return nsCpHandle;
    }

    public void setNsCpHandle(NsCpHandle nsCpHandle) {
        this.nsCpHandle = nsCpHandle;
    }
}
