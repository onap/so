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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.onap.so.logger.MsoLogger;


public class ResponseDecoderUsingJackson implements ResponseDecoder {
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, ResponseDecoderUsingJackson.class);

    private final Class clazz;
    protected final ObjectMapper objectMapper = new ObjectMapper();


    public ResponseDecoderUsingJackson(final Class clazz) {
        this.clazz = clazz;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
    }

    @Override
    public <T> T decode(@Nonnull final Response response) {
        try {
            return objectMapper
                .readValue(response.body().asInputStream(), TypeFactory.defaultInstance().constructType(clazz));
        } catch (final IOException e) {
            LOGGER.debug("Unable to map response body to an object of type: " + clazz.getSimpleName());
        }
        return null;
    }

    @Override
    public <T> List<T> decodeAsList(@Nonnull final Response response) {
        try {
            return objectMapper.readValue(response.body().asInputStream(),
                TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch (final IOException e) {
            LOGGER.debug("Unable to map response body to a list of :" + clazz.getSimpleName());
        }
        return Collections.emptyList();
    }
}
