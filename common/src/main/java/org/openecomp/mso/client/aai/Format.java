package org.openecomp.mso.client.aai;

public enum Format {

	RESOURCE("resource"),
	SIMPLE("simple"),
	RAW("raw"),
	CONSOLE("console"),
	PATHED("pathed"),
	GRAPHSON("graphson"),
	ID("id");

	private final String name;
	
	private Format(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
