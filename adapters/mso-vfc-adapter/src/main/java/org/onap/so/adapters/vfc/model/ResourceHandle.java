/***
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

public class ResourceHandle {
    private String vimId;
    private String resourceProviderId;
    private String resourceId;
    private String vimLevelResourceType;

    public String getVimId() {
        return vimId;
    }

    public void setVimId(String vimId) {
        this.vimId = vimId;
    }

    public String getResourceProviderId() {
        return resourceProviderId;
    }

    public void setResourceProviderId(String resourceProviderId) {
        this.resourceProviderId = resourceProviderId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getVimLevelResourceType() {
        return vimLevelResourceType;
    }

    public void setVimLevelResourceType(String vimLevelResourceType) {
        this.vimLevelResourceType = vimLevelResourceType;
    }
}
