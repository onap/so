package org.openecomp.mso.client.aai;


public enum AAISubgraphType {
	STAR("star"),
	PRUNE("prune");

	private final String name;

	private AAISubgraphType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}