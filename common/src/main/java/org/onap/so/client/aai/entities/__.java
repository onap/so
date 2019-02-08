package org.onap.so.client.aai.entities;

import org.onap.so.client.graphinventory.GraphInventoryObjectName;

public class __ {

	protected __() {
		
	}
	
	public static <A> DSLQueryBuilder<A, A> identity() {
		return new DSLQueryBuilder<>();
	}
	public static <A> DSLQueryBuilder<A, A> start(DSLNode node) {
		return new DSLQueryBuilder<>(node);
	}
	public static DSLQueryBuilder<DSLNode, DSLNode> node(GraphInventoryObjectName name) {
		
		return __.<DSLNode>start(new DSLNode(name));
	}
	
	public static DSLQueryBuilder<DSLNode, DSLNode> node(GraphInventoryObjectName name, DSLNodeKey... key) {
		return __.<DSLNode>start(new DSLNode(name, key));
	}
	
	public static DSLNodeKey key(String keyName, String... value) {
		return new DSLNodeKey(keyName, value);
	}
	
	public static <A, B> DSLQueryBuilder<A, B> union(final DSLQueryBuilder<?, B>... traversal) {
		
		return __.<A>identity().union(traversal);
	}
	
public static <A> DSLQueryBuilder<A, A> where(DSLQueryBuilder<A, A> traversal) {
		
		return __.<A>identity().where(traversal);
	}
}
