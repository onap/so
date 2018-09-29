package org.onap.so.bpmn.common.scripts

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyObject
import static org.mockito.Mockito.when

class CreateAAIVfModuleTest extends MsoGroovyTest{

    @Spy
    CreateAAIVfModule createAAIVfModule ;

    @Before
    public void init() throws IOException {
        super.init("CreateAAIVfModule")
        MockitoAnnotations.initMocks(this);
        when(createAAIVfModule.getAAIClient()).thenReturn(client)
    }

    @Test
    void testQueryAAIForGenericVnf(){
        when(mockExecution.getVariable("CAAIVfMod_vnfId")).thenReturn("vnfId1")
        when(mockExecution.getVariable("CAAIVfMod_vnfName")).thenReturn("vnfName")
        Optional<GenericVnf> expectedResponse = mockAAIGenericVnf("vnfId1")
        createAAIVfModule.queryAAIForGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_queryGenericVnfResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_queryGenericVnfResponse", expectedResponse.get())
    }

    @Test
    void testQueryAAIForGenericVnfNotFound(){
        when(mockExecution.getVariable("CAAIVfMod_vnfId")).thenReturn("vnfIdNotFound")
        when(mockExecution.getVariable("CAAIVfMod_vnfName")).thenReturn("vnfName")
        mockAAIGenericVnfNotFound("vnfIdNotFound")
        createAAIVfModule.queryAAIForGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_queryGenericVnfResponseCode", 404)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_queryGenericVnfResponse", "Generic Vnf not Found!")
    }


    @Test
    void testCreateGenericVnf(){
        when(mockExecution.getVariable("CAAIVfMod_vnfName")).thenReturn("vnfName")
        Mockito.doNothing().when(client).create(any(AAIResourceUri.class),anyObject())
        createAAIVfModule.createGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createGenericVnfResponseCode", 201)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createGenericVnfResponse","Vnf Created")
    }



    @Test
    void testCreateVfModule(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())

        when(mockExecution.getVariable("CAAIVfMod_personaId")).thenReturn("model1")
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("vfModuleName")
        Mockito.doNothing().when(client).create(any(AAIResourceUri.class),anyObject())
        createAAIVfModule.createVfModule(mockExecution,false)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createVfModuleResponseCode", 201)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createVfModuleResponse","Vf Module Created")
    }

    @Test
    void testParseForAddOnModule(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("newVfModule")
        createAAIVfModule.parseForAddOnModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
    }

    @Test
    void testParseForAddOnModuleTrue(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("testVfModuleNameGWPrim")
        createAAIVfModule.parseForAddOnModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_moduleExists", true)
    }

    @Test
    void testParseForBaseModule(){
        Optional<GenericVnf> genericVnfOps = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json")
        GenericVnf genericVnf = genericVnfOps.get()
        genericVnf.getVfModules().getVfModule().remove(0)
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf)
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("newVfModule")
        createAAIVfModule.parseForBaseModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
    }

    @Test
    void testParseForBaseModuleConflict(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("testVfModuleNameGWPrim")
        when(mockExecution.getVariable("CAAIVfMod_baseModuleConflict")).thenReturn(true)
        createAAIVfModule.parseForBaseModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_baseModuleConflict", true)
    }

    @Test
    void testParseForBaseModuleExists(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("newVfModule")
        when(mockExecution.getVariable("CAAIVfMod_baseModuleConflict")).thenReturn(false)
        createAAIVfModule.parseForBaseModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_baseModuleConflict", true)
    }

    @Test
    void testCreateVfModuleBase(){
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn("vfModuleName")
        Mockito.doNothing().when(client).create(any(AAIResourceUri.class),anyObject())
        createAAIVfModule.createVfModule(mockExecution,true)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createVfModuleResponseCode", 201)
        Mockito.verify(mockExecution).setVariable("CAAIVfMod_createVfModuleResponse","Vf Module Created")
    }
}
