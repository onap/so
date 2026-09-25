/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom. All rights reserved.
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

package org.onap.so.configuration;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.util.AntPathMatcher;
import io.micrometer.observation.ObservationPredicate;

/**
 * Keeps requests that only machines make out of the traces: the kubelet probes on the actuator health endpoint, and the
 * Camunda external task clients' fetchAndLock long-poll, which bpmn-infra receives a few hundred times an hour while no
 * workflow is running.
 */
@Configuration
public class UntracedPathsConfiguration {

    @Bean
    ObservationPredicate untracedPathsPredicate(
            @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath,
            @Value("${mso.tracing.untraced-paths-enabled:true}") boolean enabled) {
        List<String> untracedPaths = List.of(actuatorBasePath + "/health/**", "/**/external-task/fetchAndLock");
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return (name, context) -> {
            if (!enabled || !(context instanceof ServerRequestObservationContext)) {
                return true;
            }
            HttpServletRequest request = ((ServerRequestObservationContext) context).getCarrier();
            String path = request.getRequestURI().substring(request.getContextPath().length());
            return untracedPaths.stream().noneMatch(pattern -> pathMatcher.match(pattern, path));
        };
    }
}
