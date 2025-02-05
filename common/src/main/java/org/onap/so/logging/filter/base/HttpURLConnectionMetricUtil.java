/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.filter.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.HttpURLConnection;

public class HttpURLConnectionMetricUtil
        extends AbstractMetricLogFilter<HttpURLConnection, HttpURLConnection, HttpURLConnection> {
    protected static final Logger logger = LoggerFactory.getLogger(HttpURLConnectionMetricUtil.class);

    public void logBefore(HttpURLConnection request, ONAPComponentsList targetEntity) {
        setTargetEntity(targetEntity);
        pre(request, request);
    }

    public void logAfter(HttpURLConnection request) {
        post(request, request);
    }

    @Override
    protected String getTargetServiceName(HttpURLConnection request) {
        return request.getURL().getPath();
    }

    @Override
    protected int getHttpStatusCode(HttpURLConnection response) {
        try {
            return response.getResponseCode();
        } catch (Exception e) {
            logger.error("getHttpStatusCode failed, defaulting to 500", e);
        }
        return 500;
    }

    @Override
    protected String getResponseCode(HttpURLConnection response) {
        try {
            return String.valueOf(response.getResponseCode());
        } catch (Exception e) {
            logger.error("getResponseCode failed, defaulting to 500", e);
        }
        return "500";
    }

    @Override
    protected String getTargetEntity(HttpURLConnection request) {
        return Constants.DefaultValues.UNKNOWN_TARGET_ENTITY;
    }

    @Override
    protected void addHeader(HttpURLConnection request, String headerName, String headerValue) {
        request.setRequestProperty(headerName, headerValue);
    }
}
