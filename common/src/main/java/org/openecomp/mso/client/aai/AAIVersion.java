package org.openecomp.mso.client.aai;

public enum AAIVersion {
	V8("v8"),
	V9("v9"), 
	V10("v10"), 
	V11("v11"), 
	V12("v12");
	
	public final static AAIVersion LATEST = AAIVersion.values()[AAIVersion.values().length - 1];
	private final String value;
	private AAIVersion(String value){
		this.value = value;
	}
	@Override
	public String toString(){
		return this.value;
	}
}


