package org.onap.so.bpmn.common.workflow.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.adapter.vnf.CreateVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.DeleteVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.MsoExceptionCategory;
import org.onap.so.bpmn.common.adapter.vnf.QueryVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.RollbackVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.UpdateVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.VnfRollback;
import org.onap.so.bpmn.common.adapter.vnf.VnfStatus;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackResult;

@RunWith(MockitoJUnitRunner.class)
public class VnfAdapterNotifyServiceImplTest {


    @InjectMocks
    @Spy
    private VnfAdapterNotifyServiceImpl vnfAdapterNotifyServiceImpl;

    @Mock
    private CallbackHandlerService callbackHandlerService;

    @Mock
    private CallbackResult callbackResult;

    protected CreateVnfNotification.Outputs outputs = new CreateVnfNotification.Outputs();
    protected QueryVnfNotification.Outputs queryOutputs = new QueryVnfNotification.Outputs();
    protected UpdateVnfNotification.Outputs updateOutputs = new UpdateVnfNotification.Outputs();

    protected VnfRollback vnfRollback = new VnfRollback();

    @Test
    public void rollbackVnfNotificationTest() {
        doReturn(callbackResult).when(callbackHandlerService).handleCallback(eq("rollbackVnfNotification"),
                any(RollbackVnfNotification.class), eq("rollbackVnfNotificationCallback"),
                eq("rollbackVnfNotificationCallback"), eq("VNFRB_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));

        vnfAdapterNotifyServiceImpl.rollbackVnfNotification("messageId", true, MsoExceptionCategory.OPENSTACK,
                "Error creating stack");

        verify(callbackHandlerService, times(1)).handleCallback(eq("rollbackVnfNotification"),
                any(RollbackVnfNotification.class), eq("rollbackVnfNotificationCallback"),
                eq("rollbackVnfNotificationCallback"), eq("VNFRB_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));
    }

    @Test
    public void queryVnfNotificationTest() {
        doReturn(callbackResult).when(callbackHandlerService).handleCallback(eq("queryVnfNotification"),
                any(QueryVnfNotification.class), eq("queryVnfNotificationCallback"), eq("queryVnfNotificationCallback"),
                eq("VNFQ_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"), eq(null));

        vnfAdapterNotifyServiceImpl.queryVnfNotification("messageId", true, MsoExceptionCategory.OPENSTACK, "",
                Boolean.TRUE, "vnfid", VnfStatus.ACTIVE, queryOutputs);

        verify(callbackHandlerService, times(1)).handleCallback(eq("queryVnfNotification"),
                any(QueryVnfNotification.class), eq("queryVnfNotificationCallback"), eq("queryVnfNotificationCallback"),
                eq("VNFQ_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"), eq(null));
    }

    @Test
    public void createVnfNotificationTest() {
        doReturn(callbackResult).when(callbackHandlerService).handleCallback(eq("createVnfNotification"),
                any(CreateVnfNotification.class), eq("createVnfNotificationCallback"),
                eq("createVnfNotificationCallback"), eq("VNFC_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));

        vnfAdapterNotifyServiceImpl.createVnfNotification("messageId", true, MsoExceptionCategory.OPENSTACK, "",
                "vnfId", outputs, vnfRollback);

        verify(callbackHandlerService, times(1)).handleCallback(eq("createVnfNotification"),
                any(CreateVnfNotification.class), eq("createVnfNotificationCallback"),
                eq("createVnfNotificationCallback"), eq("VNFC_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));
    }

    @Test
    public void updateVnfNotificationTest() {
        doReturn(callbackResult).when(callbackHandlerService).handleCallback(eq("updateVnfNotification"),
                any(UpdateVnfNotification.class), eq("updateVnfNotificationCallback"),
                eq("updateVnfNotificationCallback"), eq("VNFU_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));

        vnfAdapterNotifyServiceImpl.updateVnfNotification("messageId", true, MsoExceptionCategory.OPENSTACK, "",
                updateOutputs, vnfRollback);

        verify(callbackHandlerService, times(1)).handleCallback(eq("updateVnfNotification"),
                any(UpdateVnfNotification.class), eq("updateVnfNotificationCallback"),
                eq("updateVnfNotificationCallback"), eq("VNFU_messageId"), eq("messageId"), eq("[VNF-NOTIFY]"),
                eq(null));
    }

    @Test
    public void deleteVnfNotificationTest() {
        doReturn(callbackResult).when(callbackHandlerService).handleCallback(eq("deleteVnfNotification"),
                any(DeleteVnfNotification.class), eq("deleteVnfACallback"), eq("deleteVnfACallback"), eq("VNFDEL_uuid"),
                eq("messageId"), eq("[VNF-NOTIFY]"), eq(null));

        vnfAdapterNotifyServiceImpl.deleteVnfNotification("messageId", true, MsoExceptionCategory.OPENSTACK, "");

        verify(callbackHandlerService, times(1)).handleCallback(eq("deleteVnfNotification"),
                any(DeleteVnfNotification.class), eq("deleteVnfACallback"), eq("deleteVnfACallback"), eq("VNFDEL_uuid"),
                eq("messageId"), eq("[VNF-NOTIFY]"), eq(null));
    }
}
