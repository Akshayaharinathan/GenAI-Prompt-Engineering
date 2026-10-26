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

import static com.dummyjson.utils.ProductEndpoints.ADD_PRODUCT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for: POST /products/add  (Add a new product)
 * Docs: https://dummyjson.com/docs/products/#products-add
 * <p>
 * NOTE: DummyJSON simulates the create operation. It does not persist the
 * product server-side; it echoes back the submitted payload merged with a
 * newly generated id (typically total + 1). Because nothing is actually
 * persisted, the API is intentionally lenient and will accept malformed or
 * partial payloads with 200/201 rather than rejecting them - the "invalid
 * payload" tests below assert on that documented simulated behavior while
 * still verifying the contract does not silently corrupt data or 500.
 */
@Tag("products")
@Tag("create")
@DisplayName("Create Product API Tests")
class CreateProductTests extends BaseTest {

    // ----------------------------------------------------------------
    // Positive scenarios - valid JSON payload
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POSITIVE: POST /products/add with a complete valid payload returns 201 and echoes data with a new id")
    void createProduct_validPayload_returns201WithNewId() {
        Product newProduct = new Product(null, "BMW Pencil", "A premium branded pencil", "stationery", 5.99, "BMW");

        Product created = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(newProduct)
                .when()
                .post(ADD_PRODUCT)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("BMW Pencil"))
                .body("category", equalTo("stationery"))
                .body("price", equalTo(5.99f))
                .body("brand", equalTo("BMW"))
                .extract()
                .as(Product.class);

        assertNotNull(created.getId(), "A new id should be generated for the created product");
        assertEquals("BMW Pencil", created.getTitle());
        assertEquals(5.99, created.getPrice(), 0.001);
    }

    @Test
    @DisplayName("POSITIVE: POST /products/add with only a title still returns 201 (server fills defaults)")
    void createProduct_minimalValidPayload_returns201() {
        Map<String, Object> minimalPayload = new HashMap<>();
        minimalPayload.put("title", "Minimal Product");

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(minimalPayload)
                .when()
                .post(ADD_PRODUCT)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Minimal Product"));
    }

    @Test
    @DisplayName("POSITIVE: created product id is numeric and greater than zero")
    void createProduct_returnedIdIsPositiveNumber() {
        Product newProduct = new Product(null, "Test Widget", "A test widget", "tools", 12.50, "Acme");

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(newProduct)
                .when()
                .post(ADD_PRODUCT);

        response.then().statusCode(201);
        int newId = response.path("id");
        assertTrue(newId > 0, "Newly created product id should be a positive integer, got: " + newId);
    }

    // ----------------------------------------------------------------
    // Negative scenarios - invalid JSON payload
    // ----------------------------------------------------------------

    @Test
    @DisplayName("NEGATIVE: POST /products/add with an empty JSON body does not cause a 5xx server error")
    void createProduct_emptyJsonBody_doesNotServerError() {
        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for an empty JSON body, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add with malformed/syntactically-invalid JSON returns a 4xx error")
    void createProduct_malformedJson_returns4xx() {
        String malformedJson = "{ \"title\": \"Broken Product\", \"price\": , }";

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(malformedJson)
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                "Expected a 4xx client error for malformed JSON syntax, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add with price as a non-numeric string does not cause a server error")
    void createProduct_priceAsInvalidString_doesNotServerError() {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("title", "Invalid Price Product");
        invalidPayload.put("price", "not-a-number");

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(invalidPayload)
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response when price is a non-numeric string, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add with a negative price does not cause a server error")
    void createProduct_negativePrice_doesNotServerError() {
        Product negativePriceProduct = new Product(null, "Negative Price Item", "desc", "misc", -10.0, "Acme");

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(negativePriceProduct)
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for a negative price, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add without a Content-Type header is rejected or handled gracefully")
    void createProduct_missingContentType_isHandledGracefully() {
        Response response = given()
                .baseUri(BASE_URI)
                .body("{\"title\":\"No Content Type Product\"}")
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response when Content-Type header is omitted, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add with wrong data types for known fields does not 500")
    void createProduct_wrongDataTypesForFields_doesNotServerError() {
        Map<String, Object> wrongTypesPayload = new HashMap<>();
        wrongTypesPayload.put("title", 12345);          // should be a string
        wrongTypesPayload.put("price", "free");         // should be a number
        wrongTypesPayload.put("category", true);        // should be a string

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(wrongTypesPayload)
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for mismatched field data types, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: POST /products/add with an array instead of a JSON object is handled gracefully")
    void createProduct_arrayInsteadOfObject_doesNotServerError() {
        String arrayPayload = "[\"title\", \"price\"]";

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(arrayPayload)
                .when()
                .post(ADD_PRODUCT);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response when posting a JSON array instead of an object, got: "
                        + response.statusCode());
    }
}
