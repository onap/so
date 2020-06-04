package org.onap.so.apihandlerinfra.infra.rest.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.apihandlerinfra.infra.rest.validators.ServiceInstanceDeleteValidator;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;


@RunWith(MockitoJUnitRunner.class)
public class ServiceInstanceDeleteValidatorTest {


    @InjectMocks
    @Spy
    private ServiceInstanceDeleteValidator serviceValidator;

    @Mock
    private AAIDataRetrieval aaiDataRetrieval;

    private Map<String, String> instanceIdMap = new HashMap<>();

    private ServiceInstancesRequest serviceInstancesRequest;

    @Before
    public void before() {
        serviceInstancesRequest = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setaLaCarte(true);
        requestDetails.setRequestParameters(requestParameters);
        serviceInstancesRequest.setRequestDetails(requestDetails);
    }

    @Test
    public void validateURIMatchTest() {
        assertEquals(true, serviceValidator.shouldRunFor("v8/serviceInstances/uasdfasdf", serviceInstancesRequest,
                Action.deleteInstance));
    }

    @Test
    public void validateURINotMatchTest() {
        assertEquals(false, serviceValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/vnfs/asdfasdf",
                serviceInstancesRequest, Action.deleteInstance));
    }

    @Test
    public void validateURINotMatch2Test() {
        assertEquals(false, serviceValidator.shouldRunFor("v8/serviceInstances/uasdfasdf/update",
                serviceInstancesRequest, Action.deleteInstance));
    }

    @Test
    public void validateNotALaCarteTest() {
        serviceInstancesRequest.getRequestDetails().getRequestParameters().setaLaCarte(false);
        assertEquals(false, serviceValidator.shouldRunFor("v8/serviceInstances/uasdfasdf", serviceInstancesRequest,
                Action.deleteInstance));
    }

    @Test
    public void validateSuccessTest() {
        instanceIdMap.put("serviceInstanceId", "1");
        when(aaiDataRetrieval.isServiceRelatedToGenericVnf("1")).thenReturn(false);
        when(aaiDataRetrieval.isServiceRelatedToNetworks("1")).thenReturn(false);
        when(aaiDataRetrieval.isServiceRelatedToConfiguration("1")).thenReturn(false);
        Optional<String> result = serviceValidator.validate(instanceIdMap, null, null, null);
        assertEquals(false, result.isPresent());
    }

    @Test
    public void validateFailureVnfTest() {
        instanceIdMap.put("serviceInstanceId", "1");
        when(aaiDataRetrieval.isServiceRelatedToGenericVnf("1")).thenReturn(true);
        Optional<String> result = serviceValidator.validate(instanceIdMap, null, null, null);
        assertEquals(true, result.isPresent());
    }

    @Test
    public void validateFailureNetworksTest() {
        instanceIdMap.put("serviceInstanceId", "1");
        when(aaiDataRetrieval.isServiceRelatedToGenericVnf("1")).thenReturn(false);
        when(aaiDataRetrieval.isServiceRelatedToNetworks("1")).thenReturn(true);
        Optional<String> result = serviceValidator.validate(instanceIdMap, null, null, null);
        assertEquals(true, result.isPresent());
    }

    @Test
    public void validateFailureConfigurationTest() {
        instanceIdMap.put("serviceInstanceId", "1");
        when(aaiDataRetrieval.isServiceRelatedToGenericVnf("1")).thenReturn(false);
        when(aaiDataRetrieval.isServiceRelatedToNetworks("1")).thenReturn(false);
        when(aaiDataRetrieval.isServiceRelatedToConfiguration("1")).thenReturn(true);
        Optional<String> result = serviceValidator.validate(instanceIdMap, null, null, null);
        assertEquals(true, result.isPresent());
    }

}
