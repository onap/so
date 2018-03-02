package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class is used to store instance
 * data of services aka ServiceDecomposition
 *
 * @author bb3476
 *
 */

public class Request extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String sdncRequestId;
	private String requestId;
	private ModelInfo modelInfo;
	private String productFamilyId;
	
	public String getSdncRequestId() {
		return sdncRequestId;
	}
	public void setSdncRequestId(String sdncRequestId) {
		this.sdncRequestId = sdncRequestId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	public String getProductFamilyId() {
		return productFamilyId;
	}
	public void setProductFamilyId(String productFamilyId) {
		this.productFamilyId = productFamilyId;
	}
	
	
}