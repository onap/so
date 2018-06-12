package org.openecomp.mso.client.sniro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class SniroConductorRequest implements Serializable{

	private static final long serialVersionUID = 1906052095861777655L;
	private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SniroConductorRequest.class);

	@JsonProperty("release-locks")
	private List<Resource> resources = new ArrayList<Resource>();


	public List<Resource> getResources(){
		return resources;
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
			log.error("Unable to convert SniroConductorRequest to string", e);
		}
		return json;
	}



}
