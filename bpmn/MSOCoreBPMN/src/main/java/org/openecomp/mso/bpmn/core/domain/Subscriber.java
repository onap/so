package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

public class Subscriber implements Serializable {

	private static final long serialVersionUID = -2416018315129127022L;
	private String globalId;
	private String name;
	private String commonSiteId;

	public Subscriber(String globalId, String name, String commonSiteId){
		super();
		this.globalId = globalId;
		this.name = name;
		this.commonSiteId = commonSiteId;
	}


	public String getGlobalId(){
		return globalId;
	}


	public void setGlobalId(String globalId){
		this.globalId = globalId;
	}


	public String getName(){
		return name;
	}


	public void setName(String name){
		this.name = name;
	}


	public String getCommonSiteId(){
		return commonSiteId;
	}

	public void setCommonSiteId(String commonSiteId){
		this.commonSiteId = commonSiteId;
	}


}