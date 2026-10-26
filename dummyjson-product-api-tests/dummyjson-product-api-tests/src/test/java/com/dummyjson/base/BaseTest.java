package com.dummyjson.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for all API test classes.
 * <p>
 * Centralizes RestAssured configuration: base URI, default request/response
 * specifications, and logging-on-failure so that every test class behaves
 * consistently and avoids duplicating boilerplate setup.
 * <p>
 * Base URL under test: https://dummyjson.com  (Products docs: /docs/products)
 */
public abstract class BaseTest {

    /** Base URI for the system under test. Overridable via -Dapi.base.uri=... */
    protected static final String BASE_URI = System.getProperty("api.base.uri", "https://dummyjson.com");

    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpecOk;

    @BeforeAll
    static void globalSetup() {
        RestAssured.baseURI = BASE_URI;

        // Log request/response only when a test assertion fails - keeps console output readable
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        responseSpecOk = new ResponseSpecBuilder()
                .expectContentType("application/json")
                .build();
    }
}
