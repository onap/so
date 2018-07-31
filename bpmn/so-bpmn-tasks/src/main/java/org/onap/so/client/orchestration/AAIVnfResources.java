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

package org.onap.so.client.orchestration;

import java.util.Optional;

import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIVnfResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AAIVnfResources.class);
	
	@Autowired
	private InjectionHelper injectionHelper;
	
	@Autowired
	private AAIObjectMapper aaiObjectMapper;
	
	public void createVnfandConnectServiceInstance(GenericVnf vnf, ServiceInstance serviceInstance) {
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		vnf.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		injectionHelper.getAaiClient().createIfNotExists(vnfURI, Optional.of(aaiObjectMapper.mapVnf(vnf))).connect(vnfURI, serviceInstanceURI);
	}
	
	public void createPlatformandConnectVnf(Platform platform, GenericVnf vnf) {
		AAIResourceUri platformURI = AAIUriFactory.createResourceUri(AAIObjectType.PLATFORM, platform.getPlatformName());	
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		injectionHelper.getAaiClient().createIfNotExists(platformURI, Optional.of(platform)).connect(vnfURI, platformURI);
	}
	
	public void createLineOfBusinessandConnectVnf(LineOfBusiness lineOfBusiness, GenericVnf vnf) {
		AAIResourceUri lineOfBusinessURI = AAIUriFactory.createResourceUri(AAIObjectType.LINE_OF_BUSINESS, lineOfBusiness.getLineOfBusinessName());	
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		injectionHelper.getAaiClient().createIfNotExists(lineOfBusinessURI, Optional.of(lineOfBusiness)).connect(vnfURI, lineOfBusinessURI);
	}
	
	public void deleteVnf(GenericVnf vnf) {
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		injectionHelper.getAaiClient().delete(vnfURI);
	}

	public void updateOrchestrationStatusVnf(GenericVnf vnf, OrchestrationStatus orchestrationStatus) {
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());

		GenericVnf copiedVnf = vnf.shallowCopyId();

		vnf.setOrchestrationStatus(orchestrationStatus);
		copiedVnf.setOrchestrationStatus(orchestrationStatus);
		injectionHelper.getAaiClient().update(vnfURI, aaiObjectMapper.mapVnf(copiedVnf));
	}
	
	public void updateObjectVnf(GenericVnf vnf) {
		AAIResourceUri vnfUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		injectionHelper.getAaiClient().update(vnfUri, aaiObjectMapper.mapVnf(vnf));
	}

	/**
	 * Retrieve Generic VNF from AAI using vnf Id
	 * @param vnfId - vnf-id required vnf
	 * @return AAI Generic Vnf
	 */
	public Optional<org.onap.aai.domain.yang.GenericVnf> getGenericVnf( String vnfId) {
		return injectionHelper.getAaiClient()
				.get(org.onap.aai.domain.yang.GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId));
	}
	
	/**
	 * Check inMaint flag value of Generic VNF from AAI using vnf Id
	 * @param vnfId - vnf-id required vnf
	 * @return inMaint flag value
	 */
	public boolean checkInMaintFlag(String vnfId) {		
		org.onap.aai.domain.yang.GenericVnf vnf = injectionHelper.getAaiClient()
				.get(org.onap.aai.domain.yang.GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId))
				.orElse(new org.onap.aai.domain.yang.GenericVnf());
		return vnf.isInMaint();
	}
}
