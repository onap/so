package org.onap.so.bpmn.common.scripts

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Spy
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory

import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when

class CreateAAIVfModuleVolumeGroupTest extends MsoGroovyTest{

    @Spy
    CreateAAIVfModuleVolumeGroup createAAIVfModuleVolumeGroup;

    @Before
    void init(){
        super.init("CreateAAIVfModuleVolumeGroup")
        when(createAAIVfModuleVolumeGroup.getAAIClient()).thenReturn(client)
    }

    @Test
    void testGetVfModule (){
        when(mockExecution.getVariable("CAAIVfModVG_vnfId")).thenReturn("Vnf123")
        when(mockExecution.getVariable("CAAIVfModVG_vfModuleId")).thenReturn("VfModule123")
        org.onap.aai.domain.yang.VfModule vfModuleExpected = new  org.onap.aai.domain.yang.VfModule()
        vfModuleExpected.setVfModuleId("VfModule123")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "Vnf123", "VfModule123");
        when(client.get(org.onap.aai.domain.yang.VfModule.class,resourceUri)).thenReturn(Optional.of(vfModuleExpected))
        createAAIVfModuleVolumeGroup.getVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponse", vfModuleExpected)
    }

    @Test
    void testGetVfModuleNotFound (){
        when(mockExecution.getVariable("CAAIVfModVG_vnfId")).thenReturn("Vnf123")
        when(mockExecution.getVariable("CAAIVfModVG_vfModuleId")).thenReturn("VfModule123")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "Vnf123", "VfModule123");
        when(client.get(org.onap.aai.domain.yang.VfModule.class,resourceUri)).thenReturn(Optional.empty())
        createAAIVfModuleVolumeGroup.getVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponseCode", 404)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponse", "VF-Module Not found!!")
    }

    @Test
    void testGetVfModuleException (){
        when(mockExecution.getVariable("CAAIVfModVG_vnfId")).thenReturn("Vnf123")
        when(mockExecution.getVariable("CAAIVfModVG_vfModuleId")).thenReturn("VfModule123")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "Vnf123", "VfModule123");
        when(client.get(org.onap.aai.domain.yang.VfModule.class,resourceUri)).thenThrow(new NullPointerException("Error in AAI client"))
        createAAIVfModuleVolumeGroup.getVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponseCode", 500)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_getVfModuleResponse", "AAI GET Failed:Error in AAI client")
    }

    @Test
    void testUpdateVfModule (){
        when(mockExecution.getVariable("CAAIVfModVG_vnfId")).thenReturn("Vnf123")
        when(mockExecution.getVariable("CAAIVfModVG_vfModuleId")).thenReturn("VfModule123")
        when(mockExecution.getVariable("CAAIVfModVG_aicCloudRegion")).thenReturn("CloudRegion1")
        when(mockExecution.getVariable("CAAIVfModVG_volumeGroupId")).thenReturn("VolumeGroup1")
        when(mockExecution.getVariable("CAAIVfModVG_cloudOwner")).thenReturn("cloudOwner")
        org.onap.aai.domain.yang.VfModule vfModuleExpected = new  org.onap.aai.domain.yang.VfModule()
        vfModuleExpected.setVfModuleId("VfModule123")
        vfModuleExpected.setResourceVersion("12345")
        when(mockExecution.getVariable("CAAIVfModVG_getVfModuleResponse")).thenReturn(vfModuleExpected)
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "Vnf123", "VfModule123");
        AAIResourceUri resourceUri1 = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, 'att-aic',"CloudRegion1", "VolumeGroup1")
        doNothing().when(client).connect(resourceUri ,resourceUri1 )
        createAAIVfModuleVolumeGroup.updateVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_updateVfModuleResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_updateVfModuleResponse", "Success")
    }

    @Test
    void testUpdateVfModuleAAIException (){
        when(mockExecution.getVariable("CAAIVfModVG_vnfId")).thenReturn("Vnf123")
        when(mockExecution.getVariable("CAAIVfModVG_vfModuleId")).thenReturn("VfModule123")
        when(mockExecution.getVariable("CAAIVfModVG_aicCloudRegion")).thenReturn("CloudRegion1")
        when(mockExecution.getVariable("CAAIVfModVG_volumeGroupId")).thenReturn("VolumeGroup1")
        org.onap.aai.domain.yang.VfModule vfModuleExpected = new  org.onap.aai.domain.yang.VfModule()
        vfModuleExpected.setVfModuleId("VfModule123")
        vfModuleExpected.setResourceVersion("12345")
        when(mockExecution.getVariable("CAAIVfModVG_cloudOwner")).thenReturn("cloudOwner")
        when(mockExecution.getVariable("CAAIVfModVG_getVfModuleResponse")).thenReturn(vfModuleExpected)
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "Vnf123", "VfModule123");
        AAIResourceUri resourceUri1 = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, 'cloudOwner',"CloudRegion1", "VolumeGroup1")
        doThrow(new NullPointerException("Error in AAI client")).when(client).connect(resourceUri ,resourceUri1 )
        createAAIVfModuleVolumeGroup.updateVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_updateVfModuleResponseCode", 500)
        Mockito.verify(mockExecution).setVariable("CAAIVfModVG_updateVfModuleResponse", 'AAI PUT Failed:'+ "Error in AAI client")
    }
}
