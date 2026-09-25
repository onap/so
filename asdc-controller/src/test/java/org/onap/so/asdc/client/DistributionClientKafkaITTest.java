/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.impl.DistributionClientImpl;
import org.onap.so.asdc.BaseTest;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runs a distribution through the real sdc-distribution-client against an embedded Kafka broker and a WireMock SDC.
 */
@EmbeddedKafka(kraft = true, partitions = 1,
        topics = {DistributionClientKafkaITTest.NOTIFICATION_TOPIC, DistributionClientKafkaITTest.STATUS_TOPIC})
@TestPropertySource(properties = {"mso.asdc-connections.asdc-controller1.sdcAddress=localhost:${wiremock.server.port}",
        "mso.asdc-connections.asdc-controller1.useHttpsWithSdc=false",
        "mso.asdc-connections.asdc-controller1.pollingInterval=15",
        "mso.asdc-connections.asdc-controller1.pollingTimeout=15"})
public class DistributionClientKafkaITTest extends BaseTest {

    static final String NOTIFICATION_TOPIC = "SDC-DISTR-NOTIF-TOPIC-AUTO";
    static final String STATUS_TOPIC = "SDC-DISTR-STATUS-TOPIC-AUTO";

    private static final String PNF_SERVICE_UUID = "77cf276e-905c-43f6-8d54-dda474be2f2e";
    private static final String PNF_SERVICE_INVARIANT_UUID = "913e6776-4bc3-49b9-b399-b5bb4690f0c7";
    private static final String CSAR_URL = "/sdc/v1/catalog/services/PnfService/1.0/artifacts/service-pnfservice.csar";
    private static final Duration TIMEOUT = Duration.ofSeconds(90);

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<JsonNode> statusMessages = new CopyOnWriteArrayList<>();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoSpyBean
    private ASDCConfiguration asdcConfiguration;

    @Autowired
    private ASDCController asdcController;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;

    @Autowired
    private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;

    private Producer<String, String> producer;
    private Consumer<String, String> statusTopicReader;
    private String distributionId;

    @Before
    public void setUp() throws Exception {
        distributionId = UUID.randomUUID().toString();
        doReturn("PLAINTEXT").when(asdcConfiguration).getKafkaSecurityProtocolConfig();
        doReturn("").when(asdcConfiguration).getKafkaSaslJaasConfig();

        stubSdc();
        wireMockServer
                .stubFor(post(urlEqualTo("/aai/" + AAIVersion.LATEST + "/service-design-and-creation/models/model/"
                        + PNF_SERVICE_INVARIANT_UUID + "/model-vers/model-ver/" + PNF_SERVICE_UUID + "?depth=0"))
                                .willReturn(ok()));

        // The client subscribes with auto.offset.reset=latest and exposes no hook for partition assignment, so
        // without a committed offset anything published before the assignment completes would be skipped.
        commitOffsetZero(asdcConfiguration.getConsumerGroup(), NOTIFICATION_TOPIC, STATUS_TOPIC);

        producer = new KafkaProducer<>(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString()),
                new StringSerializer(), new StringSerializer());
        statusTopicReader = new KafkaConsumer<>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(), new StringDeserializer());
        statusTopicReader.subscribe(List.of(STATUS_TOPIC));

        asdcController.initASDC();
    }

    @After
    public void tearDown() throws Exception {
        if (!asdcController.isStopped()) {
            asdcController.closeASDC();
        }
        producer.close();
        statusTopicReader.close();
    }

    @Test
    public void notificationOnKafka_IsDownloadedDeployedAndReportedOnStatusTopic() throws Exception {
        IDistributionClient client = asdcController.getDistributionClient();
        assertTrue(client instanceof DistributionClientImpl);

        publish(STATUS_TOPIC, componentDone("AAI"));
        publish(STATUS_TOPIC, componentDone("SDNC"));
        await().atMost(TIMEOUT)
                .until(() -> watchdogCDStatusRepository.findByDistributionId(distributionId).size() == 2);

        publish(NOTIFICATION_TOPIC, pnfNotification());

        await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(100)).until(() -> {
            readStatusTopic();
            return hasStatus(CSAR_URL, "NOTIFIED") && hasStatus(CSAR_URL, "DOWNLOAD_OK")
                    && hasStatus(CSAR_URL, "DEPLOY_OK") && hasStatus(null, "DISTRIBUTION_COMPLETE_OK");
        });

        wireMockServer.verify(getRequestedFor(urlEqualTo(CSAR_URL))
                .withHeader("X-ECOMP-InstanceID", equalTo(asdcConfiguration.getConsumerID()))
                .withHeader("Authorization",
                        equalTo("Basic " + Base64.getEncoder()
                                .encodeToString((asdcConfiguration.getUser() + ":" + asdcConfiguration.getPassword())
                                        .getBytes(StandardCharsets.UTF_8)))));
        assertTrue(serviceRepository.findById(PNF_SERVICE_UUID).isPresent());
        await().atMost(TIMEOUT).until(() -> !asdcController.isBusy());
        assertEquals("DISTRIBUTION_COMPLETE_OK", watchdogDistributionStatusRepository.findById(distributionId)
                .map(WatchdogDistributionStatus::getDistributionIdStatus).orElse(null));

        asdcController.closeASDC();

        assertTrue(asdcController.isStopped());
        assertNotEquals("SUCCESS", client.stop().getDistributionActionResult().name());
    }

    private void stubSdc() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo("/sdc/v1/artifactTypes"))
                .willReturn(okJson(mapper.writeValueAsString(ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST))));
        wireMockServer.stubFor(get(urlEqualTo("/sdc/v1/distributionKafkaData")).willReturn(
                okJson(mapper.writeValueAsString(Map.of("kafkaBootStrapServer", embeddedKafka.getBrokersAsString(),
                        "distrNotificationTopicName", NOTIFICATION_TOPIC, "distrStatusTopicName", STATUS_TOPIC)))));
        wireMockServer.stubFor(get(urlEqualTo(CSAR_URL)).willReturn(aResponse().withStatus(200)
                .withBody(Files.readAllBytes(Paths.get("src/test/resources/download/service-pnfservice.csar")))));
    }

    private void commitOffsetZero(String consumerGroup, String... topics) throws Exception {
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        for (String topic : topics) {
            offsets.put(new TopicPartition(topic, 0), new OffsetAndMetadata(0));
        }
        try (Admin admin =
                Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString()))) {
            admin.alterConsumerGroupOffsets(consumerGroup, offsets).all().get();
        }
    }

    private void publish(String topic, Object message) throws Exception {
        producer.send(new ProducerRecord<>(topic, mapper.writeValueAsString(message))).get();
    }

    private Map<String, Object> componentDone(String componentName) {
        return Map.of("distributionID", distributionId, "consumerID", componentName.toLowerCase() + "-id", "timestamp",
                System.currentTimeMillis(), "artifactURL", "", "status", "COMPONENT_DONE_OK", "componentName",
                componentName);
    }

    private Map<String, Object> pnfNotification() {
        Map<String, Object> csar = Map.of("artifactName", "service-pnfservice.csar", "artifactType", "TOSCA_CSAR",
                "artifactURL", CSAR_URL, "artifactChecksum", "ZjUzNjg1NDMyMTc4MWJmZjFlNDcyOGQ0Zjc1YWQwYzQ=",
                "artifactVersion", "1.0", "artifactUUID", UUID.randomUUID().toString());
        Map<String, Object> pnf = Map.of("resourceInstanceName", "PNF CDS Test", "resourceName", "PNF CDS Test",
                "resourceVersion", "1.0", "resoucreType", "PNF", "resourceUUID", "aa5d0562-80e7-43e9-af74-3085e57ab09f",
                "resourceInvariantUUID", "17d9d183-cee5-4a46-b5c4-6d5203f7d2e8", "resourceCustomizationUUID",
                "9f01263a-eaf7-4d98-a37b-3785f751903e", "category", "Application L4+", "subcategory", "Firewall",
                "artifacts", List.of());
        return Map.of("distributionID", distributionId, "serviceName", "PNF Service Test CDS", "serviceVersion", "1.0",
                "serviceUUID", PNF_SERVICE_UUID, "serviceInvariantUUID", PNF_SERVICE_INVARIANT_UUID, "resources",
                List.of(pnf), "serviceArtifacts", List.of(csar));
    }

    private void readStatusTopic() throws Exception {
        for (ConsumerRecord<String, String> record : statusTopicReader.poll(Duration.ofMillis(500))) {
            statusMessages.add(mapper.readTree(record.value()));
        }
    }

    private boolean hasStatus(String artifactUrl, String status) {
        return statusMessages.stream()
                .anyMatch(message -> distributionId.equals(message.path("distributionID").asText())
                        && status.equals(message.path("status").asText())
                        && (artifactUrl == null || artifactUrl.equals(message.path("artifactURL").asText())));
    }
}
