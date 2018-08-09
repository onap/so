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

package org.onap.so.client.sdnc;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

//TODO move to common location
public class BaseClient<I,O> {

	private HttpHeaders httpHeader;
	private String targetUrl;

	public HttpHeaders getHttpHeader() {
		return httpHeader;
	}

	public void setHttpHeader(HttpHeaders httpHeader) {
		this.httpHeader = httpHeader;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public O get(I data, Object... uriVariables) throws RestClientException {
		return run(data, HttpMethod.GET, uriVariables);
	}

	public O post(I data, Object... uriVariables) throws RestClientException {
		return run(data, HttpMethod.POST, uriVariables);
	}

	public O run(I data, HttpMethod method, Object... uriVariables) throws RestClientException {
		HttpEntity<I> requestEntity = new HttpEntity<I>(data, getHttpHeader());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		ParameterizedTypeReference<O> output = new ParameterizedTypeReference<O>() {};
		ResponseEntity<O> responseEntity = restTemplate.exchange(getTargetUrl(), method, requestEntity, output,
				uriVariables);
		return responseEntity.getBody();
	}

}