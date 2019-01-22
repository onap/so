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

import java.util.HashMap;
import java.util.Map;

import org.onap.aai.annotations.Metadata;
import org.onap.aai.domain.yang.AllottedResource;
import org.onap.aai.domain.yang.AggregateRoute;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Collection;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.Connector;
import org.onap.aai.domain.yang.Customer;
import org.onap.aai.domain.yang.ExtAaiNetwork;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.LineOfBusiness;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.NetworkTechnology;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.aai.domain.yang.OwningEntity;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.PhysicalLink;
import org.onap.aai.domain.yang.Platform;
import org.onap.aai.domain.yang.Project;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.SpPartner;
import org.onap.aai.domain.yang.Device;
import org.onap.aai.domain.yang.Subnet;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aai.domain.yang.TunnelXconnect;
import org.onap.aai.domain.yang.Vce;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VlanTag;
import org.onap.aai.domain.yang.Vnfc;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.graphinventory.GraphInventoryObjectType;
import org.onap.so.constants.Defaults;

import com.google.common.base.CaseFormat;

public enum AAIObjectType implements GraphInventoryObjectType {

	DEFAULT_CLOUD_REGION(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/{cloud-region-id}"),
	CUSTOMER(AAINamespaceConstants.BUSINESS, Customer.class),
	GENERIC_QUERY("/search", "/generic-query"),
	BULK_PROCESS("/bulkprocess", ""),
	SINGLE_TRANSACTION("/bulk/single-transaction", ""),
	GENERIC_VNF(AAINamespaceConstants.NETWORK, GenericVnf.class),
	VF_MODULE(AAIObjectType.GENERIC_VNF.uriTemplate(), VfModule.class),
	L3_NETWORK(AAINamespaceConstants.NETWORK, L3Network.class),
	NETWORK_POLICY(AAINamespaceConstants.NETWORK, NetworkPolicy.class),
	NODES_QUERY("/search", "/nodes-query"),
	CUSTOM_QUERY("/query", ""),
	ROUTE_TABLE_REFERENCE(AAINamespaceConstants.NETWORK, RouteTableReference.class),
	DEFAULT_TENANT(AAINamespaceConstants.CLOUD_INFRASTRUCTURE + "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/AAIAIC25", "/tenants/tenant/{tenant-id}"),
	VCE(AAINamespaceConstants.NETWORK, Vce.class),
	VPN_BINDING(AAINamespaceConstants.NETWORK, VpnBinding.class),
	CONFIGURATION(AAINamespaceConstants.NETWORK, Configuration.class),
	PSERVER(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, Pserver.class),
	SERVICE_SUBSCRIPTION(AAIObjectType.CUSTOMER.uriTemplate(), ServiceSubscription.class),
	SERVICE_INSTANCE(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), ServiceInstance.class),
	PROJECT(AAINamespaceConstants.BUSINESS, Project.class),
	LINE_OF_BUSINESS(AAINamespaceConstants.BUSINESS, LineOfBusiness.class),
	PLATFORM(AAINamespaceConstants.BUSINESS, Platform.class),
	OWNING_ENTITY(AAINamespaceConstants.BUSINESS, OwningEntity.class),
	ALLOTTED_RESOURCE(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), AllottedResource.class),
	PNF(AAINamespaceConstants.NETWORK, "/pnfs/pnf/{pnf-name}"),
	OPERATIONAL_ENVIRONMENT(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, OperationalEnvironment.class),
	CLOUD_REGION(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, CloudRegion.class),
	TENANT(AAIObjectType.CLOUD_REGION.uriTemplate(), Tenant.class),
	VOLUME_GROUP(AAIObjectType.CLOUD_REGION.uriTemplate(), VolumeGroup.class),
	VSERVER(AAIObjectType.TENANT.uriTemplate(), Vserver.class),
	MODEL_VER(AAINamespaceConstants.SERVICE_DESIGN_AND_CREATION + "/models/model/{model-invariant-id}", ModelVer.class),
	TUNNEL_XCONNECT(AAIObjectType.ALLOTTED_RESOURCE.uriTemplate(), TunnelXconnect.class),
	P_INTERFACE(AAIObjectType.PSERVER.uriTemplate(), PInterface.class),
	PHYSICAL_LINK(AAINamespaceConstants.NETWORK, PhysicalLink.class),
	INSTANCE_GROUP(AAINamespaceConstants.NETWORK, InstanceGroup.class),
	COLLECTION(AAINamespaceConstants.NETWORK, Collection.class),
	VNFC(AAINamespaceConstants.NETWORK, Vnfc.class),
	VLAN_TAG(AAINamespaceConstants.NETWORK, VlanTag.class),
	COMPLEX(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, Complex.class),
	CONNECTOR(AAINamespaceConstants.BUSINESS, Connector.class),
	NETWORK_TECHNOLOGY(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, NetworkTechnology.class),
	SUBNET(AAIObjectType.L3_NETWORK.uriTemplate(), Subnet.class),
	SP_PARTNER(AAINamespaceConstants.BUSINESS, SpPartner.class),
	DEVICE(AAINamespaceConstants.NETWORK, Device.class),
	EXT_AAI_NETWORK(AAINamespaceConstants.NETWORK, ExtAaiNetwork.class),
	AGGREGATE_ROUTE(AAINamespaceConstants.NETWORK, AggregateRoute.class),
	UNKNOWN("", "");

	private final String uriTemplate;
	private final String parentUri;
	private final String partialUri;
	private final Class<?> aaiObjectClass;
	private static Map<String, AAIObjectType> map = new HashMap<>();
	private AAIObjectType(String parentUri, String partialUri) {
		this.parentUri = parentUri;
		this.partialUri = partialUri;
		this.uriTemplate = parentUri + partialUri;
		this.aaiObjectClass = null;
	}

	private AAIObjectType(String parentUri, Class<?> aaiObjectClass) {
		this.parentUri = parentUri;
		this.partialUri = removeParentUri(aaiObjectClass, parentUri);
		this.uriTemplate = parentUri + partialUri;
		this.aaiObjectClass = aaiObjectClass;
	}

	@Override
	public String toString() {
		return this.uriTemplate();
	}

	public static AAIObjectType fromTypeName(String name) {
		if (map.isEmpty()) {
			for (AAIObjectType type : AAIObjectType.values()) {
				map.put(type.typeName(), type);
			}
		}

		if (map.containsKey(name)) {
			return map.get(name);
		} else {
			return AAIObjectType.UNKNOWN;
		}
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

	protected String removeParentUri(Class<?> aaiObjectClass, String parentUri) {
		 return aaiObjectClass.getAnnotation(Metadata.class).uriTemplate().replace(parentUri, "");
	}
}
