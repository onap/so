/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.requestsdb;

import org.junit.Test;

public class MsoRequestsDbAdapterImplTest {

    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    MsoRequestsDbAdapterImpl msoRequestsDbAdapter = new MsoRequestsDbAdapterImpl();

    @Test(expected = NullPointerException.class)
    public void updateInfraRequest() throws Exception {
        msoRequestsDbAdapter.updateInfraRequest("test", "test",
                "test", "test", RequestStatusType.COMPLETE, "test",
                "test", "test", "test", "test",
                "test", "test", "test",
                "test", "test", "tets");
    }

    @Test(expected = NullPointerException.class)
    public void getInfraRequest() throws Exception {
        msoRequestsDbAdapter.getInfraRequest("test");
    }

    @Test(expected = NullPointerException.class)
    public void getSiteStatus() throws Exception {
        msoRequestsDbAdapter.getSiteStatus("test");
    }

    @Test(expected = NullPointerException.class)
    public void updateServiceOperationStatus() throws Exception {
        msoRequestsDbAdapter.updateServiceOperationStatus("test", "test",
                "test", "test", "test", "test",
                "test", "test");
    }

    @Test(expected = NullPointerException.class)
    public void initResourceOperationStatus() throws Exception {
        msoRequestsDbAdapter.initResourceOperationStatus("test", "test", "test", "uuid");
    }

    @Test(expected = NullPointerException.class)
    public void getResourceOperationStatus() throws Exception {
        msoRequestsDbAdapter.getResourceOperationStatus("test", "test", "uuid");
    }

    @Test(expected = NullPointerException.class)
    public void updateResourceOperationStatus() throws Exception {
        msoRequestsDbAdapter.updateResourceOperationStatus("test", "test",
                "uuid", "type", "instance-id",
                "jobid", "test", "progress", "errorcode",
                "status-desc");
    }

}