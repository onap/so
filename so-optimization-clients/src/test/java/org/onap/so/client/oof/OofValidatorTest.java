/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Nokia
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

package org.onap.so.client.oof;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.so.client.exception.BadResponseException;

public class OofValidatorTest {

    @Test
    public void validateDemandsResponse_success() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("requestStatus", "accepted");
        new OofValidator().validateDemandsResponse(map);
    }

    @Test(expected = BadResponseException.class)
    public void validateDemandsResponse_mapIsEmpty() throws Exception {
        new OofValidator().validateDemandsResponse(Collections.emptyMap());
    }

    @Test(expected = BadResponseException.class)
    public void validateDemandsResponse_lackOfRequestStatus() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "a");
        new OofValidator().validateDemandsResponse(map);
    }

    @Test(expected = BadResponseException.class)
    public void validateDemandsResponse_lackOfRequestStatusProperValue() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("requestStatus", "a");
        map.put("statusMessage", "a");
        new OofValidator().validateDemandsResponse(map);
    }
}
