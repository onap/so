package org.onap.so.adapters.tasks.orchestration;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.MsoVnfPluginAdapterImpl;
import org.onap.so.adapters.vnf.VnfAdapterUtils;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.utils.ExternalTaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class RollbackService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(RollbackService.class);

    @Autowired
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Autowired
    private MsoNetworkAdapterImpl networkAdapterImpl;

    @Autowired
    private VnfAdapterUtils vnfAdapterUtils;

    @Autowired
    private MsoVnfPluginAdapterImpl vnfPluginImpl;

    @Autowired
    private AuditMDCSetup mdcSetup;

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        logger.debug("Starting External Task Rollback Service");
        Map<String, Object> variables = new HashMap<>();
        boolean success = false;
        boolean pollRollbackStatus = false;
        try {
            String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
            if (xmlRequest != null) {
                Optional<String> requestType = findRequestType(xmlRequest);
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Rollback Service for Create Volume Group");
                    CreateVolumeGroupRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
                    boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        vnfAdapterImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                                req.getVolumeGroupName(), null, req.getMsoRequest(), new Holder<>());
                        pollRollbackStatus = true;
                        success = true;
                    } else {
                        vnfPluginImpl.deleteVnf(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                                req.getVolumeGroupName(), req.getMsoRequest());
                        pollRollbackStatus = false;
                        success = true;
                    }
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Rollback Service for Create Vf Module");
                    CreateVfModuleRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
                    boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        vnfAdapterImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                                req.getVfModuleName(), req.getModelCustomizationUuid(), req.getMsoRequest(),
                                new Holder<>());
                        pollRollbackStatus = true;
                        success = true;
                    } else {
                        pollRollbackStatus = false;
                        success = true;
                    }
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Rollback Service for Create Network");
                    CreateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateNetworkRequest.class);
                    networkAdapterImpl.deleteNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkName(), req.getMsoRequest());
                    pollRollbackStatus = true;
                    success = true;
                }
            }
        } catch (Exception e) {
            logger.error("Error during External Task Rollback Service", e);
        }
        variables.put("OpenstackRollbackSuccess", success);
        variables.put("rollbackPerformed", true);
        variables.put("PollRollbackStatus", pollRollbackStatus);
        if (success) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task Id: {}  Successful", externalTask.getId());
        } else {
            logger.debug("The External Task Id: {}  Failed. Not Retrying", externalTask.getId());
            externalTaskService.complete(externalTask, variables);
        }
    }

    protected Optional<String> findRequestType(final String xmlString) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);

            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
            return Optional.of(doc.getDocumentElement().getNodeName());
        } catch (final Exception e) {
            logger.error("Error Finding Request Type", e);
            return Optional.empty();
        }
    }

}
