package org.onap.so.bpmn.infrastructure.process;

import com.google.protobuf.Struct;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseBPMNTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

/**
 * Basic Integration test for ServiceLevelUpgrade.bpmn workflow.
 */
public class ServiceLevelUpgradeTest extends BaseBPMNTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;

    private static final String TEST_PROCESSINSTANCE_KEY = "ServiceLevelUpgrade";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private static final Map<String, Object> executionVariables = new HashMap();
    private static final String REQUEST_ID = "50ae41ad-049c-4fe2-9950-539f111120f5";
    private static final String SERVICE_INSTANCE_ID = "5df8b6de-2083-11e7-93ae-92361f002676";
    private final String[] actionNames = new String[5];
    private final String CLASSNAME = getClass().getSimpleName();
    private String requestObject;
    private String responseObject;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() throws IOException {
        actionNames[0] = "healthCheck";
        actionNames[1] = "preCheck";
        actionNames[2] = "downloadNESw";
        actionNames[3] = "activateNESw";
        actionNames[4] = "postCheck";

        executionVariables.clear();

        requestObject = FileUtil.readResourceFile("request/" + CLASSNAME + ".json");
        responseObject = FileUtil.readResourceFile("response/" + CLASSNAME + ".json");

        executionVariables.put("bpmnRequest", requestObject);
        executionVariables.put("requestId", REQUEST_ID);
        executionVariables.put("serviceInstanceId", SERVICE_INSTANCE_ID);


        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        executionVariables.put("isAsyncProcess", "true");
        executionVariables.put(ExecutionVariableNames.PRC_CUSTOMIZATION_UUID, "38dc9a92-214c-11e7-93ae-92361f002680");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamudaInput
         */
        executionVariables.put(ExecutionVariableNames.PNF_CORRELATION_ID, "PNFDemo");
    }


    @Test
    public void workflow_validInput_expectedOutput() throws InterruptedException {

        mockCatalogDb();
        mockRequestDb();
        mockAai();

        final String msoRequestId = UUID.randomUUID().toString();
        executionVariables.put(ExecutionVariableNames.MSO_REQUEST_ID, msoRequestId);

        final String testBusinessKey = UUID.randomUUID().toString();
        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, executionVariables);

        int waitCount = 10;
        while (!isProcessInstanceEnded() && waitCount >= 0) {
            Thread.sleep(WORKFLOW_WAIT_TIME);
            waitCount--;
        }

        // Layout is to reflect the bpmn visual layout
        assertThat(pi).isEnded().hasPassedInOrder("Event_02mc8tr", "Activity_18vue7u", "Activity_0qgmx7a",
                "Activity_09bqns0", "Activity_0n17xou", "Gateway_1nr51kr", "Activity_0snmatn", "Activity_1q4o9fx",
                "Gateway_02fectw", "Activity_0ft7fa2", "Gateway_1vq11i7", "Activity_1n4rk7m", "Activity_1lz38px",
                "Event_12983th");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertThat(detailedMessages.size() == 5);
        int count = 0;
        try {
            for (ExecutionServiceInput eSI : detailedMessages) {
                for (String action : actionNames) {
                    if (action.equals(eSI.getActionIdentifiers().getActionName())
                            && eSI.getCommonHeader().getRequestId().equals(msoRequestId)) {
                        checkWithActionName(eSI, action);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("PNFSoftwareUpgrade request exception", e);
        }
        assertThat(count == actionNames.length);
    }

    private boolean isProcessInstanceEnded() {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(TEST_PROCESSINSTANCE_KEY)
                .singleResult() == null;
    }

    private void checkWithActionName(ExecutionServiceInput executionServiceInput, String action) {

        logger.info("Checking the " + action + " request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the response/PnfHealthCheck_catalogdb.json.
         */
        assertThat(actionIdentifiers.getBlueprintName()).isEqualTo("test_pnf_software_upgrade_restconf");
        assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo("1.0.0");
        assertThat(actionIdentifiers.getActionName()).isEqualTo(action);
        assertThat(actionIdentifiers.getMode()).isEqualTo("async");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow(action + "-request").getStructValue();

        assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue()).isEqualTo("PNFDemo");
        Struct propertiesStruct = requeststruct.getFieldsOrThrow(action + "-properties").getStructValue();

        assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo("PNFDemo");
        assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue())
                .isEqualTo("d88da85c-d9e8-4f73-b837-3a72a431622b");
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo("38dc9a92-214c-11e7-93ae-92361f002680");
    }

    private void mockAai() {

        final String sIUrl =
                "/business/customers/customer/5df8b6de-2083-11e7-93ae-92361f002676/service-subscriptions/service-subscription/pNF/service-instances/service-instance/ETE_Customer_807c7a02-249c-4db8-9fa9-bee973fe08ce";
        final String aaiPnfEntry = FileUtil.readResourceFile("response/Pnf_aai.json");
        final String aaiServiceInstanceEntry = FileUtil.readResourceFile("response/Service_instance_aai.json");

        /**
         * PUT the PNF correlation ID to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PnfDemo")));

        /**
         * Get the PNF entry from AAI.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")).willReturn(okJson(aaiPnfEntry)));

        /**
         * Post the pnf to AAI
         */
        wireMockServer.stubFor(post(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));

        /**
         * Get the Service Instance ID to AAI.
         */
        wireMockServer.stubFor(get(urlEqualTo("/aai/" + VERSION + sIUrl)).willReturn(okJson(aaiServiceInstanceEntry)));
    }

    private void mockRequestDb() {
        /**
         * Update Request DB
         */
        wireMockServer.stubFor(put(urlEqualTo("/infraActiveRequests/" + REQUEST_ID)));

    }

    /**
     * Mock the catalobdb rest interface.
     */
    private void mockCatalogDb() {

        String catalogdbClientResponse = FileUtil.readResourceFile("response/" + CLASSNAME + "_catalogdb.json");


        /**
         * Return valid json for the model UUID in the request file.
         */
        wireMockServer
                .stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=d88da85c-d9e8-4f73-b837-3a72a431622b"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=fe41489e-1563-46a3-b90a-1db629e4375b"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        wireMockServer.stubFor(get(urlEqualTo(
                "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=d88da85c-d9e8-4f73-b837-3a72a431622b"))
                        .willReturn(okJson(catalogdbClientResponse)));
    }

}
