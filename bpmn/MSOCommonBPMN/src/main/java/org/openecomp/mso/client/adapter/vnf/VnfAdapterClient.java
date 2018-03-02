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

package org.openecomp.mso.client.adapter.vnf;

import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleResponse;

public interface VnfAdapterClient {

	CreateVfModuleResponse createVfModule(String aaiVnfId, CreateVfModuleRequest req);

	RollbackVfModuleResponse rollbackVfModule(String aaiVnfId, String aaiVfModuleId, RollbackVfModuleRequest req);

	DeleteVfModuleResponse deleteVfModule(String aaiVnfId, String aaiVfModuleId, DeleteVfModuleRequest req);

	UpdateVfModuleResponse updateVfModule(String aaiVnfId, String aaiVfModuleId, UpdateVfModuleRequest req);

	QueryVfModuleResponse queryVfModule(String aaiVnfId, String aaiVfModuleId, String cloudSiteId, String tenantId,
			String vfModuleName, boolean skipAAI, String requestId, String serviceInstanceId);

	String healthCheck();

}
