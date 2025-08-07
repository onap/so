package org.onap.so.apihandlerinfra.infra.rest.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.apihandlerinfra.infra.rest.validators.VnfDeleteValidator;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;


@RunWith(MockitoJUnitRunner.class)
public class VnfDeleteValidatorTest {


    @InjectMocks
    @Spy
    private VnfDeleteValidator vnfValidator;

    @Mock
    private AAIDataRetrieval aaiDataRetrieval;

    private Map<String, String> instanceIdMap = new HashMap<>();

    public ServiceInstancesRequest createServiceInstancesRequest(boolean alacarte) {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setRequestDetails(new RequestDetails());
        sir.getRequestDetails().setRequestParameters(new RequestParameters());
        sir.getRequestDetails().getRequestParameters().setaLaCarte(alacarte);
        return sir;
    }

    @Test
    public void validateURIMatchTest() {
        assertEquals(true, vnfValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf",
                createServiceInstancesRequest(true), Action.deleteInstance));
    }

    @Test
    public void validateURIMatchNonAlacarteTest() {
        assertEquals(false, vnfValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf",
                createServiceInstancesRequest(false), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatchTest() {
        assertEquals(false, vnfValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf/replace",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatch2Test() {
        assertEquals(false, vnfValidator.shouldRunFor("v8/serviceInstances/uasdfasdf", new ServiceInstancesRequest(),
                Action.deleteInstance));
    }

    @Test
    public void validateFailureVfModuleVnfTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        doReturn(Optional.of("test")).when(aaiDataRetrieval).getVfModuleIdsByVnfId("1");
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null, null);
        assertEquals(true, result.isPresent());
    }

    @Test
    public void validateSuccessVfModuleVnfTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        doReturn(Optional.empty()).when(aaiDataRetrieval).getVfModuleIdsByVnfId("1");
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null, null);
        assertEquals(false, result.isPresent());
    }

    @Test
    public void validateFailureVolumeGroupVnfTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        doReturn(Optional.of("test")).when(aaiDataRetrieval).getVolumeGroupIdsByVnfId("1");
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null, null);
        assertEquals(true, result.isPresent());
    }

    @Test
    public void validateSuccessVolumeGroupVnfTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        doReturn(Optional.empty()).when(aaiDataRetrieval).getVolumeGroupIdsByVnfId("1");
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null, null);
        assertEquals(false, result.isPresent());
    }
}
