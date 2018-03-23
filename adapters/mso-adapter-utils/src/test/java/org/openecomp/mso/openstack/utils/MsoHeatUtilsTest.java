package org.openecomp.mso.openstack.utils;

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
import org.openecomp.mso.adapters.vdu.CloudInfo;
import org.openecomp.mso.adapters.vdu.PluginAction;
import org.openecomp.mso.adapters.vdu.VduArtifact;
import org.openecomp.mso.adapters.vdu.VduArtifact.ArtifactType;
import org.openecomp.mso.adapters.vdu.VduInstance;
import org.openecomp.mso.adapters.vdu.VduModelInfo;
import org.openecomp.mso.adapters.vdu.VduStateType;
import org.openecomp.mso.adapters.vdu.VduStatus;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloudify.beans.DeploymentInfo;
import org.openecomp.mso.cloudify.beans.DeploymentStatus;
import org.openecomp.mso.cloudify.utils.MsoCloudifyUtils;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.exceptions.MsoException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MsoHeatUtilsTest {

	@Test
	public void instantiateVduTest() throws MsoException, JsonProcessingException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("canonicalName");
		expected.setVduInstanceName("name");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.INSTANTIATED);
		status.setLastAction((new PluginAction("create", "complete", "")));
		expected.setStatus(status);

		MsoHeatUtils heatUtils = Mockito.spy(MsoHeatUtils.class);
		CloudSite site = new CloudSite();
		Optional<CloudSite> opSite = Optional.ofNullable(site);
		CloudConfig config = Mockito.mock(CloudConfig.class);
		heatUtils.cloudConfig = config;
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
		Map<String, byte[]> blueprintFiles = new HashMap<>();
		blueprintFiles.put(artifact.getName(), artifact.getContent());
		String instanceName = "instanceName";
		Map<String, Object> inputs = new HashMap<>();
		boolean rollbackOnFailure = true;
		String heatTemplate = new String(artifact.getContent());
		when(config.getCloudSite(cloudInfo.getCloudSiteId())).thenReturn(opSite);
		Map<String, Object> nestedTemplates = new HashMap<String, Object>();
		Map<String, Object> files = new HashMap<String, Object>();

		StackInfo stackInfo = new StackInfo();
		stackInfo.setCanonicalName("canonicalName");
		stackInfo.setName("name");
		stackInfo.setStatus(HeatStatus.CREATED);

		doReturn(stackInfo).when(heatUtils).createStack(cloudInfo.getCloudSiteId(), cloudInfo.getTenantId(),
				instanceName, heatTemplate, inputs, true, vduModel.getTimeoutMinutes(), null, nestedTemplates, files,
				rollbackOnFailure);

		VduInstance actual = heatUtils.instantiateVdu(cloudInfo, instanceName, inputs, vduModel, rollbackOnFailure);
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void queryVduTest() throws MsoException, JsonProcessingException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("canonicalName");
		expected.setVduInstanceName("name");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.INSTANTIATED);
		status.setLastAction((new PluginAction("create", "complete", "")));
		expected.setStatus(status);

		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudSiteId("cloudSiteId");
		cloudInfo.setTenantId("tenantId");
		String instanceId = "instanceId";

		StackInfo stackInfo = new StackInfo();
		stackInfo.setCanonicalName("canonicalName");
		stackInfo.setName("name");
		stackInfo.setStatus(HeatStatus.CREATED);
		
		MsoHeatUtils heatUtils = Mockito.spy(MsoHeatUtils.class);

		doReturn(stackInfo).when(heatUtils).queryStack(cloudInfo.getCloudSiteId(), cloudInfo.getTenantId(), instanceId);

		VduInstance actual = heatUtils.queryVdu(cloudInfo, instanceId);
		
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void deleteVduTest() throws MsoException {
		VduInstance expected = new VduInstance();
		expected.setVduInstanceId("canonicalName");
		expected.setVduInstanceName("name");
		VduStatus status = new VduStatus();
		status.setState(VduStateType.DELETED);
		expected.setStatus(status);

		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudSiteId("cloudSiteId");
		cloudInfo.setTenantId("tenantId");
		String instanceId = "instanceId";

		StackInfo stackInfo = new StackInfo();
		stackInfo.setCanonicalName("canonicalName");
		stackInfo.setName("name");
		stackInfo.setStatus(HeatStatus.NOTFOUND);
		
		int timeoutInMinutes = 1;
		
		MsoHeatUtils heatUtils = Mockito.spy(MsoHeatUtils.class);

		doReturn(stackInfo).when(heatUtils).deleteStack( cloudInfo.getTenantId(), cloudInfo.getCloudSiteId(), instanceId, true);

		VduInstance actual = heatUtils.deleteVdu(cloudInfo, instanceId, timeoutInMinutes);
		
		assertThat(actual, sameBeanAs(expected));
	}

}
