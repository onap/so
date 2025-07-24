/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.objects.audit;

import java.io.Serializable;
import java.net.URI;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AAIObjectAudit implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4560928512855386021L;
    private boolean doesObjectExist = false;
    private Object aaiObject;
    private URI resourceURI;
    private String aaiObjectType;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("doesObjectExist", doesObjectExist).append("aaiObject", aaiObject)
                .append("resourceURI", resourceURI).append("aaiObjectType", aaiObjectType).toString();
    }

    public String getAaiObjectType() {
        return aaiObjectType;
    }

    public void setAaiObjectType(String aaiObjectType) {
        this.aaiObjectType = aaiObjectType;
    }

    public boolean isDoesObjectExist() {
        return doesObjectExist;
    }

    public void setDoesObjectExist(boolean doesObjectExist) {
        this.doesObjectExist = doesObjectExist;
    }

    public Object getAaiObject() {
        return aaiObject;
    }

    public void setAaiObject(Object aaiObject) {
        this.aaiObject = aaiObject;
    }

    public URI getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(URI resourceURI) {
        this.resourceURI = resourceURI;
    }

}
