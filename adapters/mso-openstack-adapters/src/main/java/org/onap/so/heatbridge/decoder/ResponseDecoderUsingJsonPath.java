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
package org.onap.so.heatbridge.decoder;

import com.jayway.jsonpath.JsonPath;
import feign.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.onap.so.logger.MsoLogger;


public class ResponseDecoderUsingJsonPath implements ResponseDecoder {
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, ResponseDecoderUsingJackson.class);

    private final String jsonPath;

    public ResponseDecoderUsingJsonPath(final String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @Override
    public <T> T decode(@Nonnull Response response) {
        try {
            return JsonPath.read(response.body().asInputStream(), jsonPath);
        } catch (final IOException e) {
            LOGGER.debug("Unable to map response body using json path expression: " + jsonPath);
        }
        return null;
    }

    @Override
    public <T> List<T> decodeAsList(@Nonnull Response response) {
        try {
            return JsonPath.read(response.body().asInputStream(), jsonPath);
        } catch (final IOException e) {
            LOGGER.debug("Unable to map response body using json path expression: " + jsonPath);
        }
        return Collections.emptyList();
    }
}
