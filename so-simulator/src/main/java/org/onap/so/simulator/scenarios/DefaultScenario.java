/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.simulator.scenarios;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
@Scenario("Default")
public class DefaultScenario extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.http().receive((builder -> builder.get()));

        scenario.http().send((builder -> builder.response(HttpStatus.BAD_REQUEST)));

    }
}
