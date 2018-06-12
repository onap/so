package org.openecomp.mso.db.catalog.beans;

//TODO find this file a new location?
public enum OrchestrationAction {
	ASSIGN("Assign"),
	UNASSIGN("Unassign"),
	ACTIVATE("Activate"),
	DEACTIVATE("Deactivate"),
	CHANGE_MODEL("ChangeModel"),
	CREATE("Create"),
	DELETE("Delete"),
	CUSTOM("Custom");
	
	private final String name;
	
	private OrchestrationAction(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
