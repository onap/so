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

package org.openecomp.mso.client.aai.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.client.aai.AAIObjectName;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.jsonpath.JsonPathUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;

public class Relationships {

	private final ObjectMapper mapper;
	private Map<String, Object> map;
	private final String jsonBody;
	public Relationships(String json) {
		this.jsonBody = json;
		this.mapper = new AAICommonObjectMapperProvider().getMapper();
		try {
			this.map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
		} catch (IOException e) {
			this.map = new HashMap<>();
		}
	}
	
	public List<AAIResultWrapper> getByType(AAIObjectName type) {
		
		return this.getAll(Optional.of(type));
	}
	
	public List<AAIResultWrapper> getAll() {
		
		return this.getAll(Optional.empty());
	}
	
	
	public List<String> getRelatedLinks() {
		return this.getRelatedLinks(Optional.empty());
	}
	
	public List<String> getRelatedLinks(AAIObjectName type) {
		return this.getRelatedLinks(Optional.of(type));
	}
	
	public List<AAIResourceUri> getRelatedAAIUris() {
		return this.getRelatedAAIUris(x -> true);
	}
	
	public List<AAIResourceUri> getRelatedAAIUris(AAIObjectName type) {
		return this.getRelatedAAIUris(x -> type.typeName().equals(x));
	}
	protected List<AAIResourceUri> getRelatedAAIUris(Predicate<String> p) {
		List<AAIResourceUri> result = new ArrayList<>();
		if (map.containsKey("relationship")) {
			List<Map<String, Object>> relationships = (List<Map<String, Object>>)map.get("relationship");
			for (Map<String, Object> relationship : relationships) {
				final String relatedTo = (String)relationship.get("related-to");
				if (p.test(relatedTo)) {
					AAIObjectType type;
					try {
						type = AAIObjectType.valueOf(CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, relatedTo));
					} catch (IllegalArgumentException e) {
						type = AAIObjectType.UNKNOWN;
					}
					final String relatedLink = (String)relationship.get("related-link");
					
					result.add(AAIUriFactory.createResourceFromExistingURI(type, UriBuilder.fromPath(relatedLink).build()));
				}
			}
		}
		return result;
	}
	
	
	
	protected List<AAIResultWrapper> getAll(final Optional<AAIObjectName> type) {
		List<AAIResourceUri> relatedLinks;
		if (type.isPresent()) {
			relatedLinks = this.getRelatedAAIUris(type.get());
		} else {
			relatedLinks = this.getRelatedAAIUris();
		}
		ArrayList<AAIResultWrapper> result = new ArrayList<>();
		for (AAIResourceUri link : relatedLinks) {
			result.add(this.get(link));
		}
		return result;
	}
	
	protected AAIResultWrapper get(AAIResourceUri uri) {
		return new AAIResourcesClient().get(uri);
		
	}
	
	protected List<String> getRelatedLinks(Optional<AAIObjectName> type) {
		String matcher = "";
		if (type.isPresent()) {
			matcher = "[?(@.related-to=='" + type.get() + "')]";
		}
		return JsonPathUtil.getInstance().locateResultList(this.jsonBody, String.format("$.relationship%s.related-link", matcher));
	}
	
	public String getJson() {
		return this.jsonBody;
	}
}
