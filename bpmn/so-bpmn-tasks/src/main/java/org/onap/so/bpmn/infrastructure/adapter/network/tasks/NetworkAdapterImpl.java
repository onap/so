package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.utils.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterImpl {

    private static final Logger logger = LoggerFactory.getLogger(NetworkAdapterImpl.class);

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    public void preProcessNetworkAdapter(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            execution.setVariable("mso-request-id", gBBInput.getRequestContext().getMsoRequestId());
            execution.setVariable("mso-service-instance-id", serviceInstance.getServiceInstanceId());
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void postProcessNetworkAdapter(BuildingBlockExecution execution) {
        try {
            String workflowResponse = (String) execution.getVariable("WorkflowResponse");
            if (workflowResponse != null) {
                Optional<String> responseType = findResponseType(workflowResponse);
                if ("createNetworkResponse".equals(responseType.get())) {
                    CreateNetworkResponse createNetworkResponse =
                            (CreateNetworkResponse) unmarshalXml(workflowResponse, CreateNetworkResponse.class);
                    execution.setVariable("createNetworkResponse", createNetworkResponse);
                } else if ("deleteNetworkResponse".equals(responseType.get())) {
                    DeleteNetworkResponse deleteNetworkResponse =
                            (DeleteNetworkResponse) unmarshalXml(workflowResponse, DeleteNetworkResponse.class);
                    execution.setVariable("deleteNetworkResponse", deleteNetworkResponse);
                } else if ("updateNetworkResponse".equals(responseType.get())) {
                    UpdateNetworkResponse updateNetworkResponse =
                            (UpdateNetworkResponse) unmarshalXml(workflowResponse, UpdateNetworkResponse.class);
                    execution.setVariable("updateNetworkResponse", updateNetworkResponse);
                } else {
                    logger.warn("Unable to determine network response type");
                }
            }
        } catch (Exception e) {
            logger.error("Error Network Adapter post process", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e.getMessage(), Components.OPENSTACK);
        }
    }

    protected <T> Object unmarshalXml(String xmlString, Class<T> resultClass) throws JAXBException {
        StringReader reader = new StringReader(xmlString);
        JAXBContext context = JAXBContext.newInstance(resultClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(reader);
    }

    protected Optional<String> findResponseType(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc;
            doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
            return Optional.of(doc.getDocumentElement().getNodeName());
        } catch (Exception e) {
            logger.error("Error Finding Response Type", e);
            return Optional.empty();
        }
    }

}
