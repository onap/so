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

package org.onap.so.adapters.tasks.audit;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class AuditVServer extends AbstractAudit {
    private static final Logger logger = LoggerFactory.getLogger(AuditVServer.class);

    public void auditVservers(AAIObjectAuditList aaiObjectAuditList) {

        aaiObjectAuditList.getAuditList().forEach(aaiObjectAudit -> {
            boolean vserverExist = getAaiClient().exists(AAIUriFactory
                    .createResourceFromExistingURI(AAIObjectType.VSERVER, aaiObjectAudit.getResourceURI()));
            aaiObjectAudit.setDoesObjectExist(vserverExist);
        });
    }

    public Optional<AAIObjectAuditList> auditVserversThroughRelationships(String genericVnfId, String vfModuleName) {
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VF_MODULE, genericVnfId)
                .queryParam("vf-module-name", vfModuleName);
        Optional<AAIResultWrapper> wrapper = getAaiClient().getFirstWrapper(VfModules.class, VfModule.class, uri);
        if (wrapper.isPresent() && wrapper.get().getRelationships().isPresent()) {
            List<AAIResourceUri> relatedVservers =
                    wrapper.get().getRelationships().get().getRelatedUris(AAIObjectType.VSERVER);
            if (!relatedVservers.isEmpty()) {
                relatedVservers.forEach(vserverUri -> {
                    Optional<Vserver> vserver = getAaiClient().get(vserverUri).asBean(Vserver.class);
                    Vserver vServerShallow = new Vserver();
                    BeanUtils.copyProperties(vserver, vServerShallow);
                    AAIObjectAudit vServerAudit = new AAIObjectAudit();
                    vServerAudit.setAaiObject(vServerShallow);
                    vServerAudit.setAaiObjectType(AAIObjectType.VSERVER.typeName());
                    vServerAudit.setDoesObjectExist(true);
                    auditList.getAuditList().add(vServerAudit);
                });
            }
        }
        return Optional.of(auditList);
    }

    public Optional<AAIObjectAuditList> auditVservers(Set<Vserver> vServersToAudit, String tenantId, String cloudOwner,
            String cloudRegion) {
        if (vServersToAudit == null || vServersToAudit.isEmpty()) {
            return Optional.empty();
        }
        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        vServersToAudit.stream().forEach(vserver -> {
            try {
                logger.debug("Vserver to Audit: {}", objectMapper.getMapper().writeValueAsString(vserver));
            } catch (JsonProcessingException e) {
                logger.error("Json parse exception: ", e);
            }

        });
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        vServersToAudit.stream().forEach(vServer -> auditList.getAuditList()
                .addAll(doesVServerExistInAAI(vServer, tenantId, cloudOwner, cloudRegion).getAuditList()));
        return Optional.of(auditList);
    }

    private AAIObjectAuditList doesVServerExistInAAI(Vserver vServer, String tenantId, String cloudOwner,
            String cloudRegion) {
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        AAIObjectAudit vServerAudit = new AAIObjectAudit();
        AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
                tenantId, vServer.getVserverId());
        Vserver vServerShallow = new Vserver();
        BeanUtils.copyProperties(vServer, vServerShallow, "LInterfaces");
        boolean vServerExists = getAaiClient().exists(vserverURI);
        logger.info("v-server {} exists: {}", vServer.getVserverId(), vServerExists);
        vServerAudit.setAaiObject(vServerShallow);
        vServerAudit.setDoesObjectExist(vServerExists);
        vServerAudit.setResourceURI(vserverURI.build());
        vServerAudit.setAaiObjectType(AAIObjectType.VSERVER.typeName());
        auditList.getAuditList().add(vServerAudit);
        if (vServer.getLInterfaces() != null) {
            vServer.getLInterfaces().getLInterface().stream().forEach(lInterface -> auditList.getAuditList().addAll(
                    doesLinterfaceExistinAAI(lInterface, vServer.getVserverId(), tenantId, cloudOwner, cloudRegion)
                            .getAuditList()));
        }
        return auditList;
    }

    private AAIObjectAuditList doesLinterfaceExistinAAI(LInterface lInterface, String vServerId, String tenantId,
            String cloudOwner, String cloudRegion) {
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        AAIObjectAudit lInterfaceAudit = new AAIObjectAudit();
        AAIResourceUri linterfaceURI = AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE, cloudOwner,
                cloudRegion, tenantId, vServerId, lInterface.getInterfaceName());
        Optional<LInterface> queriedLInterface = getAaiClient().get(LInterface.class, linterfaceURI);
        if (queriedLInterface.isPresent()) {
            lInterfaceAudit.setDoesObjectExist(true);
            lInterface.setInterfaceName(lInterface.getInterfaceName());
        }
        lInterfaceAudit.setAaiObject(lInterface);
        lInterfaceAudit.setResourceURI(linterfaceURI.build());
        lInterfaceAudit.setAaiObjectType(AAIObjectType.L_INTERFACE.typeName());
        auditList.getAuditList().add(lInterfaceAudit);
        logger.info("l-interface id:{} name: {} exists: {} ", lInterface.getInterfaceId(),
                lInterface.getInterfaceName(), lInterfaceAudit.isDoesObjectExist());

        if (lInterface.getLInterfaces() != null) {
            lInterface.getLInterfaces().getLInterface().stream()
                    .forEach(subInterface -> auditList.getAuditList().add(doesSubInterfaceExistinAAI(subInterface,
                            lInterface.getInterfaceName(), vServerId, tenantId, cloudOwner, cloudRegion)));
        }
        logger.debug("l-interface {} does not contain any sub-iterfaces, skipping audit of sub-interfaces",
                lInterface.getInterfaceId());

        return auditList;
    }

    private AAIObjectAudit doesSubInterfaceExistinAAI(LInterface subInterface, String linterfaceName, String vServerId,
            String tenantId, String cloudOwner, String cloudRegion) {
        logger.info("checking if sub-l-interface {} , linterfaceName: {} vserverId: {}  exists",
                subInterface.getInterfaceName(), linterfaceName, vServerId);
        AAIObjectAudit subInterfaceAudit = new AAIObjectAudit();


        AAIResourceUri subInterfaceURI = AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, cloudOwner,
                cloudRegion, tenantId, vServerId, linterfaceName, subInterface.getInterfaceName());
        subInterfaceAudit.setResourceURI(subInterfaceURI.build());
        boolean doesExist = getAaiClient().exists(subInterfaceURI);
        logger.info("sub-l-interface-id:{} exists: {}", subInterface.getInterfaceId(), doesExist);
        subInterfaceAudit.setAaiObject(subInterface);
        subInterfaceAudit.setDoesObjectExist(doesExist);
        subInterfaceAudit.setAaiObjectType(AAIObjectType.SUB_L_INTERFACE.typeName());
        return subInterfaceAudit;
    }
}
