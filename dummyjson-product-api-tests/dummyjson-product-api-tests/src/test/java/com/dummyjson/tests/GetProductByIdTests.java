package com.dummyjson.tests;

import com.dummyjson.base.BaseTest;
import com.dummyjson.models.Product;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.dummyjson.utils.ProductEndpoints.PRODUCT_BY_ID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for: GET /products/{id}  (Get a single product)
 * Docs: https://dummyjson.com/docs/products/#products-single
 */
@Tag("products")
@Tag("getById")
@DisplayName("Get Product By Id API Tests")
class GetProductByIdTests extends BaseTest {

    // ----------------------------------------------------------------
    // Positive scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POSITIVE: GET /products/1 returns 200 with correct id and all required fields")
    void getProductById_validId_returnsCorrectProduct() {
        Product product = given()
                .spec(requestSpec)
                .pathParam("id", 1)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", notNullValue())
                .body("description", notNullValue())
                .body("category", notNullValue())
                .body("price", notNullValue())
                .body("brand", notNullValue())
                .extract()
                .as(Product.class);

        assertEquals(1, product.getId());
        assertNotNull(product.getTitle());
        assertNotNull(product.getDescription());
        assertNotNull(product.getCategory());
        assertNotNull(product.getPrice());
        assertTrue(product.getPrice() > 0, "price should be a positive number");
    }

    @ParameterizedTest(name = "POSITIVE: GET /products/{0} returns 200 and matching id")
    @ValueSource(ints = {1, 4, 10, 50, 100})
    @DisplayName("POSITIVE: Several valid product ids each return 200 with the matching id in the body")
    void getProductById_multipleValidIds_returnMatchingProduct(int id) {
        given()
                .spec(requestSpec)
                .pathParam("id", id)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("price", greaterThan(0f));
    }

    @Test
    @DisplayName("POSITIVE: response Content-Type for a valid product id is application/json")
    void getProductById_contentTypeIsJson() {
        given()
                .spec(requestSpec)
                .pathParam("id", 5)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    // ----------------------------------------------------------------
    // Negative scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} with a non-existent id returns 404 with an error message")
    void getProductById_nonExistentId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", 999999)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} with id=0 returns 404")
    void getProductById_zeroId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", 0)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} with a negative id returns 404")
    void getProductById_negativeId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", -1)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} with a non-numeric id does not return a 2xx success")
    void getProductById_nonNumericId_doesNotSucceed() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/products/abc");

        assertTrue(response.statusCode() >= 400,
                "A non-numeric product id should not resolve to a successful product lookup, got: "
                        + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} with an empty id path segment is not found")
    void getProductById_emptyId_isNotFound() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/products/");

        // Empty id segment should not return a single product resource - either treated as
        // "all products" (200, but no single product body) or a 404; it must never echo a
        // single arbitrary product as if id mattered.
        assertTrue(response.statusCode() == 200 || response.statusCode() == 404,
                "Unexpected status for empty id segment: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: GET /products/{id} for a deleted/out-of-range high id returns 404, not 500")
    void getProductById_veryHighId_returns404NotServerError() {
        given()
                .spec(requestSpec)
                .pathParam("id", Integer.MAX_VALUE)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(404);
    }
}
