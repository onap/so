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

public class ApplicationControllerClient {

	private static final int ACCEPT_SERIES = 100;
	private static final int ERROR_SERIES = 200;
	private static final int REJECT_SERIES = 300;
	private static final int SUCCESS_SERIES = 400;
	private static final int SUCCESS_STATUS = SUCCESS_SERIES + 0;
	private static final int PARTIAL_SERIES = 500;
	private static final int PARTIAL_SUCCESS_STATUS = PARTIAL_SERIES + 0;

	private final boolean useLCMBypass = false;

	private final String apiVer = "2.00";
	private final String originatorId = "MSO";
	private final int flagsTTL = 65000;
	private final static String clientName = "MSO";

	@Autowired
	public ApplicationControllerSupport appCSupport;

	private LifeCycleManagerStateful client;

	public Status runCommand(Action action, ActionIdentifiers identifier, Flags flags, Payload payload,
			String requestID) throws Exception {
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
			String requestId) throws Exception {
		Object requestObject = appCSupport.getInput(action.name());
		try {
			org.openecomp.appc.client.lcm.model.CommonHeader commonHeader = buildCommonHeader(requestId);
			requestObject.getClass().getDeclaredMethod("setCommonHeader", CommonHeader.class).invoke(requestObject,
					commonHeader);
			requestObject.getClass().getDeclaredMethod("setAction", Action.class).invoke(requestObject, action);
			requestObject.getClass().getDeclaredMethod("setActionIdentifiers", ActionIdentifiers.class)
					.invoke(requestObject, identifier);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new Exception("Error Building AppC Request: " + e.getMessage());
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
