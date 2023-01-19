package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.moi.util.AAISliceProfileUtil;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class DeleteRANNssiBBTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRANNssiBBTask.class);

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    AAISliceProfileUtil aaiSliceProfileUtil;

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    private static final ObjectMapper mapper = new ObjectMapper();


    public void deleteNssi(BuildingBlockExecution execution) throws JsonProcessingException {
        GeneralBuildingBlock gBB = execution.getGeneralBuildingBlock();
        List<Map<String, Object>> sliceProfilesData = gBB.getRequestContext().getRequestParameters().getUserParams();
        String sliceProfileIdFromRequest = mapUserParamsToSliceProfile(sliceProfilesData);
        aaiSliceProfileUtil.deleteSliceProfile(execution, sliceProfileIdFromRequest);
    }



    private String mapUserParamsToSliceProfile(List<Map<String, Object>> sliceProfilesData)
            throws JsonProcessingException {
        Map<String, Object> mapParam = (Map<String, Object>) sliceProfilesData.get(0).get("nssi");
        List<Object> list = (ArrayList<Object>) mapParam.get("sliceProfileList");
        Map<String, Object> idMap = (Map<String, Object>) list.get(0);
        String sliceProfileId = (String) idMap.get("sliceProfileId");
        return sliceProfileId;
    }

}
