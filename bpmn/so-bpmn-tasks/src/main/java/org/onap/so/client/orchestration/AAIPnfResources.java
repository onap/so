/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.onap.so.client.orchestration;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Strings;

@Component
public class AAIPnfResources {

    private static final Logger logger = LoggerFactory.getLogger(AAIPnfResources.class);

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void createPnfAndConnectServiceInstance(Pnf pnf, ServiceInstance serviceInstance) {
        AAIResourceUri pnfURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName()));
        pnf.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        AAIResourceUri serviceInstanceURI = AAIClientUriFactory
                .createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance.getServiceInstanceId()));
        injectionHelper.getAaiClient().createIfNotExists(pnfURI, Optional.of(aaiObjectMapper.mapPnf(pnf)))
                .connect(pnfURI, serviceInstanceURI);
    }

    public void updateOrchestrationStatusPnf(Pnf pnf, OrchestrationStatus orchestrationStatus) {
        AAIResourceUri pnfURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName()));
        Pnf pnfCopy = pnf.shallowCopyId();
        if (orchestrationStatus.equals(OrchestrationStatus.REGISTER)
                || orchestrationStatus.equals(OrchestrationStatus.REGISTERED)) {
            pnf.setInMaint(true);
            pnfCopy.setInMaint(true);
        } else {
            pnf.setInMaint(false);
            pnfCopy.setInMaint(false);
        }

        pnf.setOrchestrationStatus(orchestrationStatus);
        pnfCopy.setOrchestrationStatus(orchestrationStatus);
        injectionHelper.getAaiClient().update(pnfURI, aaiObjectMapper.mapPnf(pnfCopy));
    }

    public void checkIfPnfExistsInAaiAndCanBeUsed(Pnf pnf) throws Exception {
        Optional<org.onap.aai.domain.yang.Pnf> pnfFromAai =
                injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName())));
        if (pnfFromAai.isPresent()) {
            checkIfPnfCanBeUsed(pnfFromAai.get());
            updatePnfInAAI(pnf, pnfFromAai.get());
        }
    }

    private void updatePnfInAAI(Pnf pnf, org.onap.aai.domain.yang.Pnf pnfFromAai) {
        updatePnfFields(pnf, pnfFromAai);
        injectionHelper.getAaiClient().update(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName())), pnfFromAai);
        logger.debug("updatePnfInAAI: {}", pnfFromAai);
    }


    public void deletePnf(Pnf pnf) {
        AAIResourceUri pnfURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName()));
        injectionHelper.getAaiClient().delete(pnfURI);
    }

    public void updateObjectPnf(Pnf pnf) {
        Optional<org.onap.aai.domain.yang.Pnf> pnfFromAai =
                injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName())));
        logger.info("***in updateObjectPnf getPnfName====> {} ", pnfFromAai.get().getPnfName());
        injectionHelper.getAaiClient().update(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnf.getPnfName())),
                aaiObjectMapper.mapPnf((pnf)));
    }

    /**
     * Check inMaint flag value of PNF from AAI using pnfName
     *
     * @param pnfName - pnf-id required pnf
     * @return inMaint flag value
     */
    public boolean checkInMaintFlag(String pnfName) {
        org.onap.aai.domain.yang.Pnf pnf = injectionHelper.getAaiClient()
                .get(org.onap.aai.domain.yang.Pnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfName)))
                .orElse(new org.onap.aai.domain.yang.Pnf());
        return pnf.isInMaint();
    }

    private void updatePnfFields(Pnf pnf, org.onap.aai.domain.yang.Pnf pnfFromAai) {
        if (pnf.getModelInfoPnf() != null
                && StringUtils.isNotBlank(pnf.getModelInfoPnf().getModelCustomizationUuid())) {
            pnfFromAai.setModelCustomizationId(pnf.getModelInfoPnf().getModelCustomizationUuid());
        }
        if (pnf.getModelInfoPnf() != null && StringUtils.isNotBlank(pnf.getModelInfoPnf().getModelInvariantUuid())) {
            pnfFromAai.setModelInvariantId(pnf.getModelInfoPnf().getModelInvariantUuid());
        }
        if (pnf.getModelInfoPnf() != null && StringUtils.isNotBlank(pnf.getModelInfoPnf().getModelUuid())) {
            pnfFromAai.setModelVersionId(pnf.getModelInfoPnf().getModelUuid());
        }
        if (pnf.getOrchestrationStatus() != null && StringUtils.isNotBlank(pnf.getOrchestrationStatus().toString())) {
            pnfFromAai.setOrchestrationStatus(pnf.getOrchestrationStatus().toString());
        }
        if (StringUtils.isNotBlank(pnf.getRole())) {
            pnfFromAai.setNfRole(pnf.getRole());
        }
    }

    private void checkIfPnfCanBeUsed(org.onap.aai.domain.yang.Pnf pnfFromAai) throws Exception {
        isRelatedToService(pnfFromAai);
        if (isOrchestrationStatusSet(pnfFromAai)) {
            checkOrchestrationStatusOfExistingPnf(pnfFromAai);
        }
    }

    private boolean isOrchestrationStatusSet(org.onap.aai.domain.yang.Pnf pnfFromAai) {
        if (Strings.isNullOrEmpty(pnfFromAai.getOrchestrationStatus())) {
            logger.debug("pnf with name {} already exists with not set orchestration status and can be used",
                    pnfFromAai.getPnfName());
            return false;
        }
        return true;
    }

    private void checkOrchestrationStatusOfExistingPnf(org.onap.aai.domain.yang.Pnf pnfFromAai) throws Exception {
        if (OrchestrationStatus.INVENTORIED.toString().equals(pnfFromAai.getOrchestrationStatus())) {
            logger.debug("pnf with name {} already exists with orchestration status Inventoried and can be used",
                    pnfFromAai.getPnfName());
        } else {
            String errorMessage = String.format(
                    "pnf with name %s already exists with orchestration status %s, existing pnf can be used only "
                            + "if status is not set or set as Inventoried",
                    pnfFromAai.getPnfName(), pnfFromAai.getOrchestrationStatus());
            logger.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    private void isRelatedToService(org.onap.aai.domain.yang.Pnf pnfFromAai) throws Exception {
        if (pnfFromAai.getRelationshipList() != null) {
            for (Relationship relationship : pnfFromAai.getRelationshipList().getRelationship()) {
                if (relationship.getRelatedTo().equals("service-instance")) {
                    String errorMessage = prepareRelationErrorMessage(pnfFromAai, relationship);
                    logger.error(errorMessage);
                    throw new Exception(errorMessage);
                }
            }
        }
    }

    private String prepareRelationErrorMessage(org.onap.aai.domain.yang.Pnf pnfFromAai, Relationship relationship) {
        String serviceInstanceName = "";
        String serviceInstanceId = "";

        for (RelationshipData relationshipData : relationship.getRelationshipData()) {
            if (relationshipData.getRelationshipKey().equals("service-instance.service-instance-id")) {
                serviceInstanceId = relationshipData.getRelationshipValue();
                break;
            }
        }
        for (RelatedToProperty relatedToProperty : relationship.getRelatedToProperty()) {
            if (relatedToProperty.getPropertyKey().equals("service-instance.service-instance-name")) {
                serviceInstanceName = relatedToProperty.getPropertyValue();
                break;
            }
        }
        return String.format(
                "Pnf with name %s exist with orchestration status %s and is related to %s service with certain service-instance-id: %s",
                pnfFromAai.getPnfName(), pnfFromAai.getOrchestrationStatus(), serviceInstanceName, serviceInstanceId);
    }
}
