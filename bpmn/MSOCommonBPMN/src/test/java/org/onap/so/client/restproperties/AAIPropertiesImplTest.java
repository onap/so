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

package org.onap.so.client.restproperties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import org.junit.Test;

public class AAIPropertiesImplTest {

    private AAIPropertiesImpl aaiPropertiesImpl = new AAIPropertiesImpl();

    @Test
    public void getEndpointTest() {
        assertEquals("aai.endpoint", AAIPropertiesImpl.AAI_ENDPOINT);
    }

    @Test
    public void thatAuthIsBuiltFromUsernameAndPassword() {
        AAIPropertiesImpl properties = spy(aaiPropertiesImpl);
        doReturn("aai@aai.onap.org").when(properties).getUsername();
        doReturn("demo123456!").when(properties).getPassword();
        assertEquals("aai@aai.onap.org:demo123456!", properties.getAuth());
    }
}
