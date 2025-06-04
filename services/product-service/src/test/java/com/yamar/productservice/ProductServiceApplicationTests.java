package com.yamar.productservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Import(TestcontainersConfiguration.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @LocalServerPort
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String basePath;

    private String productsPath;

    @Autowired
    private MongoTemplate mongoTemplate;

    // --- SETUP ---

    @BeforeEach
    void cleanDatabaseAndSetup() {
        cleanDatabase();
        setupRestAssured();
    }

    private void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    private void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        productsPath = basePath + "/products";
    }

    // --- TESTS ---

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                {
                  "name": "Andean Charango",
                  "description": "Handmade charango from Bolivia, crafted with traditional techniques.",
                  "category": "CHARANGO",
                  "images": [
                    "https://example.com/images/charango1.jpg",
                    "https://example.com/images/charango2.jpg"
                  ],
                  "price": 250.50,
                  "originCountry": "Bolivia"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post(productsPath)
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Andean Charango"))
                .body("description", Matchers.equalTo("Handmade charango from Bolivia, crafted with traditional techniques."))
                .body("category", Matchers.equalTo("CHARANGO"))
                .body("images", Matchers.hasSize(2))
                .body("price", Matchers.equalTo(250.5f))
                .body("originCountry", Matchers.equalTo("Bolivia"));
    }

    @Test
    void shouldGetAllProducts() {
        String id1 = createProductAndGetId(
                "Andean Charango",
                "Handmade charango from Bolivia, crafted with traditional techniques.",
                "CHARANGO",
                250.50,
                "Bolivia"
        );

        String id2 = createProductAndGetId(
                "Amazon Flute",
                "Traditional flute from the Amazon rainforest.",
                "FLUTE",
                150.00,
                "Brazil"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .get(productsPath)
                .then()
                .statusCode(200)
                .body("id", Matchers.hasItems(id1, id2))
                .body("name", Matchers.hasItems("Andean Charango", "Amazon Flute"));
    }

    @Test
    void shouldUpdateProduct() {
        String originalId = createProductAndGetId(
                "Original Charango",
                "Original description",
                "CHARANGO",
                300.00,
                "Peru"
        );

        String updateRequestBody = String.format("""
                {
                  "id": "%s",
                  "name": "Updated Charango",
                  "description": "Updated description",
                  "category": "CHARANGO",
                  "images": ["https://example.com/updated1.jpg"],
                  "price": 350.00,
                  "originCountry": "Chile"
                }
                """, originalId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateRequestBody)
                .put(productsPath)
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(originalId))
                .body("name", Matchers.equalTo("Updated Charango"))
                .body("description", Matchers.equalTo("Updated description"))
                .body("category", Matchers.equalTo("CHARANGO"))
                .body("images", Matchers.hasSize(1))
                .body("price", Matchers.equalTo(350.0f))
                .body("originCountry", Matchers.equalTo("Chile"));
    }

    @Test
    void shouldGetProductById() {
        String id = createProductAndGetId(
                "Quena",
                "Traditional Andean flute",
                "FLUTE",
                120.00,
                "Peru"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .get(productsPath + "/" + id)
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(id))
                .body("name", Matchers.equalTo("Quena"))
                .body("description", Matchers.equalTo("Traditional Andean flute"))
                .body("category", Matchers.equalTo("FLUTE"))
                .body("price", Matchers.equalTo(120.0f))
                .body("originCountry", Matchers.equalTo("Peru"));
    }

    @Test
    void shouldReturnTrueIfProductExists() {
        String id = createProductAndGetId(
                "Panpipe",
                "Wind instrument from the Andes",
                "FLUTE",
                100.00,
                "Ecuador"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .get(productsPath + "/exists/" + id)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("true"));
    }

    @Test
    void shouldReturnFalseIfProductDoesNotExist() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .get(productsPath + "/exists/nonexistent-id-123")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("false"));
    }

    @Test
    void shouldDeleteProduct() {
        String id = createProductAndGetId(
                "Quena Andina",
                "Traditional Andean flute made of wood, handcrafted in Peru.",
                "FLUTE",
                120.0,
                "Peru"
        );

        RestAssured.given()
                .delete(productsPath + "/" + id)
                .then()
                .statusCode(204);

        RestAssured.given()
                .get(productsPath + "/exists/" + id)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("false"));
    }

    // --- TESTS Batch Operation---
    @Test
    void shouldGetProductsByIdsBatch() {
        String charangoId = createProductAndGetId(
                "Andean Charango",
                "Handmade charango from Bolivia",
                "CHARANGO",
                250.50,
                "Bolivia"
        );

        String quenaId = createProductAndGetId(
                "Quena Andina",
                "Traditional Andean flute",
                "FLUTE",
                120.00,
                "Peru"
        );

        String panpipeId = createProductAndGetId(
                "Panpipe",
                "Wind instrument from the Andes",
                "FLUTE",
                100.00,
                "Ecuador"
        );

        String batchRequestBody = String.format("""
                {
                  "productIds": ["%s", "%s", "%s"]
                }
                """, charangoId, quenaId, panpipeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("id", Matchers.hasItems(charangoId, quenaId, panpipeId))
                .body("name", Matchers.hasItems("Andean Charango", "Quena Andina", "Panpipe"))
                .body("price", Matchers.hasItems(250.5f, 120.0f, 100.0f))
                .body("originCountry", Matchers.hasItems("Bolivia", "Peru", "Ecuador"));
    }

    @Test
    void shouldReturnValidationErrorForEmptyBatchRequest() {
        String emptyBatchRequest = """
                {
                  "productIds": []
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyBatchRequest)
                .post(productsPath + "/batch")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturnValidationErrorForMissingProductIds() {
        String invalidRequest = "{}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .post(productsPath + "/batch")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturnValidationErrorForNullProductIds() {
        String nullRequest = """
                {
                  "productIds": null
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(nullRequest)
                .post(productsPath + "/batch")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldHandlePartiallyMissingProductsInBatch() {
        String existingId = createProductAndGetId(
                "Existing Charango",
                "This product exists",
                "CHARANGO",
                200.00,
                "Bolivia"
        );

        String batchRequestBody = String.format("""
                {
                  "productIds": ["%s", "nonexistent-id-123"]
                }
                """, existingId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleLargeBatchRequest() {
        String id1 = createProductAndGetId("Product 1", "Description 1", "CHARANGO", 100.0, "Bolivia");
        String id2 = createProductAndGetId("Product 2", "Description 2", "FLUTE", 150.0, "Peru");
        String id3 = createProductAndGetId("Product 3", "Description 3", "CHARANGO", 200.0, "Ecuador");

        String batchRequestBody = String.format("""
                {
                  "productIds": ["%s", "%s", "%s"]
                }
                """, id1, id2, id3);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3));
    }

    @Test
    void shouldValidateBatchSizeLimit() {
        StringBuilder idsBuilder = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            if (i > 0) idsBuilder.append(", ");
            idsBuilder.append("\"fake-id-").append(i).append("\"");
        }

        String batchRequestBody = String.format("""
                {
                  "productIds": [%s]
                }
                """, idsBuilder.toString());

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldMaintainOrderInBatchResponse() {
        String id1 = createProductAndGetId("A-Product", "First product", "CHARANGO", 100.0, "Bolivia");
        String id2 = createProductAndGetId("B-Product", "Second product", "FLUTE", 150.0, "Peru");
        String id3 = createProductAndGetId("C-Product", "Third product", "CHARANGO", 200.0, "Ecuador");

        String batchRequestBody = String.format("""
                {
                  "productIds": ["%s", "%s", "%s"]
                }
                """, id3, id1, id2);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("name", Matchers.hasItems("A-Product", "B-Product", "C-Product"));
    }

    // --- TESTS -> Performance ---

    @Test
    void shouldPerformBatchRequestEfficiently() {
        List<String> createdIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String id = createProductAndGetId(
                    "Instrument " + i,
                    "Description " + i,
                    "CHARANGO",
                    100.0 + i,
                    "Country " + i
            );
            createdIds.add(id);
        }

        String idsString = createdIds.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(", "));

        String batchRequestBody = String.format("""
                {
                  "productIds": [%s]
                }
                """, idsString);

        long startTime = System.currentTimeMillis();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchRequestBody)
                .post(productsPath + "/batch")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(10));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        assertTrue(executionTime < 1000, "Batch request took too long: " + executionTime + "ms");
    }

    // --- HELPERS ---

    private String createProductAndGetId(String name, String description, String category, double price, String originCountry) {
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
                .post(productsPath)
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
