package ecommerce.domain.cart.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.common.exception.UnprocessableEntityException;
import ecommerce.domain.cart.dto.AddToCartRequest;
import ecommerce.domain.cart.dto.CartItemResponse;
import ecommerce.domain.cart.dto.UpdateCartItemRequest;
import ecommerce.domain.cart.entity.CartItem;
import ecommerce.domain.cart.repository.CartItemRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponse addToCart(String email, AddToCartRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다"));

        // 상품 상태 확인
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new UnprocessableEntityException(ErrorCode.UNPROCESSABLE_ENTITY);
        }

        // 재고 확인
        if (product.getStock() < request.getQuantity()) {
            throw new UnprocessableEntityException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 이미 장바구니에 있는 상품인지 확인
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            // 이미 있으면 수량 증가
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            // 재고 확인
            if (product.getStock() < newQuantity) {
                throw new UnprocessableEntityException(ErrorCode.INSUFFICIENT_STOCK);
            }

            cartItem.setQuantity(newQuantity);
        } else {
            // 없으면 새로 추가
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
        }

        log.info("장바구니 추가: userId={}, productId={}, quantity={}",
                user.getId(), product.getId(), request.getQuantity());

        return CartItemResponse.from(cartItem);
    }

    public List<CartItemResponse> getMyCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(user.getId());

        return cartItems.stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponse updateCartItem(String email, Long cartItemId, UpdateCartItemRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("장바구니 항목을 찾을 수 없습니다"));

        // 본인의 장바구니 항목인지 확인
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new UnprocessableEntityException(ErrorCode.FORBIDDEN);
        }

        // 재고 확인
        if (cartItem.getProduct().getStock() < request.getQuantity()) {
            throw new UnprocessableEntityException(ErrorCode.INSUFFICIENT_STOCK);
        }

        cartItem.setQuantity(request.getQuantity());

        log.info("장바구니 수정: cartItemId={}, quantity={}", cartItemId, request.getQuantity());

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void removeCartItem(String email, Long cartItemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("장바구니 항목을 찾을 수 없습니다"));

        // 본인의 장바구니 항목인지 확인
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new UnprocessableEntityException(ErrorCode.FORBIDDEN);
        }

        cartItemRepository.delete(cartItem);

        log.info("장바구니 삭제: cartItemId={}", cartItemId);
    }

    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        cartItemRepository.deleteByUserId(user.getId());

        log.info("장바구니 전체 삭제: userId={}", user.getId());
    }

    public long getCartCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        return cartItemRepository.countByUserId(user.getId());
    }
}