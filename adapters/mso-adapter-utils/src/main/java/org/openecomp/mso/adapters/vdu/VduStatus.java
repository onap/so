package org.openecomp.mso.adapters.vdu;

public class VduStatus {
	
	private VduStateType state;	
	private String errorMessage;
	private PluginAction lastAction;	
	
	public VduStateType getState() {
		return state;
	}
	public void setState(VduStateType state) {
		this.state = state;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public PluginAction getLastAction() {
		return lastAction;
	}
	public void setLastAction(PluginAction lastAction) {
		this.lastAction = lastAction;
	}
	public void setLastAction (String action, String status, String rawCloudMessage) {
		lastAction = new PluginAction();
		lastAction.setAction (action);
		lastAction.setStatus (status);
		lastAction.setRawMessage(rawCloudMessage);
	}
	
}