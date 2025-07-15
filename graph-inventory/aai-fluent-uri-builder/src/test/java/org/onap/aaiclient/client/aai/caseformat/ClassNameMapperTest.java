/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.aaiclient.client.aai.caseformat;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Test;
import org.onap.aai.domain.yang.ServiceInstance;

public class ClassNameMapperTest {

    @Test
    public void classToLowerHyphen() {
        assertEquals("service-instance", ClassNameMapper.getInstance().toLowerHyphen(ServiceInstance.class));
    }

    @Test
    public void classToLowerHyphenNotFound() {
        assertNull(ClassNameMapper.getInstance().toLowerHyphen(String.class));
    }

    @Test
    public void classToUpperCamel() {
        assertEquals("service-instance", ClassNameMapper.getInstance().toUpperCamel(ServiceInstance.class));
    }

    @Test
    public void classToUpperCamelNotFound() {
        assertNull(ClassNameMapper.getInstance().toUpperCamel(String.class));
    }
}
