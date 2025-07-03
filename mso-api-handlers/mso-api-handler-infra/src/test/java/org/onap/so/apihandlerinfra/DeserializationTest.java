package org.onap.so.apihandlerinfra;

import static org.junit.Assert.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.GetOrchestrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.data.geo.GeoModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;

public class DeserializationTest extends BaseTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    @SneakyThrows
    public void foo() throws JsonMappingException, JsonProcessingException {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
            scanResult.getSubclasses(JsonDeserializer.class.getName()).forEach(cls -> {
                if (cls.getName().toLowerCase().contains("timestamp")) {
                    System.out.println("Timestamp-related deserializer: " + cls.getName());
                }
            });
        }

        ObjectMapper localMapper = new ObjectMapper();
        localMapper.registerModule(new Jdk8Module());
        localMapper.registerModule(new JSR310Module());
        localMapper.registerModule(new KotlinModule());
        localMapper.registerModule(new ParameterNamesModule());
        localMapper.registerModule(new JsonComponentModule());
        localMapper.registerModule(new GeoModule());

        String json = Files.readString(
                Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestInstanceGroup.json"));

        localMapper.readValue(json, InfraActiveRequests.class);
        mapper.readValue(json, InfraActiveRequests.class);

        assertTrue(true);
    }

    @Test
    @SneakyThrows
    public void foobar() {
        ObjectMapper localMapper = new ObjectMapper();
        // "{\"startTime\":\"2025-07-08T09:29:54.000+0000\"}"
        String json = Files.readString(
                Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestInstanceGroup.json"));

        InfraActiveRequests response = localMapper.readValue(json, InfraActiveRequests.class);

        // mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        InfraActiveRequests response2 = mapper.readValue(json, InfraActiveRequests.class);
        assertTrue(true);
    }

    @Test
    @SneakyThrows
    public void thatTimestampCanBeDeserialized() {
        ObjectMapper localMapper = new ObjectMapper();
        String localConfig = getConfigDetails(localMapper);
        String mapperConfig = getConfigDetails(mapper);
        String json = Files.readString(
                Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestInstanceGroup.json"));
        localMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        localMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        localMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        localMapper.registerModule(new Jdk8Module());
        localMapper.registerModule(new JSR310Module());
        localMapper.registerModule(new KotlinModule());
        localMapper.registerModule(new ParameterNamesModule());
        localMapper.registerModule(new JsonComponentModule());
        localMapper.registerModule(new GeoModule());
        InfraActiveRequests response = localMapper.readValue(json, InfraActiveRequests.class);

        mapper.registerModule(new JSR310Module());
        SimpleModule module = new SimpleModule();
        // module.addDeserializer(TimestampDeserializer.class);
        // mapper.registerModule()
        InfraActiveRequests response2 = mapper.readValue(json, InfraActiveRequests.class);
        assertTrue(true);
    }

    public static String getConfigDetails(ObjectMapper mapper) {
        StringBuilder sb = new StringBuilder();

        sb.append("Modules:\n");
        if (mapper.getRegisteredModuleIds().isEmpty()) {
            sb.append("\t").append("-none-").append("\n");
        }
        for (Object m : mapper.getRegisteredModuleIds()) {
            sb.append("  ").append(m).append("\n");
        }

        sb.append("\nSerialization Features:\n");
        for (SerializationFeature f : SerializationFeature.values()) {
            sb.append("\t").append(f).append(" -> ")
                    .append(mapper.getSerializationConfig().hasSerializationFeatures(f.getMask()));
            if (f.enabledByDefault()) {
                sb.append(" (enabled by default)");
            }
            sb.append("\n");
        }

        sb.append("\nDeserialization Features:\n");
        for (DeserializationFeature f : DeserializationFeature.values()) {
            sb.append("\t").append(f).append(" -> ")
                    .append(mapper.getDeserializationConfig().hasDeserializationFeatures(f.getMask()));
            if (f.enabledByDefault()) {
                sb.append(" (enabled by default)");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
