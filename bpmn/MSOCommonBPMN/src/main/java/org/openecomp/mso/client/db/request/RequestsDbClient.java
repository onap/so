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

package org.openecomp.mso.client.db.request;

import java.io.IOException;
import java.net.URI;

import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;
import uk.co.blackpepper.bowman.RestTemplateConfigurer;

@Component("RequestDbClient")
public class RequestsDbClient {

	private Client<InfraActiveRequests> infraActiveRequestClient;
    private Client<ActiveRequests> activeRequestClient;

	@Value("${mso.adapters.db.spring.endpoint}")
	private String endpoint;
	
	@Value("${mso.db.auth}")
	private String msoAdaptersAuth;

	public RequestsDbClient() {
		ClientFactory clientFactory = Configuration.builder().setRestTemplateConfigurer(new RestTemplateConfigurer() {

			public void configure(RestTemplate restTemplate) {

				restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {

					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {

						request.getHeaders().add("Authorization", msoAdaptersAuth);
						return execution.execute(request, body);
					}
				});
			}
		}).build().buildClientFactory();
		infraActiveRequestClient = clientFactory.create(InfraActiveRequests.class);
        activeRequestClient = clientFactory.create(ActiveRequests.class);
	}

	public InfraActiveRequests getInfraActiveRequestbyRequestId(String requestId) {
		return this.getSingleInfraActiveRequests(this.getUri(endpoint + "/infraActiveRequests/" + requestId));
	}

	protected InfraActiveRequests getSingleInfraActiveRequests(URI uri) {
		return infraActiveRequestClient.get(uri);
	}

	public void updateInfraActiveRequests(InfraActiveRequests request) {		
		infraActiveRequestClient.put(request);
	}
	
    public ActiveRequests getActiveRequestbyRequestId(String requestId) {
        return this.getSingleActiveRequests(this.getUri(endpoint + "/activeRequests/" + requestId));
    }

    protected ActiveRequests getSingleActiveRequests(URI uri) {
        return activeRequestClient.get(uri);
    }

	protected URI getUri(String uri) {
		return URI.create(uri);
	}
}
