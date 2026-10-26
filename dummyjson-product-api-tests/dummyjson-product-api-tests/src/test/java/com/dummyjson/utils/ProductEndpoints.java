package com.dummyjson.utils;

/**
 * Centralized endpoint paths for the DummyJSON "products" resource.
 * Keeping paths in one place avoids magic strings scattered across test classes.
 *
 * Reference: https://dummyjson.com/docs/products
 */
public final class ProductEndpoints {

    private ProductEndpoints() {
        // utility class
    }

    public static final String ALL_PRODUCTS = "/products";
    public static final String PRODUCT_BY_ID = "/products/{id}";
    public static final String ADD_PRODUCT = "/products/add";
    public static final String UPDATE_PRODUCT = "/products/{id}";
    public static final String DELETE_PRODUCT = "/products/{id}";
}
