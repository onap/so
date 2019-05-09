package org.onap.svnfm.simulator.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.model.Vnfds.Vnfc;
import org.onap.svnfm.simulator.model.Vnfds.Vnfd;

public class InstantiateOperatorProgressorTest {

    private static final String VNF_ID = "vnfTestId";
    private static final String CALLBACK_URI = "/lcn/uritest";
    private static final String VNFC_TYPE = "COMPUTE";
    private static final String RESOURCE_TEMPLATE_ID = "resTempIdTest";
    private static final String VDU_ID = "vduIdTest";

    private InstantiateOperationProgressor testedObject;

    @Before
    public void setup() {
        testedObject = new InstantiateOperationProgressor(new VnfOperation(), new SvnfmService(), null,
                new ApplicationConfig(), createVnfds(), createSubscriptionService());
    }

    @Test
    public void getAddResources_vnfIdFound() {
        List<GrantsAddResources> result = testedObject.getAddResources(VNF_ID);
        assertThat(result).hasSize(1);
        GrantsAddResources grantsAddResourceResult = result.get(0);
        assertThat(grantsAddResourceResult.getType()).hasToString(VNFC_TYPE);
        assertThat(grantsAddResourceResult.getResourceTemplateId()).isEqualTo(RESOURCE_TEMPLATE_ID);
        assertThat(grantsAddResourceResult.getVduId()).isEqualTo(VDU_ID);
    }

    @Test
    public void getAddResources_vnfIdNotFound() {
        List<GrantsAddResources> result = testedObject.getAddResources("otherVnfId");
        assertThat(result).isEmpty();
    }

    private Vnfds createVnfds() {
        Vnfd vnfd = new Vnfd();
        vnfd.setVnfdId(VNF_ID);
        List<Vnfc> vnfcList = new ArrayList<>();
        vnfcList.add(createVnfc());
        vnfd.setVnfcList(vnfcList);

        List<Vnfd> vnfdList = new ArrayList<>();
        vnfdList.add(vnfd);

        Vnfds vnfds = new Vnfds();
        vnfds.setVnfdList(vnfdList);
        return vnfds;
    }

    private Vnfc createVnfc() {
        Vnfc vnfc = new Vnfc();
        vnfc.setType(VNFC_TYPE);
        vnfc.setResourceTemplateId(RESOURCE_TEMPLATE_ID);
        vnfc.setVduId(VDU_ID);
        return vnfc;
    }

    private SubscriptionService createSubscriptionService() {
        SubscriptionService subscriptionService = new SubscriptionService();
        LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        lccnSubscriptionRequest.setCallbackUri(CALLBACK_URI);
        subscriptionService.registerSubscription(lccnSubscriptionRequest);
        return subscriptionService;
    }

}
