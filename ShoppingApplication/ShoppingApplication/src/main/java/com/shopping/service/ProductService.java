package main.java.com.shopping.service;

import com.shopping.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final List<Product> products = new ArrayList<>();
    private int nextId = 1;

    public ProductService() {
        // Seed with sample products
        addProduct("Laptop", 999.99, 10);
        addProduct("Headphones", 49.99, 25);
        addProduct("Keyboard", 79.99, 15);
        addProduct("Mouse", 29.99, 30);
        addProduct("Monitor", 299.99, 8);
    }

    public void listAllProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        System.out.println("+-------+----------------------+-----------+----------+------------+");
        System.out.println("| ID    | Name                 | Price     | Stock    | Available  |");
        System.out.println("+-------+----------------------+-----------+----------+------------+");
        for (Product product : products) {
            System.out.println(product);
        }
        System.out.println("+-------+----------------------+-----------+----------+------------+");
    }

    public List<Product> searchProduct(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<Product> results = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerKeyword)) {
                results.add(product);
            }
        }
        return results;
    }

    public Product addProduct(String name, double price, int quantity) {
        Product product = new Product(nextId++, name, price, quantity);
        products.add(product);
        return product;
    }

    public boolean deleteProduct(int productId) {
        return products.removeIf(p -> p.getId() == productId);
    }

    public Optional<Product> findById(int productId) {
        return products.stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
    }

    public List<Product> getAllProducts() {
        return products;
    }
}
