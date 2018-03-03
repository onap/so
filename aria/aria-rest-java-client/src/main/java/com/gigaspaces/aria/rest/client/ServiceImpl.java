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

import java.util.Date;

/**
 * 
 *
 * Created by DeWayne on 7/17/2017.
 */
public class ServiceImpl implements Service {
    private int id;
    private String description, name, template;
    private Date created, updated;

    public int getId(){
        return id;
    }

    public String getDescription(){
        return description;
    }

    public String getName(){
        return name;
    }

    public String getServiceTemplate(){
	return template;
    }

    public Date getCreated(){
        return created;
    }

    public Date getUpdated(){
        return updated;
    }
    
        
}    
