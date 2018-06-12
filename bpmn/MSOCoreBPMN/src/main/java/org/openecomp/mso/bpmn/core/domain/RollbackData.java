package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RollbackData implements Serializable{

	private static final long serialVersionUID = -4811571658272937718L;

	private String requestId;
	private Map<String, String> additionalData = new HashMap<String, String>();


	public String getRequestId(){
		return requestId;
	}

	public void setRequestId(String requestId){
		this.requestId = requestId;
	}

	public Map<String, String> getAdditionalData(){
		return additionalData;
	}

}
