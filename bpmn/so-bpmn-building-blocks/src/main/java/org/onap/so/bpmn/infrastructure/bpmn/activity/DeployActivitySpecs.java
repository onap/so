/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.bpmn.activity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeployActivitySpecs {
    private static final String ACTIVITY_FILE_LOCATION = "src/main/resources/ActivitySpec/";
    private static final String ACTIVITY_SPEC_URI = "/activityspec-api/v1.0/activity-spec";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final Logger logger = LoggerFactory.getLogger(DeployActivitySpecs.class);

    public static void main(String[] args) throws Exception {

        if (args == null || args.length == 0) {
            logger.info("Please specify hostname argument");
            return;
        }

        String hostname = args[0];

        File dir = new File(ACTIVITY_FILE_LOCATION);
        if (!dir.isDirectory()) {
            logger.debug("ActivitySpec store is not a directory");
            return;
        }

        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                String activitySpecName = f.getName();
                String errorMessage = deployActivitySpec(hostname, activitySpecName);
                if (errorMessage == null) {
                    logger.debug("Deployed Activity Spec: {}", activitySpecName);
                } else {
                    logger.error("Error deploying Activity Spec: {} : {}", activitySpecName, errorMessage);
                }
            }
        } else {
            logger.error("Null file list for Activity Specs.");
        }
    }

    protected static String deployActivitySpec(String hostname, String activitySpecName) throws Exception {
        String payload = new String(Files.readAllBytes(Paths.get(ACTIVITY_FILE_LOCATION + activitySpecName)));
        try {
            HttpClient client = HttpClientBuilder.create().build();

            String url = UriBuilder.fromUri(hostname).path(ACTIVITY_SPEC_URI).build().toString();
            HttpPost post = new HttpPost(url);

            StringEntity input = new StringEntity(payload);
            input.setContentType(CONTENT_TYPE_JSON);
            post.setEntity(input);

            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine != null) {
                if (statusLine.getStatusCode() != 200) {
                    return (statusLine.toString());
                } else {
                    return null;
                }
            } else {
                return ("Empty response from the remote endpoint");
            }

        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
