/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;

import java.time.LocalDateTime;
import org.onap.so.etsi.nfvo.ns.lcm.JSON;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.utils.LocalDateTimeTypeAdapter;
import org.springframework.stereotype.Component;
import org.threeten.bp.OffsetDateTime;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class GsonProvider {

    private final JSON.OffsetDateTimeTypeAdapter offsetDateTimeTypeAdapter = new JSON.OffsetDateTimeTypeAdapter();

    public Gson getGson() {
        return JSON.createGson().registerTypeAdapter(OffsetDateTime.class, offsetDateTimeTypeAdapter)
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()).create();
    }


}
