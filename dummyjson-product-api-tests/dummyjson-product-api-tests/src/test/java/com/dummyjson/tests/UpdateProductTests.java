package com.dummyjson.tests;

import com.dummyjson.base.BaseTest;
import com.dummyjson.models.Product;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.dummyjson.utils.ProductEndpoints.UPDATE_PRODUCT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for: PUT/PATCH /products/{id}  (Update a product)
 * Docs: https://dummyjson.com/docs/products/#products-update
 * <p>
 * NOTE: DummyJSON simulates the update. It returns the product with the
 * submitted fields merged in, but nothing is persisted server-side.
 */
@Tag("products")
@Tag("update")
@DisplayName("Update Product API Tests")
class UpdateProductTests extends BaseTest {

    // ----------------------------------------------------------------
    // Positive scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POSITIVE: PUT /products/1 with a valid title update returns 200 with the updated title")
    void updateProduct_validTitleUpdate_returns200WithUpdatedTitle() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "iPhone Galaxy +1");

        Product updated = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 1)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("iPhone Galaxy +1"))
                .extract()
                .as(Product.class);

        assertEquals(1, updated.getId());
        assertEquals("iPhone Galaxy +1", updated.getTitle());
    }

    @Test
    @DisplayName("POSITIVE: PUT /products/{id} updating price returns 200 with the new price reflected")
    void updateProduct_validPriceUpdate_returns200WithUpdatedPrice() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("price", 999.99);

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 4)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("price", equalTo(999.99f));
    }

    @Test
    @DisplayName("POSITIVE: PUT /products/{id} updating multiple fields at once returns 200 reflecting all changes")
    void updateProduct_multipleFieldsAtOnce_returns200WithAllChanges() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Updated Multi-Field Product");
        updatePayload.put("category", "updated-category");
        updatePayload.put("brand", "UpdatedBrand");

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 7)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(7))
                .body("title", equalTo("Updated Multi-Field Product"))
                .body("category", equalTo("updated-category"))
                .body("brand", equalTo("UpdatedBrand"));
    }

    @Test
    @DisplayName("POSITIVE: PATCH /products/{id} behaves the same as PUT for a partial update")
    void updateProduct_usingPatchVerb_alsoSucceeds() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Patched Title");

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 2)
                .body(updatePayload)
                .when()
                .patch(UPDATE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("title", equalTo("Patched Title"));
    }

    // ----------------------------------------------------------------
    // Negative scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} for a non-existent id returns 404")
    void updateProduct_nonExistentId_returns404() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Should Not Update");

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 999999)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} with an empty JSON body does not cause a server error")
    void updateProduct_emptyBody_doesNotServerError() {
        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 3)
                .body("{}")
                .when()
                .put(UPDATE_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for an empty update body, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} with malformed JSON returns a 4xx error")
    void updateProduct_malformedJson_returns4xx() {
        String malformedJson = "{ \"title\": \"Broken\", \"price\": }";

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 5)
                .body(malformedJson)
                .when()
                .put(UPDATE_PRODUCT);

        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                "Expected a 4xx client error for malformed JSON, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} with a non-numeric id does not succeed as an update")
    void updateProduct_nonNumericId_doesNotSucceed() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Should Not Apply");

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/products/abc");

        assertTrue(response.statusCode() >= 400,
                "Expected a 4xx error for a non-numeric product id on update, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} with price as a wrong data type does not cause a server error")
    void updateProduct_priceWrongType_doesNotServerError() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("price", "expensive");

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", 6)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response when price has an invalid type, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: PUT /products/{id} with a negative id returns 404")
    void updateProduct_negativeId_returns404() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Negative Id Update");

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .pathParam("id", -1)
                .body(updatePayload)
                .when()
                .put(UPDATE_PRODUCT)
                .then()
                .statusCode(404);
    }
}
