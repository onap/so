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

package org.onap.aaiclient.client.aai;

import org.onap.so.client.CacheProperties;
import org.onap.so.client.RestProperties;
import org.onap.so.utils.BasicAuth;

public interface AAIProperties extends RestProperties {

    public AAIVersion getDefaultVersion();

    default String getUsername() {
        return null;
    }

    default String getPassword() {
        return null;
    }

    /**
     * @return {@code username:password}, or {@code null} when no username is configured
     */
    default String getAuth() {
        return BasicAuth.credentials(getUsername(), getPassword());
    }

    /**
     * @deprecated credentials are no longer encrypted, so no key is needed
     */
    @Deprecated
    default String getKey() {
        return null;
    }

    @Override
    public default boolean mapNotFoundToEmpty() {
        return true;
    }

    default CacheProperties getCacheProperties() {
        return new AAICacheProperties() {};
    }

    public interface AAICacheProperties extends CacheProperties {

        default String getCacheName() {
            return "aai-http-cache";
        }
    }
}
