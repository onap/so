/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.rest.service;

import org.springframework.http.ResponseEntity;

import com.google.common.base.Optional;

/**
 * @author waqas.ikram@est.tech
 */
public interface HttpRestServiceProvider {

    /**
     * Execute the HTTP GET to the given URI template
     * 
     * @param url the URL
     * @param clazz the type of the return value
     * @return Returns the body of this entity.
     */
    public <T> Optional<T> get(final String url, final Class<T> clazz);

    /**
     * Execute the HTTP GET to the given URI template
     * 
     * @param url the URL
     * @param clazz the type of the return value
     * @return Returns the {@link ResponseEntity}.
     */
    public <T> ResponseEntity<T> getHttpResponse(final String url, final Class<T> clazz);


    /**
     * Execute the HTTP POST to the given URI template
     * 
     * @param object the entity (i.e. body) to write to the request
     * @param url the URL
     * @param clazz the type of the return value
     * @return Returns the body of this entity.
     */
    public <T> Optional<T> post(final Object object, final String url, final Class<T> clazz);

    /**
     * Execute the HTTP POST to the given URI template
     * 
     * @param object the entity (i.e. body) to write to the request
     * @param url the URL
     * @param clazz the type of the return value
     * @return Returns the {@link ResponseEntity}.
     */
    public <T> ResponseEntity<T> postHttpRequest(final Object object, final String url, final Class<T> clazz);


}
