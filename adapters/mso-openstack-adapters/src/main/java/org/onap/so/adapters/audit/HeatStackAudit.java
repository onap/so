/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.audit;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.woorea.openstack.heat.model.Link;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;

@Component
public class HeatStackAudit {

	private static final String RESOURCES = "/resources";

	protected static final Logger logger = LoggerFactory.getLogger(HeatStackAudit.class);

	@Autowired
	protected MsoHeatUtils heat;

	@Autowired
	protected AuditVServer auditVservers;

	public boolean auditHeatStack(String cloudRegion, String cloudOwner, String tenantId, String heatStackName) {
		try {
			logger.debug("Fetching Top Level Stack Information");
			Resources resources = heat.queryStackResources(cloudRegion, tenantId, heatStackName);
			List<Resource> novaResources = resources.getList().stream()
					.filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
			List<Resource> resourceGroups = resources.getList().stream()
					.filter(p -> "OS::Heat::ResourceGroup".equals(p.getType()) && p.getName().contains("subinterfaces")).collect(Collectors.toList());
			Set<Vserver> vserversToAudit = createVserverSet(resources, novaResources);
			Set<Vserver> vserversWithSubInterfaces = processSubInterfaces(cloudRegion, tenantId, resourceGroups,
					vserversToAudit); 
			return auditVservers.auditVservers(vserversWithSubInterfaces, tenantId, cloudOwner, cloudRegion);
		} catch (Exception e) {
			logger.error("Error during auditing stack resources", e);
			return false;
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
		Optional<Link> stackLink = resourceGroup.getLinks().stream().filter(link -> "nested".equals(link.getRel()))
				.findAny();
		if (stackLink.isPresent()) {
			try {
				Optional<String> path = extractResourcePathFromHref(stackLink.get().getHref());
				if (path.isPresent()) {
					logger.debug("Fetching nested Resource Stack Information");
					Resources nestedResourceGroupResources = heat.executeHeatClientRequest(path.get(), cloudRegion,
							tenantId, Resources.class);
					processNestedResourceGroup(cloudRegion, tenantId, vServersWithLInterface,
							nestedResourceGroupResources);
				} else
					throw new Exception("Error finding Path from Self Link");
			} catch (Exception e) {
				logger.error("Error Parsing Link to obtain Path", e);
				throw new Exception("Error finding Path from Self Link");
			}

		}
	}

	protected void processNestedResourceGroup(String cloudRegion, String tenantId, Set<Vserver> vServersWithLInterface,
			Resources nestedResourceGroupResources) throws Exception {
		for (Resource resourceGroupNested : nestedResourceGroupResources) {
			Optional<Link> subInterfaceStackLink = resourceGroupNested.getLinks().stream()
					.filter(link -> "nested".equals(link.getRel())).findAny();
			if (subInterfaceStackLink.isPresent()) {
				addSubInterface(cloudRegion, tenantId, vServersWithLInterface,subInterfaceStackLink.get());
			}
		}
	}

	protected void addSubInterface(String cloudRegion, String tenantId, Set<Vserver> vServersWithLInterface, Link subInterfaceStackLink) throws Exception {
			Optional<String> resourcePath = extractResourcePathFromHref(subInterfaceStackLink.getHref());
			Optional<String> stackPath = extractStackPathFromHref(subInterfaceStackLink.getHref());
			if (resourcePath.isPresent() && stackPath.isPresent()) {
				logger.debug("Fetching nested Sub-Interface Stack Information");
				Stack subinterfaceStack = heat.executeHeatClientRequest(stackPath.get(), cloudRegion, tenantId, Stack.class);
				Resources subinterfaceResources = heat.executeHeatClientRequest(resourcePath.get(), cloudRegion, tenantId, Resources.class);
				if (subinterfaceStack != null) {
					addSubInterfaceToVserver(vServersWithLInterface, subinterfaceStack, subinterfaceResources);
				}
			} else
				throw new Exception("Error finding Path from Self Link");
		
	}

	protected void addSubInterfaceToVserver(Set<Vserver> vServersWithLInterface, Stack subinterfaceStack, Resources subinterfaceResources) throws Exception {
		String parentNeutronPortId = (String) subinterfaceStack.getParameters().get("port_interface");
		logger.debug("Parent neutron Port: {} on SubInterface: {}", parentNeutronPortId, subinterfaceStack.getId());
		for (Vserver auditVserver : vServersWithLInterface)
			for (LInterface lInterface : auditVserver.getLInterfaces().getLInterface())
				
				if (parentNeutronPortId.equals(lInterface.getInterfaceId())) {
					logger.debug("Found Parent Port on VServer: {} on Port: {}", auditVserver.getVserverId(), lInterface.getInterfaceId());
					Resource contrailVm = subinterfaceResources.getList().stream().filter(resource -> "OS::ContrailV2::VirtualMachineInterface".equals(resource.getType())).findAny()
	                .orElse(null);
					if(contrailVm == null){
						throw new Exception("Cannnot find Contrail Virtual Machine Interface on Stack: "+ subinterfaceStack.getId());
					}
					LInterface subInterface = new LInterface();
					subInterface.setInterfaceId(contrailVm.getPhysicalResourceId());
					
					if(lInterface.getLInterfaces() == null)
						lInterface.setLInterfaces(new LInterfaces());
					
					lInterface.getLInterfaces().getLInterface().add(subInterface);
				}else
					logger.debug("Did Not Find Parent Port on VServer: {} Parent Port: SubInterface: {}",auditVserver.getVserverId(), 
							lInterface.getInterfaceId(),subinterfaceStack.getId());
	}

	protected Set<Vserver> createVserverSet(Resources resources, List<Resource> novaResources) {
		Set<Vserver> vserversToAudit = new HashSet<>();
		for (Resource novaResource : novaResources) {
			Vserver auditVserver = new Vserver();
			auditVserver.setLInterfaces(new LInterfaces());
			auditVserver.setVserverId(novaResource.getPhysicalResourceId());
			Stream<Resource> filteredNeutronNetworks = resources.getList().stream()
					.filter(network -> network.getRequiredBy().contains(novaResource.getLogicalResourceId()));
			filteredNeutronNetworks.forEach(network -> {
				LInterface lInterface = new LInterface();
				lInterface.setInterfaceId(network.getPhysicalResourceId());
				auditVserver.getLInterfaces().getLInterface().add(lInterface);
			});
			vserversToAudit.add(auditVserver);
		}
		return vserversToAudit;
	}

	protected Optional<String> extractResourcePathFromHref(String href) {
		URI uri;
		try {
			uri = new URI(href);			
			return Optional.of(uri.getPath().replaceFirst("/v\\d+", "")+RESOURCES);			
		} catch (Exception e) {
			logger.error("Error parsing URI", e);
		}
		return Optional.empty();
	}
	
	protected Optional<String> extractStackPathFromHref(String href) {
		URI uri;
		try {
			uri = new URI(href);			
			return Optional.of(uri.getPath().replaceFirst("/v\\d+", ""));			
		} catch (Exception e) {
			logger.error("Error parsing URI", e);
		}
		return Optional.empty();
	}
	
	
}
