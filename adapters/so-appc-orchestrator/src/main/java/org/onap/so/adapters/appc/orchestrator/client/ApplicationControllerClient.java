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

package org.onap.so.adapters.appc.orchestrator.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
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
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerOrchestratorException;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerSupport;
import org.onap.so.adapters.appc.orchestrator.client.StatusCategory;
import org.onap.appc.client.lcm.model.Payload;
import org.onap.appc.client.lcm.model.Status;
import org.onap.appc.client.lcm.model.ZULU;

@Component
public class ApplicationControllerClient {
    @Autowired
    public Environment env;

    public static final String DEFAULT_CONTROLLER_TYPE = "APPC";

    private static final String CLIENT_NAME = "MSO";

    protected static final String API_VER = "2.00";
    protected static final String ORIGINATOR_ID = "MSO";
    protected static final int FLAGS_TTL = 65000;
    private static Logger logger = LoggerFactory.getLogger(ApplicationControllerClient.class);

    @Autowired
    private ApplicationControllerSupport appCSupport;

    // APPC gave us an API where the controllerType is configured in the
    // client object, which is not what we asked for. We asked for an API
    // in which the client would have additional methods that could take
    // the controllerType as a parameter, so that we would not need to
    // maintain multiple client objects. This map should be removed when
    // the (hopefully short-term) controllerType becomes obsolete.

    private String controllerType = DEFAULT_CONTROLLER_TYPE;

    private static ConcurrentHashMap<String, LifeCycleManagerStateful> appCClients = new ConcurrentHashMap<>();

    /**
     * Creates an ApplicationControllerClient for the specified controller type.
     *
     * @param controllerType the controller type: "appc" or "sdnc".
     */
    public void setControllerType(String controllerType) {
        if (controllerType == null) {
            controllerType = DEFAULT_CONTROLLER_TYPE;
        }
        this.controllerType = controllerType.toUpperCase();
    }

    /**
     * Gets the controller type.
     *
     * @return the controllertype
     */
    public String getControllerType() {
        return controllerType;
    }

    /**
     * Returns the AppC client object associated with this ApplicationControllerClient. AppC client objects are shared
     * objects. One is created if it does not exist.
     *
     * @return the client object, or null if creation failed
     */
    public LifeCycleManagerStateful getAppCClient() {
        return appCClients.computeIfAbsent(controllerType, k -> createAppCClient(k));
    }

    protected LifeCycleManagerStateful createAppCClient(String controllerType) {

        try {
            if (controllerType == null) {
                controllerType = DEFAULT_CONTROLLER_TYPE;
            }
            controllerType = controllerType.toUpperCase();

            return AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class)
                    .createLifeCycleManagerStateful(new ApplicationContext(), getLCMProperties(controllerType));
        } catch (AppcClientException e) {
            logger.error("Error in getting LifeCycleManagerStateful: {}", e.getMessage(), e);
            // This null value will cause NullPointerException when used later.
            // Error handling could certainly be improved here.
            return null;
        }
    }

    public Status vnfCommand(Action action, String requestId, String vnfId, Optional<String> vserverId,
            Optional<String> request, String controllerType, ApplicationControllerCallback listener, String requestorId)
            throws ApplicationControllerOrchestratorException {
        this.setControllerType(controllerType);
        Status status;
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId(vnfId);
        if (vserverId.isPresent()) {
            actionIdentifiers.setVserverId(vserverId.get());
        }
        Payload payload = null;
        if (request.isPresent()) {
            payload = new Payload(request.get());

        }
        status = runCommand(action, actionIdentifiers, payload, requestId, listener, requestorId);
        if (appCSupport.getCategoryOf(status).equals(StatusCategory.ERROR)) {
            throw new ApplicationControllerOrchestratorException(status.getMessage(), status.getCode());
        } else {
            return status;
        }
    }


    public Status runCommand(Action action, org.onap.appc.client.lcm.model.ActionIdentifiers actionIdentifiers,
            org.onap.appc.client.lcm.model.Payload payload, String requestID, ApplicationControllerCallback listener,
            String requestorId) throws ApplicationControllerOrchestratorException {
        Object requestObject;
        requestObject = createRequest(action, actionIdentifiers, payload, requestID, requestorId);
        appCSupport.logLCMMessage(requestObject);
        LifeCycleManagerStateful client = getAppCClient();
        Method lcmMethod = appCSupport.getAPIMethod(action.name(), client, true);
        try {
            Object response = lcmMethod.invoke(client, requestObject, listener);
            if (response != null) {
                return appCSupport.getStatusFromGenericResponse(response);
            } else {
                return new Status();
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(String.format("%s : %s", "Unable to invoke action", action.toString()), e);
        }
    }

    protected Properties getLCMProperties() {
        return getLCMProperties("appc");
    }

    protected Properties getLCMProperties(String controllerType) {
        Properties properties = new Properties();

        properties.put("topic.read", this.env.getProperty("appc.client.topic.read.name"));
        properties.put("topic.write", this.env.getProperty("appc.client.topic.write"));
        properties.put("SDNC-topic.read", this.env.getProperty("appc.client.topic.sdnc.read"));
        properties.put("SDNC-topic.write", this.env.getProperty("appc.client.topic.sdnc.write"));
        properties.put("topic.read.timeout", this.env.getProperty("appc.client.topic.read.timeout"));
        properties.put("client.response.timeout", this.env.getProperty("appc.client.response.timeout"));
        properties.put("poolMembers", this.env.getProperty("appc.client.poolMembers"));
        properties.put("controllerType", controllerType);
        properties.put("client.key", this.env.getProperty("appc.client.key"));
        properties.put("client.secret", this.env.getProperty("appc.client.secret"));
        properties.put("client.name", CLIENT_NAME);
        properties.put("service", this.env.getProperty("appc.client.service"));
        return properties;
    }

    public Object createRequest(Action action, ActionIdentifiers identifier, Payload payload, String requestId,
            String requestorId) {
        Object requestObject = appCSupport.getInput(action.name());


        try {
            CommonHeader commonHeader = buildCommonHeader(requestId, requestorId);
            requestObject.getClass().getDeclaredMethod("setCommonHeader", CommonHeader.class).invoke(requestObject,
                    commonHeader);
            requestObject.getClass().getDeclaredMethod("setAction", Action.class).invoke(requestObject, action);
            requestObject.getClass().getDeclaredMethod("setActionIdentifiers", ActionIdentifiers.class)
                    .invoke(requestObject, identifier);
            if (payload != null) {
                logger.info("payload in RunCommand: " + payload.getValue());
                requestObject.getClass().getDeclaredMethod("setPayload", Payload.class).invoke(requestObject, payload);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Error building Appc request", e);
        }
        return requestObject;
    }

    protected CommonHeader buildCommonHeader(String requestId, String requestorId) {
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setApiVer(API_VER);
        commonHeader.setOriginatorId(ORIGINATOR_ID);
        commonHeader.setRequestId(requestId == null ? UUID.randomUUID().toString() : requestId);
        commonHeader.setSubRequestId(UUID.randomUUID().toString());
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
