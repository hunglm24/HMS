package dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    private BigDecimal totalAmount;

    public Cart() {
        items = new ArrayList<>();
        totalAmount = BigDecimal.ZERO;
    }

    public void addItem(CartItem newItem) {
        for (CartItem item : items) {
            // If the same room type and same dates, just increase quantity
            if (item.getRoomType().getId() == newItem.getRoomType().getId() &&
                item.getCheckInDate().equals(newItem.getCheckInDate()) &&
                item.getCheckOutDate().equals(newItem.getCheckOutDate())) {
                
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                calculateTotal();
                return;
            }
        }
        // Otherwise, add as new item
        items.add(newItem);
        calculateTotal();
    }

    public void updateQuantity(long roomTypeId, int newQuantity) {
        for (CartItem item : items) {
            if (item.getRoomType().getId() == roomTypeId) {
                if (newQuantity <= 0) {
                    items.remove(item);
                } else {
                    item.setQuantity(newQuantity);
                }
                break;
            }
        }
        calculateTotal();
    }

    public void removeItem(long roomTypeId) {
        items.removeIf(item -> item.getRoomType().getId() == roomTypeId);
        calculateTotal();
    }

    public void clear() {
        items.clear();
        totalAmount = BigDecimal.ZERO;
    }

    private void calculateTotal() {
        totalAmount = BigDecimal.ZERO;
        for (CartItem item : items) {
            totalAmount = totalAmount.add(item.getSubtotal());
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
