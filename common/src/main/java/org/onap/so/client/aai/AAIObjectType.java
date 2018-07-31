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

import org.onap.so.client.graphinventory.GraphInventoryObjectType;

import com.google.common.base.CaseFormat;

public enum AAIObjectType implements GraphInventoryObjectType {

	DEFAULT_CLOUD_REGION(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/cloud-regions/cloud-region/att-aic/{cloud-region-id}"),
	CUSTOMER(AAINamespaceConstants.BUSINESS, "/customers/customer/{global-customer-id}"),
	GENERIC_QUERY("/search", "/generic-query"),
	BULK_PROCESS("/bulkprocess", ""),
	GENERIC_VNF(AAINamespaceConstants.NETWORK, "/generic-vnfs/generic-vnf/{vnf-id}"),
	VF_MODULE(AAIObjectType.GENERIC_VNF.uriTemplate(), "/vf-modules/vf-module/{vf-module-id}"),
	L3_NETWORK(AAINamespaceConstants.NETWORK, "/l3-networks/l3-network/{network-id}"),
	NETWORK_POLICY(AAINamespaceConstants.NETWORK, "/network-policies/network-policy/{network-policy-id}"),
	NODES_QUERY("/search", "/nodes-query"),
	CUSTOM_QUERY("/query", ""),
	ROUTE_TABLE_REFERENCE(AAINamespaceConstants.NETWORK, "/route-table-references/route-table-reference/{route-table-reference-id}"),
	DEFAULT_TENANT(AAINamespaceConstants.CLOUD_INFRASTRUCTURE + "/cloud-regions/cloud-region/att-aic/AAIAIC25", "/tenants/tenant/{tenant-id}"),
	VCE(AAINamespaceConstants.NETWORK, "/vces/vce/{vnf-id}"),
	VPN_BINDING(AAINamespaceConstants.NETWORK, "/vpn-bindings/vpn-binding/{vpn-id}"),
	VPN_BINDINGS(AAINamespaceConstants.NETWORK, "/vpn-bindings"),
	CONFIGURATION(AAINamespaceConstants.NETWORK, "/configurations/configuration/{configuration-id}"),
	PSERVER(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers/pserver/{hostname}"),
	SERVICE_SUBSCRIPTION(AAIObjectType.CUSTOMER.uriTemplate(), "/service-subscriptions/service-subscription/{service-type}"),
	SERVICE_INSTANCE(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), "/service-instances/service-instance/{service-instance-id}"),
	PROJECT(AAINamespaceConstants.BUSINESS, "/projects/project/{id}"),
	LINE_OF_BUSINESS(AAINamespaceConstants.BUSINESS, "/lines-of-business/line-of-business/{id}"),
	PLATFORM(AAINamespaceConstants.BUSINESS, "/platforms/platform/{id}"),
	OWNING_ENTITY(AAINamespaceConstants.BUSINESS, "/owning-entities/owning-entity/{id}"),
	ALLOTTED_RESOURCE(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "/allotted-resources/allotted-resource/{id}"),
	PNF(AAINamespaceConstants.NETWORK, "/pnfs/pnf/{pnf-name}"),
	OPERATIONAL_ENVIRONMENT(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/operational-environments/operational-environment/{operational-environment-id}"),
	CLOUD_REGION(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/cloud-regions/cloud-region/{cloud-owner-id}/{cloud-region-id}"),
	TENANT(AAIObjectType.CLOUD_REGION.uriTemplate(), "/tenants/tenant/{tenant-id}"),
	VOLUME_GROUP(AAIObjectType.CLOUD_REGION.uriTemplate(), "/volume-groups/volume-group/{volume-group-id}"),
	VSERVER(AAIObjectType.TENANT.uriTemplate(), "/vservers/vserver/{vserver-id}"),
	MODEL_VER(AAINamespaceConstants.SERVICE_DESIGN_AND_CREATION + "/models/model/{model-invariant-id}", "/model-vers/model-ver/{model-version-id}"),
	TUNNEL_XCONNECT(AAIObjectType.ALLOTTED_RESOURCE.uriTemplate(), "/tunnel-xconnects/tunnel-xconnect/{tunnel-id}"),
	P_INTERFACE(AAIObjectType.PSERVER.uriTemplate(), "/p-interfaces/p-interface/{interface-name}"),
	PHYSICAL_LINK(AAINamespaceConstants.NETWORK, "/physical-links/physical-link/{link-name}"),
	INSTANCE_GROUP(AAINamespaceConstants.NETWORK, "/instance-groups/instance-group/{id}"),
	COLLECTION(AAINamespaceConstants.NETWORK, "/collections/collection/{collection-id}"),
	UNKNOWN("", "");

	private final String uriTemplate;
	private final String parentUri;
	private final String partialUri;
	private AAIObjectType(String parentUri, String partialUri) {
		this.parentUri = parentUri;
		this.partialUri = partialUri;
		this.uriTemplate = parentUri + partialUri;
	}

	@Override
	public String toString() {
		return this.uriTemplate();
	}

	@Override
	public String typeName() {
		return this.typeName(CaseFormat.LOWER_HYPHEN);
	}
	@Override
	public String typeName(CaseFormat format) {
		String enumName = this.name();
		if (this.equals(AAIObjectType.DEFAULT_CLOUD_REGION) || this.equals(AAIObjectType.DEFAULT_TENANT)) {
			enumName = enumName.replace("DEFAULT_", "");
		}

		return CaseFormat.UPPER_UNDERSCORE.to(format, enumName);
	}

	@Override
	public String uriTemplate() {
		return this.uriTemplate;
	}

	@Override
	public String partialUri() {
		return this.partialUri;
	}
}
