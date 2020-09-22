package org.onap.so.simulator.actions.aai;

import org.onap.aai.domain.yang.Vnfc;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

public class ProcessVnfc extends AbstractTestAction {

    @Override
    public void doExecute(TestContext context) {

        final Logger logger = LoggerFactory.getLogger(ProcessVnfc.class);
        try {
            logger.debug("running ProcessVnfc scenario");
            logger.debug("requestAction: " + context.getVariable("requestAction"));
            logger.debug("serviceAction: " + context.getVariable("serviceAction"));
            logger.debug("cloudOwner: " + context.getVariable("cloudOwner"));
            logger.debug("cloundRegion: " + context.getVariable("cloudRegion"));
            logger.debug("tenant: " + context.getVariable("tenant"));
            logger.debug("vfModuleId: " + context.getVariable("vfModuleId"));
            logger.debug("vnfId: " + context.getVariable("vnfId"));

            AAIResourcesClient aaiResourceClient = new AAIResourcesClient();

            if (context.getVariable("requestAction").equals("CreateVfModuleInstance")
                    && context.getVariable("serviceAction").equals("assign")
                    && context.getVariable("vfModuleName").equals("nc_dummy_id")) {

                AAIResourceUri vnfcURI =
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc("ssc_server_1"));
                Vnfc vnfc = new Vnfc();
                vnfc.setVnfcName("ssc_server_1");
                vnfc.setNfcNamingCode("oamfw");
                vnfc.setNfcFunction("EPC-OAM-FIREWALL");
                vnfc.setProvStatus("PREPROV");
                vnfc.setOrchestrationStatus("Active");
                vnfc.setInMaint(false);
                vnfc.setIsClosedLoopDisabled(false);

                vnfc.setModelInvariantId("b214d2e9-73d9-49d7-b7c4-a9ae7f06e244");
                vnfc.setModelVersionId("9e314c37-2258-4572-a399-c0dd7d5f1aec");
                vnfc.setModelCustomizationId("2bd95cd4-d7ff-4af0-985d-2adea0339921");

                if (!aaiResourceClient.exists(vnfcURI)) {
                    logger.debug("creating VNFC");
                    aaiResourceClient.create(vnfcURI, vnfc);


                } else {
                    aaiResourceClient.get(vnfcURI);
                }
                AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network()
                        .genericVnf(context.getVariable("vnfId")).vfModule(context.getVariable("vfModuleId")));
                logger.debug("creating VNFC edge to vf module");
                aaiResourceClient.connect(vfModuleURI, vnfcURI);
            }

        } catch (Exception e) {
            logger.debug("Exception in ProcessVnfc.doExecute", e);
        }
    }
}
