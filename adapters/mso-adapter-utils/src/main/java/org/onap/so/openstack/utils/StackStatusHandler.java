package org.onap.so.openstack.utils;


import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    public void updateStackStatus(Stack stack) {
        try {
            String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
            String stackStatus = mapper.writeValueAsString(stack);
            RequestProcessingData requestProcessingData =
                    requestDBClient.getRequestProcessingDataBySoRequestIdAndNameAndGrouping(requestId,
                            stack.getStackName(), stack.getId());
            if (requestProcessingData == null) {
                requestProcessingData = new RequestProcessingData();
                requestProcessingData.setGroupingId(stack.getId());
                requestProcessingData.setName(stack.getStackName());
                requestProcessingData.setTag("StackInformation");
                requestProcessingData.setSoRequestId(requestId);
                requestProcessingData.setValue(stackStatus);
                requestDBClient.saveRequestProcessingData(requestProcessingData);
            } else {
                requestProcessingData.setValue(stackStatus);
                requestDBClient.updateRequestProcessingData(requestProcessingData);
            }
        } catch (Exception e) {
            logger.warn("Error adding stack status to request database", e);
        }
    }
}
