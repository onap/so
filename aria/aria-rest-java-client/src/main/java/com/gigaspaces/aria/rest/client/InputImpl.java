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

/**
 * Created by DeWayne on 7/17/2017.
 */
public class InputImpl implements Input {
    private String name, description, value;

    public InputImpl(){}

    public InputImpl(String name,String value,String description){
        if(name==null || value==null){
            throw new IllegalArgumentException("null argument supplied");
        }
        this.name=name;
        this.value=value;
        if(description!=null)this.description=description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

}
