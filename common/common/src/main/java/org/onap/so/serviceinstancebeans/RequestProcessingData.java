/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.serviceinstancebeans;

import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.ToStringBuilder;


@JsonInclude(Include.NON_DEFAULT)
public class RequestProcessingData {

    protected String groupingId;
    protected String tag;
    protected List<HashMap<String, String>> dataPairs;

    public String getGroupingId() {
        return groupingId;
    }

    public void setGroupingId(String groupingId) {
        this.groupingId = groupingId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<HashMap<String, String>> getDataPairs() {
        return dataPairs;
    }

    public void setDataPairs(List<HashMap<String, String>> dataPairs) {
        this.dataPairs = dataPairs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("groupingId", groupingId).append("tag", tag)
                .append("dataPairs", dataPairs).toString();
    }


}
