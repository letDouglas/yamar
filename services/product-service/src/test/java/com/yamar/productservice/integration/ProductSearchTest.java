package com.yamar.productservice.integration;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.model.Category;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;

class ProductSearchTest extends AbstractIntegrationTest {

    @Test
    void shouldSyncFullLifecycleToElasticsearch() {
        // --- PHASE 1: CREATE (Command -> Event -> Elastic) ---
        ProductRequest createReq = ProductRequest.builder()
                .name("Searchable Guitar")
                .description("Initial Description")
                .category(Category.GUITAR)
                .price(BigDecimal.valueOf(1000))
                .originCountry("USA")
                .images(List.of("http://img.com"))
                .build();

        String productId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createReq)
                .post("/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Assert: Elastic has the product (Name check)
        await().atMost(Duration.ofSeconds(10)).until(() -> searchProduct("Searchable Guitar"));

        // --- PHASE 2: UPDATE (Command -> Event -> Elastic) ---
        ProductUpdateRequest updateReq = ProductUpdateRequest.builder()
                .id(productId)
                .name("Updated Guitar Name") // Changed Name
                .description("Updated Description")
                .build();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateReq)
                .put("/products")
                .then()
                .statusCode(200);

        // Assert: Elastic has the NEW name (Query by new name)
        await().atMost(Duration.ofSeconds(10)).until(() -> searchProduct("Updated Guitar Name"));

        // --- PHASE 3: DELETE (Command -> Event -> Elastic) ---
        RestAssured.given()
                .delete("/products/" + productId)
                .then()
                .statusCode(204);

        // Assert: Elastic is empty (Query by ID should yield nothing eventually)
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            List<?> results = RestAssured.given()
                    .queryParam("query", "Updated Guitar Name")
                    .get("/products/search")
                    .then()
                    .statusCode(200)
                    .extract().path("$");
            return results.isEmpty();
        });
    }

    // Helper method to keep test clean
    private boolean searchProduct(String queryTerm) {
        List<String> names = RestAssured.given()
                .queryParam("query", queryTerm)
                .get("/products/search")
                .then()
                .statusCode(200)
                .extract().path("name");
        return !names.isEmpty() && names.get(0).equals(queryTerm);
    }
}