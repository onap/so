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
import org.onap.aai.domain.yang.Flavor;

public class AddFlavorTest {

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private Flavor flavor;
    private String cloudOwner = "test-cloud-owner";
    private String cloudRegionId = "test-cloud-region-id";

    private AddFlavor aaiFlavor;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        flavor = new Flavor();
        flavor.setFlavorId("flavor-id-1");
        flavor.setFlavorName("flavor-name-1");
        flavor.setFlavorSelflink("http://test/flavor-id-1");

        aaiFlavor = new AddFlavor(flavor, cloudOwner, cloudRegionId);
    }

    @Test
    public void testSubmitSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).addFlavor(flavor, cloudOwner, cloudRegionId);

        // Arrange
        aaiFlavor.submit(aaiClient);

        // Verify
        verify(aaiClient).addFlavor(flavor, cloudOwner, cloudRegionId);
        assertTrue(aaiFlavor.isSubmitted());
    }

    @Test
    public void testRollbackSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).deleteFlavors(cloudOwner, cloudRegionId, Collections.singletonList(flavor.getFlavorId()));

        // Arrange
        aaiFlavor.rollback(aaiClient);

        // Verify
        verify(aaiClient).deleteFlavors(cloudOwner, cloudRegionId, Collections.singletonList(flavor.getFlavorId()));
        assertFalse(aaiFlavor.isSubmitted());
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test aaiFlavor submit failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).addFlavor(flavor, cloudOwner,
            cloudRegionId);

        // Arrange
        Throwable throwable = catchThrowable(() -> aaiFlavor.submit(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).addFlavor(flavor, cloudOwner, cloudRegionId);
        assertFalse(aaiFlavor.isSubmitted());
    }

    @Test
    public void testRollbackFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test aaiFlavor rollback failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).deleteFlavors
            (cloudOwner, cloudRegionId, Collections.singletonList(flavor.getFlavorId()));

        // Arrange
        Throwable throwable = catchThrowable(() -> aaiFlavor.rollback(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).deleteFlavors(cloudOwner, cloudRegionId, Collections.singletonList(flavor.getFlavorId()));
        assertFalse(aaiFlavor.isSubmitted());
    }
}
