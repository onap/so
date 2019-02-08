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
import org.onap.aai.domain.yang.Image;

public class AddImageTest {

    @Mock
    private ActiveAndAvailableInventory aaiClient;

    private Image image;
    private String cloudOwner = "test-cloud-owner";
    private String cloudRegionId = "test-cloud-region-id";

    private AddImage addImage;

    @Before
    public void setUp () {
        MockitoAnnotations.initMocks(this);
        image = new Image();
        image.setImageId("image-id-1");
        image.setImageName("image-name-1");
        image.setImageSelflink("http://test/image-id-1");

        addImage = new AddImage(image, cloudOwner, cloudRegionId);
    }

    @Test
    public void testSubmitSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).addImage(image, cloudOwner, cloudRegionId);

        // Arrange
        addImage.submit(aaiClient);

        // Verify
        verify(aaiClient).addImage(image, cloudOwner, cloudRegionId);
        assertTrue(addImage.isSubmitted());
    }

    @Test
    public void testRollbackSuccess() throws ActiveAndAvailableInventoryException {
        // Setup
        doNothing().when(aaiClient).deleteImages(cloudOwner, cloudRegionId, Collections.singletonList(image.getImageId()));

        // Arrange
        addImage.rollback(aaiClient);

        // Verify
        verify(aaiClient).deleteImages(cloudOwner, cloudRegionId, Collections.singletonList(image.getImageId()));
        assertFalse(addImage.isSubmitted());
    }

    @Test
    public void testSubmitFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addImage submit failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).addImage(image, cloudOwner,
            cloudRegionId);

        // Arrange
        Throwable throwable = catchThrowable(() -> addImage.submit(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).addImage(image, cloudOwner, cloudRegionId);
        assertFalse(addImage.isSubmitted());
    }

    @Test
    public void testRollbackFailure() throws ActiveAndAvailableInventoryException {
        // Setup
        String message = "Test addImage rollback failure.";
        doThrow(new ActiveAndAvailableInventoryException(message)).when(aaiClient).deleteImages
            (cloudOwner, cloudRegionId, Collections.singletonList(image.getImageId()));

        // Arrange
        Throwable throwable = catchThrowable(() -> addImage.rollback(aaiClient));

        // Verify
        AssertionsForClassTypes.assertThat(throwable).isInstanceOf(ActiveAndAvailableInventoryException.class)
            .hasMessage(message);
        verify(aaiClient).deleteImages(cloudOwner, cloudRegionId, Collections.singletonList(image.getImageId()));
        assertFalse(addImage.isSubmitted());
    }
}
