package org.openecomp.mso.asdc.client.test.emulators;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


public class JsonNotificationData implements INotificationData {
	
	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper();
	
	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	@JsonProperty("serviceArtifacts")
	@JsonDeserialize(using=JsonArtifactInfoDeserializer.class)
	private List<IArtifactInfo> serviceArtifacts;
	
	@JsonProperty("resources")
	@JsonDeserialize(using=JsonResourceInfoDeserializer.class)
	private List<IResourceInstance> resourcesList;
	
	public JsonNotificationData() {
		
	}
		
	/**
	 * Method instantiate a INotificationData implementation from a JSON file.
	 * 
	 * @param notifFilePath The file path in String
	 * @return A JsonNotificationData instance
	 * @throws IOException in case of the file is not readable or not accessible 
	 */
	public static JsonNotificationData instantiateNotifFromJsonFile(String notifFilePath) throws IOException {
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(notifFilePath + "notif-structure.json");
		
		//String fileLocation = System.getProperty("mso.config.path") + "notif-structure.json";
		
		//String source = fileLocation;
		//InputStream is = IOUtils.toInputStream(source, "UTF-8");
		
		//String myString = IOUtils.toString(is, "UTF-8");
		
		
		//System.out.println(myString);
		
		if (is == null) {
			//throw new FileExistsException("Resource Path does not exist: "+notifFilePath);
		}
			return mapper.readValue(is, JsonNotificationData.class);
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}
	
	@Override
	public String getWorkloadContext(){
		return (String)this.attributesMap.get("workloadContext");
	}
	  
	@Override
	public void setWorkloadContext(java.lang.String arg0){
		
	}

	@Override
	public IArtifactInfo getArtifactMetadataByUUID(String arg0) {
		return null;
	}

	@Override
	public String getDistributionID() {
		return (String)this.attributesMap.get("distributionID");
	}

	@Override
	public List<IResourceInstance> getResources() {
		return resourcesList;
	}

	@Override
	public List<IArtifactInfo> getServiceArtifacts() {
		return this.serviceArtifacts;
	}

	@Override
	public String getServiceDescription() {
		return (String)this.attributesMap.get("serviceDescription");
	}

	@Override
	public String getServiceInvariantUUID() {
		return (String)this.attributesMap.get("serviceInvariantUUID");
	}

	@Override
	public String getServiceName() {
		return (String)this.attributesMap.get("serviceName");
	}

	@Override
	public String getServiceUUID() {
		return (String)this.attributesMap.get("serviceUUID");
	}

	@Override
	public String getServiceVersion() {
		return (String)this.attributesMap.get("serviceVersion");
	}
}
