/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.onap.aai.domain.yang.LogicalLink;
import org.onap.aai.domain.yang.LogicalLinks;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ServicePluginFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServicePluginFactory.class);
    private static final ObjectMapper wrapMapper;
    private static final ObjectMapper nonWrapMapper;
    private static String OOF_DEFAULT_ENDPOINT;
    private static String THIRD_SP_DEFAULT_ENDPOINT;
    private static String INVENTORY_OSS_DEFAULT_ENDPOINT;
    private static final int DEFAULT_TIME_OUT = 60000;

    static JsonUtils jsonUtil = new JsonUtils();

    static {
        wrapMapper = new ObjectMapper();
        wrapMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        nonWrapMapper = new ObjectMapper();
        nonWrapMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }


    private static ServicePluginFactory instance;

    private static final String CUSTOM_RESOURCE_TP = "custom-resource-tp";
    private static final String VS_MONITORED = "VS_assured";
    private static final String VS_UNMONITORED = "VS_besteffort";
    private static final String TS_MONITORED = "TS1";
    private static final String TS_UNMONITORED = "TS2";
    private static final String[] CUSTOM_TP_LIST =
            new String[] {VS_MONITORED, VS_UNMONITORED, TS_MONITORED, TS_UNMONITORED};

    static {
        try (InputStream is = ClassLoader.class.getResourceAsStream("/application.properties")) {
            if (null != is) {
                Properties prop = new Properties();
                prop.load(is);
                OOF_DEFAULT_ENDPOINT = prop.getProperty("oof.default.endpoint");
                THIRD_SP_DEFAULT_ENDPOINT = prop.getProperty("third.sp.default.endpoint");
                INVENTORY_OSS_DEFAULT_ENDPOINT = prop.getProperty("inventory.oss.default.endpoint");
            } else {
                logger.error("Failed to load property file, Either property file is missing or empty!");
            }
        } catch (IOException e) {
            logger.error("Failed to load property file!", e);
        }
    }

    private ServicePluginFactory() {

    }

    public static synchronized ServicePluginFactory getInstance() {
        if (null == instance) {
            instance = new ServicePluginFactory();
        }
        return instance;
    }


    private String getInventoryOSSEndPoint() {
        return UrnPropertiesReader.getVariable("mso.service-plugin.inventory-oss-endpoint",
                INVENTORY_OSS_DEFAULT_ENDPOINT);
    }

    private String getThirdSPEndPoint() {
        return UrnPropertiesReader.getVariable("mso.service-plugin.third-sp-endpoint", THIRD_SP_DEFAULT_ENDPOINT);
    }

    private String getOOFCalcEndPoint() {
        return UrnPropertiesReader.getVariable("mso.service-plugin.oof-calc-endpoint", OOF_DEFAULT_ENDPOINT);
    }

    @SuppressWarnings("unchecked")
    public String doProcessSiteLocation(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        if (!isNeedProcessSite(uuiRequest)) {
            return uuiRequest;
        }

        Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
        if (uuiObject == null) {
            return uuiRequest;
        }
        Map<String, Object> serviceObject =
                (Map<String, Object>) uuiObject.getOrDefault("service", Collections.emptyMap());
        Map<String, Object> serviceParametersObject =
                (Map<String, Object>) serviceObject.getOrDefault("parameters", Collections.emptyMap());
        Map<String, Object> serviceRequestInputs =
                (Map<String, Object>) serviceParametersObject.getOrDefault("requestInputs", Collections.emptyMap());
        List<Object> resources =
                (List<Object>) serviceParametersObject.getOrDefault("resources", Collections.emptyList());

        if (isSiteLocationLocal(serviceRequestInputs, resources)) {
            // resources changed : added TP info
            return getJsonString(uuiObject);
        }

        List<Resource> addResourceList = new ArrayList<>();
        addResourceList.addAll(serviceDecomposition.getServiceResources());

        serviceDecomposition.setVnfResources(null);
        serviceDecomposition.setAllottedResources(null);
        serviceDecomposition.setNetworkResources(null);
        serviceDecomposition.setConfigResources(null);
        for (Resource resource : addResourceList) {
            String resourcemodelName = resource.getModelInfo().getModelName();
            if (StringUtils.containsIgnoreCase(resourcemodelName, "sppartner")) {
                // change serviceDecomposition
                serviceDecomposition.addResource(resource);
                break;
            }
        }

        return uuiRequest;
    }

    private boolean isNeedProcessSite(String uuiRequest) {
        return uuiRequest.toLowerCase().contains("address") && uuiRequest.toLowerCase().contains("clientsignal");
    }

    @SuppressWarnings("unchecked")
    private boolean isSiteLocationLocal(Map<String, Object> serviceRequestInputs, List<Object> resources) {
        Map<String, Object> tpInfoMap = getTPforVPNAttachment(serviceRequestInputs);

        if (tpInfoMap.isEmpty()) {
            return true;
        }
        String host = (String) tpInfoMap.get("host");
        logger.info("host string from tpinfo:{}", host);
        // host is empty means TP is in local, not empty means TP is in remote ONAP
        if (!host.isEmpty()) {
            return false;
        }

        Map<String, Object> accessTPInfo = new HashMap<>();
        accessTPInfo.put("access-provider-id", tpInfoMap.get("access-provider-id"));
        accessTPInfo.put("access-client-id", tpInfoMap.get("access-client-id"));
        accessTPInfo.put("access-topology-id", tpInfoMap.get("access-topology-id"));
        accessTPInfo.put("access-node-id", tpInfoMap.get("access-node-id"));
        accessTPInfo.put("access-ltp-id", tpInfoMap.get("access-ltp-id"));

        // change resources
        boolean flgResourceFound = false;
        String resourceName = (String) tpInfoMap.get("resourceName");
        for (Object curResource : resources) {
            Map<String, Object> resource = (Map<String, Object>) curResource;
            String curResourceName = (String) resource.get("resourceName");
            curResourceName = curResourceName.replaceAll(" ", "");
            if (resourceName.equalsIgnoreCase(curResourceName)) {
                flgResourceFound = true;
                logger.info("found match to add site tp info using uui template resource name");
                putResourceRequestInputs(resource, accessTPInfo);
                break;
            }
        }

        if (!flgResourceFound) {
            String attacmentResName = UrnPropertiesReader.getVariable("sp-partner.attachment-resource-name");
            for (Object curResource : resources) {
                Map<String, Object> resource = (Map<String, Object>) curResource;
                String curResourceName = (String) resource.get("resourceName");

                if (attacmentResName.equals(curResourceName)) {
                    logger.info("found match to add site tp info using customized resource name");
                    putResourceRequestInputs(resource, accessTPInfo);
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTPforVPNAttachment(Map<String, Object> serviceRequestInputs) {
        Object location = null;
        Object clientSignal = null;
        String vpnAttachmentResourceName = null;

        // support R2 uuiReq and R1 uuiReq
        // logic for R2 uuiRequest params in service level
        for (Entry<String, Object> entry : serviceRequestInputs.entrySet()) {
            String key = entry.getKey();
            if (key.toLowerCase().contains("address")) {
                location = entry.getValue();
            }
            if (key.toLowerCase().contains("clientsignal")) {
                clientSignal = entry.getValue();
                vpnAttachmentResourceName = key.substring(0, key.indexOf("_"));
            }
        }

        Map<String, Object> tpInfoMap = new HashMap<>();

        // Site resource has location param and SOTNAttachment resource has clientSignal param
        if (location == null || clientSignal == null) {
            return tpInfoMap;
        }

        // Query terminal points from InventoryOSS system by location.
        String locationAddress = (String) location;
        List<Object> locationTPList = queryAccessTPbyLocationFromInventoryOSS(locationAddress);
        if (locationTPList != null && !locationTPList.isEmpty()) {
            for (Object tp : locationTPList) {
                Map<String, Object> tpJson = (Map<String, Object>) tp;
                String loc = (String) tpJson.get("location");
                if (StringUtils.equalsIgnoreCase(locationAddress, loc)) {
                    tpInfoMap = tpJson;
                    // add resourceName
                    tpInfoMap.put("resourceName", vpnAttachmentResourceName);
                    logger.info("*** we will try to find resourcename({}) to add resource input ***",
                            vpnAttachmentResourceName);
                    break;
                }
            }
            logger.info("Get Terminal TP from InventoryOSS: {}", tpInfoMap);
            return tpInfoMap;
        }

        return tpInfoMap;
    }

    @SuppressWarnings("unchecked")
    private List<Object> queryAccessTPbyLocationFromInventoryOSS(String locationAddress) {
        String url = getInventoryOSSEndPoint();
        url += "/oss/inventory?location=" + UriUtils.encode(locationAddress, "UTF-8");
        String responseContent = sendRequest(url, "GET", "");
        List<Object> accessTPs = new ArrayList<>();
        if (null != responseContent) {
            accessTPs = getJsonObject(responseContent, List.class);
        }
        return accessTPs;
    }

    @SuppressWarnings("unchecked")
    private void putResourceRequestInputs(Map<String, Object> resource, Map<String, Object> resourceInputs) {
        Map<String, Object> resourceParametersObject = new HashMap<>();
        Map<String, Object> resourceRequestInputs = new HashMap<>();
        resourceRequestInputs.put("requestInputs", resourceInputs);
        resourceParametersObject.put("parameters", resourceRequestInputs);

        if (resource.containsKey("parameters")) {
            Map<String, Object> resParametersObject = (Map<String, Object>) resource.get("parameters");
            if (resParametersObject.containsKey("requestInputs")) {
                Map<String, Object> resRequestInputs = (Map<String, Object>) resourceRequestInputs.get("requestInputs");
                Map<String, Object> oldRequestInputs = (Map<String, Object>) resParametersObject.get("requestInputs");
                if (oldRequestInputs != null) {
                    oldRequestInputs.putAll(resRequestInputs);
                } else {
                    resParametersObject.put("requestInputs", resRequestInputs);
                }
            } else {
                resParametersObject.putAll(resourceRequestInputs);
            }
        } else {
            resource.putAll(resourceParametersObject);
        }

        return;
    }



    @SuppressWarnings("unchecked")
    public String doTPResourcesAllocation(DelegateExecution execution, String uuiRequest) {
        Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
        if (uuiObject == null) {
            return uuiRequest;
        }
        Map<String, Object> serviceObject =
                (Map<String, Object>) uuiObject.getOrDefault("service", Collections.emptyMap());
        Map<String, Object> serviceParametersObject =
                (Map<String, Object>) serviceObject.getOrDefault("parameters", Collections.emptyMap());
        Map<String, Object> serviceRequestInputs =
                (Map<String, Object>) serviceParametersObject.getOrDefault("requestInputs", Collections.emptyMap());

        if (!isNeedAllocateCrossTPResources(serviceRequestInputs)) {
            return uuiRequest;
        }

        allocateCrossTPResources(execution, serviceRequestInputs);
        return getJsonString(uuiObject);
    }

    @SuppressWarnings("unchecked")
    private boolean isNeedAllocateCrossTPResources(Map<String, Object> serviceRequestInputs) {
        if (serviceRequestInputs.containsKey("CallSource")) {
            String callSource = (String) serviceRequestInputs.get("CallSource");
            if ("ExternalAPI".equalsIgnoreCase(callSource)) {
                return false;
            }
        }
        for (String input : serviceRequestInputs.keySet()) {
            if (input.toLowerCase().contains("sotnconnectivity")) {
                return true;
            }
        }
        return false;
    }

    private void customizeTP(Map<String, Object> crossTps, String svcName, DelegateExecution execution) {
        Optional<String> customType = Arrays.stream(CUSTOM_TP_LIST).filter(svcName::contains).findFirst();
        if (customType.isPresent()) {
            logger.info("customizing TP");
            String localTPs = UrnPropertiesReader.getVariable(CUSTOM_RESOURCE_TP + "." + customType.get() + ".local");
            String remoteTPs = UrnPropertiesReader.getVariable(CUSTOM_RESOURCE_TP + "." + customType.get() + ".remote");

            String localTP = (String) crossTps.get("local-access-ltp-id");
            String remoteTP = (String) crossTps.get("remote-access-ltp-id");

            if (localTPs.contains(localTP) && remoteTPs.contains(remoteTP)) {
                logger.info("using same tp returned from AAI");
                return;
            }

            crossTps.put("local-access-ltp-id", localTPs.split(",")[0]);
            crossTps.put("remote-access-ltp-id", remoteTPs.split(",")[0]);
        }
        logger.info("cross TP info:{}", crossTps);
    }

    @SuppressWarnings("unchecked")
    private void allocateCrossTPResources(DelegateExecution execution, Map<String, Object> serviceRequestInputs) {

        String serviceName = (String) execution.getVariable("serviceInstanceName");
        Map<String, Object> crossTPs = this.getTPsfromAAI(serviceName);
        // customizeTP(crossTPs, serviceName, execution);

        if (crossTPs == null || crossTPs.isEmpty()) {
            serviceRequestInputs.put("local-access-provider-id", "");
            serviceRequestInputs.put("local-access-client-id", "");
            serviceRequestInputs.put("local-access-topology-id", "");
            serviceRequestInputs.put("local-access-node-id", "");
            serviceRequestInputs.put("local-access-ltp-id", "");
            serviceRequestInputs.put("remote-access-provider-id", "");
            serviceRequestInputs.put("remote-access-client-id", "");
            serviceRequestInputs.put("remote-access-topology-id", "");
            serviceRequestInputs.put("remote-access-node-id", "");
            serviceRequestInputs.put("remote-access-ltp-id", "");
        } else {
            serviceRequestInputs.put("local-access-provider-id", crossTPs.get("local-access-provider-id"));
            serviceRequestInputs.put("local-access-client-id", crossTPs.get("local-access-client-id"));
            serviceRequestInputs.put("local-access-topology-id", crossTPs.get("local-access-topology-id"));
            serviceRequestInputs.put("local-access-node-id", crossTPs.get("local-access-node-id"));
            serviceRequestInputs.put("local-access-ltp-id", crossTPs.get("local-access-ltp-id"));
            serviceRequestInputs.put("remote-access-provider-id", crossTPs.get("remote-access-provider-id"));
            serviceRequestInputs.put("remote-access-client-id", crossTPs.get("remote-access-client-id"));
            serviceRequestInputs.put("remote-access-topology-id", crossTPs.get("remote-access-topology-id"));
            serviceRequestInputs.put("remote-access-node-id", crossTPs.get("remote-access-node-id"));
            serviceRequestInputs.put("remote-access-ltp-id", crossTPs.get("remote-access-ltp-id"));
        }
    }

    private LogicalLink selectLogicalLink(List<LogicalLink> logicalLinks, String svcName) {
        Optional<String> customType = Arrays.stream(CUSTOM_TP_LIST).filter(svcName::contains).findFirst();
        if (customType.isPresent()) {

            String[] allowedList =
                    UrnPropertiesReader.getVariable(CUSTOM_RESOURCE_TP + "." + customType.get() + ".local").split(",");

            for (String localTp : allowedList) {
                for (LogicalLink link : logicalLinks) {
                    for (Relationship relationship : link.getRelationshipList().getRelationship()) {
                        if (relationship.getRelatedTo().equals("p-interface")
                                && relationship.getRelatedLink().contains("-ltpId-" + localTp)
                                && link.getOperationalStatus().equalsIgnoreCase("up")) {
                            logger.info("linkname:{} is matching with allowed list", link.getLinkName());
                            return link;
                        }
                    }
                }
            }

            logger.error("There is no matching logical link for allowed list :{}", Arrays.toString(allowedList));
            return null;
        } else {
            logger.info("link customization is not required");
            return logicalLinks.get(0);
        }
    }

    // This method returns Local and remote TPs information from AAI
    public Map getTPsfromAAI(String serviceName) {
        Map<String, Object> tpInfo = new HashMap<>();

        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().logicalLinks());
        AAIResourcesClient client = new AAIResourcesClient();
        Optional<LogicalLinks> result = client.get(LogicalLinks.class, uri);

        if (result.isPresent()) {
            LogicalLinks links = result.get();
            LogicalLink link = selectLogicalLink(links.getLogicalLink(), serviceName);

            if (link != null) {
                boolean isRemoteLink = false;
                logger.info("processing link :{}", link.getLinkName());
                AAIResultWrapper wrapper = new AAIResultWrapper(link);
                Optional<Relationships> optRelationships = wrapper.getRelationships();
                List<AAIResourceUri> pInterfaces = new ArrayList<>();
                if (optRelationships.isPresent()) {
                    Relationships relationships = optRelationships.get();
                    if (!relationships.getRelatedUris(Types.EXT_AAI_NETWORK).isEmpty()) {
                        isRemoteLink = true;
                    }
                    pInterfaces.addAll(relationships.getRelatedUris(Types.P_INTERFACE));
                    if (isRemoteLink) {
                        // find remote p interface
                        AAIResourceUri localTP = null;
                        AAIResourceUri remoteTP = null;

                        AAIResourceUri pInterface0 = pInterfaces.get(0);

                        if (isRemotePInterface(client, pInterface0)) {
                            remoteTP = pInterfaces.get(0);
                            localTP = pInterfaces.get(1);
                        } else {
                            localTP = pInterfaces.get(0);
                            remoteTP = pInterfaces.get(1);
                        }

                        tpInfo = getTPInfo(client, localTP, remoteTP);
                    }
                }
            }
        }
        return tpInfo;
    }

    private Map<String, Object> getTPInfo(AAIResourcesClient client, AAIResourceUri localTP, AAIResourceUri remoteTP) {

        Map<String, Object> tpInfo = new HashMap<>();

        if (localTP != null && remoteTP != null) {
            // give local tp
            String tpUrl = localTP.build().toString();
            String localNodeId = tpUrl.split("/")[4];
            tpInfo.put("local-access-node-id", localNodeId);

            logger.info("Get info for local TP :{}", localNodeId);
            Optional<Pnf> optLocalPnf = client.get(Pnf.class,
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(localNodeId)));


            getTpInfoFromLocalTp(tpInfo, optLocalPnf);

            String ltpIdStr = tpUrl.substring(tpUrl.lastIndexOf("/") + 1);
            if (ltpIdStr.contains("-")) {
                tpInfo.put("local-access-ltp-id", ltpIdStr.substring(ltpIdStr.lastIndexOf("-") + 1));
            }

            // give remote tp
            tpUrl = remoteTP.build().toString();
            PInterface intfRemote = client.get(PInterface.class, remoteTP).get();

            String remoteNodeId = tpUrl.split("/")[4];
            tpInfo.put("remote-access-node-id", remoteNodeId);

            logger.info("Get info for remote TP:{}", remoteNodeId);

            String[] networkRefRemote = intfRemote.getNetworkRef().split("-");
            Optional<Pnf> optRemotePnf = client.get(Pnf.class,
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(remoteNodeId)));

            getTpInfoFromRemoteTp(tpInfo, networkRefRemote, optRemotePnf);

            String ltpIdStrR = tpUrl.substring(tpUrl.lastIndexOf("/") + 1);
            if (ltpIdStrR.contains("-")) {
                tpInfo.put("remote-access-ltp-id", ltpIdStrR.substring(ltpIdStr.lastIndexOf("-") + 1));
            }
        }

        return tpInfo;
    }


    private void getTpInfoFromLocalTp(Map<String, Object> tpInfo, Optional<Pnf> optLocalPnf) {
        if (optLocalPnf.isPresent()) {
            Pnf localPnf = optLocalPnf.get();

            for (Relationship rel : localPnf.getRelationshipList().getRelationship()) {
                if (rel.getRelatedTo().equalsIgnoreCase("network-resource")) {
                    String[] networkRef =
                            rel.getRelatedLink().substring(rel.getRelatedLink().lastIndexOf("/") + 1).split("-");
                    if (networkRef.length == 6) {
                        tpInfo.put("local-access-provider-id", networkRef[1]);
                        tpInfo.put("local-access-client-id", networkRef[3]);
                        tpInfo.put("local-access-topology-id", networkRef[5]);
                    }
                }
            }
        }
    }

    private void getTpInfoFromRemoteTp(Map<String, Object> tpInfo, String[] networkRefRemote,
            Optional<Pnf> optRemotePnf) {
        if (optRemotePnf.isPresent()) {
            Pnf remotePnf = optRemotePnf.get();

            for (Relationship rel : remotePnf.getRelationshipList().getRelationship()) {
                if (rel.getRelatedTo().equalsIgnoreCase("network-resource")) {
                    String[] networkRef =
                            rel.getRelatedLink().substring(rel.getRelatedLink().lastIndexOf("/") + 1).split("-");
                    if (networkRef.length == 6) {
                        tpInfo.put("remote-access-provider-id", networkRefRemote[1]);
                        tpInfo.put("remote-access-client-id", networkRefRemote[3]);
                        tpInfo.put("remote-access-topology-id", networkRefRemote[5]);
                    }
                }
            }
        }
    }


    // this method check if pInterface is remote
    private boolean isRemotePInterface(AAIResourcesClient client, AAIResourceUri uri) {

        String uriString = uri.build().toString();

        if (uriString != null) {
            // get the pnfname
            String[] token = uriString.split("/");
            AAIResourceUri parent = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(token[4]));

            AAIResultWrapper wrapper = client.get(parent);
            Optional<Relationships> optRelationships = wrapper.getRelationships();
            if (optRelationships.isPresent()) {
                Relationships relationships = optRelationships.get();

                return !relationships.getRelatedUris(Types.EXT_AAI_NETWORK).isEmpty();
            }
        }

        return false;
    }

    public String preProcessService(ServiceDecomposition serviceDecomposition, String uuiRequest) {

        // now only for sotn
        if (isSOTN(serviceDecomposition, uuiRequest)) {
            // We Need to query the terminalpoint of the VPN by site location
            // info
            return preProcessSOTNService(serviceDecomposition, uuiRequest);
        }
        return uuiRequest;
    }

    public String doServiceHoming(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        // now only for sotn
        if (isSOTN(serviceDecomposition, uuiRequest)) {
            return doSOTNServiceHoming(serviceDecomposition, uuiRequest);
        }
        return uuiRequest;
    }

    private boolean isSOTN(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        // there should be a register platform , we check it very simple here.
        return uuiRequest.contains("clientSignal") && uuiRequest.contains("vpnType");
    }

    @SuppressWarnings("unchecked")
    private String preProcessSOTNService(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
        if (uuiObject == null) {
            return uuiRequest;
        }
        Map<String, Object> serviceObject =
                (Map<String, Object>) uuiObject.getOrDefault("service", Collections.emptyMap());
        Map<String, Object> serviceParametersObject =
                (Map<String, Object>) serviceObject.getOrDefault("parameters", Collections.emptyMap());
        Map<String, Object> serviceRequestInputs =
                (Map<String, Object>) serviceParametersObject.getOrDefault("requestInputs", Collections.emptyMap());
        List<Object> resources =
                (List<Object>) serviceParametersObject.getOrDefault("resources", Collections.emptyList());
        // This is a logic for demo , it could not be finalized to community.
        String srcLocation = "";
        String dstLocation = "";
        String srcClientSignal = "";
        String dstClientSignal = "";
        // support R2 uuiReq and R1 uuiReq
        // logic for R2 uuiRequest params in service level
        for (Entry<String, Object> entry : serviceRequestInputs.entrySet()) {
            if (entry.getKey().toLowerCase().contains("location")) {
                if ("".equals(srcLocation)) {
                    srcLocation = (String) entry.getValue();
                } else if ("".equals(dstLocation)) {
                    dstLocation = (String) entry.getValue();
                }
            }
            if (entry.getKey().toLowerCase().contains("clientsignal")) {
                if ("".equals(srcClientSignal)) {
                    srcClientSignal = (String) entry.getValue();
                } else if ("".equals(dstClientSignal)) {
                    dstClientSignal = (String) entry.getValue();
                }
            }
        }

        // logic for R1 uuiRequest, params in resource level
        for (Object resource : resources) {
            Map<String, Object> resourceObject = (Map<String, Object>) resource;
            Map<String, Object> resourceParametersObject = (Map<String, Object>) resourceObject.get("parameters");
            Map<String, Object> resourceRequestInputs =
                    (Map<String, Object>) resourceParametersObject.get("requestInputs");
            for (Entry<String, Object> entry : resourceRequestInputs.entrySet()) {
                if (entry.getKey().toLowerCase().contains("location")) {
                    if ("".equals(srcLocation)) {
                        srcLocation = (String) entry.getValue();
                    } else if ("".equals(dstLocation)) {
                        dstLocation = (String) entry.getValue();
                    }
                }
                if (entry.getKey().toLowerCase().contains("clientsignal")) {
                    if ("".equals(srcClientSignal)) {
                        srcClientSignal = (String) entry.getValue();
                    } else if ("".equals(dstClientSignal)) {
                        dstClientSignal = (String) entry.getValue();
                    }
                }
            }
        }

        Map<String, Object> vpnRequestInputs = getVPNResourceRequestInputs(resources);
        // here we put client signal to vpn resource inputs
        if (null != vpnRequestInputs) {
            vpnRequestInputs.put("src-client-signal", srcClientSignal);
            vpnRequestInputs.put("dst-client-signal", dstClientSignal);
        }


        // Now we need to query terminal points from SP resourcemgr system.
        List<Object> locationTerminalPointList = queryTerminalPointsFromServiceProviderSystem(srcLocation, dstLocation);
        if (locationTerminalPointList != null) {
            Map<String, Object> tpInfoMap = (Map<String, Object>) locationTerminalPointList.get(0);

            serviceRequestInputs.put("inner-src-access-provider-id", tpInfoMap.get("access-provider-id"));
            serviceRequestInputs.put("inner-src-access-client-id", tpInfoMap.get("access-client-id"));
            serviceRequestInputs.put("inner-src-access-topology-id", tpInfoMap.get("access-topology-id"));
            serviceRequestInputs.put("inner-src-access-node-id", tpInfoMap.get("access-node-id"));
            serviceRequestInputs.put("inner-src-access-ltp-id", tpInfoMap.get("access-ltp-id"));
            tpInfoMap = (Map<String, Object>) locationTerminalPointList.get(1);

            serviceRequestInputs.put("inner-dst-access-provider-id", tpInfoMap.get("access-provider-id"));
            serviceRequestInputs.put("inner-dst-access-client-id", tpInfoMap.get("access-client-id"));
            serviceRequestInputs.put("inner-dst-access-topology-id", tpInfoMap.get("access-topology-id"));
            serviceRequestInputs.put("inner-dst-access-node-id", tpInfoMap.get("access-node-id"));
            serviceRequestInputs.put("inner-dst-access-ltp-id", tpInfoMap.get("access-ltp-id"));
        }
        String newRequest = getJsonString(uuiObject);
        return newRequest;
    }

    private List<Object> queryTerminalPointsFromServiceProviderSystem(String srcLocation, String dstLocation) {
        Map<String, String> locationSrc = new HashMap<>();
        locationSrc.put("location", srcLocation);
        Map<String, String> locationDst = new HashMap<>();
        locationDst.put("location", dstLocation);
        List<Map<String, String>> locations = new ArrayList<>();
        locations.add(locationSrc);
        locations.add(locationDst);
        List<Object> returnList = new ArrayList<>();
        String reqContent = getJsonString(locations);
        String url = getThirdSPEndPoint();
        String responseContent = sendRequest(url, "POST", reqContent);
        if (null != responseContent) {
            returnList = getJsonObject(responseContent, List.class);
        }
        return returnList;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getVPNResourceRequestInputs(List<Object> resources) {
        for (Object resource : resources) {
            Map<String, Object> resourceObject = (Map<String, Object>) resource;
            Map<String, Object> resourceParametersObject = (Map<String, Object>) resourceObject.get("parameters");
            Map<String, Object> resourceRequestInputs =
                    (Map<String, Object>) resourceParametersObject.get("requestInputs");
            for (Entry<String, Object> entry : resourceRequestInputs.entrySet()) {
                if (entry.getKey().toLowerCase().contains("vpntype")) {
                    return resourceRequestInputs;
                }
            }
        }
        return null;
    }

    public static void main(String args[]) {
        String str =
                "restconf/config/GENERIC-RESOURCE-API:services/service/eca7e542-12ba-48de-8544-fac59303b14e/service-data/networks/network/aec07806-1671-4af2-b722-53c8e320a633/network-data/";

        int index1 = str.indexOf("/network/");
        int index2 = str.indexOf("/network-data");

        String str1 = str.substring(index1 + "/network/".length(), index2);
        System.out.println(str1);

    }

    @SuppressWarnings("unchecked")
    private String doSOTNServiceHoming(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        // query the route for the service.
        Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
        if (uuiObject == null) {
            return uuiRequest;
        }
        Map<String, Object> serviceObject =
                (Map<String, Object>) uuiObject.getOrDefault("service", Collections.emptyMap());
        Map<String, Object> serviceParametersObject =
                (Map<String, Object>) serviceObject.getOrDefault("parameters", Collections.emptyMap());
        Map<String, Object> serviceRequestInputs =
                (Map<String, Object>) serviceParametersObject.getOrDefault("requestInputs", Collections.emptyMap());
        Map<String, Object> oofQueryObject = new HashMap<>();
        List<Object> resources =
                (List<Object>) serviceParametersObject.getOrDefault("resources", Collections.emptyList());
        oofQueryObject.put("src-access-provider-id", serviceRequestInputs.get("inner-src-access-provider-id"));
        oofQueryObject.put("src-access-client-id", serviceRequestInputs.get("inner-src-access-client-id"));
        oofQueryObject.put("src-access-topology-id", serviceRequestInputs.get("inner-src-access-topology-id"));
        oofQueryObject.put("src-access-node-id", serviceRequestInputs.get("inner-src-access-node-id"));
        oofQueryObject.put("src-access-ltp-id", serviceRequestInputs.get("inner-src-access-ltp-id"));
        oofQueryObject.put("dst-access-provider-id", serviceRequestInputs.get("inner-dst-access-provider-id"));
        oofQueryObject.put("dst-access-client-id", serviceRequestInputs.get("inner-dst-access-client-id"));
        oofQueryObject.put("dst-access-topology-id", serviceRequestInputs.get("inner-dst-access-topology-id"));
        oofQueryObject.put("dst-access-node-id", serviceRequestInputs.get("inner-dst-access-node-id"));
        oofQueryObject.put("dst-access-ltp-id", serviceRequestInputs.get("inner-dst-access-ltp-id"));
        String oofRequestReq = getJsonString(oofQueryObject);
        String url = getOOFCalcEndPoint();
        String responseContent = sendRequest(url, "POST", oofRequestReq);

        List<Object> returnList = new ArrayList<>();
        if (null != responseContent) {
            returnList = getJsonObject(responseContent, List.class);
        }
        // in demo we have only one VPN. no cross VPNs, so get first item.
        Map<String, Object> returnRoute = getReturnRoute(returnList);
        Map<String, Object> vpnRequestInputs = getVPNResourceRequestInputs(resources);
        if (null != vpnRequestInputs) {
            vpnRequestInputs.putAll(returnRoute);
        }
        return getJsonString(uuiObject);
    }

    private Map<String, Object> getReturnRoute(List<Object> returnList) {
        Map<String, Object> returnRoute = new HashMap<>();
        for (Object returnVpn : returnList) {
            Map<String, Object> returnVpnInfo = (Map<String, Object>) returnVpn;
            String accessTopoId = (String) returnVpnInfo.get("access-topology-id");
            if ("100".equals(accessTopoId)) {
                returnRoute.putAll(returnVpnInfo);
            } else if ("101".equals(accessTopoId)) {
                for (String key : returnVpnInfo.keySet()) {
                    returnRoute.put("domain1-" + key, returnVpnInfo.get(key));
                }
            } else if ("102".equals(accessTopoId)) {
                for (String key : returnVpnInfo.keySet()) {
                    returnRoute.put("domain2-" + key, returnVpnInfo.get(key));
                }
            } else {
                for (String key : returnVpnInfo.keySet()) {
                    returnRoute.put("domain" + accessTopoId + "-" + key, returnVpnInfo.get(key));
                }
            }
        }
        return returnRoute;
    }

    private Map<String, Object> getResourceParams(Execution execution, String resourceCustomizationUuid,
            String serviceParameters) {
        List<String> resourceList =
                jsonUtil.StringArrayToList(execution, JsonUtils.getJsonValue(serviceParameters, "resources"));
        // Get the right location str for resource. default is an empty array.
        String resourceInputsFromUui = "";
        for (String resource : resourceList) {
            String resCusUuid = JsonUtils.getJsonValue(resource, "resourceCustomizationUuid");
            if (resourceCustomizationUuid.equals(resCusUuid)) {
                String resourceParameters = JsonUtils.getJsonValue(resource, "parameters");
                resourceInputsFromUui = JsonUtils.getJsonValue(resourceParameters, "requestInputs");
            }
        }
        Map<String, Object> resourceInputsFromUuiMap = getJsonObject(resourceInputsFromUui, Map.class);
        return resourceInputsFromUuiMap;
    }

    private static <T> T getJsonObject(String jsonstr, Class<T> type) {

        try {
            return wrapMapper.readValue(jsonstr, type);
        } catch (IOException e) {
            logger.error("{} {} fail to unMarshal json", MessageEnum.RA_NS_EXC.toString(),
                    ErrorCode.BusinessProcessError.getValue(), e);
        }
        return null;
    }

    public static String getJsonString(Object srcObj) {
        String jsonStr = null;
        try {
            jsonStr = nonWrapMapper.writeValueAsString(srcObj);
        } catch (JsonProcessingException e) {
            logger.debug("SdcToscaParserException", e);
        }
        return jsonStr;
    }

    private static String sendRequest(String url, String methodType, String content) {

        String msbUrl = url;
        HttpRequestBase method = null;
        HttpResponse httpResponse = null;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            int timeout = DEFAULT_TIME_OUT;

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();

            if ("POST".equalsIgnoreCase(methodType)) {
                HttpPost httpPost = new HttpPost(msbUrl);
                httpPost.setConfig(requestConfig);
                httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPost;
            } else if ("PUT".equalsIgnoreCase(methodType)) {
                HttpPut httpPut = new HttpPut(msbUrl);
                httpPut.setConfig(requestConfig);
                httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPut;
            } else if ("GET".equalsIgnoreCase(methodType)) {
                HttpGet httpGet = new HttpGet(msbUrl);
                httpGet.setConfig(requestConfig);
                httpGet.addHeader("X-FromAppId", "MSO");
                httpGet.addHeader("Accept", "application/json");
                method = httpGet;
            } else if ("DELETE".equalsIgnoreCase(methodType)) {
                HttpDelete httpDelete = new HttpDelete(msbUrl);
                httpDelete.setConfig(requestConfig);
                method = httpDelete;
            }

            httpResponse = client.execute(method);
            String responseContent = null;
            if (null != httpResponse && httpResponse.getEntity() != null) {
                try {
                    responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                } catch (ParseException e) {
                    logger.debug("ParseException in sendrequest", e);
                } catch (IOException e) {
                    logger.debug("IOException in sendrequest", e);
                }
            }
            if (null != method) {
                method.reset();
            }
            method = null;
            return responseContent;

        } catch (Exception e) {
            logger.debug("Exception in sendRequest", e);
            return null;

        } finally {
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                } catch (Exception e) {
                    logger.debug("Exception while executing finally block", e);
                }
            }
            if (method != null) {
                try {
                    method.reset();
                } catch (Exception e) {
                    logger.debug("Exception while executing finally block", e);
                }
            }
        }
    }
}
