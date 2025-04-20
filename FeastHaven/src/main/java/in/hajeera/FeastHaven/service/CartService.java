package in.hajeera.FeastHaven.service;

import in.hajeera.FeastHaven.io.CartRequest;
import in.hajeera.FeastHaven.io.CartResponse;

public interface CartService {

    CartResponse addToCart(CartRequest request);

    CartResponse getCart();

    void clearCart();

    CartResponse removeFromCart(CartRequest cartRequest);

    void deleteItem(String foodId);

}
