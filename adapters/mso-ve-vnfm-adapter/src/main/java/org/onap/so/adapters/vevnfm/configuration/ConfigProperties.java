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

import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigProperties {

    @Value("${vevnfmadapter.vnf-filter-json}")
    private String vevnfmadapterVnfFilterJson;

    @Value("${vevnfmadapter.endpoint}")
    private String vevnfmadapterEndpoint;

    @Value("${mso.key}")
    private String msoKey;

    @Value("${aai.endpoint}")
    private String aaiEndpoint;

    @Value("${aai.auth}")
    private String aaiAuth;

    @Value("${vnfm.default-endpoint}")
    private String vnfmDefaultEndpoint;

    @Value("${vnfm.subscription}")
    private String vnfmSubscription;

    @Value("${vnfm.notification}")
    private String vnfmNotification;

    @Value("${notification.vnf-filter-type}")
    private NotificationVnfFilterType notificationVnfFilterType;

    @Value("${dmaap.endpoint}")
    private String dmaapEndpoint;

    @Value("${dmaap.topic}")
    private String dmaapTopic;

    @Value("${dmaap.closed-loop.control.name}")
    private String dmaapClosedLoopControlName;

    @Value("${dmaap.version}")
    private String dmaapVersion;

    @Value("${spring.security.usercredentials[0].username}")
    private String springSecurityUsername;

    @Value("${spring.security.usercredentials[0].openpass}")
    private String springSecurityOpenpass;

    public String getVevnfmadapterVnfFilterJson() {
        return vevnfmadapterVnfFilterJson;
    }

    public String getVevnfmadapterEndpoint() {
        return vevnfmadapterEndpoint;
    }

    public String getMsoKey() {
        return msoKey;
    }

    public String getAaiEndpoint() {
        return aaiEndpoint;
    }

    public String getAaiAuth() {
        return aaiAuth;
    }

    public String getVnfmDefaultEndpoint() {
        return vnfmDefaultEndpoint;
    }

    public String getVnfmSubscription() {
        return vnfmSubscription;
    }

    public String getVnfmNotification() {
        return vnfmNotification;
    }

    public NotificationVnfFilterType getNotificationVnfFilterType() {
        return notificationVnfFilterType;
    }

    public String getDmaapEndpoint() {
        return dmaapEndpoint;
    }

    public String getDmaapTopic() {
        return dmaapTopic;
    }

    public String getDmaapClosedLoopControlName() {
        return dmaapClosedLoopControlName;
    }

    public String getDmaapVersion() {
        return dmaapVersion;
    }

    public String getSpringSecurityUsername() {
        return springSecurityUsername;
    }

    public String getSpringSecurityOpenpass() {
        return springSecurityOpenpass;
    }
}
