package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LicenseInfo implements Serializable{

	private static final long serialVersionUID = 6878164369491185856L;

	@JsonProperty("licenseDemands")
	private List<Demand> demands = new ArrayList<Demand>();


	public List<Demand> getDemands(){
		return demands;
	}

	public void setDemands(List<Demand> demands){
		this.demands = demands;
	}

}
