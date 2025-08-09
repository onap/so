/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClientPortChanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

public class NetworkCollectionCatalogDbQueryTest extends CatalogDbAdapterBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(NetworkCollectionCatalogDbQueryTest.class);
    private static final String NETWORKCOLLECTION = "NetworkCollection";

    private final String serviceUUID = "5df8b6de-2083-11e7-93ae-92361f002671";

    @LocalServerPort
    private int port;
    boolean isInitialized;

    @Autowired
    CatalogDbClientPortChanger client;

    @Before
    public void initialize() {
        client.wiremockPort = String.valueOf(port);
    }

    @Test
    @Transactional
    public void networkCollectionTest() {
        logger.debug("TEST IS STARTING UP...");
        String modelUUID = "4694a55f-58b3-4f17-92a5-796d6f5ffd0d";
        boolean found = false;
        logger.debug(Integer.toString(port));
        InstanceGroup instanceGroup = null;
        List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupList = null;
        org.onap.so.db.catalog.beans.Service service = client.getServiceByID(modelUUID);
        if (service == null) {
            logger.debug("null");
        } else {
            List<CollectionResourceCustomization> customizations = service.getCollectionResourceCustomizations();
            if (customizations.isEmpty()) {
                logger.debug("No Network Collection found. CollectionResourceCustomizations is empty");
            }
            for (CollectionResourceCustomization crc : customizations) {
                if (client.getNetworkCollectionResourceCustomizationByID(
                        crc.getModelCustomizationUUID()) instanceof NetworkCollectionResourceCustomization) {
                    if (crc.getCollectionResource() != null) {
                        if (crc.getCollectionResource().getToscaNodeType() != null) {
                            String toscaNodeType = crc.getCollectionResource().getToscaNodeType();
                            if (toscaNodeType.contains(NETWORKCOLLECTION)) {
                                logger.debug("Found a network collection");
                                instanceGroup = crc.getCollectionResource().getInstanceGroup();
                                collectionInstanceGroupList = instanceGroup.getCollectionInstanceGroupCustomizations();
                                CollectionNetworkResourceCustomization collectionNetworkCust =
                                        instanceGroup.getCollectionNetworkResourceCustomizations().get(0);
                                logger.debug("Found Collection Network Resource Customization: {}",
                                        collectionNetworkCust.getModelCustomizationUUID());
                            } else {
                                logger.debug(
                                        "No Network Collection found. toscaNodeType does not contain NetworkCollection");
                            }
                        } else {
                            logger.debug("No Network Collection found. toscaNodeType is null");
                        }
                    } else {
                        logger.debug("No Network Collection found. collectionResource is null");
                    }
                    found = true;
                } else {
                    logger.debug("Not a Network Collection Resource Customization Instance");
                }
            }
        }
        assertEquals("Number of CollectionResourceInstanceGroupCustomization in list", 2,
                collectionInstanceGroupList.size());
        assertNotNull(instanceGroup);
        assertTrue(found);
    }

    @Test
    public void buildingBlockDetailTest() {
        logger.debug("TEST IS STARTING UP...");
        logger.debug(Integer.toString(port));
        String buildingBlockFlowName = "CreateNetworkCollectionBB";
        BuildingBlockDetail buildingBlockDetail = client.getBuildingBlockDetail(buildingBlockFlowName);
        logger.debug("{}", buildingBlockDetail.getResourceType());
        assertNotNull(buildingBlockDetail);
    }

    @Test
    public void fetchServiceTopology_Test() {
        org.onap.so.db.catalog.beans.Service service = client.getServiceByID(serviceUUID);

        if (service == null) {
            fail("Service is null");
        }
        assertEquals(serviceUUID, service.getModelUUID());
        assertEquals("MSOTADevInfra_vSAMP10a_Service", service.getModelName());
    }

    @Test
    public void CollectionNetworkResourceCustomizationTest() {
        String modelCustId = "1a61be4b-3378-4c9a-91c8-c919519b2d01";
        CollectionNetworkResourceCustomization collectionNetworkCust =
                client.getCollectionNetworkResourceCustomizationByID(modelCustId);
        assertNotNull(collectionNetworkCust);
        logger.debug(collectionNetworkCust.getModelCustomizationUUID());
    }

    @Test
    public void getCvnfcCustomization() {
        client.getServiceByID(serviceUUID);
        String vfId = "cb82ffd8-252a-11e7-93ae-92361f002671";
        String vnfId = "68dc9a92-214c-11e7-93ae-92361f002671";

        CvnfcConfigurationCustomization fabricConfig =
                client.getCvnfcCustomization(serviceUUID, vnfId, vfId, "dadc2c8c-2bab-11e9-b210-d663bd873d95");
        assertEquals("386c9aa7-9318-48ee-a6d1-1bf0f85de385", fabricConfig.getModelCustomizationUUID());
    }

}
