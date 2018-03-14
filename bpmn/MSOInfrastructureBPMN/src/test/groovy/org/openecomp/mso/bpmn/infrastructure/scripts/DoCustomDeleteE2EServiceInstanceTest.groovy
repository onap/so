package org.openecomp.mso.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.openecomp.mso.bpmn.infrastructure.scripts.DoCustomDeleteE2EServiceInstance
import org.openecomp.mso.bpmn.mock.FileUtil
import org.openecomp.mso.bpmn.vcpe.scripts.GroovyTestBase

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when

class DoCustomDeleteE2EServiceInstanceTest extends GroovyTestBase {

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
    public DoCustomDeleteE2EServiceInstanceTest(){
        super("DoCustomDeleteE2EServiceInstance")
    }

    @Test
    public void preProcessRequestTest(){

        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)

        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preProcessRequest(mex)
        verify(mex).setVariable("sdncCallbackUrl", "/mso/sdncadapter/")
        verify(mex).setVariable("siParamsXml", "")
    }

    @Ignore
    @Test
    public void postProcessAAIGETTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("GENGS_SuccessIndicator")).thenReturn(true)

        String aaiGetResponse = FileUtil.readResourceFile("__files/GenericFlows/aaiGetResponse.xml")
        when(mex.getVariable("GENGS_service")).thenReturn(aaiGetResponse)
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.postProcessAAIGET(mex)
		// TODO: what to test here?
//        verify(mex).setVariable("subscriptionServiceType", "e2eserviceInstance/delete")
    }

    @Ignore
    @Test
    public void preInitResourcesOperStatusTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("serviceRelationShip")).thenReturn("[{\"resourceInstanceId\":\"3333\",\"resourceType\":\"overlay\"},{\"resourceInstanceId\":\"4444\",\"resourceType\":\"underlay\"},{\"resourceInstanceId\":\"1111\",\"resourceType\":\"vIMS\"},{\"resourceInstanceId\":\"222\",\"resourceType\":\"vEPC\"}]")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preInitResourcesOperStatus(mex)
		// TODO: what to test here?
//        verify(mex).setVariable("CVFMI_dbAdapterEndpoint", "http://localhost:8080/mso")
    }

    @Test
    public void preResourceDeleteTest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("serviceRelationShip")).thenReturn("[{\"resourceInstanceId\":\"3333\",\"resourceType\":\"overlay\"},{\"resourceInstanceId\":\"4444\",\"resourceType\":\"underlay\"},{\"resourceInstanceId\":\"1111\",\"resourceType\":\"vIMS\"},{\"resourceInstanceId\":\"222\",\"resourceType\":\"vEPC\"}]")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preResourceDelete(mex,"overlay")
        verify(mex).setVariable("resourceType", "overlay")
    }

    @Ignore
    @Test
    public void postProcessSDNCDeleteTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn("true")
        when(mex.getVariable("DDELSI_sdncResponseSuccess")).thenReturn("true")
        when(mex.getVariable("prefix")).thenReturn("DDELSI_")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        String response = FileUtil.readResourceFile("__files/GenericFlows/SDNCDeleteResponse.xml")
        String method = "deleteE2E";
        instance.postProcessSDNCDelete(mex, response, method)
		// TODO: what to test here?
//        verify(mex).setVariable("DDELSI_sdncRequestDataResponseCode", "0")
    }

    @Test
    public void postProcessAAIDELTest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("GENDS_SuccessIndicator")).thenReturn("true")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.postProcessAAIDEL(mex)
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceType")).thenReturn("VoLTE")
        when(mex.getVariable("serviceInstanceId")).thenReturn("e151059a-d924-4629-845f-264db19e50b4")
        when(mex.getVariable("requestAction")).thenReturn("ra")
        when(mex.getVariable("operationId")).thenReturn("59960003992")
        when(mex.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("/mso/sdncadapter/")
        when(mex.getVariable("GENGS_FoundIndicator")).thenReturn("true")
        when(mex.getVariable("GENGS_siResourceLink")).thenReturn("/service-subscription/e2eserviceInstance/delete/service-instances/")
        when(mex.getVariable("globalSubscriberId")).thenReturn("4993921112123")
        when(mex.getVariable("GENGS_service")).thenReturn("test3434")
        when(mex.getVariable("URN_mso_adapters_openecomp_db_endpoint")).thenReturn("http://localhost:8080/mso")
    }
}
