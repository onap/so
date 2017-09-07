package org.openecomp.mso.client.aai.entities;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL) 
public class CustomQuery {
	
	List<String> start;
	
	public String getGremlin() {
		return gremlin;
	}

	public void setGremlin(String gremlin) {
		this.gremlin = gremlin;
	}
	String query;
	String gremlin;
	
	public CustomQuery(List<String>start, String query){
		this.start=start;
		this.query= "query/" + query;
	}
	
	public CustomQuery(String gremlin) throws UnsupportedEncodingException{
		this.gremlin=gremlin;
	}
	
	public List<String> getStart() {
		return start;
	}

	public void setStart(List<String> start) {
		this.start = start;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
