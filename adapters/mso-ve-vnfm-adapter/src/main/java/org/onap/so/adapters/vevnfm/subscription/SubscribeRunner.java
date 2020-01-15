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

package org.onap.so.adapters.vevnfm.subscription;

import org.apache.logging.log4j.util.Strings;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.onap.so.adapters.vevnfm.service.AaiConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubscribeRunner implements CommandLineRunner {

    public static final String URL = "url";

    @Autowired
    private AaiConnectionService service;

    @Autowired
    private Subscriber subscriber;

    @Override
    public void run(String... args) throws Exception {
        final Map vnfm = service.receiveVnfm();
        isValid(vnfm);
        final boolean done = subscriber.subscribe(vnfm);

        if (!done) {
            throw new VeVnfmException("Could not subscribe to VNFM", null);
        }
    }

    public static String getUrl(final Map vnfm) {
        return (String) vnfm.get(URL);
    }

    private static void isValid(final Map vnfm) throws VeVnfmException {
        if (Strings.isBlank(getUrl(vnfm))) {
            throw new VeVnfmException("No 'url' field in VNFM info", null);
        }
    }
}
