package org.openecomp.mso.bpmn.infrastructure.adapter.vnf.tasks;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleResponse;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.exceptions.MarshallerException;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
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
			
			execution.setVariable("isDebugLogEnabled", "true");
			execution.setVariable("mso-request-id", gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable("mso-service-instance-id", serviceInstance.getServiceInstanceId());
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
                    }
                } else if(vnfRestResponse instanceof DeleteVfModuleResponse) {
                    VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
                    Boolean vfModuleDelete = ((DeleteVfModuleResponse) vnfRestResponse).getVfModuleDeleted();
                    if(null!= vfModuleDelete && vfModuleDelete) {
                        vfModule.setHeatStackId(null);
                    }
                } else if(vnfRestResponse instanceof CreateVolumeGroupResponse) {
                    VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
                    String heatStackId = ((CreateVolumeGroupResponse) vnfRestResponse).getVolumeGroupStackId();
                    if(!StringUtils.isEmpty(heatStackId)) {
                        volumeGroup.setHeatStackId(heatStackId);
                    }else{
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "HeatStackId is missing from create VolumeGroup Vnf Adapter response.");
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
                    CreateVolumeGroupResponse.class,DeleteVfModuleResponse.class);
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
