package org.openecomp.mso.asdc.tenantIsolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.WatchdogComponentDistributionStatus;
import org.openecomp.mso.requestsdb.WatchdogComponentDistributionStatusDb;
import org.openecomp.mso.requestsdb.WatchdogDistributionStatusDb;
import org.openecomp.mso.requestsdb.WatchdogServiceModVerIdLookupDb;

public class WatchdogDistributionTest {

	@BeforeClass
	public static void setup() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.initializeMsoProperties("MSO_PROP_ASDC", "/mso.asdc.json");
	}
	
	@Test
	public void testGetOverallDistributionStatusSuccess() {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogDistributionStatusDb watchdogDisdb = mock(WatchdogDistributionStatusDb.class);
		WatchdogComponentDistributionStatusDb watchdogComp = mock(WatchdogComponentDistributionStatusDb.class);
		
		distribution.setWatchdogDistDb(watchdogDisdb);
		distribution.setWatchdogCompDistDb(watchdogComp);
		try {
			WatchdogComponentDistributionStatus watchDogDisStatus1 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus1.setComponentDistributionStatus("SUCCESS");
			watchDogDisStatus1.setComponentName("AAI");
			
			WatchdogComponentDistributionStatus watchDogDisStatus2 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus2.setComponentDistributionStatus("SUCCESS");
			watchDogDisStatus2.setComponentName("APP");
			
			List<WatchdogComponentDistributionStatus> results = new ArrayList<>();
			results.add(watchDogDisStatus1);
			results.add(watchDogDisStatus2);
			
			when(watchdogDisdb.getWatchdogDistributionIdStatus(any(String.class))).thenReturn(null);
			when(watchdogComp.getWatchdogComponentDistributionStatus(any(String.class))).thenReturn(results);
			doNothing().when(watchdogDisdb).updateWatchdogDistributionIdStatus(any(String.class), any(String.class));

			String result = distribution.getOverallDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7fe");
			
			assertEquals("SUCCESS", result);
		} catch (Exception e) {
			fail("Unexpected result");
		}
	}
	
	@Test
	public void testGetOverallDistributionStatusFailure() {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogDistributionStatusDb watchdogDisdb = mock(WatchdogDistributionStatusDb.class);
		WatchdogComponentDistributionStatusDb watchdogComp = mock(WatchdogComponentDistributionStatusDb.class);
		
		distribution.setWatchdogDistDb(watchdogDisdb);
		distribution.setWatchdogCompDistDb(watchdogComp);
		
		try {
			WatchdogComponentDistributionStatus watchDogDisStatus1 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus1.setComponentDistributionStatus("SUCCESS");
			watchDogDisStatus1.setComponentName("AAI");
			
			WatchdogComponentDistributionStatus watchDogDisStatus2 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus2.setComponentDistributionStatus("FAILURE");
			watchDogDisStatus2.setComponentName("APP");
			
			List<WatchdogComponentDistributionStatus> results = new ArrayList<>();
			results.add(watchDogDisStatus1);
			results.add(watchDogDisStatus2);
			
			when(watchdogDisdb.getWatchdogDistributionIdStatus(any(String.class))).thenReturn(null);
			when(watchdogComp.getWatchdogComponentDistributionStatus(any(String.class))).thenReturn(results);
			doNothing().when(watchdogDisdb).updateWatchdogDistributionIdStatus(any(String.class), any(String.class));

			String result = distribution.getOverallDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7fe");
			
			assertEquals("FAILURE", result);
		} catch (Exception e) {
			fail("Unexpected result");
		}
	}
	
	@Test
	public void testGetOverallDistributionStatusException() {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogDistributionStatusDb watchdogDisdb = mock(WatchdogDistributionStatusDb.class);
		WatchdogComponentDistributionStatusDb watchdogComp = mock(WatchdogComponentDistributionStatusDb.class);
		
		distribution.setWatchdogDistDb(watchdogDisdb);
		distribution.setWatchdogCompDistDb(watchdogComp);
		try {
			WatchdogComponentDistributionStatus watchDogDisStatus1 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus1.setComponentDistributionStatus("SUCCESS");
			watchDogDisStatus1.setComponentName("AAI");
			
			WatchdogComponentDistributionStatus watchDogDisStatus2 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus2.setComponentDistributionStatus("TESTING");
			watchDogDisStatus2.setComponentName("APP");
			
			List<WatchdogComponentDistributionStatus> results = new ArrayList<>();
			results.add(watchDogDisStatus1);
			results.add(watchDogDisStatus2);
			
			when(watchdogDisdb.getWatchdogDistributionIdStatus(any(String.class))).thenReturn(null);
			when(watchdogComp.getWatchdogComponentDistributionStatus(any(String.class))).thenReturn(results);
			doNothing().when(watchdogDisdb).updateWatchdogDistributionIdStatus(any(String.class), any(String.class));

			distribution.getOverallDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7fe");
			
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("Invalid Component distribution status:"));
		}
	}
	
	@Test
	public void testGetOverallDistributionStatusIncomplete() {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogDistributionStatusDb watchdogDisdb = mock(WatchdogDistributionStatusDb.class);
		WatchdogComponentDistributionStatusDb watchdogComp = mock(WatchdogComponentDistributionStatusDb.class);
		
		distribution.setWatchdogDistDb(watchdogDisdb);
		distribution.setWatchdogCompDistDb(watchdogComp);
		try {
			WatchdogComponentDistributionStatus watchDogDisStatus1 = new WatchdogComponentDistributionStatus();
			watchDogDisStatus1.setComponentDistributionStatus("SUCCESS");
			watchDogDisStatus1.setComponentName("AAI");
			
			List<WatchdogComponentDistributionStatus> results = new ArrayList<>();
			results.add(watchDogDisStatus1);
			
			when(watchdogDisdb.getWatchdogDistributionIdStatus(any(String.class))).thenReturn(null);
			when(watchdogComp.getWatchdogComponentDistributionStatus(any(String.class))).thenReturn(results);
			
			String result = distribution.getOverallDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7fe");
			
			assertEquals("INCOMPLETE", result);
		} catch (Exception e) {
			fail("Unexpected result");
		}
	}
	
	@Test
	public void testGetOverallDistributionStatusTimeout() {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogDistributionStatusDb watchdogDisdb = mock(WatchdogDistributionStatusDb.class);
		
		distribution.setWatchdogDistDb(watchdogDisdb);
		try {
			when(watchdogDisdb.getWatchdogDistributionIdStatus(any(String.class))).thenReturn("timeout");

			String result = distribution.getOverallDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7fe");
			
			assertEquals("TIMEOUT", result);
		} catch (Exception e) {
			fail("Unexpected result");
		}
	}
	
	@Test
	public void testExecutePatchAAI() throws Exception {
		WatchdogDistribution distribution = new WatchdogDistribution();
		WatchdogServiceModVerIdLookupDb serviceLookupDb = mock(WatchdogServiceModVerIdLookupDb.class);
		CatalogDatabase catalogDb = mock(CatalogDatabase.class);
		AAIResourcesClient aaiClient = mock(AAIResourcesClient.class);
		
		Service service = new Service();
		service.setModelInvariantUUID("modelInvariantUUID");
		
		when(serviceLookupDb.getWatchdogServiceModVerId(any(String.class))).thenReturn("ff3514e3-5a33-55df");
		when(catalogDb.getServiceByModelUUID(any(String.class))).thenReturn(service);
		doNothing().when(aaiClient).update(any(AAIResourceUri.class), any(Object.class));
		
		distribution.setAaiClient(aaiClient);
		distribution.setCatalogDb(catalogDb);
		distribution.setWatchdogSerlookupDb(serviceLookupDb);
		distribution.executePatchAAI("ff3514e3-5a33-55df-13ab-12abad84e7fe", "model-id", "SUCCESS");
		
		verify(aaiClient, times(1)).update(any(AAIResourceUri.class), any(Object.class));
	}
	
	@Test
	public void testExecutePatchAAIException() throws Exception {
		WatchdogDistribution distribution = new WatchdogDistribution();
		CatalogDatabase catalogDb = mock(CatalogDatabase.class);
		WatchdogServiceModVerIdLookupDb serviceLookupDb = mock(WatchdogServiceModVerIdLookupDb.class);
		
		when(serviceLookupDb.getWatchdogServiceModVerId(any(String.class))).thenReturn("ff3514e3-5a33-55df");
		when(catalogDb.getServiceByModelUUID(any(String.class))).thenReturn(null);
		
		try {
			distribution.setCatalogDb(catalogDb);
			distribution.setWatchdogSerlookupDb(serviceLookupDb);
			distribution.executePatchAAI("ff3514e3-5a33-55df-13ab-12abad84e7fe", "model-id", "SUCCESS");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("No Service found with serviceModelVersionId:"));
		}
	}
}
