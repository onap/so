/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.apihandlerinfra;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.onap.aai.domain.yang.Tenant;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.apihandlerinfra.tasksbeans.TasksRequest;
import org.onap.so.apihandlerinfra.validation.ApplyUpdatedConfigValidation;
import org.onap.so.apihandlerinfra.validation.CloudConfigurationValidation;
import org.onap.so.apihandlerinfra.validation.ConfigurationParametersValidation;
import org.onap.so.apihandlerinfra.validation.CustomWorkflowValidation;
import org.onap.so.apihandlerinfra.validation.InPlaceSoftwareUpdateValidation;
import org.onap.so.apihandlerinfra.validation.InstanceIdMapValidation;
import org.onap.so.apihandlerinfra.validation.MembersValidation;
import org.onap.so.apihandlerinfra.validation.ModelInfoValidation;
import org.onap.so.apihandlerinfra.validation.PlatformLOBValidation;
import org.onap.so.apihandlerinfra.validation.ProjectOwningEntityValidation;
import org.onap.so.apihandlerinfra.validation.RelatedInstancesValidation;
import org.onap.so.apihandlerinfra.validation.RequestInfoValidation;
import org.onap.so.apihandlerinfra.validation.RequestParametersValidation;
import org.onap.so.apihandlerinfra.validation.RequestScopeValidation;
import org.onap.so.apihandlerinfra.validation.SubscriberInfoValidation;
import org.onap.so.apihandlerinfra.validation.UserParamsValidation;
import org.onap.so.apihandlerinfra.validation.ValidationInformation;
import org.onap.so.apihandlerinfra.validation.ValidationRule;
import org.onap.so.apihandlerinfra.vnfbeans.RequestStatusType;
import org.onap.so.apihandlerinfra.vnfbeans.VnfInputs;
import org.onap.so.apihandlerinfra.vnfbeans.VnfRequest;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.LogConstants;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.PolicyException;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MsoRequest {
    private static Logger logger = LoggerFactory.getLogger(MsoRequest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper nonNullMapper;

    static {
        nonNullMapper = new ObjectMapper();
        nonNullMapper.setSerializationInclusion(Include.NON_NULL);

    }

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private AAIDataRetrieval aaiDataRet;

    @Value("${mso.enforceDLP:false}")
    private boolean enforceDLP;


    public Response buildServiceErrorResponse(int httpResponseCode, MsoException exceptionType, String errorText,
            String messageId, List<String> variables, String version) {

        if (errorText.length() > 1999) {
            errorText = errorText.substring(0, 1999);
        }

        RequestError re = new RequestError();

        if ("PolicyException".equals(exceptionType.name())) {

            PolicyException pe = new PolicyException();
            pe.setMessageId(messageId);
            pe.setText(errorText);
            if (variables != null) {
                for (String variable : variables) {
                    pe.getVariables().add(variable);
                }
            }
            re.setPolicyException(pe);

        } else {

            ServiceException se = new ServiceException();
            se.setMessageId(messageId);
            se.setText(errorText);
            if (variables != null) {
                for (String variable : variables) {
                    se.getVariables().add(variable);
                }
            }
            re.setServiceException(se);
        }
        return builder.buildResponse(httpResponseCode, null, re, version);
    }


    // Parse request JSON
    public void parse(ServiceInstancesRequest sir, Map<String, String> instanceIdMap, Actions action, String version,
            String originalRequestJSON, int reqVersion, Boolean aLaCarteFlag) throws ValidationException, IOException {

        logger.debug("Validating the Service Instance request");
        List<ValidationRule> rules = new ArrayList<>();
        logger.debug("Incoming version is: {} coverting to int: {}", version, reqVersion);
        RequestParameters requestParameters = sir.getRequestDetails().getRequestParameters();
        ValidationInformation info =
                new ValidationInformation(sir, instanceIdMap, action, reqVersion, aLaCarteFlag, requestParameters);

        rules.add(new InstanceIdMapValidation());

        String workflowUuid = null;
        if (instanceIdMap != null) {
            workflowUuid = instanceIdMap.get("workflowUuid");
        }

        if (workflowUuid != null) {
            rules.add(new CustomWorkflowValidation());
        } else if (reqVersion >= 6 && action == Action.inPlaceSoftwareUpdate) {
            rules.add(new InPlaceSoftwareUpdateValidation());
        } else if (reqVersion >= 6 && action == Action.applyUpdatedConfig) {
            rules.add(new ApplyUpdatedConfigValidation());
        } else if (action == Action.addMembers || action == Action.removeMembers) {
            rules.add(new MembersValidation());
        } else {
            rules.add(new RequestScopeValidation());
            rules.add(new RequestParametersValidation());
            rules.add(new RequestInfoValidation());
            rules.add(new ModelInfoValidation());
            rules.add(new CloudConfigurationValidation());
            rules.add(new SubscriberInfoValidation());
            if (!enforceDLP) {
                rules.add(new PlatformLOBValidation());
                rules.add(new ProjectOwningEntityValidation());
            }
            rules.add(new RelatedInstancesValidation());
            rules.add(new ConfigurationParametersValidation());
        }
        if (reqVersion >= 7 && requestParameters != null && requestParameters.getUserParams() != null) {
            for (Map<String, Object> params : requestParameters.getUserParams()) {
                if (params.containsKey("service")) {
                    String input = mapper.writeValueAsString(params.get("service"));
                    Service validate = mapper.readValue(input, Service.class);
                    info.setUserParams(validate);
                    rules.add(new UserParamsValidation());
                    break;
                }
            }
        }
        for (ValidationRule rule : rules) {
            rule.validate(info);
        }
    }

    void parseOrchestration(ServiceInstancesRequest sir) throws ValidationException {
        RequestInfo requestInfo = sir.getRequestDetails().getRequestInfo();

        if (requestInfo == null) {
            throw new ValidationException("requestInfo");
        }

        if (empty(requestInfo.getSource())) {
            throw new ValidationException("source");
        }
        if (empty(requestInfo.getRequestorId())) {
            throw new ValidationException("requestorId");
        }
    }

    public Map<String, List<String>> getOrchestrationFilters(MultivaluedMap<String, String> queryParams) {
        final String FILTER_KEY = "filter";
        Map<String, List<String>> orchestrationFilterParams = new HashMap<>();

        Optional.ofNullable(queryParams.get(FILTER_KEY)).ifPresent(listValues -> listValues
                .forEach(value -> addValueToOrchestrationFilterParamsMap(orchestrationFilterParams, value)));

        return orchestrationFilterParams;
    }

    private void addValueToOrchestrationFilterParamsMap(Map<String, List<String>> orchestrationFilterParams,
            String value) {
        final String TOKEN_DELIMITER = ":";
        StringTokenizer stringTokenizer = new StringTokenizer(value, TOKEN_DELIMITER);

        if (!stringTokenizer.hasMoreTokens()) {
            return;
        }
        String mapKey = stringTokenizer.nextToken();
        List<String> orchestrationList = new ArrayList<>();
        while (stringTokenizer.hasMoreTokens()) {
            orchestrationList.add(stringTokenizer.nextToken());
        }

        orchestrationFilterParams.put(mapKey, orchestrationList);
    }

    public InfraActiveRequests createRequestObject(ServiceInstancesRequest servInsReq, Actions action, String requestId,
            Status status, String originalRequestJSON, String requestScope) {
        InfraActiveRequests aq = new InfraActiveRequests();
        try {
            if (null == servInsReq) {
                servInsReq = new ServiceInstancesRequest();
            }
            String networkType = "";
            String vnfType = "";
            aq.setRequestId(requestId);
            aq.setRequestAction(action.toString());
            aq.setRequestUrl(MDC.get(LogConstants.HTTP_URL));

            Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());

            aq.setStartTime(startTimeStamp);
            if (requestScope.equals(ModelType.instanceGroup.name()) && action == Action.deleteInstance) {
                aq.setRequestScope(requestScope);
            } else {
                RequestInfo requestInfo = servInsReq.getRequestDetails().getRequestInfo();
                if (requestInfo != null) {

                    if (requestInfo.getSource() != null) {
                        aq.setSource(requestInfo.getSource());
                    }
                    if (requestInfo.getCallbackUrl() != null) {
                        aq.setCallBackUrl(requestInfo.getCallbackUrl());
                    }
                    if (requestInfo.getCorrelator() != null) {
                        aq.setCorrelator(requestInfo.getCorrelator());
                    }

                    if (requestInfo.getRequestorId() != null) {
                        aq.setRequestorId(requestInfo.getRequestorId());
                    }
                }

                if (servInsReq.getRequestDetails().getModelInfo() != null
                        || (action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig)) {
                    aq.setRequestScope(requestScope);
                }

                if (servInsReq.getRequestDetails().getCloudConfiguration() != null) {
                    CloudConfiguration cloudConfiguration = servInsReq.getRequestDetails().getCloudConfiguration();
                    if (cloudConfiguration.getLcpCloudRegionId() != null) {
                        aq.setCloudRegion(cloudConfiguration.getLcpCloudRegionId());
                    }

                    if (cloudConfiguration.getTenantId() != null) {
                        aq.setTenantId(cloudConfiguration.getTenantId());
                    }

                }

                if (servInsReq.getServiceInstanceId() != null) {
                    aq.setServiceInstanceId(servInsReq.getServiceInstanceId());
                }

                if (servInsReq.getVnfInstanceId() != null) {
                    aq.setVnfId(servInsReq.getVnfInstanceId());
                }

                if (servInsReq.getPnfName() != null) {
                    aq.setRequestScope(requestScope);
                    aq.setPnfName(servInsReq.getPnfName());
                }

                if (servInsReq.getRequestDetails() != null && servInsReq.getRequestDetails().getRequestInfo() != null
                        && servInsReq.getRequestDetails().getRequestInfo().getProductFamilyId() != null) {
                    logger.debug("Retrieving productFamilyName to put into requests db");

                    org.onap.aai.domain.yang.Service service =
                            aaiDataRet.getService(servInsReq.getRequestDetails().getRequestInfo().getProductFamilyId());
                    if (service != null) {
                        logger.debug("Found service by service-id");
                        String productFamilyName = service.getServiceDescription();
                        if (productFamilyName != null) {
                            aq.setProductFamilyName(productFamilyName);
                        }
                    }
                }

                aq.setProductFamilyName(getProductFamilyNameFromAAI(servInsReq));

                aq.setTenantName(getTenantNameFromAAI(servInsReq));

                if (ModelType.service.name().equalsIgnoreCase(requestScope)) {
                    if (servInsReq.getRequestDetails().getRequestInfo() != null) {
                        if (servInsReq.getRequestDetails().getRequestInfo().getInstanceName() != null) {
                            aq.setServiceInstanceName(requestInfo.getInstanceName());
                        }
                    }
                }

                if (ModelType.network.name().equalsIgnoreCase(requestScope)) {
                    aq.setNetworkName(servInsReq.getRequestDetails().getRequestInfo().getInstanceName());
                    aq.setNetworkType(networkType);
                    aq.setNetworkId(servInsReq.getNetworkInstanceId());
                }

                if (ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)) {
                    aq.setVolumeGroupId(servInsReq.getVolumeGroupInstanceId());
                    aq.setVolumeGroupName(servInsReq.getRequestDetails().getRequestInfo().getInstanceName());
                    aq.setVnfType(vnfType);

                }

                if (ModelType.vfModule.name().equalsIgnoreCase(requestScope)) {
                    aq.setVfModuleName(requestInfo.getInstanceName());
                    aq.setVfModuleModelName(servInsReq.getRequestDetails().getModelInfo().getModelName());
                    aq.setVfModuleId(servInsReq.getVfModuleInstanceId());
                    aq.setVolumeGroupId(servInsReq.getVolumeGroupInstanceId());
                    aq.setVnfType(vnfType);

                }

                if (ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
                    aq.setConfigurationId(servInsReq.getConfigurationId());
                    aq.setConfigurationName(requestInfo.getInstanceName());
                }
                if (requestScope.equalsIgnoreCase(ModelType.instanceGroup.name())) {
                    aq.setInstanceGroupId(servInsReq.getInstanceGroupId());
                    aq.setInstanceGroupName(requestInfo.getInstanceName());
                }

                if (ModelType.vnf.name().equalsIgnoreCase(requestScope)
                        || ModelType.cnf.name().equalsIgnoreCase(requestScope)) {
                    if (requestInfo != null) {
                        aq.setVnfName(requestInfo.getInstanceName());
                    }
                    if (null != servInsReq.getRequestDetails()) {
                        RelatedInstanceList[] instanceList = servInsReq.getRequestDetails().getRelatedInstanceList();

                        if (instanceList != null) {

                            for (RelatedInstanceList relatedInstanceList : instanceList) {

                                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.service)) {
                                    aq.setVnfType(vnfType);
                                }
                            }
                        }
                    }
                }
            }

            aq.setRequestBody(originalRequestJSON);

            aq.setRequestStatus(status.toString());
            aq.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        } catch (Exception e) {
            logger.error("Exception when creation record request", e);

            if (!status.equals(Status.FAILED)) {
                throw e;
            }
        }
        return aq;
    }

    public InfraActiveRequests createRequestObject(TasksRequest taskRequest, Action action, String requestId,
            Status status, String originalRequestJSON) {
        InfraActiveRequests aq = new InfraActiveRequests();
        try {

            org.onap.so.apihandlerinfra.tasksbeans.RequestInfo requestInfo =
                    taskRequest.getRequestDetails().getRequestInfo();
            aq.setRequestId(requestId);
            aq.setRequestAction(action.name());
            aq.setRequestUrl(MDC.get(LogConstants.HTTP_URL));

            Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());

            aq.setStartTime(startTimeStamp);
            if (requestInfo != null) {

                if (requestInfo.getSource() != null) {
                    aq.setSource(requestInfo.getSource());
                }

                if (requestInfo.getRequestorId() != null) {
                    aq.setRequestorId(requestInfo.getRequestorId());
                }
            }

            aq.setRequestBody(originalRequestJSON);
            aq.setRequestStatus(status.toString());
            aq.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);

        } catch (Exception e) {
            logger.error("Exception when creation record request", e);

            if (!status.equals(Status.FAILED)) {
                throw e;
            }
        }
        return aq;
    }

    public void createErrorRequestRecord(Status status, String requestId, String errorMessage, Actions action,
            String requestScope, String requestJSON, String serviceInstanceId, ServiceInstancesRequest sir) {
        try {
            InfraActiveRequests request = new InfraActiveRequests(requestId);
            Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
            request.setStartTime(startTimeStamp);
            request.setRequestStatus(status.toString());
            request.setStatusMessage(errorMessage);
            request.setProgress((long) 100);
            request.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
            request.setRequestAction(action.toString());
            request.setRequestScope(requestScope);
            request.setRequestBody(requestJSON);
            if (serviceInstanceId != null) {
                request.setServiceInstanceId(serviceInstanceId);
            }
            Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
            request.setEndTime(endTimeStamp);
            request.setRequestUrl(MDC.get(LogConstants.HTTP_URL));
            if (sir != null) {
                if (sir.getRequestDetails() != null && sir.getRequestDetails().getRequestInfo() != null) {
                    request.setRequestorId(sir.getRequestDetails().getRequestInfo().getRequestorId());
                    request.setSource(sir.getRequestDetails().getRequestInfo().getSource());
                    if (ModelType.service.name().equalsIgnoreCase(requestScope)) {
                        if (sir.getRequestDetails().getRequestInfo().getInstanceName() != null) {
                            request.setServiceInstanceName(sir.getRequestDetails().getRequestInfo().getInstanceName());
                        }
                    }
                }
            }
            requestsDbClient.save(request);
        } catch (Exception e) {
            logger.error("Exception when updating record in DB", e);
            logger.debug("Exception: ", e);
        }
    }

    public Response buildResponse(int httpResponseCode, String errorCode, InfraActiveRequests inProgress) {
        return buildResponseWithError(httpResponseCode, errorCode, inProgress, null);
    }

    public Response buildResponseWithError(int httpResponseCode, String errorCode, InfraActiveRequests inProgress,
            String errorString) {


        // Log the failed request into the MSO Requests database

        return Response.status(httpResponseCode).entity(null).build();

    }

    public Response buildResponseFailedValidation(int httpResponseCode, String exceptionMessage) {

        return Response.status(httpResponseCode).entity(null).build();
    }


    public String getServiceType(VnfInputs vnfInputs) {
        if (vnfInputs.getServiceType() != null)
            return vnfInputs.getServiceType();
        if (vnfInputs.getServiceId() != null)
            return vnfInputs.getServiceId();
        return null;
    }

    public long translateStatus(RequestStatusType status) {
        switch (status) {
            case FAILED:
            case COMPLETE:
                return Constants.PROGRESS_REQUEST_COMPLETED;
            case IN_PROGRESS:
                return Constants.PROGRESS_REQUEST_IN_PROGRESS;
            default:
                return 0;
        }
    }

    public static String domToStr(Document doc) {
        if (doc == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.STANDALONE, "yes");
            NodeList nl = doc.getDocumentElement().getChildNodes();
            DOMSource source = null;
            for (int x = 0; x < nl.getLength(); x++) {
                Node e = nl.item(x);
                if (e instanceof Element) {
                    source = new DOMSource(e);
                    break;
                }
            }
            if (source != null) {
                t.transform(source, sr);

                String s = sw.toString();
                return s;
            }

            return null;

        } catch (Exception e) {
            logger.error("Exception in domToStr", e);
        }
        return null;
    }

    public void addBPMNSpecificInputs(VnfRequest vnfReq, VnfInputs vnfInputs, String personaModelId,
            String personaModelVersion, Boolean isBaseVfModule, String vnfPersonaModelId,
            String vnfPersonaModelVersion) {
        vnfInputs.setPersonaModelId(personaModelId);
        vnfInputs.setPersonaModelVersion(personaModelVersion);
        vnfInputs.setIsBaseVfModule(isBaseVfModule);
        vnfInputs.setVnfPersonaModelId(vnfPersonaModelId);
        vnfInputs.setVnfPersonaModelVersion(vnfPersonaModelVersion);

        vnfReq.setVnfInputs(vnfInputs);

    }

    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    public String getRequestJSON(ServiceInstancesRequest sir) throws IOException {

        logger.debug("building sir from object {}", sir);
        String requestJSON = nonNullMapper.writeValueAsString(sir);

        // Perform mapping from VID-style modelInfo fields to ASDC-style modelInfo fields

        logger.debug("REQUEST JSON before mapping: {}", requestJSON);
        // modelUuid = modelVersionId
        requestJSON = requestJSON.replaceAll("\"modelVersionId\":", "\"modelUuid\":");
        // modelCustomizationUuid = modelCustomizationId
        requestJSON = requestJSON.replaceAll("\"modelCustomizationId\":", "\"modelCustomizationUuid\":");
        // modelInstanceName = modelCustomizationName
        requestJSON = requestJSON.replaceAll("\"modelCustomizationName\":", "\"modelInstanceName\":");
        // modelInvariantUuid = modelInvariantId
        requestJSON = requestJSON.replaceAll("\"modelInvariantId\":", "\"modelInvariantUuid\":");
        logger.debug("REQUEST JSON after mapping: {}", requestJSON);

        return requestJSON;
    }


    public boolean getAlacarteFlag(ServiceInstancesRequest sir) {
        if (sir.getRequestDetails().getRequestParameters() != null
                && sir.getRequestDetails().getRequestParameters().getALaCarte() != null)
            return sir.getRequestDetails().getRequestParameters().getALaCarte();

        return false;
    }


    public String getNetworkType(ServiceInstancesRequest sir, String requestScope) {
        if (requestScope.equalsIgnoreCase(ModelType.network.name()))
            return sir.getRequestDetails().getModelInfo().getModelName();
        else
            return null;
    }


    public String getServiceInstanceType(ServiceInstancesRequest sir, String requestScope) {
        if (requestScope.equalsIgnoreCase(ModelType.network.name()))
            return sir.getRequestDetails().getModelInfo().getModelName();
        else
            return null;
    }


    public String getSDCServiceModelVersion(ServiceInstancesRequest sir) {
        String sdcServiceModelVersion = null;
        if (sir.getRequestDetails().getRelatedInstanceList() != null)
            for (RelatedInstanceList relatedInstanceList : sir.getRequestDetails().getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo();
                if (relatedInstanceModelInfo.getModelType().equals(ModelType.service))
                    sdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion();
            }
        return sdcServiceModelVersion;
    }


    public String getVfModuleType(ServiceInstancesRequest sir, String requestScope) {

        String vnfType;
        String vfModuleType = null;
        String vfModuleModelName;
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();
        String serviceModelName = null;
        String vnfModelName = null;
        String volumeGroupId = null;

        if (instanceList == null) {
            return null;
        }
        for (RelatedInstanceList relatedInstanceList : instanceList) {
            RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
            ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo();

            if (relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
                serviceModelName = relatedInstanceModelInfo.getModelName();
            } else if (relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)
                    && !(ModelType.configuration.name().equalsIgnoreCase(requestScope))) {
                vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
            } else if (relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {
                volumeGroupId = relatedInstance.getInstanceId();
            }
        }
        if (requestScope.equalsIgnoreCase(ModelType.vfModule.name())) {
            vfModuleModelName = modelInfo.getModelName();
            vnfType = serviceModelName + "/" + vnfModelName;
            vfModuleType = vnfType + "::" + vfModuleModelName;
            sir.setVolumeGroupInstanceId(volumeGroupId);
        }

        return vfModuleType;

    }

    public String getVnfType(ServiceInstancesRequest sir, String requestScope) {

        String vnfType = null;
        RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();
        String serviceModelName = null;
        String vnfModelName = null;
        String volumeGroupId = null;

        if (instanceList == null) {
            return null;
        }
        for (RelatedInstanceList relatedInstanceList : instanceList) {
            RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
            ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo();

            if (relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
                serviceModelName = relatedInstanceModelInfo.getModelName();
            } else if (relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)
                    && !(ModelType.configuration.name().equalsIgnoreCase(requestScope))) {
                vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
            } else if (relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {
                volumeGroupId = relatedInstance.getInstanceId();
            }
        }

        if (requestScope.equalsIgnoreCase(ModelType.volumeGroup.name())) {
            vnfType = serviceModelName + "/" + vnfModelName;
        } else if (requestScope.equalsIgnoreCase(ModelType.vfModule.name())) {
            vnfType = serviceModelName + "/" + vnfModelName;
            sir.setVolumeGroupInstanceId(volumeGroupId);
        } else if (requestScope.equalsIgnoreCase(ModelType.vnf.name())
                || requestScope.equalsIgnoreCase(ModelType.cnf.name()))
            vnfType = serviceModelName + "/" + sir.getRequestDetails().getModelInfo().getModelCustomizationName();

        return vnfType;
    }

    protected String getTenantNameFromAAI(ServiceInstancesRequest servInsReq) {
        String tenantName = null;
        if (servInsReq.getRequestDetails() != null && servInsReq.getRequestDetails().getCloudConfiguration() != null
                && servInsReq.getRequestDetails().getCloudConfiguration().getTenantId() != null) {
            Tenant tenant = aaiDataRet.getTenant(servInsReq.getRequestDetails().getCloudConfiguration().getCloudOwner(),
                    servInsReq.getRequestDetails().getCloudConfiguration().getLcpCloudRegionId(),
                    servInsReq.getRequestDetails().getCloudConfiguration().getTenantId());
            if (tenant != null) {
                tenantName = tenant.getTenantName();
            }
        }
        return tenantName;
    }

    protected String getProductFamilyNameFromAAI(ServiceInstancesRequest servInsReq) {
        String productFamilyName = null;
        if (servInsReq.getRequestDetails() != null && servInsReq.getRequestDetails().getRequestInfo() != null
                && servInsReq.getRequestDetails().getRequestInfo().getProductFamilyId() != null) {
            org.onap.aai.domain.yang.Service service =
                    aaiDataRet.getService(servInsReq.getRequestDetails().getRequestInfo().getProductFamilyId());
            if (service != null) {
                productFamilyName = service.getServiceDescription();
            }
        }
        return productFamilyName;
    }

}
