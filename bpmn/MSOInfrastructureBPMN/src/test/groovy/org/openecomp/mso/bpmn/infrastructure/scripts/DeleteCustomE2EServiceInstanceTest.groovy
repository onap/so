package org.openecomp.mso.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.infrastructure.scripts.DeleteCustomE2EServiceInstance
import org.openecomp.mso.bpmn.mock.FileUtil
import org.openecomp.mso.bpmn.vcpe.scripts.GroovyTestBase

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when

class DeleteCustomE2EServiceInstanceTest extends GroovyTestBase {

    private static String request

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(GroovyTestBase.PORT)

    String Prefix = "CVRCS_"
    String RbType = "DCRENI_"

    @BeforeClass
    public static void setUpBeforeClass() {
        request = FileUtil.readResourceFile("__files/InfrastructureFlows/DeleteCustomE2EService.json")
    }

    @Before
    public void init()
    {
        MockitoAnnotations.initMocks(this)
    }

    public DeleteCustomE2EServiceInstanceTest(){
        super("DeleteCustomE2EServiceInstance")
    }
    @Test
	@Ignore // 1802 merge
    public void preProcessRequestTest () {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        mex.setVariable("isDebugLogEnabled","true")
        instance.preProcessRequest(mex);

        verify(mex).getVariable(GroovyTestBase.DBGFLAG)

        verify(mex).setVariable("globalSubscriberId", "38829939920000")
        verify(mex).setVariable("operationId", "59960003992")
        verify(mex).setVariable("URN_mso_adapters_openecomp_db_endpoint", "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")
    }

    @Test
    public void sendSyncResponseTest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.sendSyncResponse(mex)
        verify(mex).setVariable("DeleteCustomE2EServiceInstanceWorkflowResponseSent", "true")
    }

    @Test
    public void prepareCompletionRequestTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.prepareCompletionRequest(mex)
        String msoComplitionRequest = FileUtil.readResourceFile("__files/GenericFlows/MsoCompletionRequest.xml")
        //verify(mex).setVariable("completionRequest", msoComplitionRequest)
    }

    @Test
    public void sendSyncErrorTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.sendSyncError(mex)

    }

    @Test
    public void prepareFalloutRequest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.prepareFalloutRequest(mex)
        String requestInfo =
                """<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>null</request-id>
					<action>DELETE</action>
					<source>null</source>
				   </request-info>"""
        //verify(mex).setVariable("falloutRequest", requestInfo)
    }

    @Test
    public void processJavaExceptionTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.processJavaException()
    }


    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceType")).thenReturn("VoLTE")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("requestAction")).thenReturn("ra")
        when(mex.getVariable("operationId")).thenReturn("59960003992")
    }
}
