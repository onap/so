/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.onap.so.bpmn.common.WorkflowTestTransformer;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;

@Configuration
@Profile({"test"})
public class TestApplicationConfig {

    @Bean
    protected ResponseTransformer[] transformerArray() {
        // Process WorkflowTestTransformer annotations
        List<ResponseTransformer> transformerList = new ArrayList<ResponseTransformer>();

        for (Field field : getClass().getFields()) {
            WorkflowTestTransformer annotation = field.getAnnotation(WorkflowTestTransformer.class);

            if (annotation == null) {
                continue;
            }

            if (!Modifier.isStatic(field.getModifiers())) {
                throw new RuntimeException(field.getDeclaringClass().getName() + "#" + field.getName()
                        + " has a @WorkflowTestTransformer " + " annotation but it is not declared static");
            }

            ResponseTransformer transformer;

            try {
                transformer = (ResponseTransformer) field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        field.getDeclaringClass().getName() + "#" + field.getName() + " is not accessible", e);
            } catch (ClassCastException e) {
                throw new RuntimeException(
                        field.getDeclaringClass().getName() + "#" + field.getName() + " is not a ResponseTransformer",
                        e);
            }

            if (transformer == null) {
                continue;
            }

            transformerList.add(transformer);
        }

        ResponseTransformer[] transformerArray =
                transformerList.toArray(new ResponseTransformer[transformerList.size()]);

        optionsCustomizer(transformerArray);

        return transformerArray;
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer(ResponseTransformer[] transformerArray) {
        return new WireMockConfigurationCustomizer() {
            @Override
            public void customize(WireMockConfiguration options) {
                options.extensions(transformerArray);
            }
        };
    }

}
