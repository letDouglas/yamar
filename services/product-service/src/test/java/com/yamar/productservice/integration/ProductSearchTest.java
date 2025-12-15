package com.yamar.productservice.integration;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.model.Category;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductSearchTest extends AbstractIntegrationTest {

    @Test
    void shouldSyncProductToElasticsearchAfterCreation() {
        // 1. Create Product (Command)
        ProductRequest request = ProductRequest.builder()
                .name("Searchable Guitar")
                .description("A guitar for testing search")
                .category(Category.GUITAR)
                .price(BigDecimal.valueOf(999.00))
                .originCountry("USA")
                .images(List.of("http://img.com"))
                .build();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/products")
                .then()
                .statusCode(201);

        // 2. Verify Mongo (Immediate)
        assertEquals(1, mongoRepository.count());

        // 3. Verify Elastic (Async)
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    List<String> names = RestAssured.given()
                            .queryParam("query", "Searchable")
                            .when()
                            .get("/products/search")
                            .then()
                            .statusCode(200)
                            .extract()
                            .path("name");

                    return !names.isEmpty() && names.get(0).equals("Searchable Guitar");
                });
    }
}