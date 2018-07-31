/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
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
import org.onap.so.cloud.CloudIdentity;
import org.onap.so.cloud.CloudSite;
import org.onap.so.cloudify.beans.DeploymentInfo;
import org.onap.so.cloudify.beans.DeploymentStatus;
import org.onap.so.cloudify.v3.client.Cloudify;
import org.onap.so.cloudify.v3.model.AzureConfig;
import org.onap.so.cloudify.v3.model.OpenstackConfig;
import org.onap.so.openstack.exceptions.MsoException;

public class MsoCloudifyUtilsTest2 {

	@Test
	public void instantiateVduTest() throws MsoException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("id");
		expected.setVduInstanceName("id");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.INSTANTIATED);
		status.setLastAction(new PluginAction(null, null, null));
		expected.setStatus(status);

		MsoCloudifyUtils cloudify = Mockito.spy(MsoCloudifyUtils.class);
		CloudSite site = new CloudSite();
		Optional<CloudSite> opSite = Optional.ofNullable(site);
		CloudConfig config = Mockito.mock(CloudConfig.class);
		cloudify.cloudConfig = config;
		Cloudify cloudifyClient = new Cloudify("cloudSite");
		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudSiteId("cloudSiteId");
		cloudInfo.setTenantId("tenantId");
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
		DeploymentInfo deployment = new DeploymentInfo();
		deployment.setId("id");
		deployment.setStatus(DeploymentStatus.INSTALLED);
		Map<String, byte[]> blueprintFiles = new HashMap<>();
		blueprintFiles.put(artifact.getName(), artifact.getContent());
		String instanceName = "instanceName";
		Map<String, Object> inputs = new HashMap<>();
		boolean rollbackOnFailure = true;

		when(config.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
		doReturn(false).when(cloudify).isBlueprintLoaded(cloudInfo.getCloudSiteId(),
				vduModel.getModelCustomizationUUID());
		doReturn(cloudifyClient).when(cloudify).getCloudifyClient(site);
		doReturn(true).when(cloudify).uploadBlueprint(cloudifyClient, vduModel.getModelCustomizationUUID(),
				artifact.getName(), blueprintFiles);
		doReturn(deployment).when(cloudify).createAndInstallDeployment(cloudInfo.getCloudSiteId(),
				cloudInfo.getTenantId(), instanceName, vduModel.getModelCustomizationUUID(), inputs, true,
				vduModel.getTimeoutMinutes(), rollbackOnFailure);

		VduInstance actual = cloudify.instantiateVdu(cloudInfo, instanceName, inputs, vduModel, rollbackOnFailure);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void queryVduTest() throws MsoException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("id");
		expected.setVduInstanceName("id");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.INSTANTIATED);
		status.setLastAction(new PluginAction(null, null, null));
		expected.setStatus(status);

		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudSiteId("cloudSiteId");
		cloudInfo.setTenantId("tenantId");
		DeploymentInfo deployment = new DeploymentInfo();
		deployment.setId("id");
		deployment.setStatus(DeploymentStatus.INSTALLED);
		String instanceId = "instanceId";

		MsoCloudifyUtils cloudify = Mockito.spy(MsoCloudifyUtils.class);

		doReturn(deployment).when(cloudify).queryDeployment(cloudInfo.getCloudSiteId(), cloudInfo.getTenantId(),
				instanceId);

		VduInstance actual = cloudify.queryVdu(cloudInfo, instanceId);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void deleteVduTest() throws MsoException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("id");
		expected.setVduInstanceName("id");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.DELETING);
		status.setLastAction(new PluginAction("deleting", null, null));
		expected.setStatus(status);

		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudSiteId("cloudSiteId");
		cloudInfo.setTenantId("tenantId");
		String instanceId = "instanceId";
		int timeoutMinutes = 1;
		DeploymentInfo deployment = Mockito.mock(DeploymentInfo.class);
		deployment.setId("id");
		deployment.setStatus(DeploymentStatus.CREATED);
		when(deployment.getId()).thenReturn("id");
		when(deployment.getStatus()).thenReturn(DeploymentStatus.CREATED);
		when(deployment.getLastAction()).thenReturn("deleting");
		MsoCloudifyUtils cloudify = Mockito.spy(MsoCloudifyUtils.class);
		doReturn(deployment).when(cloudify).uninstallAndDeleteDeployment(cloudInfo.getCloudSiteId(),
				cloudInfo.getTenantId(), instanceId, timeoutMinutes);

		VduInstance actual = cloudify.deleteVdu(cloudInfo, instanceId, timeoutMinutes);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void deploymentInfoToVduInstanceTest() {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("id");
		expected.setVduInstanceName("id");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.DELETING);
		status.setLastAction(new PluginAction("deleting", null, null));
		expected.setStatus(status);

		DeploymentInfo deployment = Mockito.mock(DeploymentInfo.class);
		deployment.setId("id");
		deployment.setStatus(DeploymentStatus.CREATED);
		when(deployment.getId()).thenReturn("id");
		when(deployment.getStatus()).thenReturn(DeploymentStatus.CREATED);
		when(deployment.getLastAction()).thenReturn("deleting");

		MsoCloudifyUtils cloudify = new MsoCloudifyUtils();

		VduInstance actual = cloudify.deploymentInfoToVduInstance(deployment);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void deploymentStatusToVduStatusTest() {
		VduStatus expected = new VduStatus();
		expected.setState(VduStateType.DELETING);
		expected.setLastAction(new PluginAction("deleting", null, null));

		DeploymentInfo deployment = Mockito.mock(DeploymentInfo.class);
		deployment.setId("id");
		deployment.setStatus(DeploymentStatus.CREATED);
		when(deployment.getId()).thenReturn("id");
		when(deployment.getStatus()).thenReturn(DeploymentStatus.CREATED);
		when(deployment.getLastAction()).thenReturn("deleting");

		MsoCloudifyUtils cloudify = new MsoCloudifyUtils();

		VduStatus actual = cloudify.deploymentStatusToVduStatus(deployment);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void getAzureConfigTest() {
		AzureConfig expected = new AzureConfig();
		expected.setSubscriptionId("subscriptionId");
		expected.setTenantId("tenantId");
		expected.setClientId("msoId");
		expected.setClientSecret("msoPass");

		MsoCloudifyUtils cloudify = new MsoCloudifyUtils();
		CloudSite cloudSite = Mockito.mock(CloudSite.class);
		CloudIdentity cloudIdentity = Mockito.mock(CloudIdentity.class);
		when(cloudSite.getIdentityService()).thenReturn(cloudIdentity);
		when(cloudIdentity.getAdminTenant()).thenReturn("subscriptionId");
		when(cloudIdentity.getMsoId()).thenReturn("msoId");
		when(cloudIdentity.getMsoPass()).thenReturn("msoPass");
		String tenantId = "tenantId";
		AzureConfig actual = cloudify.getAzureConfig(cloudSite, tenantId);

		assertThat(actual, sameBeanAs(expected));
	}
}
