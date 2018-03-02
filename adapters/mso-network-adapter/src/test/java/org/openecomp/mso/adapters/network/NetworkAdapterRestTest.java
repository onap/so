package org.openecomp.mso.adapters.network;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.mso.adapters.network.NetworkAdapterRest.CreateNetworkTask;
import org.openecomp.mso.adapters.network.NetworkAdapterRest.DeleteNetworkTask;
import org.openecomp.mso.adapters.network.NetworkAdapterRest.RollbackNetworkTask;
import org.openecomp.mso.adapters.network.NetworkAdapterRest.UpdateNetworkTask;
import org.openecomp.mso.adapters.network.exceptions.NetworkException;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.NetworkTechnology;
import org.openecomp.mso.adapters.nwrest.ProviderVlanNetwork;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.RouteTarget;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkAdapterRest.class, CreateNetworkTask.class, CreateNetworkRequest.class, DeleteNetworkTask.class, DeleteNetworkRequest.class})
public class NetworkAdapterRestTest {
	@Mock
	private static CreateNetworkTask taskMock;
	@Mock
	private static CreateNetworkRequest reqMock;
	@Mock
	private static DeleteNetworkRequest delReqMock;
	@Mock
	private static DeleteNetworkTask delTaskMock;
	@Mock
	private static RollbackNetworkRequest rollbackReqMock;
	@Mock
	private static RollbackNetworkTask rollbackTaskMock;
	
	private static final String TESTING_KEYWORD = "___TESTING___";
	
	@Test
	public void NetworkAdapterRest_createNetwork_async_Test()
	{
		NetworkAdapterRest api = new NetworkAdapterRest();
		taskMock = PowerMockito.mock(CreateNetworkTask.class);
		reqMock = PowerMockito.mock(CreateNetworkRequest.class);
		
		try {
			PowerMockito.whenNew(CreateNetworkRequest.class).withAnyArguments().thenReturn(reqMock);
			PowerMockito.when(reqMock.isSynchronous()).thenReturn(false);
			PowerMockito.when(reqMock.getNetworkId()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			PowerMockito.when(reqMock.getCloudSiteId()).thenReturn(TESTING_KEYWORD);
			PowerMockito.when(reqMock.getTenantId()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			PowerMockito.when(reqMock.getNetworkType()).thenReturn("PROVIDER");
			PowerMockito.when(reqMock.getModelCustomizationUuid()).thenReturn("modelCustUuid");
			PowerMockito.when(reqMock.getNetworkName()).thenReturn("networkName");

			ProviderVlanNetwork vlan = new ProviderVlanNetwork();
			vlan.setPhysicalNetworkName("PhysicalNetworkName");
			vlan.setVlans(new ArrayList<Integer>(Arrays.asList(123,456,654,321)));
			PowerMockito.when(reqMock.getProviderVlanNetwork()).thenReturn(vlan);

			PowerMockito.when(reqMock.getFailIfExists()).thenReturn(true);
			PowerMockito.when(reqMock.getBackout()).thenReturn(false);
			
			List<Subnet> subnets = new ArrayList<Subnet>();
			Subnet s1 = new Subnet();
			s1.setSubnetName("Subnet1");
			subnets.add(s1);
			Subnet s2 = new Subnet();
			s1.setSubnetName("Subnet2");
			subnets.add(s2);
			Subnet s3 = new Subnet();
			s1.setSubnetName("Subnet3");
			subnets.add(s3);
			PowerMockito.when(reqMock.getSubnets()).thenReturn(subnets);
			
			MsoRequest msoRequest = new MsoRequest ();
			msoRequest.setRequestId("MSORequestID123");
			PowerMockito.when(reqMock.getMsoRequest()).thenReturn(msoRequest);
			// setup spy on CreateNetworkTask
			
			PowerMockito.whenNew(CreateNetworkTask.class).withArguments(reqMock).thenReturn(taskMock);
			PowerMockito.spy(taskMock);

			Response resp = api.createNetwork(new CreateNetworkRequest());
			assertEquals(resp.getStatus(),HttpStatus.SC_ACCEPTED);			
			
			// test if another thread has executed run method
			Mockito.verify(taskMock, Mockito.times(1)).run();			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	@Test
	public void NetworkAdapterRest_createNetwork_sync_Test()
	{
		NetworkAdapterRest api = new NetworkAdapterRest();
		// setup createNetwork parameter
		// setup sync to spy on run method
		CreateNetworkRequest req = new CreateNetworkRequest();
		req.setNetworkId("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
		req.setCloudSiteId(TESTING_KEYWORD);
		req.setTenantId("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
		req.setNetworkType("PROVIDER");
		req.setModelCustomizationUuid("modelCustUuid");
		req.setNetworkName("networkName");
		ProviderVlanNetwork vlan = new ProviderVlanNetwork();
		vlan.setPhysicalNetworkName("PhysicalNetworkName");
		vlan.setVlans(new ArrayList<Integer>(Arrays.asList(123,456,654,321)));
		req.setProviderVlanNetwork(vlan);		
		req.setFailIfExists(true);
		req.setBackout(false);
		List<Subnet> subnets = new ArrayList<Subnet>();
		Subnet s1 = new Subnet();
		s1.setSubnetName("Subnet1");
		subnets.add(s1);
		Subnet s2 = new Subnet();
		s1.setSubnetName("Subnet2");
		subnets.add(s2);
		Subnet s3 = new Subnet();
		s1.setSubnetName("Subnet3");
		subnets.add(s3);
		req.setSubnets(subnets);
		MsoRequest msoRequest = new MsoRequest ();
		msoRequest.setRequestId("MSORequestID123");
		req.setMsoRequest(msoRequest);
		// set sync
		req.setNotificationUrl(null);
		// setup spy on CreateNetworkTask
		CreateNetworkTask task = api.new CreateNetworkTask(req);
		
		try {
			PowerMockito.whenNew(CreateNetworkTask.class).withArguments(req).thenReturn(task);
			Response resp = api.createNetwork(req);

			CreateNetworkResponse cnresp = (CreateNetworkResponse) resp.getEntity();
			
			assertEquals(cnresp.getNetworkFqdn(), "086f70b6-28fb-11e6-8260-0017f20fe1b8");
			assertEquals(cnresp.getNetworkId(), "b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			assertEquals(cnresp.getNeutronNetworkId(), "55e55884-28fa-11e6-8971-0017f20fe1b8");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void NetworkAdapterRest_deleteNetwork_async_Test()
	{
		NetworkAdapterRest api = new NetworkAdapterRest();
		delTaskMock = PowerMockito.mock(DeleteNetworkTask.class);
		delReqMock = PowerMockito.mock(DeleteNetworkRequest.class);
		
		try{
			PowerMockito.whenNew(DeleteNetworkRequest.class).withAnyArguments().thenReturn(delReqMock);
			PowerMockito.when(delReqMock.isSynchronous()).thenReturn(false);
			PowerMockito.when(delReqMock.getCloudSiteId()).thenReturn(TESTING_KEYWORD);
			PowerMockito.when(delReqMock.getNetworkId()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			PowerMockito.when(delReqMock.getMessageId()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			
			PowerMockito.whenNew(DeleteNetworkTask.class).withArguments(delReqMock).thenReturn(delTaskMock);
			PowerMockito.spy(delTaskMock);
			
			Response resp = api.deleteNetwork("b4a6af8c-a22b-45d5-a880-29527f8f59a7", delReqMock);
			assertEquals(resp.getStatus(), HttpStatus.SC_ACCEPTED);
			
			// test if another thread has executed run method
			// Mockito.verify(delTaskMock, Mockito.times(1)).run();				
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void NetworkAdapterRest_deleteNetwork_sync_Test()
	{
		NetworkAdapterRest api = new NetworkAdapterRest();
		DeleteNetworkRequest req = new DeleteNetworkRequest();
		req.setNotificationUrl(null);
		req.setCloudSiteId(TESTING_KEYWORD);
		req.setNetworkId("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
		req.setMessageId("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
		
		DeleteNetworkTask task = api.new DeleteNetworkTask(req);
		
		try {
			PowerMockito.whenNew(DeleteNetworkTask.class).withArguments(req).thenReturn(task);
			PowerMockito.spy(task);
			Response resp = api.deleteNetwork("b4a6af8c-a22b-45d5-a880-29527f8f59a7", req);

			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void NetworkAdapterRest_queryNetwork_Test()
	{
		/*
		 * test when network found as well as network not found
		 */
		String networkStackId = "networkStackId";
		String skipAAI = "skipAAI";
		String requestId = "msoRequest.requestId";
		String serviceInstanceId = "msoRequest.serviceInstanceId";
		String aaiNetworkId = "aaiNetworkId";
		String cloudSiteId = "cloudSiteId";
		String tenantId = "tenantId";
		String networkNameOrId = "networkNameOrId";
		MsoRequest msoRequestMock = Mockito.mock(MsoRequest.class);
		try {
			PowerMockito.whenNew(MsoRequest.class).withArguments("msoRequest.requestId", "msoRequest.serviceInstanceId").thenReturn(msoRequestMock);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		MsoRequest msoRequest = new MsoRequest("msoRequest.requestId", "msoRequest.serviceInstanceId");
		Holder<Boolean> networkExists = new Holder<Boolean>();
        Holder<String> networkId = new Holder<String>();
        Holder<String> neutronNetworkId = new Holder<String>();
        Holder<NetworkStatus> status = new Holder<NetworkStatus>();
        Holder<List<String>> routeTargets = new Holder<List<String>>();
        Holder<Map<String, String>> subnetIdMap = new Holder<Map<String, String>>();		
        
		MsoNetworkAdapterImpl mockImpl = Mockito.mock(MsoNetworkAdapterImpl.class);
		CloudConfigFactory cloudConfigMock = Mockito.mock(CloudConfigFactory.class);
		MsoPropertiesFactory msoPropertiesFactoryMock = Mockito.mock(MsoPropertiesFactory.class);
		
		try {
			PowerMockito.whenNew(MsoPropertiesFactory.class).withAnyArguments().thenReturn(msoPropertiesFactoryMock);
			PowerMockito.whenNew(CloudConfigFactory.class).withAnyArguments().thenReturn(cloudConfigMock);
			PowerMockito.whenNew(MsoNetworkAdapterImpl.class).withArguments(msoPropertiesFactoryMock, cloudConfigMock).thenReturn(mockImpl);
			
			Mockito.doAnswer(new Answer<Void>() {
				@SuppressWarnings("unchecked")
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					System.out.println("called with arguments: " + Arrays.toString(args));
					Holder<Boolean> networkExists = (Holder<Boolean>) args[4];
					networkExists.value = Boolean.TRUE;
					
			        Holder<String> networkId = (Holder<String>) args[5];
			        networkId.value = "networkId";
			        
			        Holder<String> neutronNetworkId = (Holder<String>) args[6];
			        neutronNetworkId.value = "neutronNetworkId";
			        
			        Holder<NetworkStatus> status = (Holder<NetworkStatus>) args[7];
			        status.value = NetworkStatus.ACTIVE;
			        
			        Holder<List<String>> routeTargets = (Holder<List<String>>) args[8];
			        routeTargets.value = new ArrayList<String>(Arrays.asList("routeTarget1","routeTarget2"));
			        
			        Holder<Map<String, String>> subnetIdMap = (Holder<Map<String, String>>) args[9];
			        subnetIdMap.value = new HashMap<String,String>();
			        subnetIdMap.value.put("Key1", "Val1");
			        subnetIdMap.value.put("Key2", "Val2");
			        subnetIdMap.value.put("Key3", "Val3");
			        
					return null;
				}
			}).when(mockImpl).queryNetworkContrail(Mockito.anyString(), 
					Mockito.anyString(),
					Mockito.anyString(), 
					Mockito.any(MsoRequest.class),
					Mockito.anyObject(), 
					Mockito.anyObject(), 
					Mockito.anyObject(), 
					Mockito.anyObject(), 
					Mockito.anyObject(), 
					Mockito.anyObject());

			NetworkAdapterRest api = new NetworkAdapterRest();
			Response resp = api.queryNetwork(cloudSiteId, tenantId, networkStackId, skipAAI, requestId, serviceInstanceId, aaiNetworkId);
			QueryNetworkResponse entity = (QueryNetworkResponse) resp.getEntity();
			
			assertEquals(entity.getNetworkExists(), Boolean.TRUE);
			assertEquals(entity.getNetworkId(), "networkId");
			assertEquals(entity.getNeutronNetworkId(), "neutronNetworkId");
			assertEquals(entity.getNetworkStatus(), NetworkStatus.ACTIVE);
			assertEquals(entity.getRouteTargets().size(), 2);
			assertEquals(entity.getRouteTargets().get(0), "routeTarget1");
			assertEquals(entity.getRouteTargets().get(1), "routeTarget2");
			
			assertEquals(entity.getSubnetIdMap().size(), 3);
			assertEquals(entity.getSubnetIdMap().get("Key1"), "Val1");
			assertEquals(entity.getSubnetIdMap().get("Key2"), "Val2");
			assertEquals(entity.getSubnetIdMap().get("Key3"), "Val3");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} 
		catch (NetworkException e) 
		{
			e.printStackTrace();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Test
	public void NetworkAdapterRest_rollBackNetwork_async_Test()
	{
		rollbackReqMock = PowerMockito.mock(RollbackNetworkRequest.class);
		rollbackTaskMock = PowerMockito.mock(RollbackNetworkTask.class);
		NetworkRollback ntRollbackMock = PowerMockito.mock(NetworkRollback.class);
		MsoNetworkAdapterImpl adapterImplMock = PowerMockito.mock(MsoNetworkAdapterImpl.class);
		
		try{
			PowerMockito.whenNew(RollbackNetworkRequest.class).withAnyArguments().thenReturn(rollbackReqMock);
			PowerMockito.when(rollbackReqMock.isSynchronous()).thenReturn(false);
			PowerMockito.when(rollbackReqMock.getMessageId()).thenReturn("Rollback succeeded !");
			
			PowerMockito.whenNew(RollbackNetworkTask.class).withArguments(rollbackReqMock).thenReturn(rollbackTaskMock);
			PowerMockito.spy(rollbackTaskMock);
			
			// PowerMockito.whenNew(NetworkRollback.class).withAnyArguments().thenReturn(ntRollbackMock);
			PowerMockito.when(rollbackReqMock.getNetworkRollback()).thenReturn(ntRollbackMock);
			
			PowerMockito.whenNew(MsoNetworkAdapterImpl.class).withAnyArguments().thenReturn(adapterImplMock);
			Mockito.doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					System.out.println("called with arguments: " + Arrays.toString(args));
					return null;
				}
			}).when(adapterImplMock).rollbackNetwork(ntRollbackMock);

			NetworkAdapterRest api = new NetworkAdapterRest();
			Response resp = api.rollbackNetwork(new RollbackNetworkRequest());
			
			assertEquals(resp.getStatus(), HttpStatus.SC_ACCEPTED);
			// test if another thread has executed run method
			// Mockito.verify(rollbackTaskMock, Mockito.times(1)).run();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Test
	public void NetworkAdapterRest_rollBackNetwork_sync_Test()
	{
		rollbackReqMock = PowerMockito.mock(RollbackNetworkRequest.class);
		rollbackTaskMock = PowerMockito.mock(RollbackNetworkTask.class);
		
		try
		{
			PowerMockito.whenNew(RollbackNetworkRequest.class).withAnyArguments().thenReturn(rollbackReqMock);
			PowerMockito.when(rollbackReqMock.isSynchronous()).thenReturn(true);
			
			PowerMockito.whenNew(RollbackNetworkTask.class).withArguments(rollbackReqMock).thenReturn(rollbackTaskMock);
			PowerMockito.when(rollbackTaskMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
			PowerMockito.when(rollbackTaskMock.getGenericEntityResponse()).thenReturn(null);
			Mockito.doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					System.out.println("run method called ");
					return null;
				}
			}).when(rollbackTaskMock).run();
			PowerMockito.spy(rollbackTaskMock);
			
			NetworkAdapterRest api = new NetworkAdapterRest();
			Response resp = api.rollbackNetwork(new RollbackNetworkRequest());			
			
			assertEquals(resp.getStatus(),HttpStatus.SC_OK);
			Mockito.verify(rollbackTaskMock, Mockito.times(1)).run();	
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		
	}

	@Test
	public void NetworkAdapterRest_updateNetwork_sync_TestString_Test()
	{
		UpdateNetworkRequest req = new UpdateNetworkRequest();
		req.setCloudSiteId(TESTING_KEYWORD);
		req.setTenantId("tenantId");
		req.setNotificationUrl(null);
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId("MsoRequestId");
		msoReq.setServiceInstanceId("serviceInstanceId");
		req.setMsoRequest(msoReq);
		req.setNetworkId("UpdateNetworkRequestNetworkId");
		req.setMessageId("UpdateNetworkMessageWithTestString");
		
		NetworkAdapterRest api = new NetworkAdapterRest();
		UpdateNetworkTask task = api.new UpdateNetworkTask(req);
		
		try {
			PowerMockito.whenNew(UpdateNetworkTask.class).withArguments(req).thenReturn(task);
			Response resp = api.updateNetwork("UpdateNetworkRequestNetworkId", req);
			
			assertEquals(resp.getStatus(),HttpStatus.SC_OK);
			UpdateNetworkResponse unResp = (UpdateNetworkResponse) resp.getEntity();
			assertEquals(unResp.getNetworkId(),"UpdateNetworkRequestNetworkId");
			assertEquals(unResp.getMessageId(),"UpdateNetworkMessageWithTestString");
			
			Map<String, String> map = unResp.getSubnetMap();
			for (Map.Entry<String, String> entry : map.entrySet()) {
			    String key = entry.getKey();
			    if(key.equalsIgnoreCase("mickey"))
			    {
				    Object value = entry.getValue();
				    assertEquals((String)value, "7");
			    }
			    
			    if(key.equalsIgnoreCase("clyde"))
			    {
				    Object value = entry.getValue();
				    assertEquals((String)value, "10");
			    }

			    if(key.equalsIgnoreCase("wayne"))
			    {
				    Object value = entry.getValue();
				    assertEquals((String)value, "99");
			    }
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void NetworkAdapterRest_updateNetwork_sync_ContrailRequest_Test()
	{
		try {
			MsoRequest msoReq = new MsoRequest();
			msoReq.setRequestId("MsoRequestId");
			msoReq.setServiceInstanceId("serviceInstanceId");
			
			UpdateNetworkRequest reqMock = PowerMockito.mock(UpdateNetworkRequest.class);
			PowerMockito.whenNew(UpdateNetworkRequest.class).withAnyArguments().thenReturn(reqMock);
			PowerMockito.when(reqMock.getCloudSiteId()).thenReturn("NON_"+TESTING_KEYWORD);
			PowerMockito.when(reqMock.getTenantId()).thenReturn("tenantId");
			PowerMockito.when(reqMock.getNetworkType()).thenReturn("NetworkType");
			PowerMockito.when(reqMock.getModelCustomizationUuid()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			PowerMockito.when(reqMock.getNetworkStackId()).thenReturn("b4a6af8c-a22b-45d5-a880-29527f8f59a7");
			PowerMockito.when(reqMock.getNetworkName()).thenReturn("NetworkName");
			PowerMockito.when(reqMock.getSubnets()).thenReturn(new ArrayList<Subnet>());
			
			PowerMockito.when(reqMock.isSynchronous()).thenReturn(true);
			PowerMockito.when(reqMock.getNetworkId()).thenReturn("UpdateNetworkRequestNetworkId");
			PowerMockito.when(reqMock.getMessageId()).thenReturn("UpdateNetworkMessageWithTestString");
			PowerMockito.when(reqMock.getMsoRequest()).thenReturn(msoReq);
			PowerMockito.when(reqMock.isContrailRequest()).thenReturn(true);
			ContrailNetwork cn = new ContrailNetwork();
			cn.setRouteTargets(new ArrayList<RouteTarget>());
			cn.setPolicyFqdns(new ArrayList<String>());
			cn.setRouteTableFqdns(new ArrayList<String>());
			
			PowerMockito.when(reqMock.getContrailNetwork()).thenReturn(cn);		
			
			MsoNetworkAdapterImpl msoImplMock = PowerMockito.mock(MsoNetworkAdapterImpl.class);
			PowerMockito.whenNew(MsoNetworkAdapterImpl.class).withAnyArguments().thenReturn(msoImplMock);
			
			Mockito.doAnswer(new Answer<Void>() {
				@SuppressWarnings("unchecked")
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					System.out.println("updateNetworkContrail called with arguments: " + Arrays.toString(args));
			        
					return null;
				}
			}).when(msoImplMock).updateNetworkContrail
					(Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyListOf(RouteTarget.class),
							Mockito.anyString(),
							Mockito.anyString(),
							Mockito.anyListOf(Subnet.class),
							Mockito.anyListOf(String.class),
							Mockito.anyListOf(String.class),
							Mockito.any(MsoRequest.class),
							Mockito.any(Holder.class),
							Mockito.any(Holder.class)
				    );
			PowerMockito.spy(msoImplMock);			
			
			NetworkAdapterRest api = new NetworkAdapterRest();
			Response resp = api.updateNetwork("UpdateNetworkRequestNetworkId", reqMock);

			Mockito.verify(msoImplMock, Mockito.times(1)).updateNetworkContrail
			(Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyListOf(RouteTarget.class),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyListOf(Subnet.class),
					Mockito.anyListOf(String.class),
					Mockito.anyListOf(String.class),
					Mockito.any(MsoRequest.class),
					Mockito.any(Holder.class),
					Mockito.any(Holder.class)
		    );
			
			assertEquals(resp.getStatus(),HttpStatus.SC_OK);
			UpdateNetworkResponse unResp = (UpdateNetworkResponse) resp.getEntity();
			assertEquals(unResp.getNetworkId(),"UpdateNetworkRequestNetworkId");
			assertEquals(unResp.getMessageId(),"UpdateNetworkMessageWithTestString");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void NetworkAdapterRest_updateNetwork_async_Test()
	{
		UpdateNetworkRequest updateReqMock = PowerMockito.mock(UpdateNetworkRequest.class);
		UpdateNetworkTask updateTaskMock = PowerMockito.mock(UpdateNetworkTask.class);
		MsoNetworkAdapterImpl adapterImplMock = PowerMockito.mock(MsoNetworkAdapterImpl.class);
		
		try{
			PowerMockito.whenNew(UpdateNetworkRequest.class).withAnyArguments().thenReturn(updateReqMock);
			PowerMockito.when(updateReqMock.isSynchronous()).thenReturn(false);
			PowerMockito.when(updateReqMock.getMessageId()).thenReturn("Update succeeded !");
			PowerMockito.when(updateReqMock.getNetworkId()).thenReturn("UpdateNetworkRequestNetworkId");
			
			PowerMockito.whenNew(UpdateNetworkTask.class).withArguments(updateReqMock).thenReturn(updateTaskMock);
			PowerMockito.spy(updateTaskMock);
			
			PowerMockito.whenNew(MsoNetworkAdapterImpl.class).withAnyArguments().thenReturn(adapterImplMock);
			Mockito.doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					System.out.println("updateNetworkContrail called with arguments: " + Arrays.toString(args));
					return null;
				}
			}).when(adapterImplMock).updateNetworkContrail
			(
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyListOf(RouteTarget.class),
					Mockito.anyString(),
					Mockito.anyString(),
					Mockito.anyListOf(Subnet.class),
					Mockito.anyListOf(String.class),
					Mockito.anyListOf(String.class),
					Mockito.any(MsoRequest.class),
					Mockito.any(Holder.class),
					Mockito.any(Holder.class)					
			);

			NetworkAdapterRest api = new NetworkAdapterRest();
			Response resp = api.updateNetwork("UpdateNetworkRequestNetworkId", updateReqMock);
			
			assertEquals(resp.getStatus(), HttpStatus.SC_ACCEPTED);
			// test if another thread has executed run method
			// Mockito.verify(updateTaskMock, Mockito.times(1)).run();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
