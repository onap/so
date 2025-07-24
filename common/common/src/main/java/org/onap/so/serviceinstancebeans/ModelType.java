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

package org.onap.so.serviceinstancebeans;

import java.lang.reflect.InvocationTargetException;
import com.google.common.base.CaseFormat;

/*
 * Enum for Model Type values returned by API Handler to BPMN
 */
public enum ModelType {
    service("serviceInstance"),
    vnf("vnf"),
    vfModule("vfModule"),
    volumeGroup("volumeGroup"),
    network("network"),
    configuration("configuration"),
    connectionPoint("connectionPoint"),
    pnf("pnf"),
    networkInstanceGroup("networkInstanceGroup"),
    instanceGroup("instanceGroup"),
    vpnBinding("vpnBinding"),
    cnf("cnf");


    final String name;

    private ModelType(String name) {
        this.name = name;
    }


    public <T> T getId(Object obj) {
        return this.get(obj, "Id");
    }

    public <T> T getName(Object obj) {
        return this.get(obj, "Name");
    }

    public void setId(Object obj, Object value) {
        this.set(obj, "Id", value);
    }

    public void setName(Object obj, Object value) {
        this.set(obj, "Name", value);
    }

    protected <T> T get(Object obj, String field) {
        T result = null;
        if (obj != null) {
            try {
                result = (T) obj.getClass().getMethod(String.format("%s%s%s", "get",
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this.name), field)).invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                // silent fail
            }
        }

        return result;
    }

    protected void set(Object obj, String field, Object value) {
        if (obj != null) {
            try {
                obj.getClass()
                        .getMethod(
                                String.format("%s%s%s", "set",
                                        CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this.name), field),
                                value.getClass())
                        .invoke(obj, value);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                // silent fail
            }
        }
    }
}
