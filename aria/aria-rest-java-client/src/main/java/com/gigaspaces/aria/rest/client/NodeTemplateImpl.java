/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
*/
package com.gigaspaces.aria.rest.client;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by DeWayne on 7/18/2017.
 */
public class NodeTemplateImpl implements NodeTemplate {
    private int id;
    private String name;
    private String description="";
    @JsonProperty("service_template_id")
    private int service_template_id;
    @JsonProperty("type_name")
    private String type_name="";

    public NodeTemplateImpl(){}

    public NodeTemplateImpl(int id, String name, String description, int service_template_id, String type_name){
        this.id=id;
        this.description=description;
        this.service_template_id=service_template_id;
        this.type_name=type_name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getServiceTemplateId() {
        return service_template_id;
    }

    public String getTypeName() {
        return type_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
