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

package org.onap.so.openstack.beans;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"routeTarget", "routeTargetRole"})
public class RouteTarget implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5425083030715789604L;
    private String routeTarget;
    private String routeTargetRole;

    public String getRouteTarget() {
        return routeTarget;
    }

    public void setRouteTarget(String routeTarget) {
        this.routeTarget = routeTarget;
    }

    public String getRouteTargetRole() {
        return routeTargetRole;
    }

    public void setRouteTargetRole(String routeTargetRole) {
        this.routeTargetRole = routeTargetRole;
    }


    @Override
    public String toString() {
        return "RouteTarget [routeTarget=" + routeTarget + ", routeTargetRole=" + routeTargetRole + "]";
    }

}
