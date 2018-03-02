package org.openecomp.mso.client.grm;

public enum GRMAction {
	
	FIND_RUNNING("findRunning"),
	ADD("add");

	private final String action;

	GRMAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}
}
