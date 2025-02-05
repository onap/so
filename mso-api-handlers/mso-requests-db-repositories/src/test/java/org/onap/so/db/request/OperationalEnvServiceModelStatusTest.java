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

package org.onap.so.db.request;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.onap.so.db.request.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OperationalEnvServiceModelStatusTest {

    @Autowired
    private OperationalEnvServiceModelStatusRepository repository;


    @Test
    public void updateWithoutAllKeys() throws Exception {

        OperationalEnvServiceModelStatus status = new OperationalEnvServiceModelStatus();
        status.setRequestId("request-id-1");
        status.setOperationalEnvId("oper-env-id-1");
        status.setServiceModelVersionId("service-model-ver-id-1");
        status.setVnfOperationalEnvId("vnf-oper-env-id-1");
        status.setRetryCount(0);

        repository.saveAndFlush(status);
        OperationalEnvServiceModelStatus status2 =
                repository.findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId("oper-env-id-1",
                        "service-model-ver-id-1", "request-id-1");
        status2.setRetryCount(1);
        assertEquals("request-id-1", status2.getRequestId());
        assertEquals("vnf-oper-env-id-1", status2.getVnfOperationalEnvId());

        repository.saveAndFlush(status2);

        OperationalEnvServiceModelStatus status3 = new OperationalEnvServiceModelStatus();

        status3.setRequestId("request-id-2");
        status3.setOperationalEnvId("oper-env-id-1");
        status3.setServiceModelVersionId("service-model-ver-id-2");
        status3.setVnfOperationalEnvId("vnf-oper-env-id-2");
        status3.setRetryCount(2);

        repository.saveAndFlush(status3);

        OperationalEnvServiceModelStatus exampleObj = new OperationalEnvServiceModelStatus();
        exampleObj.setOperationalEnvId("oper-env-id-1");
        exampleObj.setServiceModelVersionId("service-model-ver-id-1");
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("requestId");
        OperationalEnvServiceModelStatus foundStatus = repository.findOne(Example.of(exampleObj, matcher))
                .orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));
        if (foundStatus == null)
            throw new Exception("No status found");

        Integer integer = Integer.valueOf(1);
        assertEquals(integer, foundStatus.getRetryCount());
    }
}
