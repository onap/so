/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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

package org.onap.so.adapters.tasks.audit;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vlan;
import org.onap.aai.domain.yang.Vlans;
import org.onap.aai.domain.yang.Vserver;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoNeutronUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.woorea.openstack.heat.model.Link;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.quantum.model.Port;

@Component
public class HeatStackAudit {

    private static final String RESOURCES = "/resources";

    protected static final Logger logger = LoggerFactory.getLogger(HeatStackAudit.class);

    private static final String EXCEPTION_MSG = "Error finding Path from Self Link";

    @Autowired
    protected MsoHeatUtils heat;

    @Autowired
    protected MsoNeutronUtils neutron;

    @Autowired
    protected AuditVServer auditVservers;

    public Optional<AAIObjectAuditList> queryHeatStack(String cloudOwner, String cloudRegion, String tenantId,
            String heatStackName) {
        try {
            logger.debug("Fetching Top Level Stack Information");
            Resources resources = heat.queryStackResources(cloudRegion, tenantId, heatStackName, 3);
            List<Resource> novaResources = resources.getList().stream()
                    .filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
            if (novaResources.isEmpty())
                return Optional.of(new AAIObjectAuditList());
            else {
                Set<Vserver> vserversToAudit = createVserverSet(novaResources);
                AAIObjectAuditList aaiObjectAuditList = new AAIObjectAuditList();
                vserversToAudit.stream().forEach(vServer -> aaiObjectAuditList.getAuditList()
                        .add(createAAIObjectAudit(cloudOwner, cloudRegion, tenantId, vServer)));
                return Optional.of(aaiObjectAuditList);
            }
        } catch (Exception e) {
            logger.error("Error during query stack resources", e);
            return Optional.of(new AAIObjectAuditList());
        }
    }

    public Optional<AAIObjectAuditList> auditHeatStack(String cloudRegion, String cloudOwner, String tenantId,
            String heatStackName) {
        try {
            logger.debug("Fetching Top Level Stack Information");
            Resources resources = heat.queryStackResources(cloudRegion, tenantId, heatStackName, 3);
            List<Resource> novaResources = resources.getList().stream()
                    .filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
            List<Resource> resourceGroups = resources.getList().stream()
                    .filter(p -> "OS::Heat::ResourceGroup".equals(p.getType()) && p.getName().contains("subinterfaces"))
                    .collect(Collectors.toList());
            List<Optional<Port>> neutronPortDetails = retrieveNeutronPortDetails(resources, cloudRegion, tenantId);
            if (novaResources.isEmpty())
                return Optional.of(new AAIObjectAuditList());
            else {
                Set<Vserver> vserversToAudit = createVserverSet(resources, novaResources, neutronPortDetails);
                Set<Vserver> vserversWithSubInterfaces =
                        processSubInterfaces(cloudRegion, tenantId, resourceGroups, vserversToAudit);
                return auditVservers.auditVservers(vserversWithSubInterfaces, tenantId, cloudOwner, cloudRegion);
            }
        } catch (Exception e) {
            logger.error("Error during auditing stack resources", e);
            return Optional.empty();
        }
    }

    protected Set<Vserver> processSubInterfaces(String cloudRegion, String tenantId, List<Resource> resourceGroups,
            Set<Vserver> vServersToAudit) throws Exception {
        for (Resource resourceGroup : resourceGroups) {
            processResourceGroups(cloudRegion, tenantId, vServersToAudit, resourceGroup);
        }
        return vServersToAudit;
    }

    protected void processResourceGroups(String cloudRegion, String tenantId, Set<Vserver> vServersWithLInterface,
            Resource resourceGroup) throws Exception {
        Optional<Link> stackLink =
                resourceGroup.getLinks().stream().filter(link -> "nested".equals(link.getRel())).findAny();
        if (stackLink.isPresent()) {
            try {
                Optional<String> path = extractResourcePathFromHref(stackLink.get().getHref());
                if (path.isPresent()) {
                    logger.debug("Fetching nested Resource Stack Information");
                    Resources nestedResourceGroupResources =
                            heat.executeHeatClientRequest(path.get(), cloudRegion, tenantId, Resources.class);
                    processNestedResourceGroup(cloudRegion, tenantId, vServersWithLInterface,
                            nestedResourceGroupResources);
                } else
                    throw new Exception(EXCEPTION_MSG);
            } catch (Exception e) {
                logger.error("Error Parsing Link to obtain Path", e);
                throw new Exception(EXCEPTION_MSG);
            }
        }
    }

    protected void processNestedResourceGroup(String cloudRegion, String tenantId, Set<Vserver> vServersWithLInterface,
            Resources nestedResourceGroupResources) throws Exception {
        for (Resource resourceGroupNested : nestedResourceGroupResources) {
            Optional<Link> subInterfaceStackLink =
                    resourceGroupNested.getLinks().stream().filter(link -> "nested".equals(link.getRel())).findAny();
            if (subInterfaceStackLink.isPresent()) {
                addSubInterface(cloudRegion, tenantId, vServersWithLInterface, subInterfaceStackLink.get());
            }
        }
    }

    protected void addSubInterface(String cloudRegion, String tenantId, Set<Vserver> vServersWithLInterface,
            Link subInterfaceStackLink) throws Exception {
        Optional<String> resourcePath = extractResourcePathFromHref(subInterfaceStackLink.getHref());
        Optional<String> stackPath = extractStackPathFromHref(subInterfaceStackLink.getHref());
        if (resourcePath.isPresent() && stackPath.isPresent()) {
            logger.debug("Fetching nested Sub-Interface Stack Information");
            Stack subinterfaceStack =
                    heat.executeHeatClientRequest(stackPath.get(), cloudRegion, tenantId, Stack.class);
            Resources subinterfaceResources =
                    heat.executeHeatClientRequest(resourcePath.get(), cloudRegion, tenantId, Resources.class);
            if (subinterfaceStack != null) {
                addSubInterfaceToVserver(vServersWithLInterface, subinterfaceStack, subinterfaceResources);
            }
        } else
            throw new Exception(EXCEPTION_MSG);

    }

    protected void addSubInterfaceToVserver(Set<Vserver> vServersWithLInterface, Stack subinterfaceStack,
            Resources subinterfaceResources) throws Exception {
        String parentNeutronPortId = (String) subinterfaceStack.getParameters().get("port_interface");
        logger.debug("Parent neutron Port: {} on SubInterface: {}", parentNeutronPortId, subinterfaceStack.getId());
        for (Vserver auditVserver : vServersWithLInterface)
            for (LInterface lInterface : auditVserver.getLInterfaces().getLInterface())

                if (parentNeutronPortId.equals(lInterface.getInterfaceId())) {
                    logger.debug("Found Parent Port on VServer: {} on Port: {}", auditVserver.getVserverId(),
                            lInterface.getInterfaceId());
                    Resource contrailVm = subinterfaceResources.getList().stream()
                            .filter(resource -> "OS::ContrailV2::VirtualMachineInterface".equals(resource.getType()))
                            .findAny().orElse(null);
                    if (contrailVm == null) {
                        throw new Exception("Cannnot find Contrail Virtual Machine Interface on Stack: "
                                + subinterfaceStack.getId());
                    }
                    LInterface subInterface = new LInterface();
                    subInterface.setInterfaceId(contrailVm.getPhysicalResourceId());
                    subInterface.setIsPortMirrored(false);
                    subInterface.setInMaint(false);
                    subInterface.setIsIpUnnumbered(false);
                    String macAddr = (String) subinterfaceStack.getParameters().get("mac_address");
                    subInterface.setMacaddr(macAddr);

                    String namePrefix = (String) subinterfaceStack.getParameters().get("subinterface_name_prefix");
                    Integer vlanIndex = Integer.parseInt((String) subinterfaceStack.getParameters().get("counter"));
                    String vlanTagList = (String) subinterfaceStack.getParameters().get("vlan_tag");
                    List<String> subInterfaceVlanTagList = Arrays.asList(vlanTagList.split(","));
                    subInterface.setInterfaceName(namePrefix + "_" + subInterfaceVlanTagList.get(vlanIndex));
                    subInterface.setVlans(new Vlans());
                    Vlan vlan = new Vlan();
                    vlan.setInMaint(false);
                    vlan.setIsIpUnnumbered(false);
                    vlan.setVlanIdInner(Long.parseLong(subInterfaceVlanTagList.get(vlanIndex)));
                    vlan.setVlanInterface(namePrefix + "_" + subInterfaceVlanTagList.get(vlanIndex));
                    subInterface.getVlans().getVlan().add(vlan);
                    if (lInterface.getLInterfaces() == null)
                        lInterface.setLInterfaces(new LInterfaces());

                    lInterface.getLInterfaces().getLInterface().add(subInterface);
                } else
                    logger.debug("Did Not Find Parent Port on VServer: {} Parent Port: SubInterface: {}",
                            auditVserver.getVserverId(), lInterface.getInterfaceId(), subinterfaceStack.getId());
    }

    protected Set<Vserver> createVserverSet(Resources resources, List<Resource> novaResources,
            List<Optional<Port>> neutronPortDetails) {
        Set<Vserver> vserversToAudit = new HashSet<>();
        for (Resource novaResource : novaResources) {
            Vserver auditVserver = new Vserver();
            auditVserver.setLInterfaces(new LInterfaces());
            auditVserver.setVserverId(novaResource.getPhysicalResourceId());
            Stream<Port> filteredNeutronPorts = filterNeutronPorts(novaResource, neutronPortDetails);
            filteredNeutronPorts.forEach(port -> {
                LInterface lInterface = new LInterface();
                lInterface.setInterfaceId(port.getId());
                lInterface.setInterfaceName(port.getName());
                auditVserver.getLInterfaces().getLInterface().add(lInterface);
            });
            vserversToAudit.add(auditVserver);
        }
        return vserversToAudit;
    }

    protected Set<Vserver> createVserverSet(List<Resource> novaResources) {
        Set<Vserver> vserversToAudit = new HashSet<>();
        for (Resource novaResource : novaResources) {
            Vserver auditVserver = new Vserver();
            auditVserver.setLInterfaces(new LInterfaces());
            auditVserver.setVserverId(novaResource.getPhysicalResourceId());
            vserversToAudit.add(auditVserver);
        }
        return vserversToAudit;
    }

    protected AAIObjectAudit createAAIObjectAudit(String cloudOwner, String cloudRegion, String tenantId,
            Vserver vServer) {
        AAIObjectAudit aaiObjectAudit = new AAIObjectAudit();
        Vserver vServerShallow = new Vserver();
        BeanUtils.copyProperties(vServer, vServerShallow);
        aaiObjectAudit.setAaiObject(vServerShallow);
        aaiObjectAudit.setAaiObjectType(AAIObjectType.VSERVER.typeName());
        aaiObjectAudit.setResourceURI(AAIUriFactory
                .createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion, tenantId, vServer.getVserverId())
                .build());

        return aaiObjectAudit;
    }

    /**
     * @param novaResource Single openstack resource that is of type Nova
     * @param neutronPorts List of Neutron ports created within the stack
     * @return Filtered list of neutron ports taht relate to the nova server in openstack
     */
    protected Stream<Port> filterNeutronPorts(Resource novaResource, List<Optional<Port>> neutronPorts) {
        List<Port> filteredNeutronPorts =
                neutronPorts.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        return filteredNeutronPorts.stream()
                .filter(port -> port.getDeviceId().equalsIgnoreCase(novaResource.getPhysicalResourceId()));
    }

    /**
     * @param resources Resource stream created by the stack in openstack
     * @param cloudSiteId Unique site id to identify which openstack we talk to
     * @param tenantId The tenant within the cloud we are talking to where resouces exist
     * @return List of optional neutron ports found within the cloud site and tenant
     */
    protected List<Optional<Port>> retrieveNeutronPortDetails(Resources resources, String cloudSiteId,
            String tenantId) {
        return resources.getList().parallelStream().filter(resource -> "OS::Neutron::Port".equals(resource.getType()))
                .map(resource -> neutron.getNeutronPort(resource.getPhysicalResourceId(), tenantId, cloudSiteId))
                .collect(Collectors.toList());

    }

    protected Optional<String> extractResourcePathFromHref(String href) {
        try {
            Optional<String> stackPath = extractStackPathFromHref(href);
            if (stackPath.isPresent()) {
                return Optional.of(stackPath.get() + RESOURCES);
            } else
                return Optional.empty();
        } catch (Exception e) {
            logger.error("Error parsing URI", e);
        }
        return Optional.empty();
    }

    protected Optional<String> extractStackPathFromHref(String href) {
        try {
            URI uri = new URI(href);
            Pattern p = Pattern.compile("/stacks.*");
            Matcher m = p.matcher(uri.getPath());
            if (m.find()) {
                return Optional.of(m.group());
            } else
                return Optional.empty();
        } catch (Exception e) {
            logger.error("Error parsing URI", e);
        }
        return Optional.empty();
    }


}

