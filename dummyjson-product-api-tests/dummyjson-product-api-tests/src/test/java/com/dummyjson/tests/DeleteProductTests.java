package com.dummyjson.tests;

import com.dummyjson.base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.dummyjson.utils.ProductEndpoints.DELETE_PRODUCT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for: DELETE /products/{id}  (Delete a product)
 * Docs: https://dummyjson.com/docs/products/#products-delete
 * <p>
 * NOTE: DummyJSON simulates the delete. It returns the (still otherwise
 * intact) product enriched with "isDeleted": true and a "deletedOn"
 * timestamp; nothing is actually removed server-side.
 */
@Tag("products")
@Tag("delete")
@DisplayName("Delete Product API Tests")
class DeleteProductTests extends BaseTest {

    // ----------------------------------------------------------------
    // Positive scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POSITIVE: DELETE /products/1 returns 200 with isDeleted=true and a deletedOn timestamp")
    void deleteProduct_validId_returns200WithDeletionMarkers() {
        given()
                .spec(requestSpec)
                .pathParam("id", 1)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", notNullValue());
    }

    @Test
    @DisplayName("POSITIVE: DELETE /products/{id} still returns the original product's core fields")
    void deleteProduct_responseStillContainsOriginalProductData() {
        given()
                .spec(requestSpec)
                .pathParam("id", 10)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(200)
                .body("id", equalTo(10))
                .body("title", notNullValue())
                .body("price", notNullValue())
                .body("isDeleted", equalTo(true));
    }

    @Test
    @DisplayName("POSITIVE: deletedOn timestamp is a parseable ISO-8601 date-time")
    void deleteProduct_deletedOnIsValidIsoTimestamp() {
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", 15)
                .when()
                .delete(DELETE_PRODUCT);

        response.then().statusCode(200);

        String deletedOn = response.path("deletedOn");
        assertTrue(deletedOn != null && !deletedOn.isBlank(), "deletedOn should not be blank");

        // Will throw a DateTimeParseException (failing the test) if not valid ISO-8601
        java.time.Instant.parse(deletedOn);
    }

    // ----------------------------------------------------------------
    // Negative scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("NEGATIVE: DELETE /products/{id} for a non-existent id returns 404")
    void deleteProduct_nonExistentId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", 999999)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("NEGATIVE: DELETE /products/{id} with a non-numeric id does not succeed as a deletion")
    void deleteProduct_nonNumericId_doesNotSucceed() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .delete("/products/abc");

        assertTrue(response.statusCode() >= 400,
                "Expected a 4xx error for a non-numeric product id on delete, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: DELETE /products/{id} with a negative id returns 404")
    void deleteProduct_negativeId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", -5)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("NEGATIVE: DELETE /products/{id} called twice for the same id does not error the second time")
    void deleteProduct_calledTwiceForSameId_doesNotErrorOnSecondCall() {
        // Since DummyJSON doesn't persist state, deleting the same id twice should
        // behave identically both times (idempotent simulation) rather than 500 on retry.
        given()
                .spec(requestSpec)
                .pathParam("id", 20)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(200)
                .body("isDeleted", equalTo(true));

        given()
                .spec(requestSpec)
                .pathParam("id", 20)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(200)
                .body("isDeleted", equalTo(true));
    }

    @Test
    @DisplayName("NEGATIVE: DELETE /products/{id} with id=0 returns 404")
    void deleteProduct_zeroId_returns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", 0)
                .when()
                .delete(DELETE_PRODUCT)
                .then()
                .statusCode(404);
    }
}
