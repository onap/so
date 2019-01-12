package org.onap.so.client.sniro.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CandidateType implements Serializable{

	private static final long serialVersionUID = 2273215496314532173L;

	@JsonProperty("name")
	private String name;


	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

}
