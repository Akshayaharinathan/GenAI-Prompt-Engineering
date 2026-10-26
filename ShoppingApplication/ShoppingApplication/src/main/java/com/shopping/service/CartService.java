package main.java.com.shopping.service;

import com.shopping.model.CartItem;
import com.shopping.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartService {
    private final List<CartItem> cartItems = new ArrayList<>();

    public boolean addToCart(Product product, int quantity) {
        if (!product.isAvailable() || product.getQuantity() < quantity) {
            return false;
        }

        Optional<CartItem> existing = cartItems.stream()
                .filter(item -> item.getProduct().getId() == product.getId())
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getQuantity() < newQty) {
                return false;
            }
            item.setQuantity(newQty);
        } else {
            cartItems.add(new CartItem(product, quantity));
        }
        return true;
    }

    public void viewCart() {
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("+----------------------+----------+-----------+------------+");
        System.out.println("| Product              | Quantity | Unit Price| Total      |");
        System.out.println("+----------------------+----------+-----------+------------+");
        double grandTotal = 0;
        for (CartItem item : cartItems) {
            System.out.println(item);
            grandTotal += item.getTotalPrice();
        }
        System.out.println("+----------------------+----------+-----------+------------+");
        System.out.printf("| Grand Total: $%-47.2f |%n", grandTotal);
        System.out.println("+------------------------------------------------------------------+");
    }

    public double checkout() {
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to checkout.");
            return 0;
        }

        double grandTotal = 0;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            grandTotal += item.getTotalPrice();
        }
        cartItems.clear();
        return grandTotal;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }
}
