/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import javax.validation.constraints.NotNull;
import java.util.List;

public class NsInstanceLinks {
    @NotNull
    private Link self;
    private List<Link> nestedNsInstances;
    private Link instantiate;
    private Link terminate;
    private Link update;
    private Link scale;
    private Link heal;

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public List<Link> getNestedNsInstances() {
        return nestedNsInstances;
    }

    public void setNestedNsInstances(List<Link> nestedNsInstances) {
        this.nestedNsInstances = nestedNsInstances;
    }

    public Link getInstantiate() {
        return instantiate;
    }

    public void setInstantiate(Link instantiate) {
        this.instantiate = instantiate;
    }

    public Link getTerminate() {
        return terminate;
    }

    public void setTerminate(Link terminate) {
        this.terminate = terminate;
    }

    public Link getUpdate() {
        return update;
    }

    public void setUpdate(Link update) {
        this.update = update;
    }

    public Link getScale() {
        return scale;
    }

    public void setScale(Link scale) {
        this.scale = scale;
    }

    public Link getHeal() {
        return heal;
    }

    public void setHeal(Link heal) {
        this.heal = heal;
    }
}
