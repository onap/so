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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeleteVserversTest {

    private static final String URI_1 = "test1";
    private static final String URI_2 = "test2";

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private DeleteVservers deleteVservers;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        deleteVservers = new DeleteVservers(Arrays.asList(URI_1, URI_2));
    }

    @Test
    public void testSubmitSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).deleteByUri(URI_1);
        doNothing().when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteVservers.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        verify(aaiClient).deleteByUri(URI_2);
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        doThrow(new ActiveAndAvailableInventoryException("Failure Test")).when(aaiClient).deleteByUri(URI_1);
        doNothing().when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteVservers.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        verify(aaiClient).deleteByUri(URI_2);
        assertEquals(deleteVservers.getFailedToDeleteResources(), Collections.singletonList(URI_1));
    }
}
