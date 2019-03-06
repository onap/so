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

import java.util.Optional;
import java.util.Set;

import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditVServer extends AbstractAudit {
	private static final Logger logger = LoggerFactory.getLogger(AuditVServer.class);

	public boolean auditAllVserversDoExist(Set<Vserver> vServersToAudit, String tenantId, String cloudOwner, String cloudRegion) {
		if (vServersToAudit == null || vServersToAudit.isEmpty()){
			return false;
		}
		return vServersToAudit.stream()
				.filter(vServer -> !doesVServerExistInAAI(vServer, tenantId, cloudOwner, cloudRegion,true)).findFirst()
				.map(v -> false).orElse(true);
	}
	
	public boolean auditAllVserversDoNotExist(Set<Vserver> vServersToAudit, String tenantId, String cloudOwner, String cloudRegion) {
		if (vServersToAudit == null || vServersToAudit.isEmpty()){
			return true;
		}
		return vServersToAudit.stream()
				.filter(vServer -> doesVServerExistInAAI(vServer, tenantId, cloudOwner, cloudRegion,false)).findFirst()
				.map(v -> false).orElse(true);
	}

	private boolean doesVServerExistInAAI(Vserver vServer, String tenantId, String cloudOwner, String cloudRegion, boolean checkLinterfaces) {
		AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
				tenantId, vServer.getVserverId());
		boolean vServerExists = getAaiClient().exists(vserverURI);
		boolean doesExist = getAaiClient().exists(vserverURI);
		logger.info("v-server {} exists: {}", vServer.getVserverId(), doesExist);
		boolean allNeutronNetworksExist = true;
		if (vServerExists && vServer.getLInterfaces() != null && checkLinterfaces) {
			allNeutronNetworksExist = vServer.getLInterfaces()
					.getLInterface().stream().filter(lInterface -> !doesLinterfaceExistinAAI(lInterface,
							vServer.getVserverId(), tenantId, cloudOwner, cloudRegion))
					.findFirst().map(v -> false).orElse(true);
		}
		return vServerExists && allNeutronNetworksExist;
	}

	private boolean doesLinterfaceExistinAAI(LInterface lInterface, String vServerId, String tenantId,
			String cloudOwner, String cloudRegion) {
		boolean doesLInterfaceExist = false;
		boolean doSubInterfacesExist = true;
		AAIResourceUri linterfaceURI = AAIUriFactory
				.createResourceUri(AAIObjectPlurals.L_INTERFACE, cloudOwner, cloudRegion, tenantId, vServerId)
				.queryParam("interface-id", lInterface.getInterfaceId());
		Optional<LInterfaces> queriedLInterface = getAaiClient().get(LInterfaces.class, linterfaceURI);
		if (queriedLInterface.isPresent()) {
			if (queriedLInterface.get().getLInterface().size() > 1) {
				logger.error("Non-Unique LInterface Found stopping audit, L-Interface Id: " +lInterface.getInterfaceId());
				doesLInterfaceExist = false;
			} else {
				doesLInterfaceExist = true;
				lInterface.setInterfaceName(queriedLInterface.get().getLInterface().get(0).getInterfaceName());
			}
		}
		logger.info("l-interface id:{} name: {} exists: {}", lInterface.getInterfaceId(), lInterface.getInterfaceName(),
				doesLInterfaceExist);

		if (doesLInterfaceExist && lInterface.getLInterfaces() != null) {
			doSubInterfacesExist = lInterface.getLInterfaces().getLInterface()
					.stream().filter(subInterface -> !doesSubInterfaceExistinAAI(subInterface,
							lInterface.getInterfaceName(), vServerId, tenantId, cloudOwner, cloudRegion))
					.findFirst().map(v -> false).orElse(true);
		} else
			logger.debug("l-interface {} does not contain any sub-iterfaces", lInterface.getInterfaceId());

		return doesLInterfaceExist && doSubInterfacesExist;
	}

	private boolean doesSubInterfaceExistinAAI(LInterface subInterface, String linterfaceName, String vServerId,
			String tenantId, String cloudOwner, String cloudRegion) {
		logger.info("checking if sub-l-interface {} , linterfaceName: {} vserverId: {}  exists",
				subInterface.getInterfaceId(), linterfaceName, vServerId);

		AAIResourceUri linterfaceURI = AAIUriFactory.createResourceUri(AAIObjectPlurals.SUB_L_INTERFACE, cloudOwner,
				cloudRegion, tenantId, vServerId, linterfaceName)
				.queryParam("interface-id", subInterface.getInterfaceId());

		boolean doesExist = getAaiClient().exists(linterfaceURI);
		logger.info("sub-l-interface {} exists: {}", subInterface.getInterfaceId(), doesExist);
		return doesExist;
	}
}
