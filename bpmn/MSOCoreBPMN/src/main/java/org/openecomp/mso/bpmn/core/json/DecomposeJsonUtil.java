package org.openecomp.mso.bpmn.core.json;

import java.io.Serializable;
import java.io.IOException;


import org.openecomp.mso.bpmn.core.domain.AllottedResource;
import org.openecomp.mso.bpmn.core.domain.NetworkResource;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.VnfResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.mso.logger.MsoLogger;

public class DecomposeJsonUtil implements Serializable {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Method to construct Service Decomposition object converting
	 * JSON structure
	 * 
	 * @param jsonString - input in JSON format confirming ServiceDecomposition
	 * @return - ServiceDecomposition object
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static ServiceDecomposition JsonToServiceDecomposition(String jsonString) {
        
        ServiceDecomposition serviceDecomposition = new ServiceDecomposition();
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
       
		try {
			serviceDecomposition = om.readValue(jsonString, ServiceDecomposition.class);
		} catch (JsonParseException e) {
			LOGGER.debug("JsonParseException :",e);
		} catch (JsonMappingException e) {
			LOGGER.debug("JsonMappingException :",e);
		} catch (IOException e) {
			LOGGER.debug("IOException :",e);
		}
		
		return serviceDecomposition;
	}
	
	/**
	 * Method to construct Resource Decomposition object converting
	 * JSON structure
	 * 
	 * @param jsonString - input in JSON format confirming ResourceDecomposition
	 * @return - ServiceDecomposition object
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static VnfResource JsonToVnfResource(String jsonString) {
        
        VnfResource vnfResource = new VnfResource();
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
       
		try {
			vnfResource = om.readValue(jsonString, VnfResource.class);
		} catch (JsonParseException e) {
			LOGGER.debug("JsonParseException :",e);
		} catch (JsonMappingException e) {
			LOGGER.debug("JsonMappingException :",e);
		} catch (IOException e) {
			LOGGER.debug("IOException :",e);
		}
		return vnfResource;
	}
	
	/**
	 * Method to construct Resource Decomposition object converting
	 * JSON structure
	 * 
	 * @param jsonString - input in JSON format confirming ResourceDecomposition
	 * @return - ServiceDecomposition object
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static NetworkResource JsonToNetworkResource(String jsonString) {
        
        NetworkResource networkResource = new NetworkResource();
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
       
		try {
			networkResource = om.readValue(jsonString, NetworkResource.class);
		} catch (JsonParseException e) {
			LOGGER.debug("Exception :",e);
		} catch (JsonMappingException e) {
			LOGGER.debug("Exception :",e);
		} catch (IOException e) {
			LOGGER.debug("Exception :",e);
		}
		return networkResource;
	}
	
	/**
	 * Method to construct Resource Decomposition object converting
	 * JSON structure
	 * 
	 * @param jsonString - input in JSON format confirming ResourceDecomposition
	 * @return - ServiceDecomposition object
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static AllottedResource JsonToAllottedResource(String jsonString) {
        
		AllottedResource allottedResource = new AllottedResource();
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
       
		try {
			allottedResource = om.readValue(jsonString, AllottedResource.class);
		} catch (JsonParseException e) {
			LOGGER.debug("Exception :",e);
		} catch (JsonMappingException e) {
			LOGGER.debug("Exception :",e);
		} catch (IOException e) {
			LOGGER.debug("Exception :",e);
		}
		return allottedResource;
	}
}