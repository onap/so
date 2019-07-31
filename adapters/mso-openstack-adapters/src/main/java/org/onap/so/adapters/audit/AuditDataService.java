/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Modifications Copyright (C) 2019 IBM.
 * ===========================================================================
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
 * ============LICENSE_END====================================================
 *
 */
package org.onap.so.adapters.audit;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
public class AuditDataService {

    @Autowired
    private RequestsDbClient requestsDbClient;

    /**
     * Checks to see if an entry already exist for the given heat stack and writes audit stack data to the request
     * database if it doesn't.
     *
     * @throws JsonProcessingException
     */
    public void writeStackDataToRequestDb(AuditInventory auditInventory, AAIObjectAuditList auditList)
            throws JsonProcessingException {
        List<RequestProcessingData> requestProcessingDataList =
                requestsDbClient.getRequestProcessingDataByGroupingIdAndNameAndTag(auditInventory.getVfModuleId(),
                        auditInventory.getHeatStackName(), "AuditStackData");
        if (requestProcessingDataList.isEmpty()) {
            GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
            String auditListString = objectMapper.getMapper().writeValueAsString(auditList);;

            RequestProcessingData requestProcessingData = new RequestProcessingData();
            requestProcessingData.setSoRequestId(auditInventory.getMsoRequestId());
            requestProcessingData.setGroupingId(auditInventory.getVfModuleId());
            requestProcessingData.setName(auditInventory.getHeatStackName());
            requestProcessingData.setTag("AuditStackData");
            requestProcessingData.setValue(auditListString);

            requestsDbClient.saveRequestProcessingData(requestProcessingData);
        }
    }

    /**
     * Retrieves audit stack data from the request database.
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public Optional<AAIObjectAuditList> getStackDataFromRequestDb(AuditInventory auditInventory)
            throws JsonParseException , IOException {

        List<RequestProcessingData> requestProcessingDataList =
                requestsDbClient.getRequestProcessingDataByGroupingIdAndNameAndTag(auditInventory.getVfModuleId(),
                        auditInventory.getHeatStackName(), "AuditStackData");
        if (!requestProcessingDataList.isEmpty()) {
            RequestProcessingData requestProcessingData = requestProcessingDataList.get(0);
            String auditListString = requestProcessingData.getValue();

            GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
            AAIObjectAuditList auditList =
                    objectMapper.getMapper().readValue(auditListString, AAIObjectAuditList.class);

            return Optional.of(auditList);
        } else {
            return Optional.empty();
        }
    }


}
