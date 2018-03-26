
/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.json.JSONArray
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.infrastructure.properties.BPMNProperties;
import org.apache.http.HttpResponse
import org.json.JSONArray
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.common.recipe.ResourceInput;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import com.fasterxml.jackson.jaxrs.json.annotation.JSONP.Def;

public class DeleteSDNCNetworkResource extends AbstractServiceTaskProcessor {
    String Prefix="DDELSDNNS_"


    public void preProcessSDNCDelete (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** preProcessSDNCDelete *****", isDebugEnabled)
        String msg = ""

        try {
            def serviceInstanceId = execution.getVariable("serviceInstanceId")
            def serviceInstanceName = execution.getVariable("serviceInstanceName")
            def callbackURL = execution.getVariable("sdncCallbackUrl")
            def requestId = execution.getVariable("msoRequestId")
            def serviceId = execution.getVariable("productFamilyId")
            def subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            def globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId

            String serviceModelInfo = execution.getVariable("serviceModelInfo")
            def modelInvariantUuid = ""
            def modelVersion = ""
            def modelUuid = ""
            def modelName = ""
            if (!isBlank(serviceModelInfo))
            {
                modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
                modelVersion = jsonUtil.getJsonValue(serviceModelInfo, "modelVersion")
                modelUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelUuid")
                modelName = jsonUtil.getJsonValue(serviceModelInfo, "modelName")

                if (modelInvariantUuid == null) {
                    modelInvariantUuid = ""
                }
                if (modelVersion == null) {
                    modelVersion = ""
                }
                if (modelUuid == null) {
                    modelUuid = ""
                }
                if (modelName == null) {
                    modelName = ""
                }
            }
            if (serviceInstanceName == null) {
                serviceInstanceName = ""
            }
            if (serviceId == null) {
                serviceId = ""
            }

            def siParamsXml = execution.getVariable("siParamsXml")
            def serviceType = execution.getVariable("serviceType")
            if (serviceType == null)
            {
                serviceType = ""
            }

            def sdncRequestId = UUID.randomUUID().toString()

            String sdncDelete =
                    """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>delete</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
							<sdncadapter:MsoAction>${serviceType}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${requestId}</request-id>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
						<request-action>DeleteServiceInstance</request-action>
					</request-information>
					<service-information>
						<service-id>${serviceId}</service-id>
						<subscription-service-type>${subscriptionServiceType}</subscription-service-type>
						<onap-model-information>
					         <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
					         <model-uuid>${modelUuid}</model-uuid>
					         <model-version>${modelVersion}</model-version>
					         <model-name>${modelName}</model-name>
					    </onap-model-information>
						<service-instance-id>${serviceInstanceId}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${globalSubscriberId}</global-customer-id>
					</service-information>
					<service-request-input>
						<service-instance-name>${serviceInstanceName}</service-instance-name>
						${siParamsXml}
					</service-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

            sdncDelete = utils.formatXml(sdncDelete)
            def sdncRequestId2 = UUID.randomUUID().toString()
            String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
            execution.setVariable("sdncDelete", sdncDelete)
            execution.setVariable("sdncDeactivate", sdncDeactivate)
            utils.log("INFO","sdncDeactivate:\n" + sdncDeactivate, isDebugEnabled)
            utils.log("INFO","sdncDelete:\n" + sdncDelete, isDebugEnabled)

        } catch (BpmnError e) {
            throw e;
        } catch(Exception ex) {
            msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
        }
        utils.log("INFO"," *****Exit preProcessSDNCDelete *****", isDebugEnabled)
    }


    public void postProcessSDNCDelete(DelegateExecution execution, String response) {

        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** postProcessSDNC "  + " *****", isDebugEnabled)
        String msg = ""
        utils.log("INFO"," response " + response, isDebugEnabled)
        utils.log("INFO"," *** Exit postProcessSDNC " + " ***", isDebugEnabled)
    }

}
