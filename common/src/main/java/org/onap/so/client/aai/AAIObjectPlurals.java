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

import org.onap.aai.domain.yang.NetworkTechnologies;
import org.onap.so.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.so.constants.Defaults;

import com.google.common.base.CaseFormat;

public enum AAIObjectPlurals implements GraphInventoryObjectPlurals {

	CUSTOMER(AAINamespaceConstants.BUSINESS, "/customers"),
	GENERIC_VNF(AAINamespaceConstants.NETWORK, "/generic-vnfs"),
	PORT_GROUP(AAIObjectType.VCE.uriTemplate(), "/port-groups"),
	PSERVER(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers"),
	P_INTERFACE(AAIObjectType.PSERVER.uriTemplate(), "/p-interfaces"),
	L3_NETWORK(AAINamespaceConstants.NETWORK, "/l3-networks"),
	NETWORK_POLICY(AAINamespaceConstants.NETWORK, "/network-policies"),
	VPN_BINDING(AAINamespaceConstants.NETWORK, "/vpn-bindings"),
	SERVICE_SUBSCRIPTION(AAIObjectType.CUSTOMER.uriTemplate(), "/service-subscriptions"),
	SERVICE_INSTANCE(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), "/service-instances"),
	OWNING_ENTITY(AAINamespaceConstants.BUSINESS, "/owning-entities"),
	VOLUME_GROUP(AAIObjectType.CLOUD_REGION.uriTemplate(), "/volume-groups"),
	AVAILIBILITY_ZONE(AAIObjectType.CLOUD_REGION.uriTemplate(), "/availability-zones"),
	VF_MODULE(AAIObjectType.GENERIC_VNF.uriTemplate(), "/vf-modules"),
	CONFIGURATION(AAINamespaceConstants.NETWORK, "/configurations"),
	DEFAULT_TENANT(AAINamespaceConstants.CLOUD_INFRASTRUCTURE + "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/AAIAIC25", "/tenants"),
	NETWORK_TECHNOLOGY(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/network-technologies"),
	LOGICAL_LINK(AAINamespaceConstants.NETWORK, "/logical-links");

	private final String uriTemplate;
	private final String partialUri;
	private AAIObjectPlurals(String parentUri, String partialUri) {
		this.uriTemplate = parentUri + partialUri;
		this.partialUri = partialUri;
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
		return CaseFormat.UPPER_UNDERSCORE.to(format, this.name());
	}
}
