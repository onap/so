/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.graphinventory.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.onap.so.client.aai.entities.QueryStep;
import org.onap.so.client.graphinventory.GraphInventoryObjectName;

import com.google.common.base.Joiner;


public class DSLQueryBuilder<S, E> implements QueryStep {

	private List<QueryStep> steps = new ArrayList<>();
	private String suffix = "";
	
	public DSLQueryBuilder() {
		
	}
	public DSLQueryBuilder(DSLNode node) {
		steps.add(node);
	}
	
	public DSLQueryBuilder<S, DSLNode> node(DSLNode node) {
		steps.add(node);
		
		return (DSLQueryBuilder<S, DSLNode>) this;
	}
	public DSLQueryBuilder<S, E> output() {
		if (steps.get(steps.size() -1) instanceof DSLNode) {
			((DSLNode)steps.get(steps.size() -1)).output();
		}
		return this;
	}
	
	public <E2> DSLQueryBuilder<S, E2> union(final DSLQueryBuilder<?, E2>... union) {
		
		List<DSLQueryBuilder<?, ?>> unions = Arrays.asList(union);
		steps.add(() -> {
			StringBuilder query = new StringBuilder();
		
			query.append("> [ ").append(
					Joiner.on(", ").join(
						unions.stream().map(item -> item.build()).collect(Collectors.toList())))
					.append(" ]");
			return query.toString();
		});
		
		return (DSLQueryBuilder<S, E2>) this;
	}
	
	public DSLQueryBuilder<S, E> where(DSLQueryBuilder<?, ?> where) {

		steps.add(() -> {
			StringBuilder query = new StringBuilder();
			query.append(where.build()).append(")");
			String result = query.toString();
			if (!result.startsWith(">")) {
				result = "> " + result;
			}
			return "(" + result;
		});
		return this;
	}
	
	public DSLQueryBuilder<S, E> to(DSLQueryBuilder<?, ?> to) {
		steps.add(() -> {
			StringBuilder query = new StringBuilder();
			
			query.append("> ").append(to.build());
			return query.toString();
		});
		return this;
	}
	
	public DSLQueryBuilder<S, E> to(GraphInventoryObjectName name) {
		return to(__.node(name));
	}
	
	public DSLQueryBuilder<S, E> to(GraphInventoryObjectName name, DSLNodeKey... key) {
		return to(__.node(name, key));
	}
	
	public DSLQueryBuilder<S, E> limit(int limit) {
		suffix = " LIMIT " + limit;
		return this;
	}
	
	@Override
	public String build() {
		return compile();
	}
	
	@Override
	public String toString() {
		return build();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof QueryStep) {
				return ((QueryStep)o).build().equals(this.build());
			} else if (o instanceof String) {
				return o.equals(this.build());
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		return build().hashCode();
	}
	
	private String compile() {
		return Joiner.on(" ").join(steps.stream().map(item -> item.build()).collect(Collectors.toList())) + suffix;
	}
	
	protected QueryStep getFirst() {
		return steps.get(0);
	}
}
