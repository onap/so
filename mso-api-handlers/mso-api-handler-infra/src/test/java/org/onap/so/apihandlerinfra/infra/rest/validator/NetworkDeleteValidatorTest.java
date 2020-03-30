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
import org.onap.so.apihandlerinfra.infra.rest.validators.NetworkDeleteValidator;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;


@RunWith(MockitoJUnitRunner.class)
public class NetworkDeleteValidatorTest {


    @InjectMocks
    @Spy
    private NetworkDeleteValidator networkValidator;

    @Mock
    private AAIDataRetrieval aaiDataRetrieval;

    private Map<String, String> instanceIdMap = new HashMap<>();

    @Test
    public void validateURIMatchTest() {
        assertEquals(true, networkValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/networks/asdfasdf",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatchTest() {
        assertEquals(false, networkValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatch2Test() {
        assertEquals(false, networkValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/networks/asdfasdf/update",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatchActionTest() {
        assertEquals(false, networkValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/networks/asdfasdf",
                new ServiceInstancesRequest(), Action.createInstance));
    }

    @Test
    public void validateSuccessTest() {
        instanceIdMap.put("networkInstanceId", "1");
        when(aaiDataRetrieval.isNetworkRelatedToModules("1")).thenReturn(false);
        Optional<String> result = networkValidator.validate(instanceIdMap, null, null);
        assertEquals(false, result.isPresent());
    }

    @Test
    public void validateFailureTest() {
        instanceIdMap.put("networkInstanceId", "1");
        when(aaiDataRetrieval.isNetworkRelatedToModules("1")).thenReturn(true);
        Optional<String> result = networkValidator.validate(instanceIdMap, null, null);
        assertEquals(true, result.isPresent());
    }

}
