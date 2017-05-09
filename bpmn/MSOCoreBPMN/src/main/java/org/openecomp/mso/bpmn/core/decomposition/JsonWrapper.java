package org.openecomp.mso.bpmn.core.decomposition;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;


//import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Wrapper encapsulates needed JSON functionality 
 * to be extended by MSO service decomposition objects
 * providing ways to convert to and from JSON
 * 
 * @author 
 *
 */
@JsonInclude(Include.NON_NULL)
public abstract class JsonWrapper {

	@JsonInclude(Include.NON_NULL)
	public String toJsonString(){
		
		

		String jsonString = "";
		//convert with Jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(org.codehaus.jackson.map.SerializationConfig.Feature.WRAP_ROOT_VALUE);
		
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try {
			jsonString = ow.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonString;
	}
	
	@JsonInclude(Include.NON_NULL)
	public JSONObject toJsonObject(){

        ObjectMapper mapper = new ObjectMapper();
       // mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
        //mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        mapper.enable(org.codehaus.jackson.map.SerializationConfig.Feature.WRAP_ROOT_VALUE);
       // mapper.enable(org.codehaus.jackson.map.DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);
        JSONObject json = new JSONObject();
         try {
			json = new JSONObject(mapper.writeValueAsString(this));
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         return json; 
	}
	
	public String listToJson(List list) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(org.codehaus.jackson.map.SerializationConfig.Feature.WRAP_ROOT_VALUE);
        
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(list);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonString;
	}
	
	/**
	 * Method to construct Service Decomposition object converting
	 * JSON structure
	 * 
	 * @param jsonString - input in JSON format confirming ServiceDecomposition
	 * @return - ServiceDecomposition object
	 */
	public ServiceDecomposition JsonToServiceDecomposition(String jsonString) {
        
        ServiceDecomposition serviceDecomposition = new ServiceDecomposition();
        ObjectMapper om = new ObjectMapper();
        om.configure(Feature.UNWRAP_ROOT_VALUE, true);
       
		try {
			serviceDecomposition = om.readValue(jsonString, ServiceDecomposition.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return serviceDecomposition;
	}
	
}
