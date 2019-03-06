/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.rest.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author andrei.barcovschi@ericsson.com
 *
 */
public class Constants {

    public static final String PROCRESS_DEF_ID = "AFRFLOW:1:c6eea1b7-9722-11e8-8caf-022ac9304eeb";

    public static final String EMPTY_ARRAY_RESPONSE = "[]";

    public static final String PROCESS_INSTACE_ID = "5956a99d-9736-11e8-8caf-022ac9304eeb";

    public static final String EMPTY_STRING = "";

    public static final String SOURCE_TEST_FOLDER = "src/test/resources/camundaResponses/";

    public static final Path PROCESS_DEF_RESPONSE_JSON_FILE = Paths.get(SOURCE_TEST_FOLDER + "processDefinition.json");

    public static final Path ACTIVITY_INSTANCE_RESPONSE_JSON_FILE =
            Paths.get(SOURCE_TEST_FOLDER + "activityInstance.json");

    public static final Path PROCESS_INSTANCE_VARIABLES_RESPONSE_JSON_FILE =
            Paths.get(SOURCE_TEST_FOLDER + "processInstanceVariables.json");

    public static final Path PROCCESS_INSTANCE_RESPONSE_JSON_FILE =
            Paths.get(SOURCE_TEST_FOLDER + "processInstance.json");

    public static final Path SINGLE_PROCCESS_INSTANCE_RESPONSE_JSON_FILE =
            Paths.get(SOURCE_TEST_FOLDER + "singleprocessInstance.json");

    public static final Path SEARCH_RESULT_RESPONSE_JSON_FILE =
            Paths.get("src/test/resources/databaseResponses/searchResult.json");

    public static final String ID = UUID.randomUUID().toString();

    public static final long END_TIME_IN_MS = 1546351200000l;

    public static final long START_TIME_IN_MS = 1546346700000l;

    private Constants() {}

}
