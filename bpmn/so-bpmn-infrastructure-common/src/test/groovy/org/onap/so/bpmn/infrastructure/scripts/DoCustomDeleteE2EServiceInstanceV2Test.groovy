package org.onap.so.bpmn.infrastructure.scripts

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Spy
import org.onap.aai.domain.yang.AllottedResource
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import javax.ws.rs.core.UriBuilder
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class DoCustomDeleteE2EServiceInstanceV2Test extends MsoGroovyTest {

    @Spy
    DoCustomDeleteE2EServiceInstanceV2 doCustomDeleteE2EServiceInstanceV2

    @Before
    void init(){
        super.init("DoCustomDeleteE2EServiceInstanceV2")
        Mockito.when(doCustomDeleteE2EServiceInstanceV2.getAAIClient()).thenReturn(client)
    }

    @Test
    void testPrepareServiceDeleteResource(){
        when(mockExecution.getVariable("serviceInstance")).thenReturn(FileUtil.readResourceFile("__files/AAI/ServiceInstanceWithAR.json"))
        def relink = "/aai/v11/business/customers/customer/testCustIdInfra/service-subscriptions/service-subscription/HNPORTAL/service-instances/service-instance/testServiceInstanceIdPortalPri/allotted-resources/allotted-resource/testAllottedResourceIdPortalPri"
        AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.ALLOTTED_RESOURCE, UriBuilder.fromPath(relink).build())
        AllottedResource ar = new AllottedResource();
        ar.setId("ar1")
        ar.setType("ar")
        ar.setRole("ar")
        ar.setResourceVersion("1234")
        when(client.get(AllottedResource.class,uri)).thenReturn(Optional.of(ar))
        doCustomDeleteE2EServiceInstanceV2.prepareServiceDeleteResource(mockExecution)
        Mockito.verify(client, times(1)).get(AllottedResource.class,uri)
    }
}
