package org.onap.so.db.catalog.beans;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudIdentity;

public class CloudSiteTest {
	CloudSite cloudSite = new CloudSite();
	CloudIdentity cloudIdentity = new CloudIdentity(); 
	private static final String CLOUDVERSION = "testCloudVersion";
	private static final String CLLI = "testClli";
	private static final String ID = "testId";
	//private static final String IDENTITYSERVICE = "testIdentityService";
	private static final String ORCHESTRATOR = "testOrchestrator";
	private static final String PLATFORM = "testPlatform";
	private static final String REGIONID = "testRegionId";
	private static final String IDENTITYSERVICEID = "testIdentityServiceId";
	
	
	@Test
	public void testClousSite() {
		
		cloudSite.setCloudVersion(CLOUDVERSION);
		cloudSite.setClli(CLLI);
		cloudSite.setId(ID);
		cloudSite.setIdentityService(cloudIdentity);
		cloudSite.setOrchestrator(ORCHESTRATOR);
		cloudSite.setPlatform(PLATFORM);
		cloudSite.setRegionId(REGIONID);
		cloudSite.setIdentityServiceId(IDENTITYSERVICEID);
		
		//assertTrue (id.getId().equals("testId"));
		assertTrue(cloudSite.getCloudVersion().equals(CLOUDVERSION));
		assertTrue(cloudSite.getClli().equals(CLLI));
		assertTrue(cloudSite.getId().equals(ID));
		assertTrue(cloudSite.getIdentityService().equals(cloudIdentity));
		assertTrue(cloudSite.getOrchestrator().equals(ORCHESTRATOR));
		assertTrue(cloudSite.getPlatform().equals(PLATFORM));
		assertTrue(cloudSite.getRegionId().equals(REGIONID));
		assertTrue(cloudSite.getIdentityServiceId().equals(IDENTITYSERVICEID));
		
	}

}
