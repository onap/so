package org.openecomp.mso.bpmn.core.domain;

public enum InventoryType{

	cloud("CLOUD"),
	service("SERVICE");

	private String type;

	private InventoryType(String type){
		this.type = type;
	}

	public String type(){
		return type;
	}
}