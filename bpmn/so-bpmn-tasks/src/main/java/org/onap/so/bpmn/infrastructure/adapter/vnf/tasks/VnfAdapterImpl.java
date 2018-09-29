/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import org.apache.commons.lang3.StringUtils;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.exceptions.MarshallerException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

@Component
public class VnfAdapterImpl {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VnfAdapterCreateTasks.class);
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	
	@Autowired
	private ExceptionBuilder exceptionUtil;

	public void preProcessVnfAdapter(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			execution.setVariable("mso-request-id", gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable("mso-service-instance-id", serviceInstance.getServiceInstanceId());
			execution.setVariable("heatStackId", null);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void postProcessVnfAdapter(BuildingBlockExecution execution) {
		try {			
            String vnfAdapterResponse = execution.getVariable("vnfAdapterRestV1Response");           
            if (!StringUtils.isEmpty( vnfAdapterResponse)) {
                Object vnfRestResponse = unMarshal(vnfAdapterResponse);
                if(vnfRestResponse instanceof CreateVfModuleResponse) {
                    VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
                    String heatStackId = ((CreateVfModuleResponse) vnfRestResponse).getVfModuleStackId();
                    if(!StringUtils.isEmpty(heatStackId)) {
                        vfModule.setHeatStackId(heatStackId);
                        execution.setVariable("heatStackId", heatStackId);
                    }
                } else if(vnfRestResponse instanceof DeleteVfModuleResponse) {
                    VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
                    Boolean vfModuleDelete = ((DeleteVfModuleResponse) vnfRestResponse).getVfModuleDeleted();
                    if(null!= vfModuleDelete && vfModuleDelete) {
                        vfModule.setHeatStackId(null);
                        execution.setVariable("heatStackId", null);
                    }
                } else if(vnfRestResponse instanceof CreateVolumeGroupResponse) {
                    VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
                    String heatStackId = ((CreateVolumeGroupResponse) vnfRestResponse).getVolumeGroupStackId();
                    if(!StringUtils.isEmpty(heatStackId)) {
                        volumeGroup.setHeatStackId(heatStackId);
                        execution.setVariable("heatStackId", heatStackId);
                    }else{
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "HeatStackId is missing from create VolumeGroup Vnf Adapter response.");
                    }                    
                } else if(vnfRestResponse instanceof DeleteVolumeGroupResponse) {                	
                	VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
                	Boolean volumeGroupDelete = ((DeleteVolumeGroupResponse) vnfRestResponse).getVolumeGroupDeleted();
                	if(null!= volumeGroupDelete && volumeGroupDelete) {                		
                		volumeGroup.setHeatStackId(null);
                		execution.setVariable("heatStackId", null);
                	}
                }
            }
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

    private Object unMarshal(String input) throws MarshallerException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            JAXBContext jaxbContext = JAXBContext.newInstance(CreateVfModuleResponse.class,
                    CreateVolumeGroupResponse.class,DeleteVfModuleResponse.class,DeleteVolumeGroupResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputSource inputSource = new InputSource(new StringReader(input));
            SAXSource source = new SAXSource(xmlReader, inputSource);
            return jaxbUnmarshaller.unmarshal(source);
        } catch (Exception e) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.SchemaError, e.getMessage(), e);
            throw new MarshallerException("Error parsing VNF Adapter response. " + e.getMessage(), MsoLogger.ErrorCode.SchemaError.getValue(), e);
        }
    }
}
