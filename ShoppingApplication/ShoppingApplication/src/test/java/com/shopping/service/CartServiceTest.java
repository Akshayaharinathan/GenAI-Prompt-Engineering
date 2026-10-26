package com.shopping.service;

import com.shopping.model.Product;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.shopping.service.CartService;
import com.shopping.service.ProductService;
 
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CartServiceTest {
 
    private CartService cartService;
    private ProductService productService;
    private Product product;
 
    @BeforeAll
    void initAll() {
        System.out.println("Starting Cart Tests");
    }
 
    @BeforeEach
    void init() {
        cartService = new CartService();
        productService = new ProductService();
        product = productService.findById(1).get();
    }
 
    @AfterEach
    void afterEach() {
        System.out.println("Cart Test Completed");
    }
 
    @AfterAll
    void afterAll() {
        System.out.println("All Cart Tests Completed");
    }
 
    // ✅ Positive
    @Test
    void testAddToCart_Success() {
        boolean result = cartService.addToCart(product, 2);
        assertTrue(result);
        assertFalse(cartService.isEmpty());
    }
 
    // ✅ Negative
    @Test
    void testAddToCart_InsufficientStock() {
        boolean result = cartService.addToCart(product, 1000);
        assertFalse(result);
    }
 
    // ✅ Edge Case
    @Test
    void testAddToCart_ZeroQuantity() {
        boolean result = cartService.addToCart(product, 0);
        assertTrue(result); // logically allowed
    }
 
    // ✅ Positive Checkout
    @Test
    void testCheckout_Success() {
        cartService.addToCart(product, 2);
        double total = cartService.checkout();
 
        assertTrue(total > 0);
        assertTrue(cartService.isEmpty());
    }
 
    // ✅ Negative Checkout
    @Test
    void testCheckout_EmptyCart() {
        double total = cartService.checkout();
        assertEquals(0, total);
    }
 
    // ✅ Edge Case
    @Test
    void testCheckout_StockReduction() {
        int initialStock = product.getQuantity();
        cartService.addToCart(product, 2);
        cartService.checkout();
 
        assertEquals(initialStock - 2, product.getQuantity());
    }
}