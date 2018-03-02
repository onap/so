package org.openecomp.mso.client.aai.entities.uri;

public enum Depth {
	ZERO("0"),
	ONE("1"),
	TWO("2"),
	THREE("3"),
	FOUR("4"),
	FIVE("5"),
	SIX("6"),
	ALL("all");
	
	private final String depth;
	private Depth(String s) {
		
		this.depth = s;
	}
	
	
	@Override
	public String toString() {
		return this.depth;
	}
	
}
