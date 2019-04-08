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

import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryResourceUri;

public interface AAIResourceUri extends AAIUri, GraphInventoryResourceUri {

    public AAIResourceUri relationshipAPI();

    public AAIResourceUri relatedTo(AAIObjectPlurals plural);

    public AAIResourceUri relatedTo(AAIObjectType type, String... values);

    public AAIResourceUri resourceVersion(String version);

    public AAIResourceUri format(Format format);

    @Override
    public AAIResourceUri depth(Depth depth);

    @Override
    public AAIResourceUri nodesOnly(boolean nodesOnly);

    @Override
    public AAIResourceUri queryParam(String name, String... values);

    @Override
    public AAIResourceUri replaceQueryParam(String name, String... values);

    @Override
    public AAIResourceUri resultIndex(int index);

    @Override
    public AAIResourceUri resultSize(int size);

    @Override
    public AAIResourceUri limit(int size);

    @Override
    public AAIResourceUri clone();
}
