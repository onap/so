/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.client.test.rest;


import java.io.File;
import java.io.IOException;
import javax.transaction.Transactional;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.onap.so.asdc.client.ASDCController;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This is a TEST only rest interface. It is not used in production, it is used to aid in testing the ASDC service
 * without the need to be connected to the ASDC service broker. It starts the test at the treatNotification step and
 * simulates both the notification step as well as the artifact download step.
 * <p>
 * i.e. http://localhost:8085/test/treatNotification/v1
 * <p>
 * i.e. http://localhost:8085/test/statusData/v1
 *
 * This interface is also used in CSIT to simulate a distribution of a service, without using SDC
 *
 * @author jm5423
 */

@Path("/")
@Component
@Profile("test")
public class ASDCRestInterface {

    private static final Logger logger = LoggerFactory.getLogger(ASDCRestInterface.class);

    private final ASDCController asdcController;

    private final ToscaResourceInstaller toscaInstaller;

    @Autowired
    public ASDCRestInterface(final ASDCController asdcController, final ToscaResourceInstaller toscaInstaller) {
        this.asdcController = asdcController;
        this.toscaInstaller = toscaInstaller;
    }

    private static String targetDirectory = "src/test/resources";
    private static java.nio.file.Path targetPath = new File(targetDirectory).toPath().normalize();

    @POST
    @Path("/treatNotification/v1")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response invokeASDCService(final NotificationDataImpl request,
            @HeaderParam("resource-location") final String resourceLocation) {

        if (!isLocationValid(resourceLocation)) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            logger.info("Received message : {}", request);
            logger.info("resource-location : {}", resourceLocation);
            final DistributionClientEmulator distributionClientEmulator =
                    getDistributionClientEmulator(resourceLocation);

            asdcController.setControllerName("asdc-controller1");
            asdcController.setDistributionClient(distributionClientEmulator);

            if (asdcController.isStopped()) {
                logger.info("{} not running will try to initialize it, currrent status: {}",
                        asdcController.getClass().getName(), asdcController.getControllerStatus());
                asdcController.initASDC();
            }

            asdcController.treatNotification(request);

            if (!asdcController.isBusy()) {
                asdcController.closeASDC();
            }

            return Response.status(Status.OK).build();
        } catch (final Exception exception) {
            logger.error("Unable to process notification request", exception);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * This mitigates a sonarqube issue that is marked as critical. Since this controller is only available in the test
     * profile, there is no real vulnerability here, but this is the easiest way to resolve the issue
     */
    private boolean isLocationValid(String resourceLocation) {
        File file = new File(targetPath + resourceLocation);

        if (!file.toPath().normalize().startsWith(targetPath)) {
            return false;
        }
        return true;
    }

    private DistributionClientEmulator getDistributionClientEmulator(final String resourceLocation) {
        return new DistributionClientEmulator(resourceLocation);
    }

    @POST
    @Path("/statusData/v1")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response invokeASDCStatusData(final String request) {

        try {
            final DistributionClientEmulator distributionClientEmulator =
                    getDistributionClientEmulator("resource-examples/");
            final JsonStatusData statusData = JsonStatusData.instantiateNotifFromJsonFile("resource-examples/");

            asdcController.setDistributionClient(distributionClientEmulator);
            asdcController.initASDC();
            toscaInstaller.installTheComponentStatus(statusData);
            asdcController.closeASDC();

            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC.toString(),
                    statusData.getDistributionID(), "ASDC", "ASDC Updates Are Complete");
        } catch (final Exception e) {
            logger.info("Error caught " + e.getMessage());
            logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION.toString(),
                    "Exception caught during ASDCRestInterface", "ASDC", "invokeASDCService",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in invokeASDCService", e);
        }

        return null;
    }
}
