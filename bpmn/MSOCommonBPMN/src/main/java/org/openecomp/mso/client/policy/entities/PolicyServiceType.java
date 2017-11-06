package org.openecomp.mso.client.policy.entities;

public enum PolicyServiceType {
	GET_CONFIG("getConfig"),
	SEND_EVENT("sendEvent"),
	PUSH_POLICY("pushPolicy"),
	CREATE_POLICY("createPolicy"),
	UPDATE_POLICY("updatePolicy"),
	GET_DECISION("getDecision"),
	GET_METRICS("getMetrics"),
	DELETE_POLICY("deletePolicy"),
	LIST_CONFIG("listConfig"),
	CREATE_DICTIONARY_ITEM("createDictionaryItem"),
	UPDATE_DICTIONARY_ITEM("updateDictionaryItem"),
	GET_DICTIONARY_ITEMS("getDictionaryItems");
	
	private final String name;
	
	private PolicyServiceType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
