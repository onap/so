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

package org.onap.so.client.adapter.vnf;

import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.QueryVfModuleResponse;
import org.onap.so.adapters.vnfrest.RollbackVfModuleRequest;
import org.onap.so.adapters.vnfrest.RollbackVfModuleResponse;
import org.onap.so.adapters.vnfrest.UpdateVfModuleRequest;
import org.onap.so.adapters.vnfrest.UpdateVfModuleResponse;

public interface VnfAdapterClient {

    CreateVfModuleResponse createVfModule(String aaiVnfId, CreateVfModuleRequest req) throws VnfAdapterClientException;

    RollbackVfModuleResponse rollbackVfModule(String aaiVnfId, String aaiVfModuleId, RollbackVfModuleRequest req)
            throws VnfAdapterClientException;

    DeleteVfModuleResponse deleteVfModule(String aaiVnfId, String aaiVfModuleId, DeleteVfModuleRequest req)
            throws VnfAdapterClientException;

    UpdateVfModuleResponse updateVfModule(String aaiVnfId, String aaiVfModuleId, UpdateVfModuleRequest req)
            throws VnfAdapterClientException;

    QueryVfModuleResponse queryVfModule(String aaiVnfId, String aaiVfModuleId, String cloudSiteId, String tenantId,
            String vfModuleName, boolean skipAAI, String requestId, String serviceInstanceId)
            throws VnfAdapterClientException;
}
