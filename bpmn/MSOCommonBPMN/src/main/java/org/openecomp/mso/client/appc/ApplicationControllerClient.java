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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.appc.client.lcm.api.AppcClientServiceFactoryProvider;
import org.onap.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.onap.appc.client.lcm.api.ApplicationContext;
import org.onap.appc.client.lcm.api.LifeCycleManagerStateful;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.CommonHeader;
import org.onap.appc.client.lcm.model.Flags;
import org.onap.appc.client.lcm.model.Flags.Force;
import org.onap.appc.client.lcm.model.Flags.Mode;
import org.onap.appc.client.lcm.model.Payload;
import org.onap.appc.client.lcm.model.Status;
import org.onap.appc.client.lcm.model.ZULU;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;

public class ApplicationControllerClient {
	
	public static final String DEFAULT_CONTROLLER_TYPE = "appc";

	private static final String CLIENT_NAME = "MSO";

	private static final String API_VER = "2.00";
	private static final String ORIGINATOR_ID = "MSO";
	private static final int FLAGS_TTL = 65000;
	protected final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();

	@Autowired
	public ApplicationControllerSupport appCSupport;

	// APPC gave us an API where the controllerType is configured in the
	// client object, which is not what we asked for. We asked for an API
	// in which the client would have additional methods that could take
	// the controllerType as a parameter, so that we would not need to
	// maintain multiple client objects.  This map should be removed when
	// the (hopefully short-term) controllerType becomes obsolete.

	private final String controllerType;

	private static ConcurrentHashMap<String, LifeCycleManagerStateful> appCClients = new ConcurrentHashMap<>();

	/**
	 * Creates an ApplicationControllerClient for communication with APP-C.
	 */
	public ApplicationControllerClient() {
		this(DEFAULT_CONTROLLER_TYPE);
	}
	
	/**
	 * Creates an ApplicationControllerClient for the specified controller type.
	 * @param controllerType the controller type: "appc" or "sdnc".
	 */
	public ApplicationControllerClient(String controllerType) {
		this.controllerType = controllerType;
		appCSupport = new ApplicationControllerSupport();
	}
	
	/**
	 * Gets the controller type.
	 * @return the controllertype
	 */
	public String getControllerType() {
		return controllerType;
	}

	/**
	 * Returns the AppC client object associated with this ApplicationControllerClient.
	 * AppC client objects are shared objects.  One is created if it does not exist.
	 * @return the client object, or null if creation failed
	 */
	public LifeCycleManagerStateful getAppCClient() {
		return appCClients.computeIfAbsent(controllerType, k -> createAppCClient(k));
	}

	protected LifeCycleManagerStateful createAppCClient(String controllerType) {
		try {
			return AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class)
					.createLifeCycleManagerStateful(new ApplicationContext(), getLCMProperties(controllerType));
		} catch (AppcClientException e) {
			auditLogger.log(Level.ERROR, "Error in getting LifeCycleManagerStateful: ", e, e.getMessage());
			// This null value will cause NullPointerException when used later.
			// Error handling could certainly be improved here.
			return null;
		}
	}

	public Status runCommand(Action action, org.onap.appc.client.lcm.model.ActionIdentifiers actionIdentifiers,
			org.onap.appc.client.lcm.model.Payload payload, String requestID)
			throws ApplicationControllerOrchestratorException {
		Object requestObject = createRequest(action, actionIdentifiers, payload, requestID);
		appCSupport.logLCMMessage(requestObject);
		LifeCycleManagerStateful client = getAppCClient();
		Method lcmMethod = appCSupport.getAPIMethod(action.name(), client, false);
		try {
			Object response = lcmMethod.invoke(client, requestObject);
			return appCSupport.getStatusFromGenericResponse(response);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(String.format("%s : %s", "Unable to invoke action", action.toString()), e);
		}
	}

	protected Properties getLCMProperties() {
		return getLCMProperties("appc");
	}
	
	protected Properties getLCMProperties(String controllerType) {
		Properties properties = new Properties();
		Map<String, String> globalProperties = PropertyConfiguration.getInstance()
				.getProperties("mso.bpmn.urn.properties");
		
		properties.put("topic.read", globalProperties.get("appc.client.topic.read"));
		properties.put("topic.write", globalProperties.get("appc.client.topic.write"));
		properties.put("SDNC-topic.read", globalProperties.get("appc.client.topic.sdnc.read"));
		properties.put("SDNC-topic.write", globalProperties.get("appc.client.topic.sdnc.write"));
		properties.put("topic.read.timeout", globalProperties.get("appc.client.topic.read.timeout"));
		properties.put("client.response.timeout", globalProperties.get("appc.client.response.timeout"));
		properties.put("poolMembers", globalProperties.get("appc.client.poolMembers"));
		properties.put("controllerType", controllerType);
		properties.put("client.key", globalProperties.get("appc.client.key"));
		properties.put("client.secret", globalProperties.get("appc.client.secret"));
		properties.put("client.name", CLIENT_NAME);
		properties.put("service", globalProperties.get("appc.client.service"));
		return properties;
	}

	public Object createRequest(Action action, ActionIdentifiers identifier, Payload payload, String requestId) {
		Object requestObject = appCSupport.getInput(action.name());
		try {
			CommonHeader commonHeader = buildCommonHeader(requestId);
			requestObject.getClass().getDeclaredMethod("setCommonHeader", CommonHeader.class).invoke(requestObject,
					commonHeader);
			requestObject.getClass().getDeclaredMethod("setAction", Action.class).invoke(requestObject, action);
			requestObject.getClass().getDeclaredMethod("setActionIdentifiers", ActionIdentifiers.class)
					.invoke(requestObject, identifier);
			if (payload != null) {
				requestObject.getClass().getDeclaredMethod("setPayload", Payload.class).invoke(requestObject, payload);
			}
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			auditLogger.log(Level.ERROR, "Error building Appc request: ", e, e.getMessage());
		}
		return requestObject;
	}

	private CommonHeader buildCommonHeader(String requestId) {
		CommonHeader commonHeader = new CommonHeader();
		commonHeader.setApiVer(API_VER);
		commonHeader.setOriginatorId(ORIGINATOR_ID);
		commonHeader.setRequestId(requestId == null ? UUID.randomUUID().toString() : requestId);
		commonHeader.setSubRequestId(requestId);
		Flags flags = new Flags();
		String flagsMode = "NORMAL";
		Mode mode = Mode.valueOf(flagsMode);
		flags.setMode(mode);
		String flagsForce = "FALSE";
		Force force = Force.valueOf(flagsForce);
		flags.setForce(force);
		flags.setTtl(FLAGS_TTL);
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
