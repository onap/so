/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.nwrest;

import org.openecomp.mso.adapters.NetworkMessageRequest;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.entity.MsoRequest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.NoJackson;

import org.codehaus.jackson.map.annotate.JsonRootName;

/* README
* 1) Used JAXB/Jettison - see @NoJackson annotation on class to get RootElements REad by RestEasy
* 2) due to 1) Maps need to use this format

"networkParams": {"entry": [
                         	{"key": "network_id",
                         	"value": "59ed7b41-2983-413f-ba93-e7d437433916"},
                         	{"key": "subnet_id",
                         	"value": "086c9298-5c57-49b7-bb2b-6fd5730c5d92"},
                         	{"key": "server_name_0",
                         	"value": "RaaNetwork1"}
                         	]},
 * 3) to output json see toJSonString method below which required the @JsonRootName annotation and the WRAP_ROOT feature enabled
 * 4) Tryong to work with RESTEASY JACKSON and JAXB/JETTISON to conform to Json input/output specs
*/
@JsonRootName("createNetworkRequest")
@XmlRootElement(name = "createNetworkRequest")
@NoJackson
public class CreateNetworkRequest extends NetworkMessageRequest {
	private String cloudSiteId;
	private String tenantId;
	private String networkId;
	private String networkName;
	private String networkType;
	private String networkTypeVersion;
	private String modelCustomizationUuid;
	private NetworkTechnology networkTechnology = NetworkTechnology.NEUTRON;
	private List<Subnet> subnets;
	private ProviderVlanNetwork providerVlanNetwork;
	private ContrailNetwork contrailNetwork;
	private Boolean failIfExists = false;
	private Boolean backout = true;
	private Map<String,String> networkParams = new HashMap<>();
	private MsoRequest msoRequest = new MsoRequest();

	public CreateNetworkRequest() {
		super();
	}

	public String getCloudSiteId() {
		return cloudSiteId;
	}

	public void setCloudSiteId(String cloudSiteId) {
		this.cloudSiteId = cloudSiteId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}

	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getNetworkTypeVersion() {
		return networkTypeVersion;
	}

	public void setNetworkTypeVersion(String networkTypeVersion) {
		this.networkTypeVersion = networkTypeVersion;
	}

	public String getNetworkTechnology() {
		return networkTechnology.toString();
	}

	public void setNetworkTechnology(String networkTechnology) {
		this.networkTechnology = NetworkTechnology.valueOf(networkTechnology);
	}

	public List<Subnet> getSubnets() {
		return subnets;
	}

	public void setSubnets(List<Subnet> subnets) {
		this.subnets = subnets;
	}

	public ProviderVlanNetwork getProviderVlanNetwork() {
		return providerVlanNetwork;
	}

	public void setProviderVlanNetwork(ProviderVlanNetwork providerVlanNetwork) {
		this.providerVlanNetwork = providerVlanNetwork;
	}

	public ContrailNetwork getContrailNetwork() {
		return contrailNetwork;
	}

	public void setContrailNetwork(ContrailNetwork contrailNetwork) {
		this.contrailNetwork = contrailNetwork;
	}

	public Boolean getFailIfExists() {
		return failIfExists;
	}

	public void setFailIfExists(Boolean failIfExists) {
		this.failIfExists = failIfExists;
	}

	public Boolean getBackout() {
		return backout;
	}

	public void setBackout(Boolean backout) {
		this.backout = backout;
	}

	public Map<String, String> getNetworkParams() {
		return networkParams;
	}

	public void setNetworkParams(Map<String, String> networkParams) {
		this.networkParams = networkParams;
	}

	public MsoRequest getMsoRequest() {
		return msoRequest;
	}

	public void setMsoRequest(MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}

	public boolean isContrailRequest() {
		return (networkTechnology == NetworkTechnology.CONTRAIL) && (contrailNetwork != null);
	}
}
