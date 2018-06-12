package org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects;

public enum CandidateType {
	SERVICE_INSTANCE_ID("serviceInstanceId"),
	CLOUD_REGION_ID("cloudRegionId"),
	VNF_ID("vnfId"),
	VNF_NAME("vnfName");

	private final String name;

	private CandidateType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	//TODO added to get PojoTest to work
	public String getName(){
		return name;
	}
}
