/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */

package org.onap.so.heatbridge.actions;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.LInterface;

public class AddLInterfaceToVserverTest {

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private LInterface lInterface;
    private String cloudOwner = "test-cloud-owner";
    private String cloudRegionId = "test-cloud-region-id";
    private String tenantId = "test-tenant-id";
    private String vserverId = "test-lInterface-id";

    private AddLInterfaceToVserver addLInterfaceToVserver;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        lInterface = new LInterface();
        lInterface.setInterfaceName("lInterface-name-1");
        lInterface.setInterfaceId("lInterface-id-1");

        addLInterfaceToVserver = new AddLInterfaceToVserver(lInterface, cloudOwner, cloudRegionId,
            tenantId, vserverId);
    }

    @Test
    public void testSubmitSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).addLInterfaceToVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);

        // Arrange
        addLInterfaceToVserver.submit(aaiClient);

        // Verify
        verify(aaiClient).addLInterfaceToVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);
        assertTrue(addLInterfaceToVserver.isSubmitted());
    }

    @Test
    public void testRollbackSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient)
            .deleteLInterfaceFromVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);

        // Arrange
        addLInterfaceToVserver.rollback(aaiClient);

        // Verify
        verify(aaiClient)
            .deleteLInterfaceFromVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);
        assertFalse(addLInterfaceToVserver.isSubmitted());
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addLInterfaceToVserver submit failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).addLInterfaceToVserver(lInterface,
            cloudOwner, cloudRegionId, tenantId, vserverId);

        // Arrange
        Throwable throwable = catchThrowable(() -> addLInterfaceToVserver.submit(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).addLInterfaceToVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);
        assertFalse(addLInterfaceToVserver.isSubmitted());
    }

    @Test
    public void testRollbackFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addLInterfaceToVserver rollback failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient)
            .deleteLInterfaceFromVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);

        // Arrange
        Throwable throwable = catchThrowable(() -> addLInterfaceToVserver.rollback(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient)
            .deleteLInterfaceFromVserver(lInterface, cloudOwner, cloudRegionId, tenantId, vserverId);
        assertFalse(addLInterfaceToVserver.isSubmitted());
    }

}
