package org.onap.so.simulator.actions.aai;

import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

public class DeleteVServers extends AbstractTestAction {

    private static final Logger logger = LoggerFactory.getLogger(DeleteVServers.class);

    @Override
    public void doExecute(TestContext context) {

        try {
            logger.info("Deleting Vservers in A&AI");
            AAIResourcesClient aaiResourceClient = new AAIResourcesClient();
            String vserverId = context.getVariable("vServerId");
            String cloudRegion = context.getVariable("cloudRegion");
            String cloudOwner = context.getVariable("cloudOwner");
            String tenantId = context.getVariable("tenantId");
            AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
                    tenantId, vserverId);
            aaiResourceClient.delete(vserverURI);
            logger.error("Delete Vservers in AAI: {}", vserverURI);
        } catch (Exception e) {
            logger.error("Error Deleting VServer in A&AI", e);
        }

    }
}
