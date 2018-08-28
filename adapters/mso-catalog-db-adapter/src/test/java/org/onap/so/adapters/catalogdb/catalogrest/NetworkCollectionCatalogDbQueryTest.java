/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClientPortChanger;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NetworkCollectionCatalogDbQueryTest {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NetworkCollectionCatalogDbQueryTest.class);
	private static final String NETWORKCOLLECTION = "NetworkCollection";
	
	private final String serviceUUID = "5df8b6de-2083-11e7-93ae-92361f002671";

	@LocalServerPort
	private int port;
	boolean isInitialized;

	@Autowired
	CatalogDbClientPortChanger client;

	@Before
	public void initialize(){
		client.wiremockPort= String.valueOf(port);
	}
	
	@Test
	@Transactional
	public void networkCollectionTest() {
		msoLogger.debug("TEST IS STARTING UP...");
		String modelUUID = "4694a55f-58b3-4f17-92a5-796d6f5ffd0d";
		boolean found = false;
		msoLogger.debug(Integer.toString(port));
		InstanceGroup instanceGroup = null;
		List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupList = null;
		org.onap.so.db.catalog.beans.Service service = client.getServiceByID(modelUUID);
		if (service == null) {
			msoLogger.debug("null");
		} else {
			List<CollectionResourceCustomization> customizations = service.getCollectionResourceCustomizations();
			if (customizations.isEmpty()) {
				msoLogger.debug("No Network Collection found. CollectionResourceCustomizations is empty");
			}
			for (CollectionResourceCustomization crc : customizations) {
				if(client.getNetworkCollectionResourceCustomizationByID(crc.getModelCustomizationUUID()) 
						instanceof NetworkCollectionResourceCustomization) {
					if (crc.getCollectionResource() != null) {
						if (crc.getCollectionResource()
								.getToscaNodeType() != null) {
							String toscaNodeType = crc.getCollectionResource()
									.getToscaNodeType();
							if (toscaNodeType.contains(NETWORKCOLLECTION)) {
								msoLogger.debug("Found a network collection");
								instanceGroup = crc.getCollectionResource().getInstanceGroup();
								collectionInstanceGroupList = 
										instanceGroup.getCollectionInstanceGroupCustomizations();
								CollectionNetworkResourceCustomization collectionNetworkCust = instanceGroup.getCollectionNetworkResourceCustomizations().get(0);
								msoLogger.debug("Found Collection Network Resource Customization: " + collectionNetworkCust.getModelCustomizationUUID());
							} else {
								msoLogger.debug(
										"No Network Collection found. toscaNodeType does not contain NetworkCollection");
							}
						} else {
							msoLogger.debug("No Network Collection found. toscaNodeType is null");
						}
					} else {
						msoLogger.debug("No Network Collection found. collectionResource is null");
					}
					found = true;
				} else {
					msoLogger.debug("Not a Network Collection Resource Customization Instance");
				}
			}
		}
		assertEquals("Number of CollectionResourceInstanceGroupCustomization in list", 2, collectionInstanceGroupList.size());
		assertNotNull(instanceGroup);
		assertTrue(found);
	}
	
	@Test
	public void buildingBlockDetailTest() {
		msoLogger.debug("TEST IS STARTING UP...");
		msoLogger.debug(Integer.toString(port));
		String buildingBlockFlowName = "CreateNetworkCollectionBB";
		BuildingBlockDetail buildingBlockDetail = client.getBuildingBlockDetail(buildingBlockFlowName);
		msoLogger.debug("" + buildingBlockDetail.getResourceType());
		assertNotNull(buildingBlockDetail);
	} 
	
	@Test
	public void fetchServiceTopology_Test() {		
		org.onap.so.db.catalog.beans.Service service = client.getServiceByID(serviceUUID);

		if (service == null) {
			fail("Service is null");
		} 		
		assertEquals(serviceUUID, service.getModelUUID());
		assertEquals("MSOTADevInfra_vSAMP10a_Service",service.getModelName());
	}
	
	@Test
	public void CollectionNetworkResourceCustomizationTest() {
		String modelCustId = "1a61be4b-3378-4c9a-91c8-c919519b2d01";
		CollectionNetworkResourceCustomization collectionNetworkCust = client.getCollectionNetworkResourceCustomizationByID(modelCustId);
		assertNotNull(collectionNetworkCust);
		msoLogger.debug(collectionNetworkCust.getModelCustomizationUUID());
	}
}
