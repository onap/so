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

package org.openecomp.mso.logger;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

import java.io.IOException;
import java.security.Principal;

public class LogFilter implements Filter {
	@Override
	public void destroy() {
		// Nothing to do.
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		MDC.clear ();
		MDC.put (MsoLogger.REMOTE_HOST, httpRequest.getRemoteAddr());

		Principal userPrincipal = httpRequest.getUserPrincipal();
		if (null != userPrincipal) {
			MDC.put (MsoLogger.PARTNERNAME, userPrincipal.getName ());
		}
		//Set identity of calling application / component
		String fromAppId = httpRequest.getHeader(MsoLogger.HEADER_FROM_APP_ID);
		if(fromAppId != null && !fromAppId.isEmpty()) {
			MDC.put (MsoLogger.FROM_APP_ID, fromAppId);
		}
		chain.doFilter(httpRequest, httpResponse);
	}
	
	@Override
	public void init(final FilterConfig config) throws ServletException {
		// Nothing to do
	}
}
