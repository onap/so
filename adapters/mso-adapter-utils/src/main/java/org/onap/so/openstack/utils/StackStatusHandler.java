package org.onap.so.openstack.utils;


import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Stack;

@Component
public class StackStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(StackStatusHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RequestsDbClient requestDBClient;

    @Async
    public void updateStackStatus(Stack stack, String requestId) {
        try {
            String stackStatus = mapper.writeValueAsString(stack);
            RequestProcessingData requestProcessingData =
                    requestDBClient.getRequestProcessingDataBySoRequestIdAndNameAndGrouping(requestId, stack.getId(),
                            stack.getStackName());
            if (requestProcessingData == null) {
                requestProcessingData = new RequestProcessingData();
                requestProcessingData.setGroupingId(stack.getId());
                requestProcessingData.setName(stack.getStackName());
                requestProcessingData.setTag("StackInformation");
                requestProcessingData.setSoRequestId(requestId);
            }
            requestProcessingData.setValue(stackStatus);
            requestDBClient.saveRequestProcessingData(requestProcessingData);
        } catch (Exception e) {
            logger.warn("Error adding stack status to request database", e);
        }
    }
}
