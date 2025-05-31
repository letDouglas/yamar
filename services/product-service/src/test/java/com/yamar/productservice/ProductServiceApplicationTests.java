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
}
