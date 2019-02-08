package org.onap.so.client.aai.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;


public class DSLQueryBuilder<S, E> implements QueryStep {

	private List<QueryStep> steps = new ArrayList<>();
	
	
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
	
	public String limit(int limit) {
		return compile() + " LIMIT " + limit;
	}
	
	@Override
	public String build() {
		return compile();
	}
	
	private String compile() {
		return Joiner.on(" ").join(steps.stream().map(item -> item.build()).collect(Collectors.toList()));
	}
	
	protected QueryStep getFirst() {
		return steps.get(0);
	}
}
