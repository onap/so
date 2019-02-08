/*-
 * Copyright (C) 2017 Bell Canada. All rights reserved.
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
 */
package org.onap.so.heatbridge.utils;

import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.onap.so.logger.MsoLogger;


public class FeignUtils {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, FeignUtils.class);

    private FeignUtils() {
    }

    public static String extractResponseBody(final Response response) {
        String responseBodyAsString = "";
        if (response != null && response.body() != null) {
            try (InputStream inputStream = response.body().asInputStream()) {
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                responseBodyAsString = "Unable to extract body from response";
                LOGGER.debug(responseBodyAsString + response, e);
            }
        }
        return responseBodyAsString;
    }

}
