/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject;

import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.json.JsonUtils

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.codehaus.jackson.map.ObjectMapper

import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.rest.APIResponse;

import org.openecomp.mso.bpmn.infrastructure.vfcmodel.ScaleResource
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.ScaleNsByStepsData
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.ScaleNsData

import org.openecomp.mso.bpmn.infrastructure.vfcmodel.NSResourceInputParameter
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.NsOperationKey
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.NsScaleParameters
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.NsParameters
import org.openecomp.mso.bpmn.infrastructure.vfcmodel.LocationConstraint


/**
 * This groovy class supports the <class>DoScaleVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Scale
 */
public class DoScaleVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {

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
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", " *** preProcessRequest() *** ", isDebugEnabled)

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

        utils.log("DEBUG", " ***** Exit preProcessRequest *****", isDebugEnabled)
    }

    /**
     * scale NS task
     */
    public void scaleNetworkService(DelegateExecution execution) {

        String saleNsRequest = execution.getVariable("reqBody")
        String[] nsReqStr = saleNsRequest.split("\\|")

        def jobIdArray = ['jobId001', 'jobId002'] as String[]

        for (int i = 0; i < nsReqStr.length; i++) {
            JSONObject reqBodyJsonObj = new JSONObject(nsReqStr[i])
            String nsInstanceId = reqBodyJsonObj.getJSONObject("nsScaleParameters").getString("nsInstanceId")
            reqBodyJsonObj.getJSONObject("nsScaleParameters").remove("nsInstanceId")
            String reqBody = reqBodyJsonObj.toString()

            String url = host + scaleUrl.replaceAll("\\{nsInstanceId\\}", nsInstanceId)

            APIResponse apiResponse = postRequest(execution, url, reqBody)

            String returnCode = apiResponse.getStatusCode()
            String aaiResponseAsString = apiResponse.getResponseBodyAsString()
            String jobId = "";
            if (returnCode == "200") {
                jobId = jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
            }

            execution.setVariable("jobId", jobIdArray[i])

            String isScaleFinished = ""

            // query the requested network service scale status, if finished, then start the next one, otherwise, wait
            while (isScaleFinished != "finished"){
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

        APIResponse apiResponse = postRequest(execution,url, queryReqBody)

        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()

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
            taskProcessor.utils.log("ERROR", "Time Delay exception" + e, isDebugEnabled)
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
    private APIResponse postRequest(DelegateExecution execution, String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started Execute VFC adapter Post Process *****",  isDebugEnabled)
        utils.log("INFO","url:"+url +"\nrequestBody:"+ requestBody,  isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url)
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk")
//            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk")
            apiResponse = client.httpPost(requestBody)
            utils.log("INFO","response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(),  isDebugEnabled)
            utils.log("INFO","======== Completed Execute VF-C adapter Post Process ======== ",  isDebugEnabled)
        }catch(Exception e){
            utils.log("ERROR","Exception occured while executing VFC Post Call. Exception is: \n" + e,  isDebugEnabled)
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

