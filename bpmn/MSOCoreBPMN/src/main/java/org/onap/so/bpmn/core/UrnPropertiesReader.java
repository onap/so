/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.core;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


/**
 * Read the URN property value from the execution object or from the spring environment object
 */
@Component
@Configuration
public class UrnPropertiesReader {
    private static final Logger logger = LoggerFactory.getLogger(UrnPropertiesReader.class);
    private static Environment environment;

    @Autowired
	public void setEnvironment(Environment environment) {
	this.environment = environment;
	}
	/**
     * Return the URN property value
     * if property is present in the execution object, return the same
     * else search in the environment object. If found, add it to the execution object and return the value
     * otherwise return null
     *
     * @param variableName URN property name
     * @param execution    The flow's execution instance.
     * @return URN property value
     */
    public static String getVariable(String variableName, DelegateExecution execution) {
        Object value = execution.getVariable(variableName);
        if (value != null) {
            logger.trace("Retrieved value for the URN variable, {}, from the execution object: {}", variableName,
                String.valueOf(value));
            return String.valueOf(value);
        }
        String variableValue = null;
        if (environment != null && environment.getProperty(variableName) != null) {
            variableValue = environment.getProperty(variableName);
            logger.trace("Retrieved value for the URN variable, {}, from the environment variable: {}", variableName,
                variableValue);
            execution.setVariable(variableName, variableValue);
            return variableValue;
        }
        return variableValue;
    }

    public static String getVariable(String variableName, DelegateExecution execution, String defaultValue) {
        return Optional.ofNullable(getVariable(variableName, execution)).orElse(defaultValue);
    }

    /**
     * Return the URN property value from the environment object
     * @param variableName URN property name
     * @return URN property value
     */

    public static String getVariable(String variableName){
        if (environment != null) {
            return environment.getProperty(variableName);
        } else {
            return null;
        }
    }
    
    /**
     * Return the String array URN property value from the environment object
     * @param variableName URN property name
     * @return URN property value
     */

    public static String[] getVariablesArray(String variableName){
        if (environment != null) {
            return environment.getProperty(variableName, String[].class);
        } else {
            return null;
        }
    }
    
    public static String getVariable(String variableName, String defaultValue) {
    	return Optional.ofNullable(getVariable(variableName)).orElse(defaultValue); 
    }
}
