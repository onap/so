/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.aai;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryResultWrapper;

public class ResourceAndUrl<Wrapper extends GraphInventoryResultWrapper> {

    private String url;
    private GraphInventoryObjectType type;
    private Wrapper wrapper;

    public ResourceAndUrl(String url, GraphInventoryObjectType type, Wrapper wrapper) {
        this.url = url;
        this.type = type;
        this.wrapper = wrapper;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public GraphInventoryObjectType getType() {
        return type;
    }

    public void setType(GraphInventoryObjectType type) {
        this.type = type;
    }

}
