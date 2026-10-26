package main.java.com.shopping;

import com.shopping.service.CartService;
import com.shopping.service.ProductService;
import com.shopping.model.Product;

import java.util.Scanner;

public class ShoppingApp {
    public static void main(String[] args) {
        ProductService productService = new ProductService();
        CartService cartService = new CartService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nShopping Application");
            System.out.println("1. List Products");
            System.out.println("2. Search Products");
            System.out.println("3. Add to Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1" -> productService.listAllProducts();
                case "2" -> {
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    var results = productService.searchProduct(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No matching products found.");
                    } else {
                        results.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    System.out.print("Enter product ID: ");
                    int productId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    Product product = productService.findById(productId).orElse(null);
                    if (product == null) {
                        System.out.println("Product not found.");
                    } else if (cartService.addToCart(product, quantity)) {
                        System.out.println("Added to cart.");
                    } else {
                        System.out.println("Unable to add to cart. Check stock and quantity.");
                    }
                }
                case "4" -> cartService.viewCart();
                case "5" -> {
                    double total = cartService.checkout();
                    System.out.printf("Checkout complete. Total: $%.2f\n", total);
                }
                case "6" -> {
                    System.out.println("Exiting application.");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
