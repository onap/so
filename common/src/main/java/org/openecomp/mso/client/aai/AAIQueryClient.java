package org.openecomp.mso.client.aai;

import java.util.Optional;
import java.util.UUID;

import org.openecomp.mso.client.aai.entities.CustomQuery;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.policy.RestClient;
import org.openecomp.mso.client.aai.AAIVersion;

public class AAIQueryClient extends AAIClient {


	private final AAIVersion version;
	private Optional<String> depth = Optional.empty();
	private boolean nodesOnly = false;
	private Optional<AAISubgraphType> subgraph = Optional.empty();
	
	public AAIQueryClient() {
		super(UUID.randomUUID());
		this.version = super.getVersion();
	}
	
	public AAIQueryClient(AAIVersion version, UUID requestId) {
		super(requestId);
		this.version = version;
	}
	
	public AAIQueryClient(AAIVersion version) {
		this(version, UUID.randomUUID());
	}
	
	public String query(Format format, CustomQuery query) {
		return this.createClient(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY).queryParam("format", format.toString()))
		.addRequestId(requestId).put(query, String.class);
	}
	
	@Override
	protected AAIVersion getVersion() {
		return this.version;
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
	
	@Override
	public RestClient createClient(AAIUri uri) {
		AAIUri clone = uri.clone();
		if (this.depth.isPresent()) {
			clone.queryParam("depth", depth.get());
		}
		if (this.nodesOnly) {
			clone.queryParam("nodesOnly", "");
		}
		if (this.subgraph.isPresent()) {
			clone.queryParam("subgraph", this.subgraph.get().toString());
		}
		return super.createClient(clone);
	}
}