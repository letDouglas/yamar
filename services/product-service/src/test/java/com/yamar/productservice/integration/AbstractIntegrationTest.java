package com.yamar.productservice.integration;

import com.yamar.productservice.config.TestSecurityConfig;
import com.yamar.productservice.repository.ProductRepository;
import com.yamar.productservice.repository.search.ProductSearchRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected Integer port;

    @Autowired
    protected ProductRepository mongoRepository;

    @Autowired
    protected ProductSearchRepository elasticRepository;

    // --- INFRASTRUCTURE SETUP ---
    static final Network network = Network.newNetwork();

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withNetwork(network);

    static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.1")
            .withNetwork(network)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m");

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withNetwork(network);

    static final GenericContainer<?> schemaRegistryContainer = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.6.1"))
            .withNetwork(network)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafkaContainer.getNetworkAliases().get(0) + ":9092")
            .dependsOn(kafkaContainer)
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));

    static {
        mongoDBContainer.start();
        elasticsearchContainer.start();
        kafkaContainer.start();
        schemaRegistryContainer.start();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        String schemaRegistryUrl = "http://" + schemaRegistryContainer.getHost() + ":" + schemaRegistryContainer.getMappedPort(8081);
        registry.add("spring.kafka.producer.properties.schema.registry.url", () -> schemaRegistryUrl);
        registry.add("spring.kafka.consumer.properties.schema.registry.url", () -> schemaRegistryUrl);

        registry.add("spring.kafka.producer.properties.value.subject.name.strategy",
                () -> "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");

        registry.add("spring.kafka.consumer.properties.value.subject.name.strategy",
                () -> "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
    }

    // --- COMMON SETUP (DRY Principle) ---
    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1";
        // Clean state ensures test isolation
        mongoRepository.deleteAll();
        elasticRepository.deleteAll();
    }

    // --- HELPER METHODS ---
    protected String createProductAndGetId(String name, String description, String category, double price, String originCountry) {
        String requestBody = String.format("""
                {
                  "name": "%s",
                  "description": "%s",
                  "category": "%s",
                  "images": ["https://example.com/image1.jpg"],
                  "price": %.2f,
                  "originCountry": "%s"
                }
                """, name, description, category, price, originCountry);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}