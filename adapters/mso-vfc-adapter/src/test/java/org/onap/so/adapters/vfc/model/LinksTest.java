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

import org.junit.Test;
import static org.junit.Assert.*;

public class LinksTest {
    Links links = new Links();

    @Test
    public void getSelf() {
        links.getSelf();
    }

    @Test
    public void setSelf() {
        links.setSelf(new Link());
    }

    @Test
    public void getNsInstance() {
        links.getNsInstance();
    }

    @Test
    public void setNsInstance() {
        links.setNsInstance(new Link());
    }

    @Test
    public void getCancel() {
        links.getCancel();
    }

    @Test
    public void setCancel() {
        links.setCancel(new Link());
    }

    @Test
    public void getRetry() {
        links.getRetry();
    }

    @Test
    public void setRetry() {
        links.setRetry(new Link());
    }

    @Test
    public void getRollback() {
        links.getRollback();
    }

    @Test
    public void setRollback() {
        links.setRollback(new Link());
    }

    @Test
    public void getContinues() {
        links.getContinues();
    }

    @Test
    public void setContinues() {
        links.setContinues(new Link());
    }

    @Test
    public void getFail() {
        links.getFail();
    }

    @Test
    public void setFail() {
        links.setFail(new Link());
    }
}
