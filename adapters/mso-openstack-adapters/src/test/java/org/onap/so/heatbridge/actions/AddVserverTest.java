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
import java.util.Collections;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.Vserver;

public class AddVserverTest {

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private Vserver vserver;
    private String cloudOwner = "test-cloud-owner";
    private String cloudRegionId = "test-cloud-region-id";
    private String tenantId = "test-tenant-id";

    private AddVserver addVserver;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        vserver = new Vserver();
        vserver.setVserverId("vserver-id-1");
        vserver.setVserverName("vserver-name-1");
        vserver.setVserverSelflink("http://test/vserver-id-1");

        addVserver = new AddVserver(vserver, cloudOwner, cloudRegionId, tenantId);
    }

    @Test
    public void testSubmitSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).addVserver(vserver, cloudOwner, cloudRegionId, tenantId);

        // Arrange
        addVserver.submit(aaiClient);

        // Verify
        verify(aaiClient).addVserver(vserver, cloudOwner, cloudRegionId, tenantId);
        assertTrue(addVserver.isSubmitted());
    }

    @Test
    public void testRollbackSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient)
            .deleteVservers(cloudOwner, cloudRegionId, tenantId, Collections.singletonList(vserver.getVserverId()));

        // Arrange
        addVserver.rollback(aaiClient);

        // Verify
        verify(aaiClient)
            .deleteVservers(cloudOwner, cloudRegionId, tenantId, Collections.singletonList(vserver.getVserverId()));
        assertFalse(addVserver.isSubmitted());
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addVserver submit failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).addVserver(vserver, cloudOwner,
            cloudRegionId, tenantId);

        // Arrange
        Throwable throwable = catchThrowable(() -> addVserver.submit(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).addVserver(vserver, cloudOwner, cloudRegionId, tenantId);
        assertFalse(addVserver.isSubmitted());
    }

    @Test
    public void testRollbackFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addVserver rollback failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient)
            .deleteVservers(cloudOwner, cloudRegionId, tenantId, Collections.singletonList(vserver.getVserverId()));

        // Arrange
        Throwable throwable = catchThrowable(() -> addVserver.rollback(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient)
            .deleteVservers(cloudOwner, cloudRegionId, tenantId, Collections.singletonList(vserver.getVserverId()));
        assertFalse(addVserver.isSubmitted());
    }
}
