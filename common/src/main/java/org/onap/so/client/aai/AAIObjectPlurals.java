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

package org.onap.so.client.aai;

import java.io.Serializable;

import org.onap.so.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.so.constants.Defaults;

import com.google.common.base.CaseFormat;

public class AAIObjectPlurals implements GraphInventoryObjectPlurals, Serializable {

	private static final long serialVersionUID = 5312713297525740746L;
	
	public static final AAIObjectPlurals CUSTOMER = new AAIObjectPlurals(AAINamespaceConstants.BUSINESS, "/customers", "customer");
	public static final AAIObjectPlurals GENERIC_VNF = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/generic-vnfs", "generic-vnf");
	public static final AAIObjectPlurals PORT_GROUP = new AAIObjectPlurals(AAIObjectType.VCE.uriTemplate(), "/port-groups", "port-group");
	public static final AAIObjectPlurals PSERVER = new AAIObjectPlurals(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers", "pserver");
	public static final AAIObjectPlurals P_INTERFACE = new AAIObjectPlurals(AAIObjectType.PSERVER.uriTemplate(), "/p-interfaces", "p-interface");
	public static final AAIObjectPlurals L3_NETWORK = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/l3-networks", "l3-network");
	public static final AAIObjectPlurals NETWORK_POLICY = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/network-policies", "network-policy");
	public static final AAIObjectPlurals VPN_BINDING = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/vpn-bindings", "vpn-binding");
	public static final AAIObjectPlurals SERVICE_SUBSCRIPTION = new AAIObjectPlurals(AAIObjectType.CUSTOMER.uriTemplate(), "/service-subscriptions", "service-subscription");
	public static final AAIObjectPlurals SERVICE_INSTANCE = new AAIObjectPlurals(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), "/service-instances", "service-instance");
	public static final AAIObjectPlurals OWNING_ENTITY = new AAIObjectPlurals(AAINamespaceConstants.BUSINESS, "/owning-entities", "owning-entity");
	public static final AAIObjectPlurals VOLUME_GROUP = new AAIObjectPlurals(AAIObjectType.CLOUD_REGION.uriTemplate(), "/volume-groups", "volume-group");
	public static final AAIObjectPlurals AVAILIBILITY_ZONE = new AAIObjectPlurals(AAIObjectType.CLOUD_REGION.uriTemplate(), "/availability-zones", "availability-zone");
	public static final AAIObjectPlurals VF_MODULE = new AAIObjectPlurals(AAIObjectType.GENERIC_VNF.uriTemplate(), "/vf-modules", "vf-module");
	public static final AAIObjectPlurals CONFIGURATION = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/configurations", "configuration");
	public static final AAIObjectPlurals DEFAULT_TENANT = new AAIObjectPlurals(AAINamespaceConstants.CLOUD_INFRASTRUCTURE + "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/AAIAIC25", "/tenants", "default-tenant");
	public static final AAIObjectPlurals NETWORK_TECHNOLOGY = new AAIObjectPlurals(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/network-technologies", "network-technology");
	public static final AAIObjectPlurals LOGICAL_LINK = new AAIObjectPlurals(AAINamespaceConstants.NETWORK, "/logical-links", "logical-link");
	public static final AAIObjectPlurals L_INTERFACE = new AAIObjectPlurals(AAIObjectType.VSERVER.uriTemplate(), "/l-interfaces", "l-interface");
	public static final AAIObjectPlurals SUB_L_INTERFACE = new AAIObjectPlurals(AAIObjectType.L_INTERFACE.uriTemplate(), "/l-interfaces", "l-interface");

	private final String uriTemplate;
	private final String partialUri;
	private final String name;
	protected AAIObjectPlurals(String parentUri, String partialUri, String name) {
		this.uriTemplate = parentUri + partialUri;
		this.partialUri = partialUri;
		this.name = name;
	}

	@Override
	public String toString() {
		return this.uriTemplate();
	}

	@Override
	public String uriTemplate() {
		return this.uriTemplate;
	}

	@Override
	public String partialUri() {
		return this.partialUri;
	}

	@Override
	public String typeName() {
		return this.typeName(CaseFormat.LOWER_HYPHEN);
	}
	@Override
	public String typeName(CaseFormat format) {
		return CaseFormat.LOWER_HYPHEN.to(format, this.name);
	}
}
