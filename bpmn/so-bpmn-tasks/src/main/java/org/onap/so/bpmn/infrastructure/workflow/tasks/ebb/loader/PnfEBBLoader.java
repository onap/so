package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;

@Component
public class PnfEBBLoader {

    private static final Logger logger = LoggerFactory.getLogger(PnfEBBLoader.class);

    private final BBInputSetupUtils bbInputSetupUtils;
    private final BBInputSetup bbInputSetup;
    private final WorkflowActionExtractResourcesAAI workflowActionUtils;
    private final ExceptionBuilder exceptionBuilder;

    PnfEBBLoader(BBInputSetupUtils bbInputSetupUtils, BBInputSetup bbInputSetup,
            WorkflowActionExtractResourcesAAI workflowActionUtils, ExceptionBuilder exceptionBuilder) {
        this.bbInputSetupUtils = bbInputSetupUtils;
        this.bbInputSetup = bbInputSetup;
        this.workflowActionUtils = workflowActionUtils;
        this.exceptionBuilder = exceptionBuilder;
    }


    public void traverseAAIPnf(DelegateExecution execution, List<Resource> resourceList, String serviceId, String pnfId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            String pN = null;
            org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI =
                    bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
            List<Relationship> relationship = serviceInstanceAAI.getRelationshipList().getRelationship();
            List<RelationshipData> relationshipData;
            org.onap.aai.domain.yang.Pnf pnf = null;
            outerLoop: for (Relationship r : relationship) {
                if (r.getRelatedTo().equalsIgnoreCase("pnf")) {
                    relationshipData = r.getRelationshipData();
                    for (RelationshipData rd : relationshipData) {
                        if (rd.getRelationshipKey().equalsIgnoreCase("pnf.pnf-name")) {
                            pN = rd.getRelationshipValue();
                            pnf = bbInputSetupUtils.getAAIPnf(pN);
                            if (pnf.getPnfId().equalsIgnoreCase(pnfId)) {
                                break outerLoop;
                            }
                        }
                    }
                }

            }
            ServiceInstance serviceInstanceMSO = bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            Resource serviceResource =
                    new Resource(WorkflowType.SERVICE, serviceInstanceAAI.getServiceInstanceId(), false, null);
            resourceList.add(serviceResource);
            if (serviceInstanceMSO.getPnfs() != null) {
                findPnfWithGivenId(serviceInstanceMSO, pN, aaiResourceIds, resourceList, serviceResource);
            }
        } catch (Exception ex) {
            logger.error("Exception in traverseAAIPnf", ex);
            buildAndThrowException(execution,
                    "Could not find existing Pnf or related Instances to execute the request on.");
        }
    }


    private void findPnfWithGivenId(ServiceInstance serviceInstanceMSO, String pName,
            List<Pair<WorkflowType, String>> aaiResourceIds, List<Resource> resourceList, Resource serviceResource) {
        for (Pnf pnf : serviceInstanceMSO.getPnfs()) {
            if (pnf.getPnfName().equals(pName)) {
                aaiResourceIds.add(new Pair<>(WorkflowType.PNF, pnf.getPnfId()));
                Resource pnfResource = new Resource(WorkflowType.PNF, pnf.getPnfId(), false, serviceResource);
                org.onap.aai.domain.yang.Pnf aaiPnf = bbInputSetupUtils.getAAIPnf(pnf.getPnfName());
                pnfResource.setModelCustomizationId(aaiPnf.getModelCustomizationId());
                pnfResource.setModelVersionId(aaiPnf.getModelVersionId());
                resourceList.add(pnfResource);
                break;
            }
        }
    }


    private void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }

}
