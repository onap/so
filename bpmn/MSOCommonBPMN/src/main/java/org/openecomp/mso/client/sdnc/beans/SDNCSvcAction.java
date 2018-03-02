package org.openecomp.mso.client.sdnc.beans;

public enum SDNCSvcAction {
	ACTIVATE("activate"),
	DELETE("delete"),
	ASSIGN("assign"),
	ROLLBACK("rollback"),
	UNASSIGN("unassign"),
	DEACTIVATE("deactivate"),
	CHANGE_DELETE("changedelete"),
	CHANGE_ASSIGN("changeassign"),
	CREATE("create"),
	ENABLE("enable"),
	DISABLE("disable");
	
	private final String name;
	
	private SDNCSvcAction(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
