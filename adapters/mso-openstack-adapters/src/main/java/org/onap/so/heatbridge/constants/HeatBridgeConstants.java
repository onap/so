/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge.constants;

public class HeatBridgeConstants {

    /**
     * Openstack related constants
     */
    public static final Integer OS_DEFAULT_HEAT_NESTING = 5;
    public static final String OS_SERVER_RESOURCE_TYPE = "OS::Nova::Server";
    public static final String OS_PORT_RESOURCE_TYPE = "OS::Neutron::Port";
    public static final String OS_SRIOV_PORT_TYPE = "direct";
    public static final String OS_PCI_SLOT_KEY = "pci_slot";
    public static final String OS_PHYSICAL_NETWORK_KEY = "physical_network";
    public static final String OS_PHYSICAL_INTERFACE_KEY = "physical-interface";
    public static final String OS_VLAN_NETWORK_KEY = "vlan";
    public static final String OS_UNKNOWN_KEY = "unknown";
    public static final String OS_RESOURCES_SELF_LINK_KEY = "self";
    public static final String OS_DEFAULT_DOMAIN_NAME = "default";
    public static final String OS_KEYSTONE_V2_KEY = "v2.0";
    public static final String OS_KEYSTONE_V3_KEY = "v3";
    public static final String OS_NAME_KEY = "name";

    /**
     * AAI related constants
     */
    public static final String AAI_GENERIC_VNF = "generic-vnf";
    public static final String AAI_GENERIC_VNF_ID = "generic-vnf.vnf-id";
    public static final String AAI_PSERVER = "pserver";
    public static final String AAI_VSERVER = "vserver";
    public static final String AAI_PSERVER_HOSTNAME = "pserver.hostname";
    public static final String AAI_VF_MODULE = "vf-module";
    public static final String AAI_VF_MODULE_ID = "vf-module.vf-module-id";
    public static final String AAI_IMAGE = "image";
    public static final String AAI_IMAGE_ID = "image.image-id";
    public static final String AAI_CLOUD_OWNER = "cloud-region.cloud-owner";
    public static final String AAI_CLOUD_REGION_ID = "cloud-region.cloud-region-id";
    public static final String AAI_FLAVOR = "flavor";
    public static final String AAI_FLAVOR_ID = "flavor.flavor-id";
    public static final String AAI_RESOURCE_DEPTH_ALL = "all";
    public static final String AAI_SRIOV_PF = "sriov-pf";
    public static final String AAI_P_INTERFACE_NAME = "p-interface.interface-name";
    public static final String AAI_SRIOV_PF_PCI_ID = "sriov-pf.pf-pci-id";
    public static final String AAI_VNFC = "vnfc";
    public static final String AAI_VNFC_ID = "vnfc.vnfc-name";

    /**
     * Keys for internal usage
     */
    public static final String KEY_FLAVORS = "flavors";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_VSERVERS = "vservers";
    public static final String KEY_SRIOV_PFS = "pserverSriovPfs";
    public static final String KEY_GLOBAL_SUBSCRIBER_ID = "globalSubscriberId";
    public static final String KEY_SERVICE_TYPE = "subscriptionServiceType";
    public static final String KEY_SERVICE_INSTANCE_ID = "serviceInstanceId";
    public static final String KEY_VNF_INSTANCE_ID = "genericVnfId";
    public static final String KEY_MSO_REQUEST_ID = "msoRequestId";
    public static final String KEY_SO_WORKFLOW_EXCEPTION = "WorkflowException";
    public static final String KEY_PROCESS_STATUS_MSG = "processStatusMsg";

    private HeatBridgeConstants() {
        throw new IllegalStateException("Trying to instantiate a constants class.");
    }

}
