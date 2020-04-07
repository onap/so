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
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.onap.so.jsonpath.JsonPathUtil;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.mock;

public class QueryServiceArtifactTest {

    @Test
    public void ServiceArtifactTest() {
        QueryServiceArtifact queryServiceArtifact = new QueryServiceArtifact(createList());
        String jsonResult = queryServiceArtifact.JSON2(true, false);
        Assertions.assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.serviceArtifact[0].name").get())
                .isEqualTo("eMBB.zip");
    }

    private List<ServiceArtifact> createList() {
        List<ServiceArtifact> artifacts = new ArrayList<>();
        Service service = mock(Service.class);
        ServiceArtifact artifact = new ServiceArtifact();
        artifact.setService(service);
        artifact.setArtifactUUID("b170dbeb-2954-4a4f-ad12-6bc84b3e089e");
        artifact.setChecksum("ZWRkMGM3NzNjMmE3NzliYTFiZGNmZjVlMDE4OWEzMTA=");
        artifact.setDescription("embbCn");
        artifact.setType("OTHER");
        artifact.setName("eMBB.zip");
        artifact.setVersion("1");
        artifacts.add(artifact);
        return artifacts;
    }
}
