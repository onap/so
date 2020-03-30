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
import org.onap.so.apihandlerinfra.infra.rest.validators.VolumeGroupDeleteValidator;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;


@RunWith(MockitoJUnitRunner.class)
public class VolumeGroupDeleteValidatorTest {


    @InjectMocks
    @Spy
    private VolumeGroupDeleteValidator volumeGroupDeleteValidator;

    @Mock
    private AAIDataRetrieval aaiDataRetrieval;

    private Map<String, String> instanceIdMap = new HashMap<>();

    @Test
    public void validateURIMatchTest() {
        assertEquals(true, volumeGroupDeleteValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/volumeGroups/uuid",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatchTest() {
        assertEquals(false,
                volumeGroupDeleteValidator.shouldRunFor(
                        "v8/serviceInstances/uasdfasdf/vnfs/asdfasdf/volumeGroups/uuid/replace",
                        new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateURINotMatch2Test() {
        assertEquals(false, volumeGroupDeleteValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/uuid",
                new ServiceInstancesRequest(), Action.deleteInstance));
    }

    @Test
    public void validateSuccessTest() {
        instanceIdMap.put("volumeGroupInstanceId", "1");
        when(aaiDataRetrieval.isVolumeGroupRelatedToVFModule("1")).thenReturn(false);
        Optional<String> result = volumeGroupDeleteValidator.validate(instanceIdMap, null, null);
        assertEquals(false, result.isPresent());
    }

    @Test
    public void validateFailureVnfTest() {
        instanceIdMap.put("volumeGroupInstanceId", "1");
        when(aaiDataRetrieval.isVolumeGroupRelatedToVFModule("1")).thenReturn(true);
        Optional<String> result = volumeGroupDeleteValidator.validate(instanceIdMap, null, null);
        assertEquals(true, result.isPresent());
    }

}
