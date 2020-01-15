/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StartupService {

    public static final String URL = "url";

    @Autowired
    private AaiConnectionService aaiService;

    @Autowired
    private SubscriberService subscriberService;

    public void run() throws Exception {
        final Map vnfm = aaiService.receiveVnfm();
        isValid(vnfm);
        final boolean done = subscriberService.subscribe(vnfm);

        if (!done) {
            throw new VeVnfmException("Could not subscribe to VNFM");
        }
    }

    public static String getUrl(final Map vnfm) {
        return (String) vnfm.get(URL);
    }

    private static void isValid(final Map vnfm) throws VeVnfmException {
        if (Strings.isBlank(getUrl(vnfm))) {
            throw new VeVnfmException("No 'url' field in VNFM info");
        }
    }
}
