package org.openecomp.mso.client.aai.entities.uri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.parsers.UriParser;
import org.openecomp.mso.client.aai.entities.uri.parsers.UriParserSpringImpl;
import org.springframework.web.util.UriUtils;

public class SimpleUri implements AAIResourceUri {

	protected UriBuilder internalURI;
	protected final static String relationshipAPI = "/relationship-list/relationship";
	protected final static String relatedTo = "/related-to";
	protected final Object[] values;
	protected final AAIObjectType type;
	protected final AAIObjectPlurals pluralType;
	protected SimpleUri(AAIObjectType type, Object... values) {
		this.type = type;
		this.pluralType = null;
		this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
		this.values = values;
	}
	protected SimpleUri(AAIObjectType type, URI uri) {
		this.type = type;
		this.pluralType = null;
		this.internalURI = UriBuilder.fromPath(uri.getRawPath().replaceAll("/aai/v\\d+", ""));
		this.values = new Object[0];
	}
	protected SimpleUri(AAIObjectType type, UriBuilder builder, Object... values) {
		this.internalURI = builder;
		this.values = values;
		this.type = type;
		this.pluralType = null;
	}
	protected SimpleUri(AAIObjectPlurals type, UriBuilder builder, Object... values) {
		this.internalURI = builder;
		this.values = values;
		this.type = null;
		this.pluralType = type;
	}
	protected SimpleUri(AAIObjectPlurals type) {
		this.type = null;
		this.pluralType = type;
		this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
		this.values = new Object[0];
	}
	
	@Override
	public SimpleUri relationshipAPI() {
		this.internalURI = internalURI.path(relationshipAPI);
		return this;
	}
	
	@Override
	public SimpleUri relatedTo(AAIObjectPlurals plural) {
		
		this.internalURI = internalURI.path(relatedTo).path(plural.partialUri());
		return this;
	}
	@Override
	public SimpleUri relatedTo(AAIObjectType type, String... values) {
		this.internalURI = internalURI.path(relatedTo).path(UriBuilder.fromPath(type.partialUri()).build(values).toString());
		return this;
	}
	
	@Override
	public SimpleUri resourceVersion(String version) {
		this.internalURI = internalURI.queryParam("resource-version", version);
		return this;
	}
	
	@Override
	public SimpleUri queryParam(String name, String... values) {
		this.internalURI = internalURI.queryParam(name, values);
		return this;
	}
	
	@Override
	public URI build() {
		return build(this.values);
	}
	
	protected URI build(Object... values) {
		//This is a workaround because resteasy does not encode URIs correctly
		final String[] encoded = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			try {
				encoded[i] = UriUtils.encode(values[i].toString(), StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				encoded[i] = values[i].toString();
			}
		}
		return internalURI.buildFromEncoded(encoded);
	}
	
	@Override
	public Map<String, String> getURIKeys() {
		return this.getURIKeys(this.build().toString());
	}
	
	protected Map<String, String> getURIKeys(String uri) {
		UriParser parser;
		if (this.type != null) {
			if (!("".equals(this.getTemplate(type)))) {
				parser = new UriParserSpringImpl(this.getTemplate(type));
			} else {
				return new HashMap<>();
			}
		} else {
			parser = new UriParserSpringImpl(this.getTemplate(pluralType));
		}
		
		
		return parser.parse(uri);
	}
	
	@Override
	public SimpleUri clone() {
		if (this.type != null) {
			return new SimpleUri(this.type, this.internalURI.clone(), values);
		} else {
			return new SimpleUri(this.pluralType, this.internalURI.clone(), values);
		}
	}
	
	@Override
	public AAIObjectType getObjectType() {
		return this.type;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AAIUri) {
			return this.build().equals(((AAIUri)o).build());
		}
		return false;
	}
	@Override
	public SimpleUri depth(Depth depth) {
		this.internalURI.queryParam("depth", depth.toString());
		return this;
	}
	@Override
	public SimpleUri nodesOnly(boolean nodesOnly) {
		if (nodesOnly) {
			this.internalURI.queryParam("nodes-only", "");
		}
		return this;
	}
	
	protected String getTemplate(AAIObjectType type) {
		return type.uriTemplate();
	}
	
	protected String getTemplate(AAIObjectPlurals type) {
		return type.uriTemplate();
	}
	
}
