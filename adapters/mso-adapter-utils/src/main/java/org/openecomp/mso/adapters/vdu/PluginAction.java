package org.openecomp.mso.adapters.vdu;

/**
 * Java beam representing a detailed action performed within a plugin during VDU
 * orchestration. This allows the plugin to convey more detailed information about
 * recent activities it has performed.  It is primarily intended for logging and
 * troubleshooting, so plugins are free to populate this as desired.
 */
public class PluginAction {
	
	private String action;	
	private String status;
	private String rawMessage;	
	
	public PluginAction () {
	}
	
	public PluginAction (String action, String status, String rawMessage) {
		this.action = action;
		this.status = status;
		this.rawMessage = rawMessage;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRawMessage() {
		return rawMessage;
	}
	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}
	
}