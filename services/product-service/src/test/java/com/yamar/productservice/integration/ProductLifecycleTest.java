package com.yamar.productservice.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;

class ProductLifecycleTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                {
                  "name": "Andean Charango",
                  "description": "Handmade charango.",
                  "category": "CHARANGO",
                  "images": ["http://img.com"],
                  "price": 250.50,
                  "originCountry": "Bolivia"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/products")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", equalTo("Andean Charango"));
    }

    @Test
    void shouldUpdateProduct() {
        String id = createProductAndGetId("Old Name", "Desc", "CHARANGO", 100.0, "Peru");

        String updateBody = String.format("""
                {
                  "id": "%s",
                  "name": "New Name",
                  "description": "Desc",
                  "category": "CHARANGO",
                  "images": ["http://img.com"],
                  "price": 100.0,
                  "originCountry": "Peru"
                }
                """, id);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .put("/products")
                .then()
                .statusCode(200)
                .body("name", equalTo("New Name"));
    }

    @Test
    void shouldDeleteProduct() {
        String id = createProductAndGetId("To Delete", "Desc", "FLUTE", 50.0, "Peru");

        RestAssured.given()
                .delete("/products/" + id)
                .then()
                .statusCode(204);

        RestAssured.given()
                .get("/products/exists/" + id)
                .then()
                .statusCode(200)
                .body(equalTo("false"));
    }

    @Test
    void shouldGetProductsBatch() {
        String id1 = createProductAndGetId("P1", "D1", "FLUTE", 10.0, "Peru");
        String id2 = createProductAndGetId("P2", "D2", "FLUTE", 20.0, "Peru");

        String batchBody = String.format("{\"productIds\": [\"%s\", \"%s\"]}", id1, id2);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(batchBody)
                .post("/products/batch")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }
}