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

package org.openecomp.mso.db.catalog.beans;


import java.io.Serializable;

public class HeatNestedTemplate implements Serializable {

    private String parentTemplateId;
    private String childTemplateId;
    private String providerResourceFile;
    public static final long serialVersionUID = -1322322139926390329L;

    public HeatNestedTemplate () {
        super ();
    }

    public void setParentTemplateId (String parentTemplateId) {
        this.parentTemplateId = parentTemplateId;
    }

    public String getParentTemplateId () {
        return this.parentTemplateId;
    }

    public void setChildTemplateId (String childTemplateId) {
        this.childTemplateId = childTemplateId;
    }

    public String getChildTemplateId () {
        return this.childTemplateId;
    }

    public void setProviderResourceFile (String providerResourceFile) {
        this.providerResourceFile = providerResourceFile;
    }

    public String getProviderResourceFile () {
        return this.providerResourceFile;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append("ParentTemplateId=").append(this.parentTemplateId);
        sb.append(", ChildTemplateId=").append(this.childTemplateId);
        if (this.providerResourceFile == null) {
            sb.append (", providerResourceFile=null");
        } else {
            sb.append(",providerResourceFile=").append(this.providerResourceFile);
        }
        return sb.toString ();
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof HeatNestedTemplate)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        HeatNestedTemplate hnt = (HeatNestedTemplate) o;
        if (hnt.getChildTemplateId () == this.childTemplateId && hnt.getParentTemplateId () == this.parentTemplateId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result;
        result = this.parentTemplateId.hashCode() + this.childTemplateId.hashCode();
        return result;
    }
}
