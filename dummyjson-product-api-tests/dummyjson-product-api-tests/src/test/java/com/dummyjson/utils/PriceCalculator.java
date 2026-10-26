package com.dummyjson.utils;

import com.dummyjson.models.Product;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;

import static com.dummyjson.utils.ProductEndpoints.PRODUCT_BY_ID;
import static io.restassured.RestAssured.given;

/**
 * Utility responsible for fetching individual products by id and computing
 * the total price across an inclusive id range.
 * <p>
 * Used to satisfy the requirement: "find the total price of the products
 * with id 4 to 10".
 */
public final class PriceCalculator {

    private PriceCalculator() {
        // utility class
    }

    /**
     * Calls GET /products/{id} for every id in [fromId, toId] (inclusive),
     * fetches the price field from each, and returns the running total.
     *
     * @param fromId starting product id (inclusive)
     * @param toId   ending product id (inclusive)
     * @return total price summed across all fetched products
     */
    public static double calculateTotalPrice(int fromId, int toId) {
        if (fromId > toId) {
            throw new IllegalArgumentException("fromId (" + fromId + ") must be <= toId (" + toId + ")");
        }

        double total = 0.0;
        for (int id = fromId; id <= toId; id++) {
            Product product = fetchProductById(id);
            total += product.getPrice();
        }
        return total;
    }

    /**
     * Same as {@link #calculateTotalPrice(int, int)} but also returns the
     * individual products that were fetched, so callers can validate the sum
     * against the individual prices without re-hitting the API.
     */
    public static PriceBreakdown calculateTotalPriceWithBreakdown(int fromId, int toId) {
        if (fromId > toId) {
            throw new IllegalArgumentException("fromId (" + fromId + ") must be <= toId (" + toId + ")");
        }

        List<Product> products = new ArrayList<>();
        double total = 0.0;
        for (int id = fromId; id <= toId; id++) {
            Product product = fetchProductById(id);
            products.add(product);
            total += product.getPrice();
        }
        return new PriceBreakdown(products, total);
    }

    private static Product fetchProductById(int id) {
        Response response = given()
                .pathParam("id", id)
                .when()
                .get(PRODUCT_BY_ID);

        response.then().statusCode(200);
        return response.as(Product.class);
    }

    /**
     * Simple value holder pairing the list of fetched products with their
     * aggregate total price.
     */
    public static final class PriceBreakdown {
        private final List<Product> products;
        private final double total;

        public PriceBreakdown(List<Product> products, double total) {
            this.products = products;
            this.total = total;
        }

        public List<Product> getProducts() {
            return products;
        }

        public double getTotal() {
            return total;
        }
    }
}
