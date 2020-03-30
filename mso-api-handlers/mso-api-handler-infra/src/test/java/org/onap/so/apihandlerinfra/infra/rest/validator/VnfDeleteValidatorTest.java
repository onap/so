package org.onap.so.apihandlerinfra.infra.rest.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
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
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;


@RunWith(MockitoJUnitRunner.class)
public class VnfDeleteValidatorTest {


    @InjectMocks
    @Spy
    private VnfDeleteValidator vnfValidator;

    @Mock
    private AAIDataRetrieval aaiDataRetrieval;

    private Map<String, String> instanceIdMap = new HashMap<>();

    @Test
    public void validateURIMatchTest() {
        assertEquals(true, vnfValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf",
                new ServiceInstancesRequest(), Action.deleteInstance));
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
    public void validateSuccessTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        when(aaiDataRetrieval.isVnfRelatedToVolumes("1")).thenReturn(false);
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null);
        assertEquals(false, result.isPresent());
    }

    @Test
    public void validateFailureVnfTest() {
        instanceIdMap.put("vnfInstanceId", "1");
        when(aaiDataRetrieval.isVnfRelatedToVolumes("1")).thenReturn(true);
        Optional<String> result = vnfValidator.validate(instanceIdMap, null, null);
        assertEquals(true, result.isPresent());
    }

}
