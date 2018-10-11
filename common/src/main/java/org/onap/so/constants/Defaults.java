package org.onap.so.constants;

public enum Defaults {

	CLOUD_OWNER("att-aic");
	
	
	private final String value;
	
	private Defaults(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
