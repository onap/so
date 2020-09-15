package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.SliceTaskInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoAllocateNSSIV2 extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DoAllocateNSSIV2.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")

//        String nssmfOperation
//
//        String nsstInput = execution.getVariable("nsstInput")
//        String modelUuid = jsonUtil.getJsonValue(nsstInput, "modelUuid")
//        //modelUuid="2763777c-27bd-4df7-93b8-c690e23f4d3f"
//        String nssiInstanceId = jsonUtil.getJsonValue(nsstInput, "serviceInstanceId")
//        String serviceModelInfo = """{
//            "modelInvariantUuid":"",
//            "modelUuid":"${modelUuid}",
//            "modelVersion":""
//             }"""
//        execution.setVariable("serviceModelInfo", serviceModelInfo)
//        execution.setVariable("nssiInstanceId", nssiInstanceId)
//        String nssiProfileID = UUID.randomUUID().toString()
//        execution.setVariable("nssiProfileID", nssiProfileID)
//        if(isBlank(nssiInstanceId))
//        {
//            nssmfOperation="create"
//            nssiInstanceId = UUID.randomUUID().toString()
//        }else {
//            nssmfOperation = "update"
//        }
//        execution.setVariable("nssmfOperation",nssmfOperation)
//        execution.setVariable("nssiInstanceId",nssiInstanceId)
//
//        def isNSSICreated = false
//        execution.setVariable("isNSSICreated",isNSSICreated)
//
//        int currentCycle = 0
//        execution.setVariable("currentCycle", currentCycle)


        SliceTaskInfo sliceTaskInfo = execution.getVariable("sliceTaskInfo") as SliceTaskInfo
        if (StringUtils.isBlank(sliceTaskInfo.suggestNssiId)) {
            execution.setVariable("nssmfOperation", "create")
        } else {
            execution.setVariable("nssmfOperation", "update")
        }
        logger.trace("Exit preProcessRequest")
    }

    /**
     * prepare nssi request
     * @param execution
     */
    void prepareNSSIReq(DelegateExecution execution) {

    }

    void getNSSTInfo(DelegateExecution execution){
        logger.trace("Enter getNSSTInfo in DoAllocateNSSI()")
        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        ModelInfo modelInfo = serviceDecomposition.getModelInfo()
        String serviceRole = "nssi"
        String nssiServiceInvariantUuid = serviceDecomposition.modelInfo.getModelInvariantUuid()
        String nssiServiceUuid = serviceDecomposition.modelInfo.getModelUuid()
        String nssiServiceType = serviceDecomposition.getServiceType()
        String uuiRequest = execution.getVariable("uuiRequest")
        String nssiServiceName = "nssi_"+jsonUtil.getJsonValue(uuiRequest, "service.name")
        execution.setVariable("nssiServiceName",nssiServiceName)
        execution.setVariable("nssiServiceType",nssiServiceType)
        execution.setVariable("nssiServiceInvariantUuid",nssiServiceInvariantUuid)
        execution.setVariable("nssiServiceUuid",nssiServiceUuid)
        execution.setVariable("serviceRole",serviceRole)

        String content = serviceDecomposition.getServiceInfo().getServiceArtifact().get(0).getContent()
        String nsstID = jsonUtil.getJsonValue(content, "metadata.id")
        String nsstVendor = jsonUtil.getJsonValue(content, "metadata.vendor")
        String nsstDomain = jsonUtil.getJsonValue(content, "metadata.domainType")
        String nsstType = jsonUtil.getJsonValue(content, "metadata.type")

        execution.setVariable("nsstID",nsstID)
        execution.setVariable("nsstVendor",nsstVendor)
        execution.setVariable("nsstDomain",nsstDomain)
        execution.setVariable("nssiServiceUuid",nssiServiceUuid)
        execution.setVariable("nsstType",nsstType)

        String nsstContentInfo = """{
        "NsstID":"${nsstID}",
        "Vendor":"${nsstVendor}",
        "type":"${nsstType}"
         }"""

        logger.trace("Exit getNSSTInfo in DoAllocateNSSI()")
    }

}
