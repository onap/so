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

package org.onap.so.client.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.scram.internals.ScramMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class KafkaConsumerImpl extends KafkaClient {

    protected static Logger logger = LoggerFactory.getLogger(KafkaConsumerImpl.class);
    private static final String kafkaBootstrapServers = "kafkaBootstrapServers";
    private Consumer<String, String> consumer;

    public KafkaConsumerImpl(String bootstrapServers) throws Exception {
        super("kafka/default-consumer.properties");
        setProperties(bootstrapServers);
    }


    public List<String> get(String topic, String consumerGroup, String consumerId) {
        logger.info("consuming message from kafka topic: {}", topic);
        this.properties.put("group.id", consumerGroup);
        this.properties.put("client.id", consumerId);
        if (consumer == null) {
            consumer = getKafkaConsumer(properties);
            consumer.subscribe(Arrays.asList(topic));
        }
        ArrayList<String> msgs = new ArrayList<>();
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, String> rec : records) {
            msgs.add(rec.value());
        }
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<READING THE CONSUMED MESSAGES<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        msgs.forEach(msg -> logger.info("MESSAGE CONSUMED FROM KAFKA : <<<<<{}>>>>>", msg));
        return msgs;
    }

    private void setProperties(String bootstrapServers) throws Exception {
        if (bootstrapServers == null) {
            logger.error("Environment Variable {} is missing", kafkaBootstrapServers);
            throw new Exception("Environment Variable " + kafkaBootstrapServers + " is missing");
        } else {
            this.properties.put("bootstrap.servers", bootstrapServers);
        }

        if (System.getenv("JAAS_CONFIG") == null) {
            logger.info("Not using any authentication for kafka interaction");
        } else {
            logger.info("Using {} authentication provided for kafka interaction",
                    ScramMechanism.SCRAM_SHA_512.mechanismName());
            this.properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_PLAINTEXT.name);
            this.properties.put(SaslConfigs.SASL_MECHANISM, ScramMechanism.SCRAM_SHA_512.mechanismName());
            this.properties.put(SaslConfigs.SASL_JAAS_CONFIG, System.getenv("JAAS_CONFIG"));
        }
    }

    public static KafkaConsumer<String, String> getKafkaConsumer(Properties properties) {
        return new KafkaConsumer<>(properties);
    }

    public void setConsumer(Consumer<String, String> kafkaConsumer) {
        this.consumer = kafkaConsumer;
    }

    public void close() {
        if (consumer != null) {
            logger.info("Closing the Kafka Consumer");
            consumer.close();
            consumer = null;
        }
    }

}
