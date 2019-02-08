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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;

public class DeleteFlavorsTest {

    private static final String URI_1 = "test1";
    private static final String URI_2 = "test2";

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private DeleteFlavors deleteFlavors;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        deleteFlavors = new DeleteFlavors(Arrays.asList(URI_1, URI_2));
    }

    @Test
    public void testSubmitSuccessWhenFlavor() throws ActiveAndAvailableInventoryException {
        // Setup an Flavor that has no relationships
        when(aaiClient.getAaiObjectByUriIfPresent(URI_1, Flavor.class)).thenReturn(new Flavor());

        // Setup second Flavor with relationship
        Flavor flavor2 = new Flavor();
        RelationshipList flavor2RelationshipList = new RelationshipList();
        List<Relationship> flavor2Relations = flavor2RelationshipList.getRelationship();
        Relationship rel = new Relationship();
        rel.setRelatedTo("blah");
        rel.setRelatedLink("blah-blah");
        flavor2Relations.add(rel);
        flavor2.setRelationshipList(flavor2RelationshipList);
        when(aaiClient.getAaiObjectByUriIfPresent(URI_2, Flavor.class)).thenReturn(flavor2);

        doNothing().when(aaiClient).deleteByUri(URI_1);
        doNothing().when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteFlavors.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        verify(aaiClient, times(0)).deleteByUri(URI_2);
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        when(aaiClient.getAaiObjectByUriIfPresent(URI_1, Flavor.class)).thenReturn(new Flavor());
        doThrow(new ActiveAndAvailableInventoryException("Failure Test")).when(aaiClient).deleteByUri(URI_1);

        when(aaiClient.getAaiObjectByUriIfPresent(URI_2, Flavor.class)).thenReturn(new Flavor());
        doThrow(new ActiveAndAvailableInventoryException("Failure Test")).when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteFlavors.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        assertEquals(deleteFlavors.getFailedToDeleteResources(), Arrays.asList(URI_1, URI_2));
    }
}
