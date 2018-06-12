package org.openecomp.mso.apihandlerinfra;

public enum TestApi {
	GR_API("GR-API-DEFAULT"),
	VNF_API("VNF-API-DEFAULT");
	
	private final String modelName;
	
	private TestApi(String modelName) {
		this.modelName = modelName;
	}
	
	public String getModelName() {
		return modelName;
	}

}