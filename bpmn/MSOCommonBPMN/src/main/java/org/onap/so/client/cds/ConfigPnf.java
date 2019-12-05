package org.onap.so.client.cds;

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Optional;

import static org.onap.so.client.cds.PayloadConstants.*;

public class ConfigPnf implements VirtualComponent {
    private JsonObject pnfObject;
    private String resolutionKey;
    private String blueprintName;
    private String blueprintVersion;
    private String action;
    private DelegateExecution execution;

    @Override
    public Optional<String> buildRequestPayload(String action) throws Exception {

        this.action = action;
        JsonObject pnfObject = buildPropertyObjectForPnf(execution);
        String requestBuilder = "{\"" + action + SEPARATOR + PNF_SCOPE + SEPARATOR + REQUEST + "\":{" + "\""+ RESOLUTION_KEY +"\":"
                + "\"" + resolutionKey + "\","
                + PropertyPayloadBuilder.buildConfigProperties(action, PNF_SCOPE, pnfObject) + '}';

        return Optional.of(requestBuilder);
    }


    @Override
    public String getBlueprintName() {
        return blueprintName;
    }

    @Override
    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (DelegateExecution) executionObject;
    }

    private JsonObject buildPropertyObjectForPnf(final DelegateExecution execution)
            throws Exception {
        pnfObject = new JsonObject();
        resolutionKey = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        blueprintName = String.valueOf(execution.getVariable(PRC_BLUEPRINT_NAME));
        blueprintVersion = String.valueOf(execution.getVariable(PRC_BLUEPRINT_VERSION));

        pnfObject.addProperty("service-instance-id", String.valueOf(execution.getVariable(SERVICE_INSTANCE_ID)));
        pnfObject.addProperty("service-model-uuid", String.valueOf(execution.getVariable(MODEL_UUID)));
        pnfObject.addProperty("pnf-id", String.valueOf(execution.getVariable(PNF_UUID)));
        pnfObject.addProperty("pnf-name", String.valueOf(execution.getVariable(PNF_CORRELATION_ID)));
        pnfObject.addProperty("pnf-customization-uuid", String.valueOf(execution.getVariable(PRC_CUSTOMIZATION_UUID)));

        /**
         * add your customized properties here for specified actions.
         */
        switch(action){
            case "sw-activate":
            case "sw-download":
                pnfObject.addProperty("software-version", String.valueOf(execution.getVariable("softwareVersion")));
                break;
        }
        return pnfObject;
    }
}
