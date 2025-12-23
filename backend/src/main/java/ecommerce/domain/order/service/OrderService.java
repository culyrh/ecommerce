package ecommerce.domain.order.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ForbiddenException;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.common.exception.UnprocessableEntityException;
import ecommerce.domain.order.dto.OrderRequest;
import ecommerce.domain.order.dto.OrderResponse;
import ecommerce.domain.order.dto.OrderUpdateRequest;
import ecommerce.domain.order.entity.Order;
import ecommerce.domain.order.entity.OrderItem;
import ecommerce.domain.order.enums.OrderStatus;
import ecommerce.domain.order.repository.OrderRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(String email, OrderRequest request) {
        log.info("주문 생성 시도: email={}, items={}", email, request.getItems().size());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 주문 번호 생성
        String orderNumber = generateOrderNumber();

        // 주문 엔티티 생성
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .address(request.getAddress())
                .orderItems(new ArrayList<>())
                .build();

        // 주문 항목 생성 및 재고 차감
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var itemRequest : request.getItems()) {
            // 상품 조회
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.PRODUCT_NOT_FOUND,
                            "상품 ID: " + itemRequest.getProductId()
                    ));

            // 재고 확인
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new UnprocessableEntityException(
                        ErrorCode.INSUFFICIENT_STOCK,
                        String.format("상품 '%s'의 재고가 부족합니다. (요청: %d, 재고: %d)",
                                product.getName(), itemRequest.getQuantity(), product.getStock())
                );
            }

            // 재고 차감
            product.setStock(product.getStock() - itemRequest.getQuantity());
            product.setSalesCount(product.getSalesCount() + itemRequest.getQuantity());

            // 소계 계산
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .seller(product.getSeller())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        // 주문 금액 설정
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // TODO: 쿠폰 적용 로직 Phase 8에서 구현

        // 주문 항목 추가
        order.getOrderItems().addAll(orderItems);

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 사용자 누적 구매액 업데이트
        user.setTotalPurchaseAmount(
                user.getTotalPurchaseAmount() != null
                        ? user.getTotalPurchaseAmount().add(totalAmount)
                        : totalAmount
        );
        userRepository.save(user);

        log.info("주문 생성 완료: orderId={}, orderNumber={}, totalAmount={}",
                savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getTotalAmount());

        // TODO: Phase 8에서 첫 구매 체크 및 웰컴 쿠폰 발급
        // TODO: Phase 8에서 VIP 쿠폰 발급 체크 (누적 50만원 이상)

        return OrderResponse.from(savedOrder);
    }

    /**
     * 내 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String email, Pageable pageable) {
        log.info("내 주문 목록 조회: email={}, page={}", email, pageable.getPageNumber());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);

        return orders.map(OrderResponse::from);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long id) {
        log.info("주문 상세 조회: email={}, orderId={}", email, id);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 주문만 조회할 수 있습니다");
        }

        return OrderResponse.from(order);
    }

    /**
     * 주문 정보 수정 (배송 정보만)
     */
    @Transactional
    public OrderResponse updateOrder(String email, Long id, OrderUpdateRequest request) {
        log.info("주문 정보 수정: email={}, orderId={}", email, id);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 주문만 수정할 수 있습니다");
        }

        // 배송 전 상태에서만 수정 가능
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new UnprocessableEntityException(
                    ErrorCode.INVALID_ORDER_STATUS,
                    "배송 전 상태에서만 주문 정보를 수정할 수 있습니다"
            );
        }

        // 변경 사항 적용
        if (request.getRecipientName() != null) {
            order.setRecipientName(request.getRecipientName());
        }
        if (request.getRecipientPhone() != null) {
            order.setRecipientPhone(request.getRecipientPhone());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("주문 정보 수정 완료: orderId={}", updatedOrder.getId());

        return OrderResponse.from(updatedOrder);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(String email, Long id) {
        log.info("주문 취소: email={}, orderId={}", email, id);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 주문만 취소할 수 있습니다");
        }

        // 배송 전 상태에서만 취소 가능
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new UnprocessableEntityException(
                    ErrorCode.INVALID_ORDER_STATUS,
                    "배송 전 상태에서만 주문을 취소할 수 있습니다"
            );
        }

        // 주문 상태 변경
        order.setStatus(OrderStatus.CANCELLED);

        // 재고 복구
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                product.setSalesCount(product.getSalesCount() - orderItem.getQuantity());
                productRepository.save(product);
            }
        }

        // 사용자 누적 구매액 차감
        user.setTotalPurchaseAmount(
                user.getTotalPurchaseAmount().subtract(order.getTotalAmount())
        );
        userRepository.save(user);

        orderRepository.save(order);
        log.info("주문 취소 완료: orderId={}", id);
    }

    /**
     * 주문 번호 생성
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }
}