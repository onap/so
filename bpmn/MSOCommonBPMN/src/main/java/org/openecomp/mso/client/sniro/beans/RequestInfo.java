package org.openecomp.mso.client.sniro.beans;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("requestInfo")
public class RequestInfo implements Serializable{

	private static final long serialVersionUID = -759180997599143791L;

	@JsonProperty("transactionId")
	String transactionId;
	@JsonProperty("requestId")
	String requestId;

	public String getTransactionId(){
		return transactionId;
	}

	public void setTransactionId(String transactionId){
		this.transactionId = transactionId;
	}

	public String getRequestId(){
		return requestId;
	}

	public void setRequestId(String requestId){
		this.requestId = requestId;
	}


}
