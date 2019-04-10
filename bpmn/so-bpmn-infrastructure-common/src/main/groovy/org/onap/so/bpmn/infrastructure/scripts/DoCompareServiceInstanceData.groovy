/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.workflow.service.ServicePluginFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * This groovy class supports the <class>DoCompareServiceInstanceData.bpmn</class> process.
 *
 * Inputs:
 * @param - serviceInstanceData-original
 * @param - serviceInstanceId
 * @param - uuiRequest
 * @param - model-invariant-id-original
 * @param - model-version-id-original
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 *
 * Outputs:
 * @param - addResourceList
 * @param - delResourceList
 * @param - uuiRequest-add
 * @param - uuiRequest-del
 *
 */
public class DoCompareServiceInstanceData extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
    private static final Logger logger = LoggerFactory.getLogger( DoCompareServiceInstanceData.class);

    public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.info("INFO"," ***** preProcessRequest *****",  isDebugEnabled)
		try {
            checkInput("serviceInstanceData-original", execution, isDebugEnabled)
            checkInput("serviceInstanceId", execution, isDebugEnabled)
            checkInput("uuiRequest", execution, isDebugEnabled)
            checkInput("model-invariant-id-original", execution, isDebugEnabled)
            checkInput("model-version-id-original", execution, isDebugEnabled)
            checkInput("msoRequestId", execution, isDebugEnabled)
		} catch (Exception ex){
            String msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

    private void checkInput(String inputName, DelegateExecution execution, isDebugEnabled) {
        String msg
        Object inputValue = execution.getVariable(inputName)
        if (inputValue == null) {
            msg = "Input" + inputName + "is null"
            logger.info("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
    }


    public void prepareDecomposeService_Original(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        try {
            logger.debug( " ***** Inside prepareDecomposeService_Original of update generic e2e service ***** ")
            String modelInvariantUuid = execution.getVariable("model-invariant-id-original")
            String modelUuid = execution.getVariable("model-version-id-original")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""

            execution.setVariable("serviceModelInfo_Original", serviceModelInfo)

            logger.debug( " ***** Completed prepareDecomposeService_Original of update generic e2e service ***** ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. Unexpected Error from method prepareDecomposeService_Original() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void processDecomposition_Original(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        logger.debug( " ***** Inside processDecomposition_Original() of update generic e2e service flow ***** ")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            execution.setVariable("serviceDecomposition_Original", serviceDecomposition)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. processDecomposition_Original() - " + ex.getMessage()
            logger.debug( exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void doCompareUuiRquestInput(DelegateExecution execution) {

        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        logger.info("INFO", "======== Start doCompareUuiRquestInput Process ======== ", isDebugEnabled)

        String uuiRequest_Target = execution.getVariable("uuiRequest")
        Map<String, Object> serviceParametersObject_Target = getServiceParametersObject(uuiRequest_Target)
        Map<String, Object> serviceRequestInputs_Target = (Map<String, Object>) serviceParametersObject_Target.get("requestInputs")
        List<Object> resources_Target = (List<Object>) serviceParametersObject_Target.get("resources")

        String uuiRequest_Original = ((ServiceInstance) execution.getVariable("serviceInstanceData-original")).getInputParameters()
        Map<String, Object> serviceParametersObject_Original = getServiceParametersObject(uuiRequest_Original)
        Map<String, Object> serviceRequestInputs_Original = (Map<String, Object>) serviceParametersObject_Original.get("requestInputs")
        List<Object> resources_Original = (List<Object>) serviceParametersObject_Original.get("resources")

        logger.info("INFO", "uuiRequest is: " + uuiRequest_Target, isDebugEnabled)

        //the resources which are included by resources_Target but resources_Original are the resources going to be added
        ArrayList<Object> resourceList_Add = findResourceListIncluded(resources_Target, resources_Original)
        HashMap<String, Object> serviceRequestInputs_Add = getServiceRequestInputsIncluded(resourceList_Add, serviceRequestInputs_Target, serviceParametersObject_Target)
        String uuiRequest_add = loadUuiRequestJsonString(uuiRequest_Target, resourceList_Add, serviceRequestInputs_Add)
        execution.setVariable("uuiRequest", uuiRequest_add)
        logger.info("INFO", "uuiRequest will be changed to: " + uuiRequest_add, isDebugEnabled)

        //the resources which are included by resources_Original but resources_Target are the resources going to be deleted
        ArrayList<Object> resourceList_Del = findResourceListIncluded(resources_Original, resources_Target)
        HashMap<String, Object> serviceRequestInputs_Del = getServiceRequestInputsIncluded(resourceList_Del, serviceRequestInputs_Original, serviceParametersObject_Original)
        String uuiRequest_del = loadUuiRequestJsonString(uuiRequest_Original, resourceList_Del, serviceRequestInputs_Del)
        execution.setVariable("uuiRequest-del", uuiRequest_del)
        logger.info("INFO", "uuiRequest-del: " + uuiRequest_del, isDebugEnabled)

        List<Resource> addResourceList = findResourceList(resourceList_Add, execution)
        execution.setVariable("addResourceList", addResourceList)
        logger.info("INFO", "addResourceList: " + addResourceList, isDebugEnabled)

        List<Resource> delResourceList = findResourceList(resourceList_Del, execution)
        execution.setVariable("delResourceList", delResourceList)
        logger.info("INFO", "delResourceList: " + delResourceList, isDebugEnabled)

        logger.info("INFO", "======== COMPLETED doCompareUuiRquestInput Process ======== ", isDebugEnabled)
    }

    private List<Resource> findResourceList(ArrayList<Object> uuiResourceList, DelegateExecution execution) {
        HashSet<String> addResourceCustomizationUuidSet = getCustomizationUuidSet(uuiResourceList)
        Set<Resource> resourceSet = new HashSet<>()
        ServiceDecomposition serviceDecomposition_Original = execution.getVariable("serviceDecomposition_Original")
        List<Resource> allSR_original = serviceDecomposition_Original.getServiceResources()
        for (Resource resource : allSR_original) {
            if (addResourceCustomizationUuidSet.contains(resource.getModelInfo().getModelCustomizationUuid())) {
                resourceSet.add(resource)
            }
        }
        List<Resource> resourceList = new ArrayList<String>(resourceSet)
        resourceList
    }

    private HashSet<String> getCustomizationUuidSet(ArrayList<Object> resourceList_Add) {
        Set<String> addRsourceCustomizationUuidSet = new HashSet<>()
        for (Map<String, Object> resourceAdded : resourceList_Add) {
            addRsourceCustomizationUuidSet.add(resourceAdded.get("rsourceCustomizationUuid"))
        }
        addRsourceCustomizationUuidSet
    }

    private String loadUuiRequestJsonString(String uuiRequest_Target, ArrayList<Object> resourceList_Add, HashMap<String, Object> serviceRequestInputs_Add) {
        Map<String, Object> uuiObject = ServicePluginFactory.getInstance().getJsonObject(uuiRequest_Target, Map.class)
        Map<String, Object> serviceObject = (Map<String, Object>) uuiObject.get("service")
        Map<String, Object> serviceParametersObject = (Map<String, Object>) serviceObject.get("parameters")
        serviceParametersObject.put("resources", resourceList_Add)
        serviceParametersObject.put("requestInputs", serviceRequestInputs_Add)
        String uuiRequest_add = ServicePluginFactory.getInstance().getJsonString(serviceObject)
        uuiRequest_add
    }

    private HashMap<String, Object> getServiceRequestInputsIncluded(ArrayList<Object> resourceList_Add, Map<String, Object> serviceRequestInputs_Target, Map<String, Object> serviceParametersObject_Target) {
        ArrayList<String> newResourceNames = getNewResourceNames(resourceList_Add)
        Map<String, Object> serviceRequestInputs_Add = new HashMap<String, Object>()
        for (String inputKey : serviceRequestInputs_Target.keySet()) {
            String resourceName = (inputKey.split("_"))[0]
            if (newResourceNames.contains(resourceName)) {
                serviceRequestInputs_Add.put(inputKey, serviceParametersObject_Target.get(inputKey))
            }
        }
        serviceRequestInputs_Add
    }

    private ArrayList<String> getNewResourceNames(ArrayList<Object> addResourceList) {
        Set<String> newResourceNames = new ArrayList<String>()
        for (Object resourceObject : addResourceList) {
            Map<String, Object> resourceAdded = (Map<String, Object>) resourceObject
            String resName = new String(resourceAdded.get("resourceName"))
            normalizeName(resName)
            newResourceNames.add(resName)
        }
        newResourceNames
    }

    private void normalizeName(String resName) {
        resName.replaceAll("_", "")
        resName.replaceAll(" ", "")
        resName.toLowerCase()
    }

    private ArrayList<Object> findResourceListIncluded(List<Object> resources_Target, List<Object> resources_Original) {
        List<Object> addResourceList = new ArrayList<Object>()
        for (Object resource_Target : resources_Target) {
            Map<String, Object> resourceObject_Target = (Map<String, Object>) resource_Target
            boolean isNewResourceInstance = isNewResourceInstance(resourceObject_Target, resources_Original)
            if (isNewResourceInstance) {
                addResourceList.add(resource_Target)
            }
        }
        addResourceList
    }

    private boolean isNewResourceInstance(Map<String, Object> resourceObject_Target, List<Object> resources_Original) {
        String resourceIndex_Target = null
        if (resourceObject_Target.keySet().contains("resourceIndex")) {
            resourceIndex_Target = resourceObject_Target.get("resourceIndex")
        }
        String resourceName_Target = resourceObject_Target.get("resourceName")
        boolean isNewResourceInstance = true
        for (Object resource_Original : resources_Original) {
            Map<String, Object> resourceObject_Original = (Map<String, Object>) resource_Original
            String resourceIndex_Original = null
            if (resourceObject_Original.keySet().contains("resourceIndex")) {
                resourceIndex_Original = resourceObject_Original.get("resourceIndex")
            }
            String resourceName_Original = resourceObject_Original.get("resourceName")
            if (resourceName_Target.equals(resourceName_Original)) {
                if (resourceIndex_Target != null && resourceIndex_Original != null) {
                    if (resourceIndex_Target.equals(resourceIndex_Original)) {
                        isNewResourceInstance = false
                    }
                } else {
                    isNewResourceInstance = false
                }
            }
        }
        isNewResourceInstance
    }

    private Map<String, Object> getServiceParametersObject(String uuiRequest_Target) {
        Map<String, Object> uuiObject = ServicePluginFactory.getInstance().getJsonObject(uuiRequest_Target, Map.class)
        Map<String, Object> serviceObject = (Map<String, Object>) uuiObject.get("service")
        Map<String, Object> serviceParametersObject = (Map<String, Object>) serviceObject.get("parameters")
        serviceParametersObject
    }

}
