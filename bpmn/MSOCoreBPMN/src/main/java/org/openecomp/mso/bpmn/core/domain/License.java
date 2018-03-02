package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Stores licensing information and is an attribute
 * of a <class>HomingSolution</class>
 *
 */
@JsonRootName("license")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class License extends JsonWrapper implements Serializable  {

	private static final long serialVersionUID = 1L;

	StringBuilder sb = new StringBuilder();

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> entitlementPoolList = new ArrayList<String>();
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> licenseKeyGroupList = new ArrayList<String>();


	public List<String> getEntitlementPoolList() {
		return entitlementPoolList;
	}

	public void setEntitlementPoolList(List<String> entitlementPoolList) {
		this.entitlementPoolList = entitlementPoolList;
	}

	public List<String> getLicenseKeyGroupList() {
		return licenseKeyGroupList;
	}

	public void setLicenseKeyGroupList(List<String> licenseKeyGroupList) {
		this.licenseKeyGroupList = licenseKeyGroupList;
	}

	/**
	 * This method adds a Entitlement Pool Uuid
	 * to the EntitlementPoolList
	 *
	 * @param the EntitlementPoolUuid
	 */
	public void addEntitlementPool(String entitlementPoolUuid) {
		entitlementPoolList.add(entitlementPoolUuid);
	}

	/**
	 * This method adds a License Key Group Uuid
	 * to the LicenseKeyGroupList
	 *
	 * @param the licenseKeyGroupUuid
	 */
	public void addLicenseKeyGroup(String licenseKeyGroupUuid) {
		licenseKeyGroupList.add(licenseKeyGroupUuid);
	}

	/**
	 * This method returns the licenseKeyGroupList
	 * as a json array
	 *
	 * @return the strList
	 */
	@JsonIgnore
	public JSONArray getLicenseKeyGroupListAsString() {
        JSONArray array = new JSONArray(licenseKeyGroupList);
		return array;
	}

	/**
	 * This method returns the entitlementPoolList
	 * as a json array
	 *
	 * @return the strList
	 */
	@JsonIgnore
	public JSONArray getEntitlementPoolListAsString() {
        JSONArray array = new JSONArray(entitlementPoolList);
		return array;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
