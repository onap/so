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

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "subscriberInfo")
@JsonInclude(Include.NON_DEFAULT)
public class SubscriberInfo implements Serializable {

    private static final long serialVersionUID = -1750701712128104652L;
    @JsonProperty("globalSubscriberId")
    protected String globalSubscriberId;
    @JsonProperty("subscriberName")
    protected String subscriberName;

    /**
     * Gets the value of the globalSubscriberId property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    /**
     * Sets the value of the globalSubscriberId property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGlobalSubscriberId(String value) {
        this.globalSubscriberId = value;
    }

    /**
     * Gets the value of the subscriberName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSubscriberName() {
        return subscriberName;
    }

    /**
     * Sets the value of the subscriberName property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSubscriberName(String value) {
        this.subscriberName = value;
    }

    @Override
    public String toString() {
        return "SubscriberInfo [globalSubscriberId=" + globalSubscriberId + ", subscriberName=" + subscriberName + "]";
    }

}
