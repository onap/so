package org.onap.so.bpmn.infrastructure.scripts

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.so.bpmn.common.recipe.BpmnRestClient
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.properties.BPMNProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Type

class DoCreatenetworkE2EOpenstackAdapterRequest extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DoCreatevfmE2EOpenstackAdapterRequest.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

    public void preProcessRequest(DelegateExecution execution) {
        logger.trace("preProcessRequest ")
        String msg = ""

        List addResourceList = execution.getVariable("addResourceList")
        if (addResourceList == null) {
            msg = "Input addResourceList is null"
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        else if (addResourceList.size() == 0) {
            msg = "No resource in addResourceList"
            logger.info(msg)
        }
        logger.trace("Exit preProcessRequest ")
    }

    public void prepareRequest(DelegateExecution execution)
    {

    }

    public void sendOpenstackRequest(DelegateExecution execution)
    {
        logger.trace("Start executeResourceRecipe Process ")

        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String serviceType = execution.getVariable("serviceType")
            String resourceInput = execution.getVariable("resourceInput")
            String resourceModelUUID = execution.getVariable("resourceModelUUID")

            // requestAction is action, not opertiontype
            //String requestAction = resourceInput.getOperationType()
            String requestAction = "createInstance"
            JSONObject resourceRecipe = catalogDbUtils.getResourceRecipe(execution, resourceModelUUID, requestAction)

            if (resourceRecipe != null) {
                String recipeURL = BPMNProperties.getProperty("bpelURL", "http://so-bpmn-infra.onap:8081") + resourceRecipe.getString("orchestrationUri")
                int recipeTimeOut = resourceRecipe.getInt("recipeTimeout")
                String recipeParamXsd = resourceRecipe.get("paramXSD")

                BpmnRestClient bpmnRestClient = new BpmnRestClient()
                HttpResponse resp = bpmnRestClient.post(recipeURL, requestId, recipeTimeOut, requestAction, serviceInstanceId, serviceType, resourceInput, recipeParamXsd)

                def currentIndex = execution.getVariable("currentResourceIndex")
                List<Resource> instanceResourceList = execution.getVariable("instanceResourceList") as List<Resource>
                Resource curr
                entResource = instanceResourceList.get(currentIndex)
                if(ResourceType.NETWORK == currentResource.getResourceType()) {
                    if (resp.getStatusLine().getStatusCode() > 199 && resp.getStatusLine().getStatusCode() < 300) {
                        String responseString = EntityUtils.toString(resp.getEntity(), "UTF-8")
                        if (responseString != null) {
                            Gson gson = new Gson()
                            Type type = new TypeToken<Map<String, String>>() {}.getType()
                            Map<String, Object> map = gson.fromJson(responseString, type)
                            Map<String, String> map1 = gson.fromJson(map.get("response"), type)
                            execution.setVariable("network-id",map1.get("network-id"))
                        }
                    }
                }
            } else {
                String exceptionMessage = "Resource receipe is not found for resource modeluuid: " + resourceModelUUID
                logger.trace(exceptionMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, exceptionMessage)
            }

            logger.trace("======== end executeResourceRecipe Process ======== ")
        }catch(BpmnError b){
            logger.debug("Rethrowing MSOWorkflowException")
            throw b
        }catch(Exception e){
            logger.debug("Error occured within DoCreateResources executeResourceRecipe method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured during DoCreateResources executeResourceRecipe Catalog")
        }
    }
}
