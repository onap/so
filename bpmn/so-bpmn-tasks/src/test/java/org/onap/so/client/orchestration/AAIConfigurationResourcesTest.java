/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.client.orchestration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.bpmn.common.data.TestDataSetup;


@RunWith(MockitoJUnitRunner.class)
public class AAIConfigurationResourcesTest extends TestDataSetup{
	
	
	
	private Configuration configuration;
    private ServiceProxy serviceProxy;
    private ServiceInstance serviceInstance;
    private GenericVnf genericVnf;
    private VpnBinding vpnBinding;
    private VfModule vfModule;
    
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;
    
    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;
    
    @Mock
    protected InjectionHelper MOCK_injectionHelper;
    
    @InjectMocks
	private AAIConfigurationResources aaiConfigurationResources = new AAIConfigurationResources();
    
    @Before
    public void before() {
    	configuration = buildConfiguration();
        serviceProxy = buildServiceProxy();
        serviceInstance = buildServiceInstance();
        genericVnf = buildGenericVnf();
        vfModule = buildVfModule();
        vpnBinding = buildVpnBinding();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }
	
    @Test
    public void createConfigurationTest() {
    	doReturn(new org.onap.aai.domain.yang.Configuration()).when(MOCK_aaiObjectMapper).mapConfiguration(configuration);
        doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Configuration.class));
        
        aaiConfigurationResources.createConfiguration(configuration);
        
        assertEquals(OrchestrationStatus.INVENTORIED, configuration.getOrchestrationStatus());
        verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Configuration.class));
    }

    @Test
    public void updateConfigurationTest() {       
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Configuration.class));
        configuration.setConfigurationType("VNR");
        configuration.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
        aaiConfigurationResources.updateConfiguration(configuration);
        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), any(org.onap.aai.domain.yang.Configuration.class));
    }

    @Test
    public void connectConfigurationToServiceInstanceTest() {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectConfigurationToServiceInstance(configuration.getConfigurationId(), serviceInstance.getServiceInstanceId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }
    
    @Test
    public void disconnectConfigurationToServiceInstanceTest(){
        doNothing().when(MOCK_aaiResourcesClient).disconnect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.disconnectConfigurationToServiceInstance("TEST_CONFIGURATION_ID", "TEST_SERVICE_INSTANCE_ID");
        verify(MOCK_aaiResourcesClient, times(1)).disconnect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void connectConfigurationToGenericVnfTest() {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectConfigurationToGenericVnf(configuration.getConfigurationId(), genericVnf.getVnfId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }
    
    @Test
    public void connectConfigurationToVfModuleTest() {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectConfigurationToVfModule(configuration.getConfigurationId(), vfModule.getVfModuleId(),genericVnf.getVnfId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }
    
    @Test
    public void connectConfigurationToVnfcTest() {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectConfigurationToVnfc(configuration.getConfigurationId(), "vnfcName");
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void connectConfigurationToVpnBindingTest() {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectConfigurationToVpnBinding(configuration.getConfigurationId(), vpnBinding.getVpnId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void getConfigurationFromRelatedLinkTest () {
        Optional<org.onap.aai.domain.yang.Configuration>  configuration = Optional.of(new org.onap.aai.domain.yang.Configuration());
        configuration.get().setConfigurationId("config1");
        doReturn(configuration).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.Configuration.class),isA(AAIResourceUri.class));
        aaiConfigurationResources.getConfigurationFromRelatedLink("http://localhost:8090/aai/v12/network/configurations/configuration/config1");
        verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.Configuration.class),isA(AAIResourceUri.class));
    }

    @Test
    public void connectVrfConfigurationToVnrConfigurationTest() throws Exception {
        Configuration vrfConfiguration = buildConfiguration();
        Configuration vnrConfiguration = buildConfiguration();
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        aaiConfigurationResources.connectVrfConfigurationToVnrConfiguration(vrfConfiguration.getConfigurationId(),vnrConfiguration.getConfigurationId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void connectConfigurationToPnfObjectTest() throws Exception {
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        
        Pnf primaryPnf = serviceProxy.getServiceInstance().getPnfs().stream().filter(o -> o.getRole().equals("Primary")).findFirst().get();
        
        aaiConfigurationResources.connectConfigurationToPnfObject(primaryPnf.getPnfId(), configuration.getConfigurationId());
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void getConfigurationTest() {
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, "configurationId");
        doReturn(Optional.of(new org.onap.aai.domain.yang.Configuration())).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.Configuration.class, aaiResourceUri);
        aaiConfigurationResources.getConfiguration("configurationId");
        verify(MOCK_aaiResourcesClient, times(1)).get(org.onap.aai.domain.yang.Configuration.class, aaiResourceUri);
    }

    @Test
    public void deleteConfigurationTest() {
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, "configurationId");
        doNothing().when(MOCK_aaiResourcesClient).delete(aaiResourceUri);
        aaiConfigurationResources.deleteConfiguration("configurationId");
        verify(MOCK_aaiResourcesClient, times(1)).delete(aaiResourceUri);
    }
    
    @Test
	public void updateOrchestrationStatusConfigurationTest() throws Exception {
		configuration.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
		doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Configuration.class));
		aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration,OrchestrationStatus.ACTIVE);		
		verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), any(org.onap.aai.domain.yang.Configuration.class));
		assertEquals(OrchestrationStatus.ACTIVE, configuration.getOrchestrationStatus());
	}
}