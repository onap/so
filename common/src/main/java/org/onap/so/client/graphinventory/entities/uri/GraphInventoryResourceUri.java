/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.graphinventory.entities.uri;

import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.so.client.graphinventory.GraphInventoryObjectType;

public interface GraphInventoryResourceUri extends GraphInventoryUri {
    public GraphInventoryResourceUri relationshipAPI();

    public GraphInventoryResourceUri relatedTo(GraphInventoryObjectPlurals plural);

    public GraphInventoryResourceUri relatedTo(GraphInventoryObjectType type, String... values);

    public GraphInventoryResourceUri resourceVersion(String version);

    public GraphInventoryResourceUri format(Format format);

    @Override
    public GraphInventoryResourceUri depth(Depth depth);

    @Override
    public GraphInventoryResourceUri nodesOnly(boolean nodesOnly);

    @Override
    public GraphInventoryResourceUri queryParam(String name, String... values);

    @Override
    public GraphInventoryResourceUri replaceQueryParam(String name, String... values);

    @Override
    public GraphInventoryResourceUri resultIndex(int index);

    @Override
    public GraphInventoryResourceUri resultSize(int size);

    @Override
    public GraphInventoryResourceUri limit(int size);

    @Override
    public GraphInventoryResourceUri clone();
}
