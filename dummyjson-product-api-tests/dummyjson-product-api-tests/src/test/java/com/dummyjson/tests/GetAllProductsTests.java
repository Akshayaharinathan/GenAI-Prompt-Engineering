package com.dummyjson.tests;

import com.dummyjson.base.BaseTest;
import com.dummyjson.models.Product;
import com.dummyjson.models.ProductsResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dummyjson.utils.ProductEndpoints.ALL_PRODUCTS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for: GET /products  (Get all products)
 * Docs: https://dummyjson.com/docs/products/#products-all
 */
@Tag("products")
@Tag("getAll")
@DisplayName("Get All Products API Tests")
class GetAllProductsTests extends BaseTest {

    // ----------------------------------------------------------------
    // Positive scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POSITIVE: GET /products returns 200 with default page of 30 products")
    void getAllProducts_returns200AndDefaultPageSize() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(ALL_PRODUCTS);

        response.then()
                .statusCode(200)
                .body("products", notNullValue())
                .body("products.size()", equalTo(30))
                .body("total", greaterThan(0))
                .body("limit", equalTo(30))
                .body("skip", equalTo(0));
    }

    @Test
    @DisplayName("POSITIVE: GET /products deserializes into ProductsResponse with required fields populated")
    void getAllProducts_deserializesIntoPojoWithRequiredFields() {
        ProductsResponse productsResponse = given()
                .spec(requestSpec)
                .when()
                .get(ALL_PRODUCTS)
                .then()
                .statusCode(200)
                .extract()
                .as(ProductsResponse.class);

        assertNotNull(productsResponse.getProducts(), "products list should not be null");
        assertFalse(productsResponse.getProducts().isEmpty(), "products list should not be empty");

        Product first = productsResponse.getProducts().get(0);
        assertNotNull(first.getId(), "id should be present");
        assertNotNull(first.getTitle(), "title should be present");
        assertNotNull(first.getDescription(), "description should be present");
        assertNotNull(first.getCategory(), "category should be present");
        assertNotNull(first.getPrice(), "price should be present");
        assertNotNull(first.getBrand(), "brand may be present for most products");
    }

    @Test
    @DisplayName("POSITIVE: GET /products?limit=10 returns exactly 10 products")
    void getAllProducts_withLimit_returnsRequestedCount() {
        given()
                .spec(requestSpec)
                .queryParam("limit", 10)
                .when()
                .get(ALL_PRODUCTS)
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(10))
                .body("limit", equalTo(10));
    }

    @Test
    @DisplayName("POSITIVE: GET /products?limit=10&skip=10 paginates correctly")
    void getAllProducts_withLimitAndSkip_paginatesCorrectly() {
        given()
                .spec(requestSpec)
                .queryParam("limit", 10)
                .queryParam("skip", 10)
                .when()
                .get(ALL_PRODUCTS)
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(10))
                .body("skip", equalTo(10))
                .body("products[0].id", equalTo(11));
    }

    @Test
    @DisplayName("POSITIVE: GET /products?limit=0 returns all available products (no pagination)")
    void getAllProducts_withLimitZero_returnsAllProducts() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", 0)
                .when()
                .get(ALL_PRODUCTS);

        response.then().statusCode(200);

        int total = response.path("total");
        List<Object> products = response.path("products");
        assertEquals(total, products.size(), "When limit=0, all products should be returned");
    }

    @Test
    @DisplayName("POSITIVE: GET /products?select=title,price returns only requested fields")
    void getAllProducts_withSelect_returnsOnlySelectedFields() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", 5)
                .queryParam("select", "title,price")
                .when()
                .get(ALL_PRODUCTS);

        response.then().statusCode(200);

        // 'id' is always returned by the API regardless of select; category/brand should be absent
        response.then()
                .body("products[0].title", notNullValue())
                .body("products[0].price", notNullValue())
                .body("products[0].category", nullValue())
                .body("products[0].brand", nullValue());
    }

    @Test
    @DisplayName("POSITIVE: GET /products?sortBy=title&order=asc returns a valid title list")
    void getAllProducts_sortedAscendingByTitle_returnsTitleList() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("sortBy", "title")
                .queryParam("order", "asc")
                .when()
                .get(ALL_PRODUCTS);

        response.then().statusCode(200);

        List<String> titles = response.path("products.title");
        assertFalse(titles.isEmpty(), "Expected at least one title in the response");
        for (String title : titles) {
            assertNotNull(title, "Each product should have a non-null title");
            assertFalse(title.isBlank(), "Each product title should be non-blank");
        }
    }

    @Test
    @DisplayName("POSITIVE: Every product in the default page has a non-negative price")
    void getAllProducts_allPricesAreNonNegative() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(ALL_PRODUCTS);

        response.then().statusCode(200);

        List<Float> prices = response.path("products.price");
        for (Float price : prices) {
            assertTrue(price >= 0, "Price should never be negative, found: " + price);
        }
    }

    // ----------------------------------------------------------------
    // Negative scenarios
    // ----------------------------------------------------------------

    @Test
    @DisplayName("NEGATIVE: GET /products with a non-numeric limit is handled gracefully (no 5xx)")
    void getAllProducts_withInvalidLimitType_doesNotServerError() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", "abc")
                .when()
                .get(ALL_PRODUCTS);

        // The API should not blow up with a server error even with a malformed query param
        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for invalid 'limit' param, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: GET /products?limit=-5 (negative limit) does not cause a server error")
    void getAllProducts_withNegativeLimit_doesNotServerError() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", -5)
                .when()
                .get(ALL_PRODUCTS);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for negative 'limit' param, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: GET /products?skip=999999 (skip beyond dataset) returns an empty product list")
    void getAllProducts_withSkipBeyondDataset_returnsEmptyList() {
        given()
                .spec(requestSpec)
                .queryParam("skip", 999999)
                .when()
                .get(ALL_PRODUCTS)
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(0));
    }

    @Test
    @DisplayName("NEGATIVE: GET on a misspelled products path returns 404")
    void getAllProducts_misspelledPath_returns404() {
        given()
                .spec(requestSpec)
                .when()
                .get("/productz")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("NEGATIVE: GET /products?sortBy=nonExistentField does not cause a server error")
    void getAllProducts_sortByInvalidField_doesNotServerError() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("sortBy", "nonExistentField12345")
                .queryParam("order", "asc")
                .when()
                .get(ALL_PRODUCTS);

        assertTrue(response.statusCode() < 500,
                "Expected a non-5xx response for an invalid sortBy field, got: " + response.statusCode());
    }

    @Test
    @DisplayName("NEGATIVE: total field is always >= number of products returned in the page")
    void getAllProducts_totalIsAlwaysGreaterOrEqualToPageSize() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", 5)
                .when()
                .get(ALL_PRODUCTS);

        response.then()
                .statusCode(200)
                .body("total", greaterThanOrEqualTo(5));
    }
}
