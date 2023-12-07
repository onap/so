package org.onap.so.client.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.Properties;

public class KafkaClient {
    protected static Logger logger = LoggerFactory.getLogger(KafkaClient.class);
    protected final Properties properties;

    public KafkaClient(String filepath) throws IOException {
        Resource resource = new ClassPathResource(filepath);
        this.properties = new Properties();
        properties.load(resource.getInputStream());

    }

}
