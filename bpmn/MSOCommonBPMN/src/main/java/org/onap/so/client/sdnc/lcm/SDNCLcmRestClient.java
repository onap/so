/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.lcm;

import java.net.URI;
import java.util.Optional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.client.RestClient;
import org.onap.so.client.sdnc.lcm.beans.LcmInput;
import org.onap.so.client.sdnc.lcm.beans.LcmOutput;
import org.onap.so.client.sdnc.lcm.beans.LcmRestRequest;
import org.onap.so.client.sdnc.lcm.beans.LcmRestResponse;

public class SDNCLcmRestClient extends RestClient {

    private final SDNCLcmProperties sdncLcmProperties;

    public SDNCLcmRestClient(SDNCLcmProperties props, URI path) {
        this(props, path, "application/json", "application/json");
    }

    public SDNCLcmRestClient(SDNCLcmProperties props, URI path, String accept, String contentType) {
        super(props, Optional.of(path), accept, contentType);
        this.sdncLcmProperties = props;
    }

    @Override
    protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
        headerMap.add("ALL", Pair.with("Authorization", sdncLcmProperties.getBasicAuth()));
    }

    @Override
    public ONAPComponents getTargetEntity() {
        return ONAPComponents.SDNC;
    }

    public LcmRestResponse sendRequest(LcmRestRequest lcmRestRequest) {
        return post(lcmRestRequest, LcmRestResponse.class);
    }

    public LcmOutput sendRequest(LcmInput lcmInput) {
        LcmRestRequest lcmRestRequest = new LcmRestRequest();
        lcmRestRequest.setInput(lcmInput);

        LcmRestResponse lcmRestResponse = sendRequest(lcmRestRequest);
        return lcmRestResponse.getOutput();
    }
}
