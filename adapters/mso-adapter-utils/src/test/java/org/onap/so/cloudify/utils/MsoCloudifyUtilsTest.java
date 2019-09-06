/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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
package org.onap.so.cloudify.utils;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.PluginAction;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloudify.client.APIV31;
import org.onap.so.cloudify.client.APIV31.ExecutionStatus;
import org.onap.so.cloudify.client.DeploymentV31;
import org.onap.so.cloudify.client.ExecutionV31;
import org.onap.so.cloudify.utils.MsoCloudifyUtils.DeploymentAction;
import org.onap.so.cloudify.utils.MsoCloudifyUtils.DeploymentState;
import org.onap.so.cloudify.utils.MsoCloudifyUtils.DeploymentStatus;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class MsoCloudifyUtilsTest {

    private static final String CLOUD_SITE_ID = "cloudSiteIdTest";
    private static final String BLUEPRINT_ID = "bluePrintIdTest";
    private static final String FILE_NAME = "fileName";


    @InjectMocks
    MsoCloudifyUtils cloudifyUtils;

    MsoCloudifyUtils cloudifyUtilsSpy;

    @Mock
    private CloudConfig cloudConfig;

    @Mock
    private APIV31 cloudifyClient;

    @Before
    public void before() {
        cloudifyUtilsSpy = Mockito.spy(cloudifyUtils);
    }

    @Test
    public void instantiateVduTest() throws MsoException {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("id");
        expected.setVduInstanceName("id");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.INSTANTIATED);
        status.setLastAction(new PluginAction("install", "installed", null));
        expected.setStatus(status);

        CloudSite site = new CloudSite();
        Optional<CloudSite> opSite = Optional.ofNullable(site);
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("cloudSiteId");
        cloudInfo.setTenantId("tenantId");
        CloudifyManager cloudifyManager = new CloudifyManager();
        cloudifyManager.setCloudifyUrl("http://testUrl");
        VduModelInfo vduModel = new VduModelInfo();
        vduModel.setModelCustomizationUUID("blueprintId");
        vduModel.setTimeoutMinutes(1);
        VduArtifact artifact = new VduArtifact();
        artifact.setName("name");
        artifact.setType(ArtifactType.MAIN_TEMPLATE);
        byte[] content = new byte[1];
        artifact.setContent(content);
        List<VduArtifact> artifacts = new ArrayList<>();
        artifacts.add(artifact);
        vduModel.setArtifacts(artifacts);
        Map<String, byte[]> blueprintFiles = new HashMap<>();
        blueprintFiles.put(artifact.getName(), artifact.getContent());
        String instanceName = "vduInstanceName";
        Map<String, Object> inputs = new HashMap<>();
        boolean rollbackOnFailure = true;
        DeploymentV31 deployment = new DeploymentV31();
        deployment.setId("id");
        deployment.setInputs(inputs);
        deployment.setOutputs(new HashMap<String, Object>());
        DeploymentState ds = new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.INSTALLED);

        when(cloudConfig.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
        when(cloudConfig.getCloudifyManager(any())).thenReturn(cloudifyManager);
        doReturn(cloudifyClient).when(cloudifyUtilsSpy).getCloudifyClient(site);
        doReturn(false).when(cloudifyUtilsSpy).isBlueprintLoaded(anyString(), anyString());
        doNothing().when(cloudifyUtilsSpy).uploadBlueprint(anyString(), anyString(), anyString(), eq(null));
        doReturn(deployment).when(cloudifyUtilsSpy).createAndInstallDeployment(eq(cloudInfo.getCloudSiteId()),
                any(String.class), eq(instanceName), any(String.class), eq(inputs), eq(true),
                eq(vduModel.getTimeoutMinutes()), eq(rollbackOnFailure));
        doReturn(ds).when(cloudifyUtilsSpy).getDeploymentStatus(cloudInfo.getCloudSiteId(), deployment);

        VduInstance actual =
                cloudifyUtilsSpy.instantiateVdu(cloudInfo, instanceName, inputs, vduModel, rollbackOnFailure);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void queryVduTest() throws MsoException {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("id");
        expected.setVduInstanceName("id");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.INSTANTIATED);
        status.setLastAction(new PluginAction("install", "installed", null));
        expected.setStatus(status);

        CloudSite site = new CloudSite();
        Optional<CloudSite> opSite = Optional.ofNullable(site);
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("cloudSiteId");
        cloudInfo.setTenantId("tenantId");
        String instanceId = "id";
        DeploymentV31 deployment = new DeploymentV31();
        deployment.setId(instanceId);
        deployment.setInputs(new HashMap<String, Object>());
        deployment.setOutputs(new HashMap<String, Object>());
        DeploymentState ds = new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.INSTALLED);

        when(cloudConfig.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
        doReturn(cloudifyClient).when(cloudifyUtilsSpy).getCloudifyClient(any());
        doReturn(deployment).when(cloudifyClient).getDeployment(instanceId);
        doReturn(ds).when(cloudifyUtilsSpy).getDeploymentStatus(cloudInfo.getCloudSiteId(), deployment);

        VduInstance actual = cloudifyUtilsSpy.queryVdu(cloudInfo, instanceId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void deleteVduTest() throws MsoException {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("id");
        expected.setVduInstanceName("id");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.DELETING);
        status.setLastAction(new PluginAction("uninstall", "uninstalling", null));
        expected.setStatus(status);

        CloudSite site = new CloudSite();
        Optional<CloudSite> opSite = Optional.ofNullable(site);
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("cloudSiteId");
        cloudInfo.setTenantId("tenantId");
        String instanceId = "id";
        int timeoutMinutes = 1;
        DeploymentV31 deployment = new DeploymentV31();
        deployment.setId(instanceId);
        deployment.setInputs(new HashMap<String, Object>());
        deployment.setOutputs(new HashMap<String, Object>());
        ExecutionV31 ex = new ExecutionV31();
        ex.setStatus(ExecutionStatus.STATUS_TERMINATED.toString());
        DeploymentState ds = new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.UNINSTALLING);

        when(cloudConfig.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
        doReturn(cloudifyClient).when(cloudifyUtilsSpy).getCloudifyClient(any());
        doReturn(deployment).when(cloudifyClient).getDeployment(instanceId);
        doReturn(ex).when(cloudifyClient).runExecution(anyString(), anyString(), Mockito.isNull(), eq(false), eq(false),
                eq(false), any(), eq(timeoutMinutes * 60), eq(false));
        doReturn(deployment).when(cloudifyClient).deleteDeployment(any(), any());
        doReturn(ds).when(cloudifyUtilsSpy).getDeploymentStatus(cloudInfo.getCloudSiteId(), deployment);

        VduInstance actual = cloudifyUtilsSpy.deleteVdu(cloudInfo, instanceId, timeoutMinutes);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void deploymentInfoToVduInstanceTest() throws MsoException {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("id");
        expected.setVduInstanceName("id");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.INSTANTIATED);
        status.setLastAction(new PluginAction("install", "installed", null));
        expected.setStatus(status);

        DeploymentState ds = new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.INSTALLED);
        DeploymentV31 deployment = new DeploymentV31();
        deployment.setId("id");
        deployment.setInputs(new HashMap<String, Object>());
        deployment.setOutputs(new HashMap<String, Object>());
        doReturn(ds).when(cloudifyUtilsSpy).getDeploymentStatus("id", deployment);

        VduInstance actual = cloudifyUtilsSpy.deploymentToVduInstance("id", deployment);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void deploymentStatusToVduStatusTest() throws Exception {
        VduStatus expected = new VduStatus();
        expected.setState(VduStateType.DELETING);
        expected.setLastAction(new PluginAction("uninstall", "uninstalling", null));

        CloudSite site = new CloudSite();
        Optional<CloudSite> opSite = Optional.ofNullable(site);
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("cloudSiteId");
        cloudInfo.setTenantId("tenantId");
        DeploymentState ds = new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.UNINSTALLING);
        DeploymentV31 deployment = new DeploymentV31();
        deployment.setId("id");
        deployment.setInputs(new HashMap<String, Object>());
        deployment.setOutputs(new HashMap<String, Object>());

        when(cloudConfig.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
        doReturn(cloudifyClient).when(cloudifyUtilsSpy).getCloudifyClient(any());
        doReturn(ds).when(cloudifyUtilsSpy).getDeploymentStatus(any(), any());

        VduStatus actual = cloudifyUtilsSpy.deploymentStatusToVduStatus(cloudInfo.getCloudSiteId(), deployment);

        assertThat(actual, sameBeanAs(expected));
    }


    @Test
    public void uploadBlueprintSuccessful() throws MsoException {
        // given
        byte[] blueprint = new byte[1];

        doNothing().when(cloudifyUtilsSpy).uploadBlueprint(any(String.class), eq(BLUEPRINT_ID), eq(FILE_NAME),
                eq(blueprint));
        // when
        cloudifyUtilsSpy.uploadBlueprint(CLOUD_SITE_ID, BLUEPRINT_ID, FILE_NAME, blueprint);
        // then
        verify(cloudifyUtilsSpy).uploadBlueprint(any(String.class), eq(BLUEPRINT_ID), eq(FILE_NAME), eq(blueprint));
    }

}

