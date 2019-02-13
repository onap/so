/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai.entities.uri;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.GraphInventoryObjectType;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryResourceUri;
import org.onap.so.client.graphinventory.entities.uri.SimpleUri;

public class AAISimpleUri extends SimpleUri implements AAIResourceUri {
	
	private static final long serialVersionUID = -6397024057188453229L;

	protected AAISimpleUri(AAIObjectType type, Object... values) {
		super(type, values);
		
	}
	protected AAISimpleUri(AAIObjectType type, URI uri) {
		super(type, uri);
	}
	protected AAISimpleUri(AAIObjectType type, UriBuilder builder, Object... values) {
		super(type, builder, values);
	}
	protected AAISimpleUri(AAIObjectPlurals type, UriBuilder builder, Object... values) {
		super(type, builder, values);
	}
	protected AAISimpleUri(AAIObjectPlurals type) {
		super(type);
	}
	protected AAISimpleUri(AAIObjectPlurals type, Object... values) {
		super(type, values);
	}
	protected AAISimpleUri(AAIResourceUri parentUri, AAIObjectType childType, Object... childValues) {
		super(parentUri, childType, childValues);
	}
	
	protected AAISimpleUri(AAIResourceUri parentUri, AAIObjectPlurals childType) {
		super(parentUri, childType);
	}
	
	@Override
	public AAISimpleUri relationshipAPI() {
		return (AAISimpleUri) super.relationshipAPI();
	}
	
	@Override
	public AAISimpleUri relatedTo(AAIObjectPlurals plural) {
		return (AAISimpleUri) super.relatedTo(plural);
	}
	@Override
	public AAISimpleUri relatedTo(AAIObjectType type, String... values) {
		return (AAISimpleUri) super.relatedTo(type, values);
	}
	
	@Override
	public AAISimpleUri resourceVersion(String version) {
		return (AAISimpleUri) super.resourceVersion(version);
	}
	
	@Override
	public AAISimpleUri queryParam(String name, String... values) {
		return (AAISimpleUri) super.queryParam(name, values);
	}
	
	@Override
	public AAISimpleUri replaceQueryParam(String name, String... values) {
		return (AAISimpleUri) super.replaceQueryParam(name, values);
	}
	
	@Override
	public AAISimpleUri resultIndex(int index) {
		return (AAISimpleUri) super.resultIndex(index);
	}
	
	@Override
	public AAISimpleUri resultSize(int size) {
		return (AAISimpleUri) super.resultSize(size);
	}
	
	@Override
	public AAISimpleUri limit(int size) {
		return (AAISimpleUri) super.limit(size);
	}
	
	@Override
	public AAISimpleUri clone() {
		if (this.type != null) {
			return new AAISimpleUri((AAIObjectType)this.type, this.internalURI.clone(), values);
		} else {
			return new AAISimpleUri((AAIObjectPlurals)this.pluralType, this.internalURI.clone(), values);
		}
	}
	
	@Override
	public AAIObjectType getObjectType() {
		return (AAIObjectType)this.type;
	}

	@Override
	public AAISimpleUri depth(Depth depth) {
		return (AAISimpleUri) super.depth(depth);
	}
	@Override
	public AAISimpleUri nodesOnly(boolean nodesOnly) {
		return (AAISimpleUri)super.nodesOnly(nodesOnly);
	}
	
	@Override
	public AAISimpleUri format(Format format) {
		return (AAISimpleUri)super.format(format);
	}
}
