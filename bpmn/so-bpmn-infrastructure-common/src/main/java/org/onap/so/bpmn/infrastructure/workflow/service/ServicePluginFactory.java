/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.runtime.Execution;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ServicePluginFactory {

	// SOTN calculate route
	public static final String OOF_Default_EndPoint = "http://192.168.1.223:8443/oof/sotncalc";

	public static final String Third_SP_Default_EndPoint = "http://192.168.1.223:8443/sp/resourcemgr/querytps";
	
	public static final String Inventory_OSS_Default_EndPoint = "http://192.168.1.199:8443/oss/inventory";

	private static final int DEFAULT_TIME_OUT = 60000;

	static JsonUtils jsonUtil = new JsonUtils();

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ServicePluginFactory.class);

	private static ServicePluginFactory instance;
	

	public static synchronized ServicePluginFactory getInstance() {
		if (null == instance) {
			instance = new ServicePluginFactory();
		}
		return instance;
	}

	private ServicePluginFactory() {

	}

	
	private String getInventoryOSSEndPoint(){
		return UrnPropertiesReader.getVariable("mso.service-plugin.inventory-oss-endpoint", Inventory_OSS_Default_EndPoint);
	}
	private String getThirdSPEndPoint(){
		return UrnPropertiesReader.getVariable("mso.service-plugin.third-sp-endpoint", Third_SP_Default_EndPoint);
	}

	private String getOOFCalcEndPoint(){
		return UrnPropertiesReader.getVariable("mso.service-plugin.oof-calc-endpoint", OOF_Default_EndPoint);
	}
	
	public ServiceDecomposition doProcessSiteLocation(ServiceDecomposition serviceDecomposition, String uuiRequest) {		
		ServiceDecomposition serviceDecompositionforLocal = serviceDecomposition;

		if (isSiteLocationLocal(serviceDecomposition, uuiRequest)) {
			return serviceDecomposition;
		}

		List<Resource> addResourceList = serviceDecomposition.getServiceResources();
		for (Resource resource : addResourceList) {
			String resourcemodelName = resource.getModelInfo().getModelName();
			if (!StringUtils.containsIgnoreCase(resourcemodelName, "sp-partner")) {
				serviceDecompositionforLocal.deleteResource(resource);
				break;
			}
			if (!StringUtils.containsIgnoreCase(resourcemodelName, "sppartner")) {
				serviceDecompositionforLocal.deleteResource(resource);
				break;
			}
		}

		return serviceDecompositionforLocal;
	}

	public boolean isSiteLocationLocal(ServiceDecomposition serviceDecomposition, String uuiRequest) {
        boolean isSiteLocationLocal = true;

        String serviceModelName = serviceDecomposition.getModelInfo().getModelName();
        String serviceParameters = JsonUtils.getJsonValue(uuiRequest, "service.parameters");
    	String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs");
    	JSONObject inputParameters = new JSONObject(requestInputs);
    	
    	if(StringUtils.containsIgnoreCase(serviceModelName, "site") && inputParameters.has("location"))
		{
			Object location  = inputParameters.get("location");
			JSONObject locationObj = new JSONObject(location);
			String locationONAP = queryLocationFromInventoryOSS(locationObj);
			if(StringUtils.containsIgnoreCase(locationONAP, "remote")) {
				isSiteLocationLocal = false;
			}
		}

		return isSiteLocationLocal;
	}
	
	private String queryLocationFromInventoryOSS(JSONObject locationObj) {
		String reqContent = getJsonString(locationObj);
		String url = getInventoryOSSEndPoint();
		String responseContent = sendRequest(url, "POST", reqContent);
		String locationONAP = "";
		if (null != responseContent) {
			locationONAP = getJsonObject(responseContent, String.class);
		}
		return locationONAP;
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

	private String preProcessSOTNService(ServiceDecomposition serviceDecomposition, String uuiRequest) {
		Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
		Map<String, Object> serviceObject = (Map<String, Object>) uuiObject.get("service");
		Map<String, Object> serviceParametersObject = (Map<String, Object>) serviceObject.get("parameters");
		Map<String, Object> serviceRequestInputs = (Map<String, Object>) serviceParametersObject.get("requestInputs");
		List<Object> resources = (List<Object>) serviceParametersObject.get("resources");
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
			Map<String, Object> resourceRequestInputs = (Map<String, Object>) resourceParametersObject
					.get("requestInputs");
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
		vpnRequestInputs.put("src-client-signal", srcClientSignal);
		vpnRequestInputs.put("dst-client-signal", dstClientSignal);

		// Now we need to query terminal points from SP resourcemgr system.
		List<Object> locationTerminalPointList = queryTerminalPointsFromServiceProviderSystem(srcLocation, dstLocation);
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

	private Map<String, Object> getVPNResourceRequestInputs(List<Object> resources) {
		for (Object resource : resources) {
			Map<String, Object> resourceObject = (Map<String, Object>) resource;
			Map<String, Object> resourceParametersObject = (Map<String, Object>) resourceObject.get("parameters");
			Map<String, Object> resourceRequestInputs = (Map<String, Object>) resourceParametersObject
					.get("requestInputs");
			for (Entry<String, Object> entry : resourceRequestInputs.entrySet()) {
				if (entry.getKey().toLowerCase().contains("vpntype")) {
					return resourceRequestInputs;
				}
			}
		}
		return null;
	}
	
	public static void main(String args[]){
		String str = "restconf/config/GENERIC-RESOURCE-API:services/service/eca7e542-12ba-48de-8544-fac59303b14e/service-data/networks/network/aec07806-1671-4af2-b722-53c8e320a633/network-data/";
		
		int index1 = str.indexOf("/network/");
		int index2 = str.indexOf("/network-data");
		
		String str1 = str.substring(index1 + "/network/".length(), index2);
		System.out.println(str1);
		
	}

	private String doSOTNServiceHoming(ServiceDecomposition serviceDecomposition, String uuiRequest) {
		// query the route for the service.
		Map<String, Object> uuiObject = getJsonObject(uuiRequest, Map.class);
		Map<String, Object> serviceObject = (Map<String, Object>) uuiObject.get("service");
		Map<String, Object> serviceParametersObject = (Map<String, Object>) serviceObject.get("parameters");
		Map<String, Object> serviceRequestInputs = (Map<String, Object>) serviceParametersObject.get("requestInputs");
		Map<String, Object> oofQueryObject = new HashMap<>();
		List<Object> resources = (List<Object>) serviceParametersObject.get("resources");
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
		vpnRequestInputs.putAll(returnRoute);
		String newRequest = getJsonString(uuiObject);
		return newRequest;
	}
	
	private Map<String, Object> getReturnRoute(List<Object> returnList){
		Map<String, Object> returnRoute = new HashMap<>();
		for(Object returnVpn :returnList){
			Map<String, Object> returnVpnInfo = (Map<String, Object>) returnVpn;
		    String accessTopoId = (String)returnVpnInfo.get("access-topology-id");
			if("100".equals(accessTopoId)){
				returnRoute.putAll(returnVpnInfo);
			}
			else if("101".equals(accessTopoId)){
				for(String key : returnVpnInfo.keySet()){
					returnRoute.put("domain1-" + key, returnVpnInfo.get(key));
				}
			}
			else if("102".equals(accessTopoId)){
				for(String key : returnVpnInfo.keySet()){
					returnRoute.put("domain2-" + key, returnVpnInfo.get(key));
				}
			}
			else{
				for(String key : returnVpnInfo.keySet()){
					returnRoute.put("domain" + accessTopoId +"-" + key, returnVpnInfo.get(key));
				}
			}
		}
		return returnRoute;
	}

	private Map<String, Object> getResourceParams(Execution execution, String resourceCustomizationUuid,
			String serviceParameters) {
		List<String> resourceList = jsonUtil.StringArrayToList(execution,
				(String) JsonUtils.getJsonValue(serviceParameters, "resources"));
		// Get the right location str for resource. default is an empty array.
		String resourceInputsFromUui = "";
		for (String resource : resourceList) {
			String resCusUuid = (String) JsonUtils.getJsonValue(resource, "resourceCustomizationUuid");
			if (resourceCustomizationUuid.equals(resCusUuid)) {
				String resourceParameters = JsonUtils.getJsonValue(resource, "parameters");
				resourceInputsFromUui = JsonUtils.getJsonValue(resourceParameters, "requestInputs");
			}
		}
		Map<String, Object> resourceInputsFromUuiMap = getJsonObject(resourceInputsFromUui, Map.class);
		return resourceInputsFromUuiMap;
	}

	public static <T> T getJsonObject(String jsonstr, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		try {
			return mapper.readValue(jsonstr, type);
		} catch (IOException e) {
			LOGGER.error(MessageEnum.RA_NS_EXC, "", "", MsoLogger.ErrorCode.BusinessProcesssError,
					"fail to unMarshal json", e);
		}
		return null;
	}

	public static String getJsonString(Object srcObj) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		String jsonStr = null;
		try {
			jsonStr = mapper.writeValueAsString(srcObj);
		} catch (JsonProcessingException e) {
			LOGGER.debug("SdcToscaParserException", e);
			e.printStackTrace();
		}
		return jsonStr;
	}

	private static String sendRequest(String url, String methodType, String content) {
		
		String msbUrl = url;
		HttpRequestBase method = null;
		HttpResponse httpResponse = null;

		try {
			int timeout = DEFAULT_TIME_OUT;

			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
					.setConnectionRequestTimeout(timeout).build();

			HttpClient client = HttpClientBuilder.create().build();

			if ("POST".equals(methodType.toUpperCase())) {
				HttpPost httpPost = new HttpPost(msbUrl);
				httpPost.setConfig(requestConfig);
				httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
				method = httpPost;
			} else if ("PUT".equals(methodType.toUpperCase())) {
				HttpPut httpPut = new HttpPut(msbUrl);
				httpPut.setConfig(requestConfig);
				httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
				method = httpPut;
			} else if ("GET".equals(methodType.toUpperCase())) {
				HttpGet httpGet = new HttpGet(msbUrl);
				httpGet.setConfig(requestConfig);
				method = httpGet;
			} else if ("DELETE".equals(methodType.toUpperCase())) {
				HttpDelete httpDelete = new HttpDelete(msbUrl);
				httpDelete.setConfig(requestConfig);
				method = httpDelete;
			}

			// now have no auth
			// String userCredentials =
			// SDNCAdapterProperties.getEncryptedProperty(Constants.SDNC_AUTH_PROP,
			// Constants.DEFAULT_SDNC_AUTH, Constants.ENCRYPTION_KEY);
			// String authorization = "Basic " +
			// DatatypeConverter.printBase64Binary(userCredentials.getBytes());
			// method.setHeader("Authorization", authorization);

			httpResponse = client.execute(method);
			String responseContent = null;
			if (null != httpResponse && httpResponse.getEntity() != null) {
				try {
					responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != method) {
				method.reset();
			}
			method = null;
			return responseContent;

		} catch (SocketTimeoutException | ConnectTimeoutException e) {
			return null;

		} catch (Exception e) {
			return null;

		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (Exception e) {
				}
			}
			if (method != null) {
				try {
					method.reset();
				} catch (Exception e) {

				}
			}
		}
	}
}
