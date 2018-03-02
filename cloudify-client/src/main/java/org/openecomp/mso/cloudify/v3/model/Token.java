package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName("token")
// The Token object is returned without a root element
public class Token implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("role")
    private String role;

    @JsonProperty("value")
    private String value;
    
    //  Any expiration?  Maybe something in the Headers?
    
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	@Override
    public String toString() {
        return "Token{" +
                "role='" + role + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
