package org.openecomp.mso.cloudify.v3.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName("blueprint")
public class Blueprint implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("description")
    private String description;

    @JsonProperty("id")
    private String id;
    
    @JsonProperty("main_file_name")
    private String mainFileName;
    
    @JsonProperty("plan")
    private Map<String, Object> plan = null;
    
    @JsonProperty("tenant_name")
    private String tenantName;
    
    @JsonProperty("updated_at")
    private Date updatedAt;
    
    // ObjectMapper instance to parse Json stack outputs
    @JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper();

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMainFileName() {
        return mainFileName;
    }

    public void setMainFileName(String mainFileName) {
        this.mainFileName = mainFileName;
    }
    
    public Map<String, Object> getPlan() {
    	return this.plan;
    }
    
    public void setPlan(Map<String, Object> plan) {
    	this.plan = plan;
    }

    public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


	/*
	 * Return an  output as a Json-mapped Object of the provided type.
	 * This is useful for json-object outputs.
	 */
	public <T> T getMapValue (Map<String,Object> map, String key, Class<T> type)
	{
		if (map.containsKey(key)) {
			try {
				String s = mapper.writeValueAsString(map.get(key));
				return (mapper.readValue(s, type));
			}
			catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	@Override
    public String toString() {
        return "Deployment{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", mainFileName='" + mainFileName + '\'' +
                ", tenantName='" + tenantName + '\'' +
                '}';
    }

    /*  Add a definition of the Cloudify "plan" attribute once we know what it is.

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static final class Plan {
	}
	
*/
    

}
