/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openecomp.mso.requestsdb.OperationStatus;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class GetE2EServiceInstanceResponse {

	protected OperationStatus operation;

	public OperationStatus getOperationStatus() {
		return operation;
	}

	public void setOperationStatus(OperationStatus requestDB) {
		this.operation = requestDB;
	}

	public OperationStatus getOperation() {
		return operation;
	}

	public void setOperation(OperationStatus operation) {
		this.operation = operation;
	}

}
