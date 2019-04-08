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

package org.onap.so.client.aai.entities.uri;

import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.so.client.graphinventory.GraphInventoryObjectType;

public class NodesUri extends AAISimpleUri {

    private static final long serialVersionUID = 8818689895730182042L;

    protected NodesUri(AAIObjectType type, Object... values) {
        super(type, values);
    }

    protected NodesUri(AAIObjectPlurals type) {
        super(type);
    }


    @Override
    protected String getTemplate(GraphInventoryObjectType type) {
        return UriBuilder.fromUri("/nodes").path(type.partialUri()).toTemplate();
    }

    @Override
    protected String getTemplate(GraphInventoryObjectPlurals type) {
        return UriBuilder.fromUri("/nodes").path(type.partialUri()).toTemplate();
    }
}
