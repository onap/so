package org.onap.so.bpmn.infrastructure.appc.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.Vserver;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVm;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVnf;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppcOrchestratorPreProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AppcOrchestratorPreProcessor.class);
    public static final String CONTROLLER_TYPE_DEFAULT = "APPC";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private AAIVnfResources aaiVnfResources;

    public void buildAppcTaskRequest(BuildingBlockExecution execution, String actionName) {
        try {
            Action action = Action.valueOf(actionName);
            ApplicationControllerTaskRequest appcTaskRequest = new ApplicationControllerTaskRequest();
            appcTaskRequest.setAction(action);
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            GenericVnf vnf = null;
            try {
                vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            } catch (BBObjectNotFoundException e) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "No valid VNF exists");
            }
            String vnfId = null;
            String vnfName = null;
            String vnfType = null;
            String vnfHostIpAddress = null;

            if (vnf != null) {
                vnfId = vnf.getVnfId();
                vnfName = vnf.getVnfName();
                vnfType = vnf.getVnfType();
                vnfHostIpAddress = vnf.getIpv4OamAddress();
            }
            String msoRequestId = gBBInput.getRequestContext().getMsoRequestId();

            String aicIdentity = execution.getVariable("aicIdentity");
            String identityUrl = execution.getVariable("identityUrl");
            appcTaskRequest.setIdentityUrl(identityUrl);

            if (gBBInput.getRequestContext().getRequestParameters() != null) {
                String payload = gBBInput.getRequestContext().getRequestParameters().getPayload();
                if (payload == null) {
                    payload = "";
                }
                String existingSoftwareVersion = JsonUtils.getJsonValue(payload, "existing-software-version");
                appcTaskRequest.setExistingSoftwareVersion(existingSoftwareVersion);
                String newSoftwareVersion = JsonUtils.getJsonValue(payload, "new-software-version");
                appcTaskRequest.setNewSoftwareVersion(newSoftwareVersion);
                String operationsTimeout = JsonUtils.getJsonValue(payload, "operations-timeout");
                appcTaskRequest.setOperationsTimeout(operationsTimeout);
            }

            ControllerSelectionReference controllerSelectionReference = catalogDbClient
                    .getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, action.toString());
            String controllerType = null;
            if (controllerSelectionReference != null) {
                controllerType = controllerSelectionReference.getControllerName();
            } else {
                controllerType = CONTROLLER_TYPE_DEFAULT;
            }
            appcTaskRequest.setControllerType(controllerType);

            execution.setVariable("vmIdList", null);
            execution.setVariable("vserverIdList", null);
            execution.setVariable("vmIndex", 0);
            execution.setVariable("vmIdListSize", 0);

            String vfModuleId = null;
            VfModule vfModule = null;
            try {
                vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            } catch (BBObjectNotFoundException e) {
            }
            if (vfModule != null) {
                vfModuleId = vfModule.getVfModuleId();
            }
            if (action.equals(Action.Snapshot)) {
                try {
                    getVserversForAppc(execution, vnf);
                } catch (Exception e) {
                    logger.warn("Unable to retrieve vservers for vnf: " + vnfId);
                }
            }

            ApplicationControllerVnf applicationControllerVnf = new ApplicationControllerVnf();
            applicationControllerVnf.setVnfHostIpAddress(vnfHostIpAddress);
            applicationControllerVnf.setVnfId(vnfId);
            applicationControllerVnf.setVnfName(vnfName);
            appcTaskRequest.setApplicationControllerVnf(applicationControllerVnf);

            execution.setVariable("appcOrchestratorRequest", appcTaskRequest);
        } catch (Exception e) {
            logger.error("Error building ApplicationControllerTaskRequest Object", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }

    public void addVmInfoToAppcTaskRequest(BuildingBlockExecution execution) {
        try {
            ApplicationControllerTaskRequest appcTaskRequest =
                    (ApplicationControllerTaskRequest) execution.getVariable("appcOrchestratorRequest");
            ArrayList<String> vmIdList = execution.getVariable("vmIdList");
            ArrayList<String> vserverIdList = execution.getVariable("vserverIdList");
            Integer vmIndex = (Integer) execution.getVariable("vmIndex");

            if (vmIdList != null && !vmIdList.isEmpty() && vserverIdList != null && !vserverIdList.isEmpty()) {
                execution.setVariable("vmIdListSize", vmIdList.size());
                if (vmIndex < vmIdList.size()) {
                    ApplicationControllerVm applicationControllerVm = new ApplicationControllerVm();
                    applicationControllerVm.setVmId(vmIdList.get(vmIndex));
                    applicationControllerVm.setVserverId(vserverIdList.get(vmIndex));
                    if (appcTaskRequest.getApplicationControllerVnf() == null) {
                        ApplicationControllerVnf applicationControllerVnf = new ApplicationControllerVnf();
                        appcTaskRequest.setApplicationControllerVnf(applicationControllerVnf);
                    }
                    appcTaskRequest.getApplicationControllerVnf().setApplicationControllerVm(applicationControllerVm);
                    execution.setVariable("appcOrchestratorRequest", appcTaskRequest);
                    vmIndex++;
                    execution.setVariable("vmIndex", vmIndex);
                }
            }
        } catch (Exception e) {
            logger.error("Error adding VM info to ApplicationControllerTaskRequest Object", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }

    protected void getVserversForAppc(BuildingBlockExecution execution, GenericVnf vnf) throws Exception {
        AAIResultWrapper aaiRW = aaiVnfResources.queryVnfWrapperById(vnf);

        if (aaiRW != null && aaiRW.getRelationships().isPresent()) {
            Relationships relationships = aaiRW.getRelationships().get();
            if (relationships != null) {
                List<AAIResourceUri> vserverUris = relationships.getRelatedAAIUris(AAIObjectType.VSERVER);
                ArrayList<String> vserverIds = new ArrayList<String>();
                ArrayList<String> vserverSelfLinks = new ArrayList<String>();
                for (AAIResourceUri j : vserverUris) {
                    String vserverId = j.getURIKeys().get("vserver-id");
                    vserverIds.add(vserverId);
                    Optional<Vserver> oVserver = aaiVnfResources.getVserver(j);
                    if (oVserver.isPresent()) {
                        Vserver vserver = oVserver.get();
                        String vserverSelfLink = vserver.getVserverSelflink();
                        vserverSelfLinks.add(vserverSelfLink);
                    }
                }
                logger.debug("vmIdsArray is: {}", vserverSelfLinks);
                logger.debug("vserverIdsArray is: {}", vserverIds);
                execution.setVariable("vmIdList", vserverSelfLinks);
                execution.setVariable("vserverIdList", vserverIds);
            }
        }
    }
}
