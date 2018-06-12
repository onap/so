package org.openecomp.mso.client.sniro.beans;

import java.io.Serializable;

import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class SniroManagerRequest implements Serializable{

	private static final long serialVersionUID = -1541132882892163132L;
	private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SniroManagerRequest.class);

	@JsonRawValue
	@JsonProperty("requestInfo")
	private String requestInformation;
	@JsonRawValue
	@JsonProperty("serviceInfo")
	private String serviceInformation;
	@JsonRawValue
	@JsonProperty("placementInfo")
	private String placementInformation;
	@JsonRawValue
	@JsonProperty("licenseInfo")
	private String licenseInformation;


	public String getRequestInformation() {
		return requestInformation;
	}
	public void setRequestInformation(String requestInformation) {
		this.requestInformation = requestInformation;
	}
	public String getServiceInformation() {
		return serviceInformation;
	}
	public void setServiceInformation(String serviceInformation) {
		this.serviceInformation = serviceInformation;
	}
	public String getPlacementInformation() {
		return placementInformation;
	}
	public void setPlacementInformation(String placementInformation) {
		this.placementInformation = placementInformation;
	}
	public String getLicenseInformation() {
		return licenseInformation;
	}
	public void setLicenseInformation(String licenseInformation) {
		this.licenseInformation = licenseInformation;
	}


	@JsonInclude(Include.NON_NULL)
	public String toJsonString(){
		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try{
			json = ow.writeValueAsString(this);
		}catch (Exception e){
			log.error("Unable to convert SniroManagerRequest to string", e);
		}
		return json.replaceAll("\\\\", "");
	}


}
