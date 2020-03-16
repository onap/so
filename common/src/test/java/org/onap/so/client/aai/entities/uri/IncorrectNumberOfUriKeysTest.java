/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai.entities.uri;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.exceptions.IncorrectNumberOfUriKeys;

public class IncorrectNumberOfUriKeysTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyIncorrectNumberOfKeysSingle() {

        thrown.expect(IncorrectNumberOfUriKeys.class);
        thrown.expectMessage(equalTo("Expected 3 variables: [cloud-owner, cloud-region-id, volume-group-id]"));
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, "volume-group-id");

    }

    @Test
    public void verifyIncorrectNumberOfKeysPlural() {

        thrown.expect(IncorrectNumberOfUriKeys.class);
        AAISimplePluralUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VOLUME_GROUP, "my-cloud-owner");

    }

    @Test
    public void verifyIncorrectNumberOfKeysFromParent() {

        thrown.expect(IncorrectNumberOfUriKeys.class);
        AAIResourceUri parentUri =
                AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, "my-cloud-owner", "my-cloud-region-id");
        AAIResourceUri uri = AAIUriFactory.createResourceFromParentURI(parentUri, AAIObjectType.VOLUME_GROUP);
    }

    @Test
    public void verifyIncorrectNumberOfKeysHttpAware() {

        thrown.expect(IncorrectNumberOfUriKeys.class);
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "customer-id", "subscription-id");
    }
}
