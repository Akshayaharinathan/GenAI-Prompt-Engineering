package com.dummyjson.tests;

import com.dummyjson.base.BaseTest;
import com.dummyjson.models.Product;
import com.dummyjson.utils.PriceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dummyjson.utils.ProductEndpoints.PRODUCT_BY_ID;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the total price of products with id 4 through 10 (inclusive).
 * <p>
 * Requirement: "write the method to find the total price of the products
 * with id 4 to 10. Print the total price and also validate."
 */
@Tag("products")
@Tag("priceCalculation")
@DisplayName("Total Price Calculation Tests (Product IDs 4-10)")
class TotalPriceCalculationTests extends BaseTest {

    private static final int FROM_ID = 4;
    private static final int TO_ID = 10;

    @Test
    @DisplayName("POSITIVE: Total price for product ids 4-10 is computed, printed, and validated against the sum of individual prices")
    void calculateTotalPrice_forIds4to10_matchesSumOfIndividualPrices() {

        // Step 1: independently fetch each product's price directly via RestAssured
        // so we have a ground truth to validate the calculator's output against.
        double expectedTotal = 0.0;
        List<Double> individualPrices = new java.util.ArrayList<>();

        for (int id = FROM_ID; id <= TO_ID; id++) {
        Number priceRaw = given()
        .spec(requestSpec)
        .pathParam("id", id)
        .when()
        .get(PRODUCT_BY_ID)
        .then()
        .statusCode(200)
        .extract()
        .path("price");
Double price = priceRaw.doubleValue();


            assertTrue(price != null && price >= 0, "Price for product id " + id + " should be a non-negative number");
            individualPrices.add(price);
            expectedTotal += price;
        }

        // Step 2: use the PriceCalculator utility under test to compute the same total
        double actualTotal = PriceCalculator.calculateTotalPrice(FROM_ID, TO_ID);

        // Step 3: print the result, as required
        System.out.printf(
                "%n[Total Price] Sum of prices for product ids %d to %d = %.2f%n",
                FROM_ID, TO_ID, actualTotal
        );
        System.out.println("[Total Price] Individual prices: " + individualPrices);

        // Step 4: validate
        assertEquals(expectedTotal, actualTotal, 0.001,
                "Calculated total price should equal the sum of the individually-fetched prices");
        assertTrue(actualTotal > 0, "Total price across 7 real products should be greater than zero");
    }

    @Test
    @DisplayName("POSITIVE: Total price breakdown returns exactly 7 products (ids 4 through 10 inclusive)")
    void calculateTotalPriceWithBreakdown_returnsExactlySevenProducts() {
        PriceCalculator.PriceBreakdown breakdown = PriceCalculator.calculateTotalPriceWithBreakdown(FROM_ID, TO_ID);

        System.out.printf("%n[Total Price Breakdown] Total = %.2f across %d products%n",
                breakdown.getTotal(), breakdown.getProducts().size());
        breakdown.getProducts().forEach(p ->
                System.out.printf("  -> id=%d, title='%s', price=%.2f%n", p.getId(), p.getTitle(), p.getPrice()));

        assertEquals(7, breakdown.getProducts().size(), "Expected exactly 7 products for the inclusive range 4-10");

        double sumFromBreakdownList = breakdown.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum();

        assertEquals(sumFromBreakdownList, breakdown.getTotal(), 0.001,
                "The reported total should equal the sum of prices in the returned product list");
    }

    @Test
    @DisplayName("POSITIVE: each product id from 4 to 10 is present exactly once in the breakdown, in order")
    void calculateTotalPriceWithBreakdown_containsExpectedIdsInOrder() {
        PriceCalculator.PriceBreakdown breakdown = PriceCalculator.calculateTotalPriceWithBreakdown(FROM_ID, TO_ID);

        List<Integer> expectedIds = List.of(4, 5, 6, 7, 8, 9, 10);
        List<Integer> actualIds = breakdown.getProducts().stream().map(Product::getId).toList();

        assertEquals(expectedIds, actualIds, "Product ids in the breakdown should be 4..10 inclusive, in ascending order");
    }

    @Test
    @DisplayName("NEGATIVE: calculateTotalPrice throws IllegalArgumentException when fromId > toId")
    void calculateTotalPrice_invalidRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PriceCalculator.calculateTotalPrice(10, 4));
    }

    @Test
    @DisplayName("NEGATIVE: calculateTotalPrice for a single-id range (4 to 4) equals that product's own price")
    void calculateTotalPrice_singleIdRange_equalsThatProductsPrice() {
        Number priceOfProduct4 = given()
                .spec(requestSpec)
                .pathParam("id", 4)
                .when()
                .get(PRODUCT_BY_ID)
                .then()
                .statusCode(200)
                .extract()
                .path("price");
    Double price4 = priceOfProduct4.doubleValue();
        double total = PriceCalculator.calculateTotalPrice(4, 4);

        assertEquals(price4, total, 0.001);
        assertFalse(total <= 0, "A real product's price should not be zero or negative");
    }
}
