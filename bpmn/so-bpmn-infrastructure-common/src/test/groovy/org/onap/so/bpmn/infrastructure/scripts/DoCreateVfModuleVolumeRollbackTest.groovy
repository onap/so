package org.onap.so.bpmn.infrastructure.scripts;

import org.junit.Before;
import org.junit.Test
import org.onap.aai.domain.yang.VfModule
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when;

public class DoCreateVfModuleVolumeRollbackTest extends MsoGroovyTest {

    private  DoCreateVfModuleVolumeRollback doCreateVfModuleVolumeRollback;
    @Before
    public void init(){
        super.init("DoCreateVfModuleVolumeRollback");
        doCreateVfModuleVolumeRollback = spy(DoCreateVfModuleVolumeRollback.class);
        when(doCreateVfModuleVolumeRollback.getAAIClient()).thenReturn(client)
    }

    @Test
    void callRESTDeleteAAIVolumeGroupTest(){
        String volumeGroupName = "volumeGroupName"
        String cloudRegionId = "cloudRegionId"
        when(mockExecution.getVariable("DCVFMODVOLRBK_volumeGroupName")).thenReturn(volumeGroupName)
        when(mockExecution.getVariable("DCVFMODVOLRBK_lcpCloudRegionId")).thenReturn(cloudRegionId)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegionId).queryParam("volume-group-name", volumeGroupName)
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId")
        VolumeGroups groups = new VolumeGroups();
        groups.getVolumeGroup().add(volumeGroup)
        when(client.get(VolumeGroups.class,uri)).thenReturn(Optional.of(groups))

        doCreateVfModuleVolumeRollback.callRESTDeleteAAIVolumeGroup(mockExecution,null)
    }

}
