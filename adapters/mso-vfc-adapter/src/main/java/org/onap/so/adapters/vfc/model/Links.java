/***
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

public class Links {
    private Link self;
    private Link nsInstance;
    private Link cancel;
    private Link retry;
    private Link rollback;
    private Link continues;
    private Link fail;

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getNsInstance() {
        return nsInstance;
    }

    public void setNsInstance(Link nsInstance) {
        this.nsInstance = nsInstance;
    }

    public Link getCancel() {
        return cancel;
    }

    public void setCancel(Link cancel) {
        this.cancel = cancel;
    }

    public Link getRetry() {
        return retry;
    }

    public void setRetry(Link retry) {
        this.retry = retry;
    }

    public Link getRollback() {
        return rollback;
    }

    public void setRollback(Link rollback) {
        this.rollback = rollback;
    }

    public Link getContinues() {
        return continues;
    }

    public void setContinues(Link continues) {
        this.continues = continues;
    }

    public Link getFail() {
        return fail;
    }

    public void setFail(Link fail) {
        this.fail = fail;
    }
}
