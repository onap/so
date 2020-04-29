package org.onap.so.simulator.actions.aai;

import org.onap.aai.domain.yang.L3Network;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;


public class ProcessNetwork extends AbstractTestAction {

    @Override
    public void doExecute(TestContext context) {
        final Logger logger = LoggerFactory.getLogger(ProcessNetwork.class);
        try {
            int random = (int) (Math.random() * 50 + 1);

            AAIResourcesClient aaiResourceClient = new AAIResourcesClient();

            if (context.getVariable("action").equals("assign")) {
                String networkId = context.getVariable("generatedNetworkId");
                AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId);
                L3Network network = new L3Network();
                network.setNetworkId(networkId);
                network.setNetworkName(context.getVariable("networkName"));
                network.setNetworkType(context.getVariable("networkType"));
                network.setNetworkTechnology("SR_IOV");
                network.setPhysicalNetworkName("PhysicalNetwork" + random);
                aaiResourceClient.create(networkURI, network);
            } else if (context.getVariable("action").equals("delete")) {
                String networkId = context.getVariable("networkId");
                AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId);
                aaiResourceClient.delete(networkURI);
            }
        } catch (Exception e) {
            logger.debug("Exception in ProcessNetwork.doExecute", e);
        }

    }
}
