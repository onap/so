package org.onap.so.adapters.tasks.audit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.tasks.audit.AuditDataService;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuditDataServiceTest {

    @InjectMocks
    AuditDataService auditDataService = new AuditDataService();

    @Mock
    protected RequestsDbClient requestsDbClient;

    AuditInventory auditInventory = new AuditInventory();

    @Before
    public void before() throws JsonProcessingException {
        auditInventory.setCloudOwner("testCloudOwner");
        auditInventory.setCloudRegion("testLcpCloudRegionId");
        auditInventory.setHeatStackName("testVfModuleName1");
        auditInventory.setVfModuleId("testVnfModuleId");
        auditInventory.setTenantId("testTenantId");
        auditInventory.setGenericVnfId("testVnfId1");
    }

    @Test
    public void testWriteStackDataToRequestDb() throws Exception {
        Mockito.doReturn(new ArrayList<RequestProcessingData>()).when(requestsDbClient)
                .getRequestProcessingDataByGroupingIdAndNameAndTag(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(requestsDbClient).saveRequestProcessingData(Mockito.any());

        AAIObjectAuditList auditList = new AAIObjectAuditList();
        auditList.setHeatStackName("testHeatStackName");
        AAIObjectAudit audit = new AAIObjectAudit();
        Vserver vserver = new Vserver();
        vserver.setVserverId("testVserverId");
        audit.setAaiObject(vserver);
        auditList.getAuditList().add(audit);

        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        String auditListString = objectMapper.getMapper().writeValueAsString(auditList);;

        RequestProcessingData requestProcessingData = new RequestProcessingData();
        requestProcessingData.setSoRequestId(auditInventory.getMsoRequestId());
        requestProcessingData.setGroupingId(auditInventory.getVfModuleId());
        requestProcessingData.setName(auditInventory.getHeatStackName());
        requestProcessingData.setTag("AuditStackData");
        requestProcessingData.setValue(auditListString);

        auditDataService.writeStackDataToRequestDb(auditInventory, auditList);
        Mockito.verify(requestsDbClient, Mockito.times(1)).saveRequestProcessingData(requestProcessingData);
    }

    @Test
    public void testGetStackDataToRequestDb() throws Exception {
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        auditList.setHeatStackName("testHeatStackName");
        AAIObjectAudit audit = new AAIObjectAudit();
        Vserver vserver = new Vserver();
        vserver.setVserverId("testVserverId");
        audit.setAaiObject(vserver);
        auditList.getAuditList().add(audit);
        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        String auditListString = objectMapper.getMapper().writeValueAsString(audit);

        List<RequestProcessingData> list = new ArrayList();
        RequestProcessingData requestProcessingData = new RequestProcessingData();
        requestProcessingData.setId(234321432);
        requestProcessingData.setGroupingId("testVfModuleId");
        requestProcessingData.setName("heatStackName");
        requestProcessingData.setTag("AuditStackData");
        requestProcessingData.setValue(auditListString);
        list.add(requestProcessingData);

        Mockito.doReturn(list).when(requestsDbClient).getRequestProcessingDataByGroupingIdAndNameAndTag(Mockito.any(),
                Mockito.any(), Mockito.any());
        auditDataService.getStackDataFromRequestDb(auditInventory);
        Mockito.verify(requestsDbClient, Mockito.times(1)).getRequestProcessingDataByGroupingIdAndNameAndTag(
                "testVnfModuleId", "testVfModuleName1", "AuditStackData");
    }

}
