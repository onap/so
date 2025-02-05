/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject;

import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils

import org.camunda.bpm.engine.delegate.BpmnError
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.so.client.HttpClientFactory
import org.onap.so.logging.filter.base.ErrorCode

import jakarta.ws.rs.core.Response

import org.onap.so.bpmn.infrastructure.vfcmodel.ScaleResource
import org.onap.so.client.HttpClient
import org.onap.so.bpmn.infrastructure.vfcmodel.ScaleNsByStepsData
import org.onap.so.bpmn.infrastructure.vfcmodel.ScaleNsData

import org.onap.so.bpmn.infrastructure.vfcmodel.NSResourceInputParameter
import org.onap.so.bpmn.infrastructure.vfcmodel.NsOperationKey
import org.onap.so.bpmn.infrastructure.vfcmodel.NsScaleParameters
import org.onap.so.bpmn.infrastructure.vfcmodel.NsParameters
import org.onap.so.bpmn.infrastructure.vfcmodel.LocationConstraint
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logging.filter.base.ONAPComponents;




/**
 * This groovy class supports the <class>DoScaleVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Scale
 */
public class DoScaleVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoScaleVFCNetworkServiceInstance.class);


    String host = "http://mso.mso.testlab.openecomp.org:8080"

    String scaleUrl = "/vfc/rest/v1/vfcadapter/ns/{nsInstanceId}/scale"

    String queryJobUrl = "/vfc/rest/v1/vfcadapter/jobs/{jobId}"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    public void preProcessRequest(DelegateExecution execution) {
        logger.trace("preProcessRequest() ")

        List<NSResourceInputParameter> nsRIPList = convertScaleNsReq2NSResInputParamList(execution)
        String requestJsonStr = ""
        int size = nsRIPList.size()
        for (int i = 0; i < size; i++) {
            NSResourceInputParameter nsRIP = nsRIPList.get(i)

            if (i == size - 1) {
                requestJsonStr += objectToJsonStr(nsRIP)
            } else {
                requestJsonStr += objectToJsonStr(nsRIP) + "|"
            }
        }

        execution.setVariable("reqBody", requestJsonStr)

        logger.trace("Exit preProcessRequest ")
    }

    /**
     * scale NS task
     */
    public void scaleNetworkService(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        String saleNsRequest = execution.getVariable("reqBody")
        String[] nsReqStr = saleNsRequest.split("\\|")

        for (int i = 0; i < nsReqStr.length; i++) {
            JSONObject reqBodyJsonObj = new JSONObject(nsReqStr[i])
            String nsInstanceId = reqBodyJsonObj.getJSONObject("nsScaleParameters").getString("nsInstanceId")
            String nodeTemplateUUID = reqBodyJsonObj.getJSONObject("nsOperationKey").getString("nodeTemplateUUID")
            reqBodyJsonObj.getJSONObject("nsScaleParameters").remove("nsInstanceId")
            String reqBody = reqBodyJsonObj.toString()

            String url = host + scaleUrl.replaceAll("\\{nsInstanceId\\}", nsInstanceId)

            Response apiResponse = postRequest(execution, url, reqBody)

            String returnCode = apiResponse.getStatus()
            String aaiResponseAsString = apiResponse.readEntity(String.class)
            String jobId = ""
            if (returnCode == "200" || returnCode == "202") {
                jobId = jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
            }
            logger.info( "scaleNetworkService get a ns scale job Id:" + jobId)
            execution.setVariable("jobId", jobId)
            execution.setVariable("nodeTemplateUUID", nodeTemplateUUID)

            String isScaleFinished = ""

            if(jobId =="" || jobId == null){
                continue
            }
            // query the requested network service scale status, if finished, then start the next one, otherwise, wait
            while (isScaleFinished != "finished" && isScaleFinished != "error"){
                timeDelay()
                queryNSProgress(execution)
                isScaleFinished = execution.getVariable("operationStatus")
            }
        }
    }

    /**
     * query NS task
     */
    private void queryNSProgress(DelegateExecution execution) {
        String jobId = execution.getVariable("jobId")
        String url = host + queryJobUrl.replaceAll("\\{jobId\\}", jobId)

        NsOperationKey nsOperationKey = new NsOperationKey()
        // is this net work service ID or E2E service ID?
        nsOperationKey.setServiceId(execution.getVariable("serviceId"))
        nsOperationKey.setServiceType(execution.getVariable("serviceType"))
        nsOperationKey.setGlobalSubscriberId(execution.getVariable("globalSubscriberId"))
        nsOperationKey.setNodeTemplateUUID(execution.getVariable("nodeTemplateUUID"))
        nsOperationKey.setOperationId(execution.getVariable("operationId"))
        String queryReqBody = objectToJsonStr(nsOperationKey)

        Response apiResponse = postRequest(execution,url, queryReqBody)

        String returnCode = apiResponse.getStatus()
        String aaiResponseAsString = apiResponse.readEntity(String.class)

        String operationStatus = "error"

        if (returnCode == "200") {
            operationStatus = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.status")
        }

        execution.setVariable("operationStatus", operationStatus)
    }

    /**
     * delay 5 sec
     *
     */
    private void timeDelay() {
        try {
            Thread.sleep(5000)
        } catch (InterruptedException e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Time Delay exception" + e, "BPMN",
                    ErrorCode.UnknownError.getValue());
        }
    }

    /**
     * finish NS task
     */
    public void finishNSScale(DelegateExecution execution) {
        //no need to do anything util now
        System.out.println("Scale finished.")
    }

    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private Response postRequest(DelegateExecution execution, String urlString, String requestBody){
        logger.trace("Started Execute VFC adapter Post Process ")
        logger.info("url:"+urlString +"\nrequestBody:"+ requestBody)
        Response apiResponse = null
        try{
			URL url = new URL(urlString);

            HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.VNF_ADAPTER)
			httpClient.addAdditionalHeader("Authorization", "Basic QlBFTENsaWVudDpwYXNzd29yZDEk")

			apiResponse = httpClient.post(requestBody)

            logger.info("response code:"+ apiResponse.getStatus() +"\nresponse body:"+ apiResponse.readEntity(String.class))
            logger.trace("Completed Execute VF-C adapter Post Process ")
        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Exception occured " +
                    "while executing VFC Post Call.", "BPMN", ErrorCode.UnknownError.getValue(), e);
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
    }

    /**
     * create a Scale Resource object list from a NSScaleRequestJso nString
     * This method is for the specific request from Scale Network Service BPMN workflow
     * @param nsScaleRequestJsonString , a specific request Json string which conform to ?? class
     * @return List < ScaleResource >
     */
    private List<ScaleResource> jsonGetNsResourceList(String nsScaleRequestJsonString) {
        List<ScaleResource> list = new ArrayList<ScaleResource>()
        JSONObject jsonObject = new JSONObject(nsScaleRequestJsonString)

        JSONObject jsonResource = jsonObject.getJSONObject("service")
        JSONArray arr = jsonResource.getJSONArray("resources")

        for (int i = 0; i < arr.length(); i++) {
            JSONObject tempResource = arr.getJSONObject(i)
            ScaleResource resource = new ScaleResource()
            resource.setResourceInstanceId(tempResource.getString("resourceInstanceId"))
            resource.setScaleType(tempResource.getString("scaleType"))

            JSONObject jsonScaleNsData = tempResource.getJSONObject("scaleNsData")
            JSONObject jsonScaleNsByStepData = jsonScaleNsData.getJSONObject("scaleNsByStepsData")

            ScaleNsData scaleNsData = new ScaleNsData()
            ScaleNsByStepsData stepsData = new ScaleNsByStepsData()

            stepsData.setAspectId(jsonScaleNsByStepData.getString("aspectId"))
            stepsData.setScalingDirection(jsonScaleNsByStepData.getString("scalingDirection"))
            stepsData.setNumberOfSteps(Integer.parseInt(jsonScaleNsByStepData.getString("numberOfSteps")))

            scaleNsData.setScaleNsByStepsData(stepsData)
            resource.setScaleNsData(scaleNsData)
            list.add(resource)
        }

        return list
    }

    /**
     * Convert a java class to JSON string
     * @param obj
     * @return
     */
    private String objectToJsonStr(Object obj) {
        ObjectMapper mapper = new ObjectMapper()
        String jsonStr = null
        try {
            jsonStr = mapper.writeValueAsString(obj)
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage())
        }
        return jsonStr

    }

    /**
     * create a NSResourceInputParameter list from a Scale Network request Json string
     * @return
     */
    private List<NSResourceInputParameter> convertScaleNsReq2NSResInputParamList(DelegateExecution execution) {
        String saleNsRequest = execution.getVariable("bpmnRequest")

        //String requestId = execution.getVariable("msoRequestId")
        //String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceInstanceName = execution.getVariable("serviceInstanceName")
        //String nodeTemplateUUID = execution.getVariable("nodeTemplateUUID")
        String serviceType = execution.getVariable("serviceType")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String operationId = execution.getVariable("operationId")
        String serviceId = execution.getVariable("serviceId")
        String nsServiceDescription = execution.getVariable("requestDescription")

        String resource = JsonUtils.getJsonValue(saleNsRequest, "service.resources")

        // set nsScaleParameters properties
        List<ScaleResource> scaleResourcesList = jsonGetNsResourceList(saleNsRequest)
        List<NSResourceInputParameter> nsResourceInputParameterList = new ArrayList<NSResourceInputParameter>()

        for (ScaleResource sr : scaleResourcesList) {
            NSResourceInputParameter nsResourceInputParameter = new NSResourceInputParameter()
            NsOperationKey nsOperationKey = new NsOperationKey()
            NsParameters nsParameters = new NsParameters()
            NsScaleParameters nsScaleParameters = new NsScaleParameters()
            nsParameters.setLocationConstraints(new ArrayList<LocationConstraint>())
            nsParameters.setAdditionalParamForNs(new HashMap<String, Object>())

            // set NsOperationKey properties
            nsOperationKey.setGlobalSubscriberId(globalSubscriberId)
            nsOperationKey.setServiceId(serviceId)
            nsOperationKey.setServiceType(serviceType)
            // for ns scale the resourceInstanceId is the nodeTemplateUUID
            nsOperationKey.setNodeTemplateUUID(sr.getResourceInstanceId())
            nsOperationKey.setOperationId(operationId)

            nsScaleParameters.setScaleType(sr.getScaleType())
            nsScaleParameters.setNsInstanceId(sr.getResourceInstanceId())

            ScaleNsByStepsData scaleNsByStepsData = new ScaleNsByStepsData()
            scaleNsByStepsData.setScalingDirection(sr.getScaleNsData().getScaleNsByStepsData().getScalingDirection())
            scaleNsByStepsData.setNumberOfSteps(sr.getScaleNsData().getScaleNsByStepsData().getNumberOfSteps())
            scaleNsByStepsData.setAspectId(sr.getScaleNsData().getScaleNsByStepsData().getAspectId())

            List<ScaleNsByStepsData> scaleNsByStepsDataList = new ArrayList<ScaleNsByStepsData>()
            scaleNsByStepsDataList.add(scaleNsByStepsData)
            nsScaleParameters.setScaleNsByStepsData(scaleNsByStepsDataList)

            nsResourceInputParameter.setNsOperationKey(nsOperationKey)
            nsResourceInputParameter.setNsServiceName(serviceInstanceName)
            nsResourceInputParameter.setNsServiceDescription(nsServiceDescription)
            nsResourceInputParameter.setNsParameters(nsParameters)
            nsResourceInputParameter.setNsScaleParameters(nsScaleParameters)

            nsResourceInputParameterList.add(nsResourceInputParameter)
        }
        return nsResourceInputParameterList
    }
}

