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

package org.onap.so.apihandler.common;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ResponseBuilder {
	
	@Value("${mso.infra.default.versions.apiMinorVersion}")
	private String apiMinorVersion;
	@Value("${mso.infra.default.versions.apiPatchVersion}")
	private String apiPatchVersion;
	
	public Response buildResponse(int status, String requestId, Object jsonResponse, String apiVersion) {
		
		if (apiVersion.matches("v[1-9]")) {
			apiVersion = apiVersion.substring(1);
		}
			
		String latestVersion = apiVersion + "." + apiMinorVersion + "." + apiPatchVersion;
		
		javax.ws.rs.core.Response.ResponseBuilder builder = Response.status(status)
															.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
															.header(CommonConstants.X_MINOR_VERSION, apiMinorVersion)
															.header(CommonConstants.X_PATCH_VERSION, apiPatchVersion)
															.header(CommonConstants.X_LATEST_VERSION, latestVersion);
		
		if(StringUtils.isNotBlank(requestId)) {
			builder.header(CommonConstants.X_TRANSACTION_ID, requestId);
		}
		
		return builder.entity(jsonResponse).build();
	}

}
