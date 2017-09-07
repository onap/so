package org.openecomp.mso.bpmn.core.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("allottedResource")
public class AllottedResource extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public AllottedResource(){
		resourceType = ResourceType.ALLOTTED_RESOURCE;
		setResourceId(UUID.randomUUID().toString());
	}

	/*
	 * fields specific to Allotted Resource resource type
	 */
	private String allottedResourceType;
	private String allottedResourceRole;
	private String providingServiceModelInvariantUuid;
	private String nfFunction;
	private String nfType;
	private String nfRole;
	private String nfNamingCode;
	
	/*
	 * GET and SET
	 */
	public String getAllottedResourceType() {
		return allottedResourceType;
	}
	public void setAllottedResourceType(String allottedResourceType) {
		this.allottedResourceType = allottedResourceType;
	}
	public String getAllottedResourceRole() {
		return allottedResourceRole;
	}
	public void setAllottedResourceRole(String allottedResourceRole) {
		this.allottedResourceRole = allottedResourceRole;
	}
	public String getProvidingServiceModelInvariantUuid() {
		return providingServiceModelInvariantUuid;
	}
	public void setProvidingServiceModelInvariantUuid(
			String providingServiceModelInvariantUuid) {
		this.providingServiceModelInvariantUuid = providingServiceModelInvariantUuid;
	}
	public String getNfFunction() {
		return nfFunction;
	}
	public void setNfFunction(String nfFunction) {
		this.nfFunction = nfFunction;
	}
	public String getNfType() {
		return nfType;
	}
	public void setNfType(String nfType) {
		this.nfType = nfType;
	}
	public String getNfRole() {
		return nfRole;
	}
	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}
	public String getNfNamingCode() {
		return nfNamingCode;
	}
	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
}