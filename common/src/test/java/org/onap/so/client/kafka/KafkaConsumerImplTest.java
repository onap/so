package org.onap.so.client.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
public class KafkaConsumerImplTest {
    private KafkaConsumerImpl consumer;
    private static MockConsumer<String, String> mockConsumer;
    @SystemStub
    EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setup() {
        environmentVariables.set("JAAS_CONFIG", "jaas.config");
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        configureMockConsumer();
    }

    @Test
    public void consumerShouldConsumeMessages() throws Exception {
        consumer = new KafkaConsumerImpl("localhost:9092");
        consumer.setConsumer(mockConsumer);
        List<String> response = consumer.get("TOPIC", "CG1", "C1");
        assertThat(response).contains("I", "like", "pizza");
    }

    private void configureMockConsumer() {
        mockConsumer.assign(Arrays.asList(new TopicPartition("TOPIC", 0)));

        HashMap<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(new TopicPartition("TOPIC", 0), 0L);
        mockConsumer.updateBeginningOffsets(beginningOffsets);
        mockConsumer.addRecord(new ConsumerRecord<String, String>("TOPIC", 0, 0L, "key", "I"));
        mockConsumer.addRecord(new ConsumerRecord<String, String>("TOPIC", 0, 1L, "key", "like"));
        mockConsumer.addRecord(new ConsumerRecord<String, String>("TOPIC", 0, 2L, "key", "pizza"));

    }
}
