package com.yas.cart.service;

import com.yas.cart.exception.BadRequestException;
import com.yas.cart.exception.NotFoundException;
import com.yas.cart.model.Cart;
import com.yas.cart.model.CartItem;
import com.yas.cart.repository.CartItemRepository;
import com.yas.cart.repository.CartRepository;
import com.yas.cart.viewmodel.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    private static final String CART_ITEM_UPDATED_MSG = "PRODUCT %s";

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public List<CartListVm> getCarts() {
        return cartRepository.findAll()
                .stream().map(CartListVm::fromModel)
                .toList();
    }

    public List<CartGetDetailVm> getCartDetailByCustomerId(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .stream().map(CartGetDetailVm::fromModel)
                .toList();
    }

    public CartGetDetailVm addToCart(List<CartItemVm> cartItemVms) {
        // Call API to check all products will be added to cart are existed
        List<Long> productIds = cartItemVms.stream().map(CartItemVm::productId).toList();
        List<ProductThumbnailVm> productThumbnailVmList = productService.getProducts(productIds);
        if (productThumbnailVmList.size() != productIds.size()) {
            throw new NotFoundException("Some products are not existed");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = auth.getName();

        Cart cart = cartRepository.findByCustomerId(customerId).stream().findFirst().orElse(null);
        if (cart == null) {
            cart = new Cart(null, customerId, new HashSet<>());
            cart.setCreatedOn(ZonedDateTime.now());
        }

        List<CartItem> existedCartItems = cartItemRepository.findAllByCart(cart);

        for (CartItemVm cartItemVm : cartItemVms) {
            CartItem cartItem = getCartItemByProductId(existedCartItems, cartItemVm.productId());
            if (cartItem.getId() != null) {
                cartItem.setQuantity(cartItem.getQuantity() + cartItemVm.quantity());
            } else {
                cartItem.setCart(cart);
                cartItem.setProductId(cartItemVm.productId());
                cartItem.setQuantity(cartItemVm.quantity());
                cart.getCartItems().add(cartItem);
            }
        }
        cart = cartRepository.saveAndFlush(cart);

        return CartGetDetailVm.fromModel(cart);
    }

    public CartGetDetailVm getLastCart() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return cartRepository.findByCustomerId(auth.getName())
                .stream().reduce((first, second) -> second)
                .map(CartGetDetailVm::fromModel).orElse(CartGetDetailVm.fromModel(new Cart()));
    }

    private CartItem getCartItemByProductId(List<CartItem> cartItems, Long productId) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(productId))
                return cartItem;
        }
        return new CartItem();
    }

    public CartItemPutVm updateCartItems(CartItemVm cartItemVm) {
        CartGetDetailVm currentCart = getLastCart();

        if (currentCart.cartDetails().isEmpty()) {
            throw new BadRequestException("There is no cart item in current cart to update !");
        }

        List<CartDetailVm> cartDetailListVm = currentCart.cartDetails();
        boolean itemExist = cartDetailListVm.stream().anyMatch(item -> item.productId().equals(cartItemVm.productId()));
        if (!itemExist) {
            throw new NotFoundException(String.format("There is no product with ID: %s in the current cart", cartItemVm.productId()));
        }

        Long cartId = currentCart.id();
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, cartItemVm.productId())
                .orElseThrow(() -> new NotFoundException("Non exist cart item with ID: " + cartId));

        int newQuantity = cartItemVm.quantity();
        cartItem.setQuantity(newQuantity);
        if (newQuantity == 0) {
            cartItemRepository.delete(cartItem);
            return CartItemPutVm.fromModel(cartItem, String.format(CART_ITEM_UPDATED_MSG, "DELETED"));
        } else {
            CartItem savedCartItem = cartItemRepository.saveAndFlush(cartItem);
            cartItem.setQuantity(newQuantity);
            return CartItemPutVm.fromModel(savedCartItem, String.format(CART_ITEM_UPDATED_MSG, "UPDATED"));
        }
    }
}
