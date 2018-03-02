package org.openecomp.mso.asdc.client.test.emulators;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.api.notification.IStatusData;
import org.openecomp.sdc.utils.DistributionStatusEnum;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonStatusData implements IStatusData {
	
	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper();
	
	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	public JsonStatusData() {
		
	}
	
	@Override
	public String getErrorReason(){
		return "MSO FAILURE";
	}
	
	@Override
	public String getDistributionID(){
		//return (String)this.attributesMap.get("distributionID");
		return "35120a87-1f82-4276-9735-f6de5a244d65";
	}
	  
	@Override
	public String getConsumerID(){
		//return (String)this.attributesMap.get("consumerID");
		return "mso.123456";
	}
	  
	@Override
	public String getComponentName(){
		//return (String)this.attributesMap.get("componentName");
		return "SDN-C";
	}
	  
	@Override
	public Long getTimestamp(){
		//return (String)this.attributesMap.get("timestamp");
		return null;
	}
	  
	@Override
	public String getArtifactURL(){
		//return (String)this.attributesMap.get("artifactURL");
		return "/sdc/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml";
	}
	  
	@Override
	public DistributionStatusEnum getStatus(){
		//return (DistributionStatusEnum)this.attributesMap.get(DistributionStatusEnum.DEPLOY_OK);
		return DistributionStatusEnum.COMPONENT_DONE_OK;
	}
		
	/**
	 * Method instantiate a INotificationData implementation from a JSON file.
	 * 
	 * @param notifFilePath The file path in String
	 * @return A JsonNotificationData instance
	 * @throws IOException in case of the file is not readable or not accessible 
	 */
	public static JsonStatusData instantiateNotifFromJsonFile(String notifFilePath) throws IOException {
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(notifFilePath + "status-structure.json");
		
		//String fileLocation = System.getProperty("mso.config.path") + "notif-structure.json";
		
		//String source = fileLocation;
		//InputStream is = IOUtils.toInputStream(source, "UTF-8");
		
		//String myString = IOUtils.toString(is, "UTF-8");
		
		
		//System.out.println(myString);
		
		if (is == null) {
			//throw new FileExistsException("Resource Path does not exist: "+notifFilePath);
		}
			return mapper.readValue(is, JsonStatusData.class);
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}
	
}
