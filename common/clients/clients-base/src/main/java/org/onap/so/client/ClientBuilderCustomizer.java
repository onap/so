/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.client;

import javax.ws.rs.client.ClientBuilder;

/**
 * Allows customization of the JAX-RS {@link ClientBuilder} used by {@link RestClient}.
 * <p>
 * Implementations can register additional filters or features (e.g. for distributed trace context propagation) without
 * replacing the existing SSL, timeout, and caching configuration.
 */
@FunctionalInterface
public interface ClientBuilderCustomizer {

    ClientBuilder customize(ClientBuilder builder);
}
