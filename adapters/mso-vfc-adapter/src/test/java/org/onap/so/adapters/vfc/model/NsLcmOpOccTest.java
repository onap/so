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
import org.onap.so.adapters.vfc.constant.CommonConstant;
import java.time.LocalDateTime;
import java.util.Date;
import static org.junit.Assert.*;

public class NsLcmOpOccTest {
    NsLcmOpOcc nsLcmOpOcc = new NsLcmOpOcc();

    @Test
    public void getLcmOperationType() {
        nsLcmOpOcc.getLcmOperationType();
    }

    @Test
    public void setLcmOperationType() {
        nsLcmOpOcc.setLcmOperationType(CommonConstant.lcmOperationType.INSTANTIATE);
    }

    @Test
    public void getCancelMode() {
        nsLcmOpOcc.getCancelMode();
    }

    @Test
    public void setCancelMode() {
        nsLcmOpOcc.setCancelMode(CommonConstant.cancelMode.GRACEFUL);
    }

    @Test
    public void getOperationState() {
        nsLcmOpOcc.getOperationState();
    }

    @Test
    public void setOperationState() {
        nsLcmOpOcc.setOperationState(CommonConstant.operationState.COMPLETED);
    }

    @Test
    public void getId() {
        nsLcmOpOcc.getId();
    }

    @Test
    public void setId() {
        nsLcmOpOcc.setId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getStatusEnteredTime() {
        nsLcmOpOcc.getStatusEnteredTime();
    }

    @Test
    public void setStatusEnteredTime() {
        nsLcmOpOcc.setStatusEnteredTime(LocalDateTime.now().toString());
    }

    @Test
    public void getNsInstanceId() {
        nsLcmOpOcc.getNsInstanceId();
    }

    @Test
    public void setNsInstanceId() {
        nsLcmOpOcc.setNsInstanceId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getStartTime() {
        nsLcmOpOcc.getStartTime();
    }

    @Test
    public void setStartTime() {
        nsLcmOpOcc.setStartTime(LocalDateTime.now().toString());
    }

    @Test
    public void getAutomaticInvocation() {
        nsLcmOpOcc.getAutomaticInvocation();
    }

    @Test
    public void setAutomaticInvocation() {
        nsLcmOpOcc.setAutomaticInvocation(true);
    }

    @Test
    public void getOperationParams() {
        nsLcmOpOcc.getOperationParams();
    }

    @Test
    public void setOperationParams() {
        nsLcmOpOcc.setOperationParams("Dummy operationParams");
    }

    @Test
    public void getCancelPending() {
        nsLcmOpOcc.getCancelPending();
    }

    @Test
    public void setCancelPending() {
        nsLcmOpOcc.setCancelPending(true);
    }

    @Test
    public void getError() {
        nsLcmOpOcc.getError();
    }

    @Test
    public void setError() {
        nsLcmOpOcc.setError(new ProblemDetails());
    }

    @Test
    public void getLinks() {
        nsLcmOpOcc.getLinks();
    }

    @Test
    public void setLinks() {
        nsLcmOpOcc.setLinks(new Links());
    }
}
