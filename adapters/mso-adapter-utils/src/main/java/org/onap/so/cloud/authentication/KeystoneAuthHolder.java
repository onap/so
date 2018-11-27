package org.onap.so.cloud.authentication;

import java.io.Serializable;
import java.util.Calendar;

public class KeystoneAuthHolder implements Serializable {

	private static final long serialVersionUID = -9073252905181739224L;
	
	private String id;
	private Calendar expiration;
	private String serviceUrl;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Calendar getexpiration() {
		return expiration;
	}
	public void setexpiration(Calendar expiration) {
		this.expiration = expiration;
	}
	public String getServiceUrl() {
		return serviceUrl;
	}
	public void setHeatUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
}
