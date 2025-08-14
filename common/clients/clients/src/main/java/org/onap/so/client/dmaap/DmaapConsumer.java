/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.dmaap;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.onap.so.client.dmaap.exceptions.DMaaPConsumerFailure;
import org.onap.so.client.dmaap.exceptions.ExceededMaximumPollingTime;
import org.onap.so.client.dmaap.rest.RestConsumer;
import com.google.common.base.Stopwatch;

public abstract class DmaapConsumer extends DmaapClient {
    static final int MAX_ELAPSED_TIME = 180000;

    public DmaapConsumer() throws IOException {
        super("dmaap/default-consumer.properties");
    }

    public Consumer getConsumer() {
        return new RestConsumer(this.properties);
    }

    public boolean consume() throws Exception {
        Consumer mrConsumer = this.getConsumer();
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        try {
            while (this.continuePolling()) {
                if (stopwatch.elapsed(TimeUnit.MILLISECONDS) >= this.getMaximumElapsedTime()) {
                    final String message =
                            "exceeded maximum retries on " + this.getRequestId() + " on " + this.getTopic();
                    logger.error(message);
                    throw new ExceededMaximumPollingTime(message);
                }
                stopwatch.start();
                Iterable<String> itr = mrConsumer.fetch();
                stopwatch.stop();
                for (String message : itr) {
                    if (this.isAccepted(message)) {
                        logger.info("accepted message found for {} on {}", this.getRequestId(), this.getTopic());
                    }
                    logger.info("received dmaap message: {}", message);
                    if (this.isFailure(message)) {
                        this.stopProcessingMessages();
                        final String errorMsg = "failure received from dmaap topic " + this.getTopic();
                        logger.error(errorMsg);
                        throw new DMaaPConsumerFailure(errorMsg);
                    } else {
                        this.processMessage(message);
                    }
                }
            }
            return true;
        } finally {
            if (stopwatch.isRunning()) {
                stopwatch.stop();
            }
        }
    }

    /**
     * Should this consumer continue to consume messages from the topic?
     * 
     * @return
     */
    public abstract boolean continuePolling();

    /**
     * Process a message from a DMaaP topic
     *
     * @param message
     * @throws Exception
     */
    public abstract void processMessage(String message) throws Exception;

    /**
     * Has the request been accepted by the receiving system? Should the consumer move to processing messages?
     *
     * @param message
     * @return
     */
    public abstract boolean isAccepted(String message);

    /**
     * has the request failed?
     *
     * @param message
     * @return
     */
    public abstract boolean isFailure(String message);

    /**
     * The request id to filter messages on
     * 
     * @return
     */
    public abstract String getRequestId();

    /**
     * Logic that defines when the consumer should stop processing messages
     */
    public abstract void stopProcessingMessages();

    /**
     * time in milliseconds
     */
    public int getMaximumElapsedTime() {
        return MAX_ELAPSED_TIME;
    }



}
