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

public class AAIObjectPlurals implements AAIObjectBase, GraphInventoryObjectPlurals, Serializable {

    private static final long serialVersionUID = 5312713297525740746L;

    public static final AAIObjectPlurals CUSTOMER =
            new AAIObjectPlurals(AAIObjectType.CUSTOMER, AAINamespaceConstants.BUSINESS, "/customers");
    public static final AAIObjectPlurals GENERIC_VNF =
            new AAIObjectPlurals(AAIObjectType.GENERIC_VNF, AAINamespaceConstants.NETWORK, "/generic-vnfs");
    public static final AAIObjectPlurals PORT_GROUP =
            new AAIObjectPlurals(AAIObjectType.PORT_GROUP, AAIObjectType.VCE.uriTemplate(), "/port-groups");
    public static final AAIObjectPlurals PSERVER =
            new AAIObjectPlurals(AAIObjectType.PSERVER, AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers");
    public static final AAIObjectPlurals P_INTERFACE =
            new AAIObjectPlurals(AAIObjectType.P_INTERFACE, AAIObjectType.PSERVER.uriTemplate(), "/p-interfaces");
    public static final AAIObjectPlurals L3_NETWORK =
            new AAIObjectPlurals(AAIObjectType.L3_NETWORK, AAINamespaceConstants.NETWORK, "/l3-networks");
    public static final AAIObjectPlurals NETWORK_POLICY =
            new AAIObjectPlurals(AAIObjectType.NETWORK_POLICY, AAINamespaceConstants.NETWORK, "/network-policies");
    public static final AAIObjectPlurals VPN_BINDING =
            new AAIObjectPlurals(AAIObjectType.VPN_BINDING, AAINamespaceConstants.NETWORK, "/vpn-bindings");
    public static final AAIObjectPlurals SERVICE_SUBSCRIPTION = new AAIObjectPlurals(AAIObjectType.SERVICE_SUBSCRIPTION,
            AAIObjectType.CUSTOMER.uriTemplate(), "/service-subscriptions");
    public static final AAIObjectPlurals SERVICE_INSTANCE = new AAIObjectPlurals(AAIObjectType.SERVICE_INSTANCE,
            AAIObjectType.SERVICE_SUBSCRIPTION.uriTemplate(), "/service-instances");
    public static final AAIObjectPlurals OWNING_ENTITY =
            new AAIObjectPlurals(AAIObjectType.OWNING_ENTITY, AAINamespaceConstants.BUSINESS, "/owning-entities");
    public static final AAIObjectPlurals VOLUME_GROUP = new AAIObjectPlurals(AAIObjectType.VOLUME_GROUP,
            AAIObjectType.CLOUD_REGION.uriTemplate(), "/volume-groups");
    public static final AAIObjectPlurals AVAILIBILITY_ZONE = new AAIObjectPlurals(AAIObjectType.AVAILIBILITY_ZONE,
            AAIObjectType.CLOUD_REGION.uriTemplate(), "/availability-zones");
    public static final AAIObjectPlurals VF_MODULE =
            new AAIObjectPlurals(AAIObjectType.VF_MODULE, AAIObjectType.GENERIC_VNF.uriTemplate(), "/vf-modules");
    public static final AAIObjectPlurals CONFIGURATION =
            new AAIObjectPlurals(AAIObjectType.CONFIGURATION, AAINamespaceConstants.NETWORK, "/configurations");
    public static final AAIObjectPlurals DEFAULT_TENANT =
            new AAIObjectPlurals(AAIObjectType.DEFAULT_TENANT, AAINamespaceConstants.CLOUD_INFRASTRUCTURE
                    + "/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER + "/AAIAIC25", "/tenants");
    public static final AAIObjectPlurals NETWORK_TECHNOLOGY = new AAIObjectPlurals(AAIObjectType.NETWORK_TECHNOLOGY,
            AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/network-technologies");
    public static final AAIObjectPlurals LOGICAL_LINK =
            new AAIObjectPlurals(AAIObjectType.LOGICAL_LINK, AAINamespaceConstants.NETWORK, "/logical-links");
    public static final AAIObjectPlurals L_INTERFACE =
            new AAIObjectPlurals(AAIObjectType.L_INTERFACE, AAIObjectType.VSERVER.uriTemplate(), "/l-interfaces");
    public static final AAIObjectPlurals SUB_L_INTERFACE =
            new AAIObjectPlurals(AAIObjectType.L_INTERFACE, AAIObjectType.L_INTERFACE.uriTemplate(), "/l-interfaces");
    public static final AAIObjectPlurals INSTANCE_GROUP =
            new AAIObjectPlurals(AAIObjectType.INSTANCE_GROUP, AAINamespaceConstants.NETWORK, "/instance-groups");
    public static final AAIObjectPlurals PNF =
            new AAIObjectPlurals(AAIObjectType.PNF, AAINamespaceConstants.NETWORK, "/pnfs");

    private final String uriTemplate;
    private final String partialUri;
    private final AAIObjectType type;

    protected AAIObjectPlurals(AAIObjectType type, String parentUri, String partialUri) {
        this.uriTemplate = parentUri + partialUri;
        this.partialUri = partialUri;
        this.type = type;
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
    public AAIObjectType getType() {
        return this.type;
    }

    @Override
    public String typeName() {
        return this.getType().typeName();
    }

    @Override
    public String typeName(CaseFormat format) {
        return this.getType().typeName(format);
    }
}
