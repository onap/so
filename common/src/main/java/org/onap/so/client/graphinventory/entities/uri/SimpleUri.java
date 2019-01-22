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

package org.onap.so.client.graphinventory.entities.uri;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.client.aai.entities.uri.AAIUri;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.so.client.graphinventory.GraphInventoryObjectType;
import org.onap.so.client.graphinventory.entities.uri.parsers.UriParser;
import org.onap.so.client.graphinventory.entities.uri.parsers.UriParserSpringImpl;
import org.onap.so.client.graphinventory.exceptions.IncorrectNumberOfUriKeys;
import org.springframework.web.util.UriUtils;

public class SimpleUri implements GraphInventoryResourceUri, Serializable {

	private static final long serialVersionUID = -337701171277616439L;
	
	protected transient UriBuilder internalURI;
	protected final static String relationshipAPI = "/relationship-list/relationship";
	protected final static String relatedTo = "/related-to";
	protected final Object[] values;
	protected final GraphInventoryObjectType type;
	protected final GraphInventoryObjectPlurals pluralType;
	protected SimpleUri(GraphInventoryObjectType type, Object... values) {
		this.type = type;
		this.pluralType = null;
		this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
		this.values = values;
		validateValuesSize(this.getTemplate(type), values);
	}
	protected SimpleUri(GraphInventoryObjectType type, URI uri) {
		this.type = type;
		this.pluralType = null;
		this.internalURI = UriBuilder.fromPath(uri.getRawPath().replaceAll("/aai/v\\d+", ""));
		this.values = new Object[0];
	}
	protected SimpleUri(GraphInventoryObjectType type, UriBuilder builder, Object... values) {
		this.internalURI = builder;
		this.values = values;
		this.type = type;
		this.pluralType = null;
	}
	protected SimpleUri(GraphInventoryObjectPlurals type, UriBuilder builder, Object... values) {
		this.internalURI = builder;
		this.values = values;
		this.type = null;
		this.pluralType = type;
	}
	protected SimpleUri(GraphInventoryObjectPlurals type) {
		this.type = null;
		this.pluralType = type;
		this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
		this.values = new Object[0];
	}
	protected SimpleUri(GraphInventoryObjectPlurals type, Object... values) {
		this.type = null;
		this.pluralType = type;
		this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
		this.values = values;
		validateValuesSize(this.getTemplate(type), values);
	}
	protected SimpleUri(GraphInventoryResourceUri parentUri, GraphInventoryObjectType childType, Object... childValues) {
		this.type = childType;
		this.pluralType = null;
		this.internalURI = UriBuilder.fromUri(parentUri.build()).path(childType.partialUri());
		this.values = childValues;
		validateValuesSize(childType.partialUri(), values);
	}
	
	protected void setInternalURI(UriBuilder builder) {
		this.internalURI = builder;
	}
	@Override
	public SimpleUri relationshipAPI() {
		this.internalURI = internalURI.path(relationshipAPI);
		return this;
	}
	
	@Override
	public SimpleUri relatedTo(GraphInventoryObjectPlurals plural) {
		
		this.internalURI = internalURI.path(relatedTo).path(plural.partialUri());
		return this;
	}
	@Override
	public SimpleUri relatedTo(GraphInventoryObjectType type, String... values) {
		this.internalURI = internalURI.path(relatedTo).path(UriBuilder.fromPath(type.partialUri()).build(values).toString());
		return this;
	}
	
	@Override
	public SimpleUri resourceVersion(String version) {
		this.internalURI = internalURI.replaceQueryParam("resource-version", version);
		return this;
	}
	
	@Override
	public SimpleUri queryParam(String name, String... values) {
		this.internalURI = internalURI.queryParam(name, values);
		return this;
	}
	
	@Override
	public SimpleUri replaceQueryParam(String name, String... values) {
		this.internalURI = internalURI.replaceQueryParam(name, values);
		return this;
	}
	
	@Override
	public SimpleUri resultIndex(int index) {
		this.internalURI = internalURI.replaceQueryParam("resultIndex", index);
		return this;
	}
	
	@Override
	public SimpleUri resultSize(int size) {
		this.internalURI = internalURI.replaceQueryParam("resultSize", size);
		return this;
	}
	
	@Override
	public SimpleUri limit(int size) {
		return this.resultIndex(0).resultSize(size);
	}
	
	@Override
	public URI build() {
		return build(this.values);
	}
	
	protected URI build(Object... values) {
		//This is a workaround because resteasy does not encode URIs correctly
		final String[] encoded = new String[values.length];
		for (int i = 0; i < values.length; i++) {			
				encoded[i] = UriUtils.encode(values[i].toString(), StandardCharsets.UTF_8.toString());			
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
	public GraphInventoryObjectType getObjectType() {
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
	public int hashCode() {		
		return new HashCodeBuilder().append(this.build()).toHashCode();
	}
	
	
	@Override
	public SimpleUri depth(Depth depth) {
		this.internalURI.replaceQueryParam("depth", depth.toString());
		return this;
	}
	@Override
	public SimpleUri nodesOnly(boolean nodesOnly) {
		if (nodesOnly) {
			this.internalURI.replaceQueryParam("nodes-only", "");
		}
		return this;
	}
	
	@Override
	public SimpleUri format(Format format) {
		this.internalURI.replaceQueryParam("format", format);
		return this;
	}
	
	public void validateValuesSize(String template, Object... values) {
		UriParser parser = new UriParserSpringImpl(template);
		Set<String> variables = parser.getVariables();
		if (variables.size() != values.length) {
			throw new IncorrectNumberOfUriKeys(String.format("Expected %s variables: %s", variables.size(), variables));
		}
	}
	
	protected String getTemplate(GraphInventoryObjectType type) {
		return type.uriTemplate();
	}
	
	protected String getTemplate(GraphInventoryObjectPlurals type) {
		return type.uriTemplate();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeUTF(this.internalURI.toTemplate());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		 ois.defaultReadObject();
		 String uri = ois.readUTF();
		 this.setInternalURI(UriBuilder.fromUri(uri));
	}
}
