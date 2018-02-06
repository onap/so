/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.appc;

import java.beans.BeanInfo;

import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.openecomp.appc.client.lcm.api.AppcClientServiceFactoryProvider;
import org.openecomp.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.openecomp.appc.client.lcm.api.ApplicationContext;
import org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.openecomp.appc.client.lcm.model.Action;
import org.openecomp.appc.client.lcm.model.ActionIdentifiers;
import org.openecomp.appc.client.lcm.model.AuditOutput;
import org.openecomp.appc.client.lcm.model.CommonHeader;
import org.openecomp.appc.client.lcm.model.Flags;
import org.openecomp.appc.client.lcm.model.Flags.Force;
import org.openecomp.appc.client.lcm.model.Flags.Mode;
import org.openecomp.appc.client.lcm.model.Payload;
import org.openecomp.appc.client.lcm.model.Status;
import org.openecomp.appc.client.lcm.model.ZULU;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.openecomp.mso.logger.MsoLogger;

public class ApplicationControllerClient {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    
	private static final int PARTIAL_SERIES = 500;

	private final String apiVer = "2.00";
	private final String originatorId = "MSO";
	private final int flagsTTL = 65000;
	private final static String clientName = "MSO";

	@Autowired
	public ApplicationControllerSupport appCSupport;

	private LifeCycleManagerStateful client;

	public Status runCommand(Action action, ActionIdentifiers identifier, Flags flags, Payload payload,
			String requestID) throws IllegalAccessException,NoSuchMethodException,AppcClientException,JsonProcessingException,InvocationTargetException {
		Object requestObject = createRequest(action, identifier, flags, payload, requestID);
		client = getAppCClient();
		Method lcmMethod = appCSupport.getAPIMethod(action.name(), client, false);
		appCSupport.logLCMMessage(requestObject);
		Object response = lcmMethod.invoke(client, requestObject);
		return appCSupport.getStatusFromGenericResponse(response);
	}

	public void shutdownclient() {
		AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class)
				.shutdownLifeCycleManager(false);
	}

	public LifeCycleManagerStateful getAppCClient() throws AppcClientException {
		if (client == null)
			client = AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class)
					.createLifeCycleManagerStateful(new ApplicationContext(), getLCMProperties());
		return client;
	}

	private Properties getLCMProperties() {
		return getLCMPropertiesHelper();
	}

	protected Properties getLCMPropertiesHelper() {
		Properties properties = new Properties();
		Map<String, String> globalProperties = PropertyConfiguration.getInstance()
				.getProperties("mso.bpmn.urn.properties");

		properties.put("topic.read", globalProperties.get("appc.topic.read"));
		properties.put("topic.read.timeout", globalProperties.get("appc.topic.read.timeout"));
		properties.put("client.response.timeout", globalProperties.get("appc.client.response.timeout"));
		properties.put("topic.write", globalProperties.get("appc.topic.write"));
		properties.put("poolMembers", globalProperties.get("appc.pool.members"));
		properties.put("client.key", globalProperties.get("appc.client.key"));
		properties.put("client.secret", globalProperties.get("appc.client.secret"));
		properties.put("client.name", clientName);
		return properties;
	}

	public Object createRequest(Action action, ActionIdentifiers identifier, Flags flags, Payload payload,
			String requestId) throws IllegalAccessException,NoSuchMethodException,InvocationTargetException {
		Object requestObject = appCSupport.getInput(action.name());
		try {
			org.openecomp.appc.client.lcm.model.CommonHeader commonHeader = buildCommonHeader(requestId);
			requestObject.getClass().getDeclaredMethod("setCommonHeader", CommonHeader.class).invoke(requestObject,
					commonHeader);
			requestObject.getClass().getDeclaredMethod("setAction", Action.class).invoke(requestObject, action);
			requestObject.getClass().getDeclaredMethod("setActionIdentifiers", ActionIdentifiers.class)
					.invoke(requestObject, identifier);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
		    LOGGER.debug("Exception:", e);
			throw new IllegalAccessException("Error Building AppC Request: " + e.getMessage());
		}
		return requestObject;
	}

	private org.openecomp.appc.client.lcm.model.CommonHeader buildCommonHeader(String requestId) {
		org.openecomp.appc.client.lcm.model.CommonHeader commonHeader = new org.openecomp.appc.client.lcm.model.CommonHeader();
		commonHeader.setApiVer(apiVer);
		commonHeader.setOriginatorId(originatorId);
		commonHeader.setRequestId(requestId == null ? UUID.randomUUID().toString() : requestId);
		commonHeader.setSubRequestId(requestId);
		org.openecomp.appc.client.lcm.model.Flags flags = new org.openecomp.appc.client.lcm.model.Flags();
		String flagsMode = "NORMAL";
		Mode mode = Mode.valueOf(flagsMode);
		flags.setMode(mode);
		String flagsForce = "FALSE";
		Force force = Force.valueOf(flagsForce);
		flags.setForce(force);
		flags.setTtl(flagsTTL);
		commonHeader.setFlags(flags);
		Instant timestamp = Instant.now();
		ZULU zulu = new ZULU(timestamp.toString());
		commonHeader.setTimestamp(zulu);
		return commonHeader;
	}

	public Flags createRequestFlags() {
		Flags flags = new Flags();
		flags.setTtl(6000);
		return flags;
	}
}
