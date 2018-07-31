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

package org.onap.so.client.aai;

import java.util.Optional;

import org.onap.so.client.RestClient;
import org.onap.so.client.aai.entities.CustomQuery;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public class AAIQueryClient extends AAIClient {

	private Optional<String> depth = Optional.empty();
	private boolean nodesOnly = false;
	private Optional<AAISubgraphType> subgraph = Optional.empty();
	
	public AAIQueryClient() {
		super();
	}
	
	public AAIQueryClient(AAIVersion version) {
		super();
		this.version = version;
	}
	
	public String query(Format format, CustomQuery query) {
		return this.createClient(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY).queryParam("format", format.toString()))
		.put(query, String.class);
	}
	
	public AAIQueryClient depth (String depth) {
		this.depth = Optional.of(depth);
		return this;
	}
	public AAIQueryClient nodesOnly() {
		this.nodesOnly = true;
		return this;
	}
	public AAIQueryClient subgraph(AAISubgraphType type){
		
		subgraph =  Optional.of(type);

		return this;
	}
	
	protected GraphInventoryUri setupQueryParams(GraphInventoryUri uri) {
		GraphInventoryUri clone = uri.clone();
		if (this.depth.isPresent()) {
			clone.queryParam("depth", depth.get());
		}
		if (this.nodesOnly) {
			clone.queryParam("nodesOnly", "");
		}
		if (this.subgraph.isPresent()) {
			clone.queryParam("subgraph", this.subgraph.get().toString());
		}
		return clone;
	}
	@Override
	protected RestClient createClient(GraphInventoryUri uri) {
		return super.createClient(setupQueryParams(uri));
	}
}
