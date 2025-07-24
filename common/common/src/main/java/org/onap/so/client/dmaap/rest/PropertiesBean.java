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

package org.onap.so.client.dmaap.rest;

import java.util.Properties;

public class PropertiesBean {

    private String auth;
    private String key;
    private String environment;
    private String partition;
    private String contentType;
    private String host;
    private String topic;
    private String timeout;


    public PropertiesBean(Properties properties) {
        this.withAuth(properties.getProperty("auth")).withKey(properties.getProperty("key"))
                .withTopic(properties.getProperty("topic")).withEnvironment(properties.getProperty("environment"))
                .withHost(properties.getProperty("host")).withTimeout(properties.getProperty("timeout", "20000"))
                .withPartition(properties.getProperty("partition"))
                .withContentType(properties.getProperty("contentType", "application/json"));
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public PropertiesBean withAuth(String auth) {
        this.auth = auth;
        return this;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PropertiesBean withKey(String key) {
        this.key = key;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public PropertiesBean withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public PropertiesBean withPartition(String partition) {
        this.partition = partition;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public PropertiesBean withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public PropertiesBean withHost(String host) {
        this.host = host;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public PropertiesBean withTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public PropertiesBean withTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }



}
