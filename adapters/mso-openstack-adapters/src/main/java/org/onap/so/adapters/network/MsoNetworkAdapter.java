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

package org.onap.so.adapters.network;


import java.util.List;
import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Holder;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.NetworkStatus;
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.openstack.beans.Subnet;

@WebService(name = "NetworkAdapter", targetNamespace = "http://org.onap.so/network")
public interface MsoNetworkAdapter {
    // TODO: Rename all of these to include Vlan in the service name? At least for the
    // create and update calls, since they are specific to VLAN-based provider networks.

    /**
     * This is the "Create Network" Web Service Endpoint definition.
     */
    @WebMethod
    public void createNetwork(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkType") @XmlElement(required = true) String networkType,
            @WebParam(name = "modelCustomizationUuid") String modelCustomizationUuid,
            @WebParam(name = "networkName") @XmlElement(required = true) String networkName,
            @WebParam(name = "physicalNetworkName") String physicalNetworkName,
            @WebParam(name = "vlans") List<Integer> vlans, @WebParam(name = "shared") String shared,
            @WebParam(name = "external") String external, @WebParam(name = "failIfExists") Boolean failIfExists,
            @WebParam(name = "backout") Boolean backout, @WebParam(name = "subnets") List<Subnet> subnets,
            @WebParam(name = "networkParams") Map<String, String> networkParams,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "networkId", mode = Mode.OUT) Holder<String> networkId,
            @WebParam(name = "neutronNetworkId", mode = Mode.OUT) Holder<String> neutronNetworkId,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap,
            @WebParam(name = "rollback", mode = Mode.OUT) Holder<NetworkRollback> rollback) throws NetworkException;

    @WebMethod
    public void createNetworkContrail(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkType") @XmlElement(required = true) String networkType,
            @WebParam(name = "modelCustomizationUuid") String modelCustomizationUuid,
            @WebParam(name = "networkName") @XmlElement(required = true) String networkName,
            @WebParam(name = "routeTargets") List<RouteTarget> routeTargets, @WebParam(name = "shared") String shared,
            @WebParam(name = "external") String external, @WebParam(name = "failIfExists") Boolean failIfExists,
            @WebParam(name = "backout") Boolean backout, @WebParam(name = "subnets") List<Subnet> subnets,
            @WebParam(name = "networkParams") Map<String, String> networkParams,
            @WebParam(name = "policyFqdns") List<String> policyFqdns,
            @WebParam(name = "routeTableFqdns") List<String> routeTableFqdns,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "networkId", mode = Mode.OUT) Holder<String> networkId,
            @WebParam(name = "neutronNetworkId", mode = Mode.OUT) Holder<String> neutronNetworkId,
            @WebParam(name = "networkFqdn", mode = Mode.OUT) Holder<String> networkFqdn,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap,
            @WebParam(name = "rollback", mode = Mode.OUT) Holder<NetworkRollback> rollback) throws NetworkException;

    /**
     * This is the "Update VLANs" Web Service Endpoint definition. This webservice replaces the set of VLANs on a
     * network.
     */
    @WebMethod
    public void updateNetwork(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkType") @XmlElement(required = true) String networkType,
            @WebParam(name = "modelCustomizationUuid") String modelCustomizationUuid,
            @WebParam(name = "networkId") @XmlElement(required = true) String networkId,
            @WebParam(name = "networkName") @XmlElement(required = true) String networkName,
            @WebParam(name = "physicalNetworkName") @XmlElement(required = true) String physicalNetworkName,
            @WebParam(name = "vlans") @XmlElement(required = true) List<Integer> vlans,
            @WebParam(name = "shared") String shared, @WebParam(name = "external") String external,
            @WebParam(name = "subnets") List<Subnet> subnets,
            @WebParam(name = "networkParams") Map<String, String> networkParams,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap,
            @WebParam(name = "rollback", mode = Mode.OUT) Holder<NetworkRollback> rollback) throws NetworkException;

    @WebMethod
    public void updateNetworkContrail(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkType") @XmlElement(required = true) String networkType,
            @WebParam(name = "modelCustomizationUuid") String modelCustomizationUuid,
            @WebParam(name = "networkId") @XmlElement(required = true) String networkId,
            @WebParam(name = "networkName") @XmlElement(required = true) String networkName,
            @WebParam(name = "routeTargets") List<RouteTarget> routeTargets, @WebParam(name = "shared") String shared,
            @WebParam(name = "external") String external, @WebParam(name = "subnets") List<Subnet> subnets,
            @WebParam(name = "networkParams") Map<String, String> networkParams,
            @WebParam(name = "policyFqdns") List<String> policyFqdns,
            @WebParam(name = "routeTableFqdns") List<String> routeTableFqdns,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap,
            @WebParam(name = "rollback", mode = Mode.OUT) Holder<NetworkRollback> rollback) throws NetworkException;

    /**
     * TODO: This is the "Add VLAN" Web Service Endpoint definition. This webservice adds a VLAN to a network. This
     * service assumes that PO supports querying the current vlans in real time. Otherwise, the caller must have the
     * complete list and should use updateVlans instead.
     * 
     * @WebMethod public void addVlan (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
     * @WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
     * @WebParam(name="networkType") @XmlElement(required=true) String networkType,
     * @WebParam(name="networkId") @XmlElement(required=true) String networkId,
     * @WebParam(name="physicalNetworkName") @XmlElement(required=true) String physicalNetworkName,
     * @WebParam(name="vlan") @XmlElement(required=true) Integer vlan,
     * @WebParam(name="rollback", mode=Mode.OUT) Holder<NetworkRollback> rollback ) throws NetworkException;
     */

    /**
     * TODO: This is the "Remove VLAN" Web Service Endpoint definition. This webservice removes a VLAN from a network.
     * This service assumes that PO supports querying the current vlans in real time. Otherwise, the caller must have
     * the complete list and should use updateVlans instead.
     *
     * This service returns an indicator (noMoreVLans) if the VLAN that was removed was the last one on the network.
     *
     * It is not clear that Rollback will work for delete. The network can be recreated from the NetworkRollback object,
     * but the network ID (and stack ID for Heat-based orchestration) will be different. The caller will need to know to
     * update these identifiers in the inventory DB (A&AI).
     * 
     * @WebMethod public void removeVlan (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
     * @WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
     * @WebParam(name="networkType") @XmlElement(required=true) String networkType,
     * @WebParam(name="networkId") @XmlElement(required=true) String networkId,
     * @WebParam(name="physicalNetworkName") @XmlElement(required=true) String physicalNetworkName,
     * @WebParam(name="vlan") @XmlElement(required=true) Integer vlan,
     * @WebParam(name="noMoreVlans", mode=Mode.OUT) Holder<Boolean> noMoreVlans,
     * @WebParam(name="rollback", mode=Mode.OUT) Holder<NetworkRollback> rollback ) throws NetworkException;
     */

    /**
     * This is the "Query Network" Web Service Endpoint definition. TODO: Should this just return the NetworkInfo
     * complete structure?
     */
    @WebMethod
    public void queryNetwork(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkNameOrId") @XmlElement(required = true) String networkNameOrId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "networkExists", mode = Mode.OUT) Holder<Boolean> networkExists,
            @WebParam(name = "networkId", mode = Mode.OUT) Holder<String> networkId,
            @WebParam(name = "neutronNetworkId", mode = Mode.OUT) Holder<String> neutronNetworkId,
            @WebParam(name = "status", mode = Mode.OUT) Holder<NetworkStatus> status,
            @WebParam(name = "vlans", mode = Mode.OUT) Holder<List<Integer>> vlans,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap)
            throws NetworkException;

    @WebMethod
    public void queryNetworkContrail(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkNameOrId") @XmlElement(required = true) String networkNameOrId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "networkExists", mode = Mode.OUT) Holder<Boolean> networkExists,
            @WebParam(name = "networkId", mode = Mode.OUT) Holder<String> networkId,
            @WebParam(name = "neutronNetworkId", mode = Mode.OUT) Holder<String> neutronNetworkId,
            @WebParam(name = "status", mode = Mode.OUT) Holder<NetworkStatus> status,
            @WebParam(name = "routeTargets", mode = Mode.OUT) Holder<List<RouteTarget>> routeTargets,
            @WebParam(name = "subnetIdMap", mode = Mode.OUT) Holder<Map<String, String>> subnetIdMap)
            throws NetworkException;

    /**
     * This is the "Delete Network" Web Service endpoint definition.
     */
    @WebMethod
    public void deleteNetwork(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "networkType") @XmlElement(required = true) String networkType,
            @WebParam(name = "modelCustomizationUuid") String modelCustomizationUuid,
            @WebParam(name = "networkId") @XmlElement(required = true) String networkId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "networkDeleted", mode = Mode.OUT) Holder<Boolean> networkDeleted,
            @WebParam(name = "pollForCompletion") @XmlElement(required = false) Boolean pollForCompletion)
            throws NetworkException;

    /**
     * This is the "Rollback Network" Web Service endpoint definition.
     */
    @WebMethod
    public void rollbackNetwork(@WebParam(name = "rollback") @XmlElement(required = true) NetworkRollback rollback,
            @WebParam(name = "pollForCompletion") @XmlElement(required = false) Boolean pollForCompletion)
            throws NetworkException;

    @WebMethod
    public void healthCheck();
}
