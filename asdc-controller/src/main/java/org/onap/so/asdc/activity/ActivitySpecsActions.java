/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.activity;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.logger.LoggingAnchor;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.net.URL;

@Component
public class ActivitySpecsActions {

    private static final String ACTIVITY_SPEC_URI = "/v1.0/activity-spec";
    private static final String ACTIVITY_SPEC_URI_SUFFIX = "/versions/latest/actions";
    private static final String CERTIFY_ACTIVITY_PAYLOAD = "{\"action\": \"CERTIFY\"}";
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    private final HttpClientFactory httpClientFactory = new HttpClientFactory();
    protected static final Logger logger = LoggerFactory.getLogger(ActivitySpecsActions.class);

    public String createActivitySpec(String hostname, ActivitySpec activitySpec) {
        if (activitySpec == null) {
            return null;
        }

        try {
            String payload = mapper.writer().writeValueAsString(activitySpec);

            String urlString = UriBuilder.fromUri(hostname).path(ACTIVITY_SPEC_URI).build().toString();
            URL url = new URL(urlString);

            HttpClient httpClient = httpClientFactory.newJsonClient(url, ONAPComponents.SDC);
            httpClient.addAdditionalHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

            Response response = httpClient.post(payload);

            int statusCode = response.getStatus();
            if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                logger.warn(LoggingAnchor.THREE, "ActivitySpec", activitySpec.getName(), "already exists in SDC");
                return null;
            }
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
                logger.warn(LoggingAnchor.THREE, "Error creating activity spec", activitySpec.getName(), statusCode);
                return null;
            }

            if (response.getEntity() == null) {
                logger.warn(LoggingAnchor.TWO, "No activity spec response returned", activitySpec.getName());
                return null;
            }
            ActivitySpecCreateResponse activitySpecCreateResponse =
                    response.readEntity(ActivitySpecCreateResponse.class);
            if (activitySpecCreateResponse == null) {
                logger.warn(LoggingAnchor.TWO, "Unable to read activity spec", activitySpec.getName());
                return null;
            }
            return activitySpecCreateResponse.getId();


        } catch (Exception e) {
            logger.warn(LoggingAnchor.TWO, "Exception creating activitySpec", e);
        }

        return null;
    }

    public boolean certifyActivitySpec(String hostname, String activitySpecId) {
        if (activitySpecId == null) {
            return false;
        }

        try {
            String path = ACTIVITY_SPEC_URI + "/" + activitySpecId + ACTIVITY_SPEC_URI_SUFFIX;

            String urlString = UriBuilder.fromUri(hostname).path(path).build().toString();
            URL url = new URL(urlString);

            HttpClient httpClient = httpClientFactory.newJsonClient(url, ONAPComponents.SDC);
            httpClient.addAdditionalHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

            Response response = httpClient.put(CERTIFY_ACTIVITY_PAYLOAD);

            int statusCode = response.getStatus();

            if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                logger.warn(LoggingAnchor.THREE, "ActivitySpec with id", activitySpecId, "is already certified in SDC");
                return false;
            }
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn(LoggingAnchor.THREE, "Error certifying activity", activitySpecId, statusCode);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.warn(LoggingAnchor.TWO, "Exception certifying activitySpec", e);
            return false;
        }

    }
}
