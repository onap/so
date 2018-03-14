/*
 * ============LICENSE_START==========================================
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 *
 */

package org.openecomp.mso.cloud.authentication.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.woorea.openstack.keystone.model.Authentication;

@JsonRootName("auth")
public class RackspaceAuthentication extends Authentication {
		
	private static final long serialVersionUID = 5451283386875662918L;

	public static final class Token {
		
		private String username;
		private String apiKey;

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}
		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}
		/**
		 * @return the apiKey
		 */
		public String getApiKey() {
			return apiKey;
		}
		/**
		 * @param apiKey the apiKey to set
		 */
		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}
	}
	
	@JsonProperty("RAX-KSKEY:apiKeyCredentials")
	private Token token = new Token();
	
	public RackspaceAuthentication (String username, String apiKey) {
		this.token.username = username;
		this.token.apiKey = apiKey;
	
	}

	/**
	 * @return the token
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(Token token) {
		this.token = token;
	}
	
}
