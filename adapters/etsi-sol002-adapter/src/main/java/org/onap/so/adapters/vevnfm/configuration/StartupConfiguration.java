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

package org.onap.so.adapters.vevnfm.configuration;

import java.util.List;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.service.StartupService;
import org.onap.so.adapters.vevnfm.service.SubscriptionScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
public class StartupConfiguration {

    public static final String TEST_PROFILE = "test";

    private final Environment environment;
    private final StartupService startupService;
    private final SubscriptionScheduler subscriptionScheduler;

    @Autowired
    public StartupConfiguration(final Environment environment, final StartupService startupService,
            final SubscriptionScheduler subscriptionScheduler) {
        this.environment = environment;
        this.startupService = startupService;
        this.subscriptionScheduler = subscriptionScheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        if (!environment.acceptsProfiles(Profiles.of(TEST_PROFILE))) {
            final List<EsrSystemInfo> infos = startupService.receiveVnfm();
            subscriptionScheduler.setInfos(infos);
        }
    }
}
