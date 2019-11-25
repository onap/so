package org.onap.so.db.catalog.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "BBNameSelectionReference")
public class BBNameSelectionReference {
	private static final long serialVersionUID = 1L;
	
    @Column(name = "ACTOR")
    private String ControllerActor;

    @Column(name = "SCOPE")
    private String scope;
    
    @Column(name = "ACTION")
    private String action;
    
    @Column(name = "BB_NAME")
    private String bb_name;

	public String getControllerActor() {
		return ControllerActor;
	}

	public void setControllerActor(String controllerActor) {
		ControllerActor = controllerActor;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getBB_NAME() {
		return bb_name;
	}

	public void setBB_NAME(String bB_NAME) {
		bb_name = bB_NAME;
	}

	@Override
	public int hashCode() {
		 return new HashCodeBuilder().append(ControllerActor).append(bb_name).append(scope).append(action).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BBNameSelectionReference)) {
            return false;
        }
		BBNameSelectionReference castOther = (BBNameSelectionReference) other;
        return new EqualsBuilder().append(bb_name, castOther.bb_name).isEquals();
	}

	
	
    
    



}
