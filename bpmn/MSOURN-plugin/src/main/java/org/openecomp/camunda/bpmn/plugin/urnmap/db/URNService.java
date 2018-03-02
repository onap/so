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

package org.openecomp.camunda.bpmn.plugin.urnmap.db;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class URNService {


  public List<URNData> getProperties() {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
    ProcessEngineConfigurationImpl processEngineConfiguration = processEngine.getProcessEngineConfiguration();

    MyBatisQueryCommandExecutor commandExecutor = new MyBatisQueryCommandExecutor(processEngineConfiguration, "mappings.xml");
    return commandExecutor.executeQueryCommand(new Command<List<URNData>>() {

      @SuppressWarnings("unchecked")
      public List<URNData> execute(CommandContext commandContext) {
        return (List<URNData>) commandContext.getDbSqlSession().selectList("retrieveUrnKeyValuePair", null);
      }
    });
  }

}
