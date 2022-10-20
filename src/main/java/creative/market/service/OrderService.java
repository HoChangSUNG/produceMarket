package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.order.Order;
import creative.market.domain.order.OrderProduct;
import creative.market.domain.order.OrderStatus;
import creative.market.domain.product.Product;
import creative.market.domain.user.User;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.OrderProductRepository;
import creative.market.repository.OrderRepository;
import creative.market.repository.ProductRepository;
import creative.market.repository.user.UserRepository;
import creative.market.service.dto.OrderProductParamDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    @Transactional
    public Long order(Long userId, List<OrderProductParamDTO> orderProductParams,Address address) { // 상품 주문

        // 구매하는 user 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        // orderProducts 생성
        List<OrderProduct> orderProducts = createOrderProducts(orderProductParams);

        // order 생성
        Order order = createOrder(orderProducts,user,address);

        // order 저장
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void orderCancel(Long orderProductId,Long userId) { // 주문 취소

        OrderProduct orderProduct = orderProductRepository.findByIdWithOrder(orderProductId)
                .orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
        buyerAccessCheck(orderProduct,userId); // 상품 취소 권한 확인
        orderProduct.cancel();
    }

    private void buyerAccessCheck(OrderProduct orderProduct, Long userId) {
        if (!orderProduct.getOrder().getUser().getId().equals(userId)) {
            throw new LoginAuthenticationException("주문 취소 권한이 없습니다.");
        }
    }

    private Order createOrder(List<OrderProduct> orderProducts,User user,Address address) {
        return Order.builder()
                .orderProducts(orderProducts)
                .user(user)
                .address(address).build();
    }

    private List<OrderProduct> createOrderProducts(List<OrderProductParamDTO> orderProductParams) {
        return orderProductParams.stream()
                .map(orderProductParam -> createOrderProduct(orderProductParam))
                .collect(Collectors.toList());
    }

    private OrderProduct createOrderProduct(OrderProductParamDTO param) {
        Product product = productRepository.findById(param.getProductId())
                .orElseThrow(() -> new NoSuchElementException("주문한 상품이 존재하지 않습니다."));
        return OrderProduct.builder()
                .product(product)
                .price(product.getPrice())
                .count(param.getCount())
                .status(OrderStatus.ORDER).build();
    }
}
