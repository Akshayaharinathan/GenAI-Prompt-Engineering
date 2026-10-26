package test.java.com.shopping.service;

import com.shopping.model.Product;
import com.shopping.service.ProductService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
 
import java.util.List;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
 
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductServiceTest {
 
    private ProductService productService;
 
    @BeforeAll
    void setupAll() {
        System.out.println("Starting ProductService Tests");
    }
 
    @BeforeEach
    void setup() {
        productService = new ProductService();
    }
 
    @AfterEach
    void tearDown() {
        System.out.println("Test Completed");
    }
 
    @AfterAll
    void tearDownAll() {
        System.out.println("All ProductService Tests Done");
    }
 
    // ✅ Positive Test
    @Test
    void testAddProduct_Success() {
        Product product = productService.addProduct("Tablet", 500, 10);
        assertNotNull(product);
        assertEquals("Tablet", product.getName());
    }
 
    // ✅ Negative Test
    @Test
    void testDeleteProduct_InvalidId() {
        boolean result = productService.deleteProduct(999);
        assertFalse(result);
    }
 
    // ✅ Edge Case
    @Test
    void testSearchProduct_NoMatch() {
        List<Product> result = productService.searchProduct("XYZ");
        assertTrue(result.isEmpty());
    }
 
    // ✅ Positive Search
    @ParameterizedTest
    @ValueSource(strings = {"laptop", "LAPTOP"})
    void testSearchProduct_CaseInsensitive(String keyword) {
        List<Product> result = productService.searchProduct(keyword);
        assertFalse(result.isEmpty());
    }
 
    // ✅ FindById Positive
    @Test
    void testFindById_Valid() {
        Optional<Product> product = productService.findById(1);
        assertTrue(product.isPresent());
    }
 
    // ✅ Edge Case
    @Test
    void testAddProduct_ZeroQuantity() {
        Product product = productService.addProduct("Pen", 10, 0);
        assertFalse(product.isAvailable());
    }
}
 