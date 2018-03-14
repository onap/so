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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CloudifyRequest<R> {
	
	private CloudifyClient client;
	
	public CloudifyRequest() {
		
	}
	
	public CloudifyRequest(CloudifyClient client, HttpMethod method, CharSequence path, Entity<?> entity, Class<R> returnType) {
		this.client = client;
		this.method = method;
		this.path = new StringBuilder(path);
		this.entity = entity;
		this.returnType = returnType;
		header("Accept", "application/json");
	}
	
	private String endpoint;
	
	private HttpMethod method;
	
	private StringBuilder path = new StringBuilder();
	
	private Map<String, List<Object>> headers = new HashMap<String, List<Object>>();
	
	private Entity<?> entity;
	
	private Class<R> returnType;
	
	private boolean basicAuth = false;
	private String user = null;
	private String password = null;
	
	public CloudifyRequest<R> endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
	public String endpoint() {
		return endpoint;
	}

	public CloudifyRequest<R> method(HttpMethod method) {
		this.method = method;
		return this;
	}
	
	public HttpMethod method() {
		return method;
	}
	
	public CloudifyRequest<R> path(String path) {
		this.path.append(path);
		return this;
	}
	
	public String path() {
		return path.toString();
	}

	public CloudifyRequest<R> header(String name, Object value) {
		if(value != null) {
			headers.put(name, Arrays.asList(value));
		}
		return this;
	}
	
	public Map<String, List<Object>> headers() {
		return headers;
	}
	
	public <T> Entity<T> entity(T entity, String contentType) {
		return new Entity<T>(entity, contentType);
	}
	
	public Entity<?> entity() {
		return entity;
	}
	
	public <T> Entity<T> json(T entity) {
		return entity(entity, "application/json");
	}
	
	public void returnType(Class<R> returnType) {
		this.returnType = returnType;
	}
	
	public Class<R> returnType() {
		return returnType;
	}
	
	/*
	 * Use Basic Authentication for this request.  If not set, the client will use Token authentication
	 * if a token provider is defined.  Otherwise, no authentication will be applied.
	 */
	public void setBasicAuthentication (String user, String password) {
		this.basicAuth = true;
		this.user = user;
		this.password= password;
	}
	
	public boolean isBasicAuth () {
		return this.basicAuth;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public R execute() {
		return client.execute(this);
	}
	
	public CloudifyResponse request() {
		return client.request(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CloudifyRequest [endpoint=" + endpoint + ", method=" + method
				+ ", path=" + path + ", headers=" + headers + ", entity="
				+ entity + ", returnType=" + returnType + "]";
	}

	private Map<String, List<Object> > queryParams = new LinkedHashMap<String, List<Object> >();

	public Map<String, List<Object> > queryParams() {
		return queryParams;
	}

	public CloudifyRequest<R> queryParam(String key, Object value) {
		if (queryParams.containsKey(key)) {
			List<Object> values = queryParams.get(key);
			values.add(value);
		} else {
			List<Object> values = new ArrayList<Object>();
			values.add(value);
			queryParams.put(key, values);
		}

		return this;
    }
	
	protected static String buildPath(String ... elements) {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (String element : elements) {
            stringBuilder.append(element);
        }

	    return stringBuilder.toString();
	}
}
