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

package org.openecomp.mso.cloudify.base.client;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import org.openecomp.mso.cloudify.v3.client.Cloudify;
import org.openecomp.mso.cloudify.v3.client.TokensResource.GetToken;
import org.openecomp.mso.cloudify.v3.model.Token;

/**
 * Cloudify Token Provider that uses the Cloudify client API itself to obtain a token
 * 
 * @author JC1348
 *
 */
public class CloudifyClientTokenProvider implements CloudifyTokenProvider {

	String user;
	String password;
	String token;
	Date expiration;
	Cloudify cloudify = null;

	public CloudifyClientTokenProvider(String cloudifyEndpoint, String user, String password) {
		this.user = user;
		this.password = password;
		
		cloudify = new Cloudify (cloudifyEndpoint);
	}

	@Override
	public String getToken() {
		Date now = new Date();
		if (token != null && expiration != null && expiration.after(now)) {
			return token;
		}

		// Create a "Get Token" request.  Force basic authentication to acquire the token itself.
		GetToken tokenRequest = cloudify.tokens().token();
		tokenRequest.setBasicAuthentication(user, password);
		Token newToken = tokenRequest.execute();
		
		token = newToken.getValue();
		
		if (expiration == null) {
			expiration = new Date();
		}
		// TODO:  Make this property driven (or see if it comes back somehow in response)
		expiration = DateUtils.addMinutes(expiration, 10);
		
		return token;
	}

	@Override
	/**
	 * This doesn't actually expire the token in Cloudify.  It just prevents this token provider
	 * from using it.
	 */
	public void expireToken() {
		expiration = null;
		token = null;
	}

}
