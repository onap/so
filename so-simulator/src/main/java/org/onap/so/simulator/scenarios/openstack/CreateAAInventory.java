package org.onap.so.simulator.scenarios.openstack;

import java.io.InputStream;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.springframework.core.io.ClassPathResource;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

public class CreateAAInventory extends AbstractTestAction {

    @Override
    public void doExecute(TestContext context) {
        try {
            String stackName = context.getVariable("stackName");

            if (stackName != null && stackName.equals("replace_module")) {
                String vServerId = "92272b67-d23f-42ca-87fa-7b06a9ec81f3";
                AAIResourcesClient aaiResourceClient = new AAIResourcesClient();
                AAICommonObjectMapperProvider aaiMapper = new AAICommonObjectMapperProvider();
                InputStream vserverFile =
                        new ClassPathResource("openstack/gr_api/CreateAAIInventory.json").getInputStream();
                Vserver vserver = aaiMapper.getMapper().readValue(vserverFile, Vserver.class);
                AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, "cloudOwner",
                        "regionOne", "0422ffb57ba042c0800a29dc85ca70f8", vServerId);
                aaiResourceClient.create(vserverURI, vserver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
