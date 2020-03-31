/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
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
package org.onap.so.adapters.catalogdb.catalogrest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.jsonpath.JsonPathUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class QueryServiceInfoTest {

    @Test
    public void serviceInfoTest() {
        QueryServiceInfo queryServiceInfo = new QueryServiceInfo(createList());
        String jsonResult = queryServiceInfo.JSON2(true, false);
        String serviceInfo = jsonResult.substring(jsonResult.indexOf("{"), jsonResult.length());
        Assertions.assertThat(JsonPathUtil.getInstance().locateResult(serviceInfo, "$.id").get()).isEqualTo("1");
    }

    private List<ServiceInfo> createList() {
        Service service = mock(Service.class);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(1);
        serviceInfo.setService(service);
        serviceInfo.setServiceInput(null);
        serviceInfo.setServiceProperties(null);

        List<ServiceInfo> serviceInfos = new ArrayList<>();
        serviceInfos.add(serviceInfo);
        return serviceInfos;
    }
}
