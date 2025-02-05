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

package org.onap.so.client.adapter.network;

import jakarta.ws.rs.core.Response;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.QueryNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;

public interface NetworkAdapterClient {
    CreateNetworkResponse createNetwork(CreateNetworkRequest req) throws NetworkAdapterClientException;

    DeleteNetworkResponse deleteNetwork(String aaiNetworkId, DeleteNetworkRequest req)
            throws NetworkAdapterClientException;

    RollbackNetworkResponse rollbackNetwork(String aaiNetworkId, RollbackNetworkRequest req)
            throws NetworkAdapterClientException;

    QueryNetworkResponse queryNetwork(String aaiNetworkId, String cloudSiteId, String tenantId, String networkStackId,
            boolean skipAAI, String requestId, String serviceInstanceId) throws NetworkAdapterClientException;

    UpdateNetworkResponse updateNetwork(String aaiNetworkId, UpdateNetworkRequest req)
            throws NetworkAdapterClientException;

    jakarta.ws.rs.core.Response createNetworkAsync(CreateNetworkRequest req) throws NetworkAdapterClientException;

    jakarta.ws.rs.core.Response deleteNetworkAsync(String aaiNetworkId, DeleteNetworkRequest req)
            throws NetworkAdapterClientException;

    jakarta.ws.rs.core.Response rollbackNetworkAsync(String aaiNetworkId, RollbackNetworkRequest req)
            throws NetworkAdapterClientException;

    Response updateNetworkAsync(String aaiNetworkId, UpdateNetworkRequest req) throws NetworkAdapterClientException;

}
