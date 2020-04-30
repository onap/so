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

package org.onap.aaiclient.client.aai;

import com.google.common.base.CaseFormat;
import org.onap.aai.annotations.Metadata;
import org.onap.aai.domain.yang.AggregateRoute;
import org.onap.aai.domain.yang.AllottedResource;
import org.onap.aai.domain.yang.AvailabilityZone;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Collection;
import org.onap.aai.domain.yang.CommunicationServiceProfile;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.Connector;
import org.onap.aai.domain.yang.Customer;
import org.onap.aai.domain.yang.Device;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.ExtAaiNetwork;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LineOfBusiness;
import org.onap.aai.domain.yang.LogicalLink;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.NetworkTechnology;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.aai.domain.yang.OwningEntity;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.PhysicalLink;
import org.onap.aai.domain.yang.Platform;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.PortGroup;
import org.onap.aai.domain.yang.Project;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.Service;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceProfile;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aai.domain.yang.SpPartner;
import org.onap.aai.domain.yang.SriovPf;
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
import org.onap.aai.domain.yang.Zone;
import org.onap.aaiclient.client.aai.entities.uri.AAIFluentTypeReverseLookup;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;
import org.onap.so.constants.Defaults;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AAIObjectType implements AAIObjectBase, GraphInventoryObjectType, Serializable {

    private static final long serialVersionUID = -2877184776691514600L;
    private static Map<String, AAIObjectType> map = new HashMap<>();

    public static final AAIObjectType DEFAULT_CLOUD_REGION = new AAIObjectType(
            AAINamespaceConstants.CLOUD_INFRASTRUCTURE,
            "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/{cloud-region-id}", "default-cloud-region");
    public static final AAIObjectType CUSTOMER = new AAIObjectType(AAINamespaceConstants.BUSINESS, Customer.class);
    public static final AAIObjectType GENERIC_QUERY = new AAIObjectType("/search", "/generic-query", "generic-query");
    public static final AAIObjectType BULK_PROCESS = new AAIObjectType("/bulkprocess", "", "bulkprocess");
    public static final AAIObjectType SINGLE_TRANSACTION =
            new AAIObjectType("/bulk/single-transaction", "", "single-transaction");
    public static final AAIObjectType GENERIC_VNF = new AAIObjectType(AAINamespaceConstants.NETWORK, GenericVnf.class);
    public static final AAIObjectType GENERIC_VNFS =
            new AAIObjectType(AAINamespaceConstants.NETWORK, "/generic-vnfs", "generic-vnfs");
    public static final AAIObjectType VF_MODULE =
            new AAIObjectType(AAIObjectType.GENERIC_VNF.uriTemplate(), VfModule.class);
    public static final AAIObjectType L3_NETWORK = new AAIObjectType(AAINamespaceConstants.NETWORK, L3Network.class);
    public static final AAIObjectType NETWORK_POLICY =
            new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkPolicy.class);
    public static final AAIObjectType NODES_QUERY = new AAIObjectType("/search", "/nodes-query", "nodes-query");
    public static final AAIObjectType CUSTOM_QUERY = new AAIObjectType("/query", "", "query");
    public static final AAIObjectType ROUTE_TABLE_REFERENCE =
            new AAIObjectType(AAINamespaceConstants.NETWORK, RouteTableReference.class);
    public static final AAIObjectType DEFAULT_TENANT =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE + "/cloud-regions/cloud-region/"
                    + Defaults.CLOUD_OWNER + "/AAIAIC25", "/tenants/tenant/{tenant-id}", "default-tenant");
    public static final AAIObjectType VCE = new AAIObjectType(AAINamespaceConstants.NETWORK, Vce.class);
    public static final AAIObjectType PORT_GROUP = new AAIObjectType(AAIObjectType.VCE.uriTemplate(), PortGroup.class);
    public static final AAIObjectType VPN_BINDING = new AAIObjectType(AAINamespaceConstants.NETWORK, VpnBinding.class);
    public static final AAIObjectType CONFIGURATION =
            new AAIObjectType(AAINamespaceConstants.NETWORK, Configuration.class);
    public static final AAIObjectType PSERVER =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, Pserver.class);
    public static final AAIObjectType SERVICE_SUBSCRIPTION =
            new AAIObjectType(AAIObjectType.CUSTOMER.uriTemplate(), ServiceSubscription.class);

    public static final AAIObjectType SERVICE_INSTANCE_METADATA = new AAIObjectType(
            AAIObjectType.SERVICE_INSTANCE + "/metadata", org.onap.aai.domain.yang.v13.Metadata.class);

    public static final AAIObjectType SERVICE = new AAIObjectType(
            AAINamespaceConstants.SERVICE_DESIGN_AND_CREATION + "/services/service/{service-id}", Service.class);
    public static final AAIObjectType SERVICE_INSTANCE =
            new AAIObjectType(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), ServiceInstance.class);
    public static final AAIObjectType PROJECT = new AAIObjectType(AAINamespaceConstants.BUSINESS, Project.class);
    public static final AAIObjectType LINE_OF_BUSINESS =
            new AAIObjectType(AAINamespaceConstants.BUSINESS, LineOfBusiness.class);
    public static final AAIObjectType PLATFORM = new AAIObjectType(AAINamespaceConstants.BUSINESS, Platform.class);
    public static final AAIObjectType OWNING_ENTITY =
            new AAIObjectType(AAINamespaceConstants.BUSINESS, OwningEntity.class);
    public static final AAIObjectType ALLOTTED_RESOURCE =
            new AAIObjectType(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), AllottedResource.class);
    public static final AAIObjectType ALLOTTED_RESOURCE_ALL =
            new AAIObjectType(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "/allotted-resources", "allottedResources");
    public static final AAIObjectType PNF = new AAIObjectType(AAINamespaceConstants.NETWORK, Pnf.class);
    public static final AAIObjectType OPERATIONAL_ENVIRONMENT =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, OperationalEnvironment.class);
    public static final AAIObjectType CLOUD_REGION =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, CloudRegion.class);
    public static final AAIObjectType TENANT =
            new AAIObjectType(AAIObjectType.CLOUD_REGION.uriTemplate(), Tenant.class);
    public static final AAIObjectType VOLUME_GROUP =
            new AAIObjectType(AAIObjectType.CLOUD_REGION.uriTemplate(), VolumeGroup.class);
    public static final AAIObjectType VSERVER = new AAIObjectType(AAIObjectType.TENANT.uriTemplate(), Vserver.class);
    public static final AAIObjectType MODEL_VER = new AAIObjectType(
            AAINamespaceConstants.SERVICE_DESIGN_AND_CREATION + "/models/model/{model-invariant-id}", ModelVer.class);
    public static final AAIObjectType TUNNEL_XCONNECT =
            new AAIObjectType(AAIObjectType.ALLOTTED_RESOURCE.uriTemplate(), TunnelXconnect.class);
    public static final AAIObjectType P_INTERFACE =
            new AAIObjectType(AAIObjectType.PSERVER.uriTemplate(), PInterface.class);
    public static final AAIObjectType SRIOV_PF =
            new AAIObjectType(AAIObjectType.P_INTERFACE.uriTemplate(), SriovPf.class);
    public static final AAIObjectType LOGICAL_LINK =
            new AAIObjectType(AAINamespaceConstants.NETWORK, LogicalLink.class);
    public static final AAIObjectType PHYSICAL_LINK =
            new AAIObjectType(AAINamespaceConstants.NETWORK, PhysicalLink.class);
    public static final AAIObjectType INSTANCE_GROUP =
            new AAIObjectType(AAINamespaceConstants.NETWORK, InstanceGroup.class);
    public static final AAIObjectType COLLECTION = new AAIObjectType(AAINamespaceConstants.NETWORK, Collection.class);
    public static final AAIObjectType VNFC = new AAIObjectType(AAINamespaceConstants.NETWORK, Vnfc.class);
    public static final AAIObjectType VLAN_TAG = new AAIObjectType(AAINamespaceConstants.NETWORK, VlanTag.class);
    public static final AAIObjectType COMPLEX =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, Complex.class);
    public static final AAIObjectType CONNECTOR = new AAIObjectType(AAINamespaceConstants.BUSINESS, Connector.class);
    public static final AAIObjectType NETWORK_TECHNOLOGY =
            new AAIObjectType(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, NetworkTechnology.class);
    public static final AAIObjectType SUBNET = new AAIObjectType(AAIObjectType.L3_NETWORK.uriTemplate(), Subnet.class);
    public static final AAIObjectType SP_PARTNER = new AAIObjectType(AAINamespaceConstants.BUSINESS, SpPartner.class);
    public static final AAIObjectType DEVICE = new AAIObjectType(AAINamespaceConstants.NETWORK, Device.class);
    public static final AAIObjectType EXT_AAI_NETWORK =
            new AAIObjectType(AAINamespaceConstants.NETWORK, ExtAaiNetwork.class);
    public static final AAIObjectType AGGREGATE_ROUTE =
            new AAIObjectType(AAINamespaceConstants.NETWORK, AggregateRoute.class);
    public static final AAIObjectType L_INTERFACE =
            new AAIObjectType(AAIObjectType.VSERVER.uriTemplate(), LInterface.class);
    public static final AAIObjectType SUB_L_INTERFACE = new AAIObjectType(AAIObjectType.L_INTERFACE.uriTemplate(),
            "/l-interfaces/l-interface/{sub-interface-name}", "sub-l-interface");
    public static final AAIObjectType IMAGE = new AAIObjectType(AAIObjectType.CLOUD_REGION.uriTemplate(), Image.class);
    public static final AAIObjectType FLAVOR =
            new AAIObjectType(AAIObjectType.CLOUD_REGION.uriTemplate(), Flavor.class);
    public static final AAIObjectType UNKNOWN = new AAIObjectType("", "", "unknown") {

        private static final long serialVersionUID = 9208984071038447607L;

        @Override
        public boolean passThrough() {
            return true;
        }
    };
    public static final AAIObjectType DSL = new AAIObjectType("/dsl", "", "dsl");
    public static final AAIObjectType VNFM = new AAIObjectType(
            AAINamespaceConstants.EXTERNAL_SYSTEM + "/esr-vnfm-list/esr-vnfm/{vnfm-id}", EsrVnfm.class);
    public static final AAIObjectType VNFM_LIST =
            new AAIObjectType(AAINamespaceConstants.EXTERNAL_SYSTEM, "/esr-vnfm-list", "vnfm-list");
    public static final AAIObjectType VNFM_ESR_SYSTEM_INFO_LIST =
            new AAIObjectType(AAINamespaceConstants.EXTERNAL_SYSTEM + "/esr-vnfm-list",
                    "/esr-vnfm/{vnfm-id}/esr-system-info-list", "vnfm-esr-system-info-list");
    public static final AAIObjectType CLOUD_ESR_SYSTEM_INFO_LIST = new AAIObjectType(
            AAIObjectType.CLOUD_REGION.uriTemplate(), "/esr-system-info-list", "cloud-esr-system-info-list");
    public static final AAIObjectType ZONE = new AAIObjectType(AAINamespaceConstants.NETWORK, Zone.class);
    public static final AAIObjectType AVAILIBILITY_ZONE =
            new AAIObjectType(AAIObjectType.CLOUD_REGION.uriTemplate(), AvailabilityZone.class);
    public static final AAIObjectType THIRDPARTY_SDNC_LIST = new AAIObjectType(AAINamespaceConstants.EXTERNAL_SYSTEM,
            "/esr-thirdparty-sdnc-list", "thirdparty-sdnc-list");
    public static final AAIObjectType THIRDPARTY_SDNC_SYSTEM_INFO_LIST =
            new AAIObjectType(AAINamespaceConstants.EXTERNAL_SYSTEM + "/esr-thirdparty-sdnc-list",
                    "/esr-thirdparty-sdnc/{sdnc-id}/esr-system-info-list", "thirdparty-sdnc-system-info-list");
    public static final AAIObjectType COMMUNICATION_SERVICE_PROFILE =
            new AAIObjectType(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), CommunicationServiceProfile.class);
    public static final AAIObjectType SERVICE_PROFILE =
            new AAIObjectType(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), ServiceProfile.class);
    public static final AAIObjectType SERVICE_PROFILE_ALL =
            new AAIObjectType(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "/service-profiles", "serviceProfiles");
    public static final AAIObjectType SLICE_PROFILE =
            new AAIObjectType(AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), SliceProfile.class);
    public static final AAIObjectType SLICE_PROFILE_ALL =
            new AAIObjectType(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "/slice-profiles", "sliceProfiles");
    public static final AAIObjectType COMMUNICATION_PROFILE_ALL = new AAIObjectType(
            AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "/communication-service-profiles", "communicationProfiles");
    public static final AAIObjectType QUERY_ALLOTTED_RESOURCE =
            new AAIObjectType(AAIObjectType.SERVICE_INSTANCE.uriTemplate(), "?depth=2", "service-Instance");

    private final String uriTemplate;
    private final String parentUri;
    private final String partialUri;
    private final Class<?> aaiObjectClass;
    private final String name;

    static {
        /* Locate any AAIObjectTypes on the classpath and add them to our map */
        java.util.Collection<URL> packages = ClasspathHelper.forPackage("");
        Reflections r =
                new Reflections(new ConfigurationBuilder().setUrls(packages).setScanners(new SubTypesScanner()));

        Set<Class<? extends AAIObjectType>> resources = r.getSubTypesOf(AAIObjectType.class);

        for (Class<? extends AAIObjectType> customTypeClass : resources) {
            AAIObjectType customType;
            try {
                customType = customTypeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
    }

    protected AAIObjectType() {
        this.parentUri = null;
        this.partialUri = null;
        this.uriTemplate = null;
        this.aaiObjectClass = null;
        this.name = null;
    }

    protected AAIObjectType(String parentUri, String partialUri, String name) {
        this(parentUri, partialUri, name, true);
    }

    public AAIObjectType(String parentUri, String partialUri, String name, boolean register) {
        this.parentUri = parentUri;
        this.partialUri = partialUri;
        this.uriTemplate = parentUri + partialUri;
        this.aaiObjectClass = null;
        this.name = name;
        if (register && !AAIObjectType.map.containsKey(name)) {
            AAIObjectType.map.put(name, this);
        }
    }

    protected AAIObjectType(String parentUri, Class<?> aaiObjectClass) {
        this.parentUri = parentUri;
        this.partialUri = removeParentUri(aaiObjectClass, parentUri);
        this.uriTemplate = parentUri + partialUri;
        this.aaiObjectClass = aaiObjectClass;
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, aaiObjectClass.getSimpleName());
        if (!AAIObjectType.map.containsKey(name)) {
            AAIObjectType.map.put(name, this);
        }
    }

    @Override
    public String toString() {
        return this.uriTemplate();
    }

    public static AAIObjectType fromTypeName(String name, String uri) {

        return new AAIFluentTypeReverseLookup().fromName(name, uri);
    }

    public static AAIObjectType fromTypeName(String name) {
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
        return CaseFormat.LOWER_HYPHEN.to(format, this.name.replace("default-", ""));
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
    public int hashCode() {
        return this.typeName().hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof AAIObjectBase) {
            return this.typeName().equals(((AAIObjectBase) o).typeName());
        }

        return false;
    }

    protected String removeParentUri(Class<?> aaiObjectClass, String parentUri) {
        return aaiObjectClass.getAnnotation(Metadata.class).uriTemplate().replaceFirst(Pattern.quote(parentUri), "");
    }
}
