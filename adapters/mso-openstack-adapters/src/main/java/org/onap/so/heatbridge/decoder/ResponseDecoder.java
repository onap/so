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

import feign.Response;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface needs to be used when implementing a custom decoder to be used when parsing a Feign {@link Response}
 */
public interface ResponseDecoder {

    /**
     * Decode a Feign {@link Response} to a specific object which type is inferred.
     *
     * @param response The Feign response object.
     * @param <T> The type of object to be returned.
     * @return An object of the inferred type.
     */
    @Nullable <T> T decode(@Nonnull Response response);

    /**
     * Decode a Feign {@link Response} to a List of objects whose type is inferred.
     *
     * @param response The Feign response object.
     * @param <T> The type of object to be returned.
     * @return A list of objects of the inferred type.
     */
    <T> List<T> decodeAsList(@Nonnull Response response);
}
