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
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;

public class DeleteImagesTest {

    private static final String URI_1 = "test1";
    private static final String URI_2 = "test2";

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private DeleteImages deleteImages;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        deleteImages = new DeleteImages(Arrays.asList(URI_1, URI_2));
    }

    @Test
    public void testSubmitSuccessWhenImage() throws ActiveAndAvailableInventoryException {
        // Setup an image that has no relationships
        when(aaiClient.getAaiObjectByUriIfPresent(URI_1, Image.class)).thenReturn(new Image());

        // Setup second image with relationship
        Image image2 = new Image();
        RelationshipList image2RelationshipList = new RelationshipList();
        List<Relationship> image2Relations = image2RelationshipList.getRelationship();
        Relationship rel = new Relationship();
        rel.setRelatedTo("blah");
        rel.setRelatedLink("blah-blah");
        image2Relations.add(rel);
        image2.setRelationshipList(image2RelationshipList);
        when(aaiClient.getAaiObjectByUriIfPresent(URI_2, Image.class)).thenReturn(image2);

        doNothing().when(aaiClient).deleteByUri(URI_1);
        doNothing().when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteImages.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        verify(aaiClient, times(0)).deleteByUri(URI_2);
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        when(aaiClient.getAaiObjectByUriIfPresent(URI_1, Image.class)).thenReturn(new Image());
        doThrow(new ActiveAndAvailableInventoryException("Failure Test")).when(aaiClient).deleteByUri(URI_1);

        when(aaiClient.getAaiObjectByUriIfPresent(URI_2, Image.class)).thenReturn(new Image());
        doThrow(new ActiveAndAvailableInventoryException("Failure Test")).when(aaiClient).deleteByUri(URI_2);

        // Arrange
        deleteImages.submit(aaiClient);

        // Verify
        verify(aaiClient).deleteByUri(URI_1);
        assertEquals(deleteImages.getFailedToDeleteResources(), Arrays.asList(URI_1, URI_2));
    }
}
