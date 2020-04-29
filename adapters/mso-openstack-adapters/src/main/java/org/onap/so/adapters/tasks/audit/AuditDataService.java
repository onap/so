package org.onap.so.adapters.tasks.audit;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
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
            throws JsonParseException, JsonMappingException, IOException {

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
