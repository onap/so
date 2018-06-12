package org.openecomp.mso.apihandler.common;

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
