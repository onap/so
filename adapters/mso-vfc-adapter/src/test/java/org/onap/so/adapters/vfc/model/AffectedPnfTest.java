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

public class AffectedPnfTest {
    AffectedPnf affectedPnf = new AffectedPnf();

    @Test
    public void getPnfid() {
        affectedPnf.getPnfid();
    }

    @Test
    public void setPnfid() {
        affectedPnf.setPnfid("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getPnfdid() {
        affectedPnf.getPnfdid();
    }

    @Test
    public void setPnfdid() {
        affectedPnf.setPnfdid("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getPnfProfileId() {
        affectedPnf.getPnfProfileId();
    }

    @Test
    public void setPnfProfileId() {
        affectedPnf.setPnfProfileId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getPnfName() {
        affectedPnf.getPnfName();
    }

    @Test
    public void setPnfName() {
        affectedPnf.setPnfName("Router");
    }

    @Test
    public void getCpInstanceId() {
        affectedPnf.getCpInstanceId();
    }

    @Test
    public void setCpInstanceId() {
        affectedPnf.setCpInstanceId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }
}
