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

package org.onap.so.client.graphinventory;

import java.util.Optional;

import org.onap.so.client.aai.entities.CustomQuery;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public abstract class GraphInventoryQueryClient<S, I> {

	private Optional<String> depth = Optional.empty();
	private boolean nodesOnly = false;
	private Optional<GraphInventorySubgraphType> subgraph = Optional.empty();
	private GraphInventoryClient client;
	
	public GraphInventoryQueryClient(GraphInventoryClient client) {
		this.client = client;
	}
	
	protected abstract GraphInventoryUri getQueryUri();
	
	public String query(Format format, I query) {
		return client.createClient(setupQueryParams(getQueryUri().queryParam("format", format.toString()))).put(query, String.class);
	}
	
	public S depth (String depth) {
		this.depth = Optional.of(depth);
		return (S) this;
	}
	public S nodesOnly() {
		this.nodesOnly = true;
		return (S) this;
	}
	public S subgraph(GraphInventorySubgraphType type){
		
		subgraph =  Optional.of(type);

		return (S) this;
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
}
