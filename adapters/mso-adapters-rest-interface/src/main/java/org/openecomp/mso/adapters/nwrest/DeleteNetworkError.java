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

package org.openecomp.mso.adapters.nwrest;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

@XmlRootElement(name = "deleteNetworkError")
public class DeleteNetworkError extends NetworkExceptionResponse implements Serializable {
	private static final long serialVersionUID = 2735474165790444180L;

	public DeleteNetworkError() {
		super("");
	}

	public DeleteNetworkError(String message) {
		super(message);
	}

	public DeleteNetworkError(String message, MsoExceptionCategory category, boolean unused, String messageid) {
		super(message, category, unused, messageid);
	}
}
