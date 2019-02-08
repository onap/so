package org.onap.so.client.aai.entities;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DSLQuery {

	private String dsl;

	public DSLQuery() {
		
	}
	
	public DSLQuery(String dsl) {
		this.dsl = dsl;
	}
	
	public String getDsl() {
		return dsl;
	}

	public void setDsl(String dsl) {
		this.dsl = dsl;
	}
	
	
}
