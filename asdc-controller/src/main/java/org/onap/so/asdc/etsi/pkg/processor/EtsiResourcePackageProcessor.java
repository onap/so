/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.asdc.etsi.pkg.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.onap.so.asdc.etsi.pkg.processor.exceptions.SOL004ResourcePackageFailureException;
import org.onap.so.asdc.etsi.pkg.processor.exceptions.SOL004ResourcePackageProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableSet;


/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class EtsiResourcePackageProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(EtsiResourcePackageProcessor.class);
    private static final String ONBOARDED_PACKAGE_DIR_PATH = "Artifacts/Deployment/ONBOARDED_PACKAGE";
    private final SdcResourceProvider sdcResourceProvider;
    private final EtsiCatalogServiceProvider catalogServiceProvider;
    private static final int SLEEP_TIME_IN_SECONDS = 5;

    private static final ImmutableSet<JobStatus> JOB_FINISHED_STATES =
            ImmutableSet.of(JobStatus.FINISHED, JobStatus.ERROR, JobStatus.TIMEOUT);

    @Value("${etsi-catalog-manager.rest.timeoutInSeconds:300}")
    private int timeOutInSeconds;

    @Autowired
    public EtsiResourcePackageProcessor(final SdcResourceProvider sdcResourceProvider,
            final EtsiCatalogServiceProvider catalogServiceProvider) {
        this.sdcResourceProvider = sdcResourceProvider;
        this.catalogServiceProvider = catalogServiceProvider;
    }

    public void processPackageIfExists(final String vnfUuid) {
        LOGGER.debug("Processing vnf with UUID: {} ", vnfUuid);
        try {
            final Optional<byte[]> optional = sdcResourceProvider.getVnfResource(vnfUuid);
            if (optional.isPresent()) {
                final byte[] resourceContent = optional.get();

                if (containsOnBoardedSol004Package(resourceContent)) {
                    final EtsiCatalogPackageOnboardingJob onboardingJob = catalogServiceProvider
                            .onBoardResource(new EtsiCatalogPackageOnboardingRequest().csarId(vnfUuid));
                    LOGGER.debug("Successfully created job with id: {} to onboard vnf with UUID: {}",
                            onboardingJob.getJobId(), vnfUuid);

                    if (onboardingJob.getJobId() == null) {
                        throw new SOL004ResourcePackageFailureException(
                                "Received invalid jobId " + onboardingJob.getJobId());
                    }

                    final Optional<EtsiCatalogPackageOnboadingJobStatus> jobStatusOptional =
                            waitForJobToFinish(onboardingJob);

                    if (!jobStatusOptional.isPresent()) {
                        final String message = "Job status timeout reached failed to onboard vnf with UUID: " + vnfUuid;
                        LOGGER.debug(message, vnfUuid);
                        throw new SOL004ResourcePackageFailureException(message);
                    }

                    final EtsiCatalogPackageOnboadingJobStatus onboadingJobStatus = jobStatusOptional.get();
                    final JobStatus jobStatus = getJobStatus(onboadingJobStatus);
                    final ErrorCode errorCode = getErrorCode(onboadingJobStatus);

                    LOGGER.debug("Final job status: {}, error code: {}", jobStatus, errorCode);
                    if (!JobStatus.FINISHED.equals(jobStatus) && !ErrorCode.PACKAGE_EXIST.equals(errorCode)) {
                        final String message = "Failed to onboard vnf with UUID: " + vnfUuid + " job status: "
                                + jobStatus + " errorCode: " + errorCode;
                        LOGGER.debug(message, vnfUuid);
                        throw new SOL004ResourcePackageFailureException(message);
                    }
                    LOGGER.debug("Successfully onboarded package in ETSI catalog .. ");
                }

            }
        } catch (final Exception exception) {
            final String message = "Unable to process resource received from SDC";
            LOGGER.error(message, exception);
            throw new SOL004ResourcePackageProcessingException(message, exception);
        }

    }

    private Optional<EtsiCatalogPackageOnboadingJobStatus> waitForJobToFinish(
            final EtsiCatalogPackageOnboardingJob onboardingJob) throws InterruptedException {
        JobStatus currentJobStatus = null;
        final long startTimeInMillis = System.currentTimeMillis();
        final long timeOutTime = startTimeInMillis + TimeUnit.SECONDS.toMillis(timeOutInSeconds);

        LOGGER.debug("Will wait till {} for {} job to finish", Instant.ofEpochMilli(timeOutTime).toString(),
                onboardingJob.getJobId());

        while (timeOutTime > System.currentTimeMillis()) {

            final EtsiCatalogPackageOnboadingJobStatus onboadingJobStatus =
                    catalogServiceProvider.getJobStatus(onboardingJob.getJobId());
            LOGGER.debug("Current job status {} ", onboadingJobStatus);

            currentJobStatus = getJobStatus(onboadingJobStatus);
            if (JOB_FINISHED_STATES.contains(currentJobStatus)) {
                return Optional.of(onboadingJobStatus);
            }

            LOGGER.debug("Onboarding not finished yet, will try again in {} seconds", SLEEP_TIME_IN_SECONDS);
            TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);

        }
        LOGGER.warn("Timeout current job status: {}", currentJobStatus);
        return Optional.empty();
    }

    private boolean containsOnBoardedSol004Package(final byte[] resourceContent) throws IOException {
        try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(resourceContent))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName() != null && entry.getName().contains(ONBOARDED_PACKAGE_DIR_PATH)) {
                    LOGGER.debug("Found entry: {} that contains {} in name", entry.getName(),
                            ONBOARDED_PACKAGE_DIR_PATH);
                    return true;
                }
            }

        }
        LOGGER.debug("Unable to find {} dir in downloaded package", ONBOARDED_PACKAGE_DIR_PATH);
        return false;
    }

    private JobStatus getJobStatus(final EtsiCatalogPackageOnboadingJobStatus onboadingJobStatus) {
        if (onboadingJobStatus.getResponseDescriptor() != null) {
            return JobStatus.getJobStatus(onboadingJobStatus.getResponseDescriptor().getStatus());
        }
        LOGGER.warn("Found null ResponseDescriptor {}", onboadingJobStatus);
        return JobStatus.UNKNOWN;
    }

    private ErrorCode getErrorCode(final EtsiCatalogPackageOnboadingJobStatus onboadingJobStatus) {
        if (onboadingJobStatus.getResponseDescriptor() != null) {
            return ErrorCode.getErrorCode(onboadingJobStatus.getResponseDescriptor().getErrorCode());
        }
        LOGGER.warn("Found null ResponseDescriptor {}", onboadingJobStatus);
        return ErrorCode.UNKNOWN;
    }

}
