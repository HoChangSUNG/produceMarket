package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.Cart;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.order.OrderProduct;
import creative.market.domain.order.OrderStatus;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.CartRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.order.OrderRepository;
import creative.market.repository.ProductRepository;
import creative.market.service.dto.OrderProductParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static creative.market.util.AvailableDay.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private KindGradeRepository kindGradeRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderProductRepository orderProductRepository;

    @Autowired
    ProductService productService;

    @Autowired
    CartService cartService;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("상품 주문 성공, userType=BUYER가 주문")
    void orderSuccess1() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        //when
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //then
        //10000*3 + 3000*5 + 40000*1 = 85000
        assertThat(findOrder.getTotalPrice()).isEqualTo(85000);
        assertThat(findOrder.getUser().getId()).isEqualTo(productBuyer.getId());
        assertThat(findOrder.getAddress().equals(orderAddress)).isTrue();

        assertThat(findOrder.getOrderProducts().size()).isEqualTo(3);
        assertThat(findOrder.getOrderProducts()).extracting(OrderProduct::getTotalPrice).contains(30000, 15000, 40000);
        assertThat(findOrder.getOrderProducts()).extracting("status").contains(OrderStatus.ORDER);
    }

    @Test
    @DisplayName("상품 주문 성공, userType=SELLER 주문")
    void orderSuccess2() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Seller productBuyer = createSeller("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress, "상호명3");
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 12000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 8000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(1, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(3, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        //when
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //then
        //12000*2 + 8000*1 + 40000*3 = 152000
        assertThat(findOrder.getTotalPrice()).isEqualTo(152000);
        assertThat(findOrder.getUser()).isEqualTo(productBuyer);
        assertThat(findOrder.getAddress().equals(orderAddress)).isTrue();

        assertThat(findOrder.getOrderProducts().size()).isEqualTo(3);
        assertThat(findOrder.getOrderProducts()).extracting(OrderProduct::getTotalPrice).contains(24000, 8000, 120000);
        assertThat(findOrder.getOrderProducts()).extracting("status").contains(OrderStatus.ORDER);
    }

    @Test
    @DisplayName("상품 주문 성공, userType=SELLER 주문, 장바구니에 있는 상품 주문시 장바구니에서 해당 상품 제거")
    void orderSuccess3() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Seller productBuyer = createSeller("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress, "상호명3");
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 12000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 8000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        //상품 주문 준비
        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(1, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(3, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);

        //상품 장바구니에 등록
        cartService.register(orderProductParam2.getProductId(), 7, productBuyer.getId());
        cartService.register(orderProductParam3.getProductId(), 6, productBuyer.getId());

        //when
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //then

        //12000*2 + 8000*1  = 32000
        assertThat(findOrder.getTotalPrice()).isEqualTo(32000);
        assertThat(findOrder.getUser()).isEqualTo(productBuyer);
        assertThat(findOrder.getAddress().equals(orderAddress)).isTrue();

        assertThat(findOrder.getOrderProducts().size()).isEqualTo(2);
        assertThat(findOrder.getOrderProducts()).extracting(OrderProduct::getTotalPrice).contains(24000, 8000);
        assertThat(findOrder.getOrderProducts()).extracting("status").contains(OrderStatus.ORDER);

        // 장바구니 검증
        // product2, product3이 장바구니에 존재 -> product1,product2를 주문 -> product2는 장바구니에서 삭제되고 product3만 존재해야 함
        List<Cart> cartList = cartRepository.findByUserId(productBuyer.getId());
        assertThat(cartList.size()).isEqualTo(1);
        assertThat(cartList).extracting(cart -> cart.getProduct().getId()).contains(orderProductParam3.getProductId());
        assertThat(cartList).extracting(cart -> cart.getProduct().getId()).doesNotContain(orderProductParam2.getProductId());
    }

    @Test
    @DisplayName("상품 주문 실패, 존재하지 않는 user가 주문한 경우")
    void orderFail1() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Seller productBuyer = createSeller("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress, "상호명3");
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 12000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 8000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(1, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(3, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");

        //when

        //then
        assertThatThrownBy(() -> orderService.order(productBuyer.getId() + 1, orderParamList, orderAddress))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 회원입니다.");


    }

    @Test
    @DisplayName("상품 주문 실패, 존재하지 않는 상품을 주문 신청한 경우")
    void orderFail2() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Seller productBuyer = createSeller("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress, "상호명3");
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 12000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 8000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);
        em.remove(product1); // product1 제거

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(1, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(3, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        em.flush();
        em.clear();
        //when

        //then
        assertThatThrownBy(() -> orderService.order(productBuyer.getId(), orderParamList, orderAddress))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("주문할 상품이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("상품 주문 실패, 자신이 등록한 상품을 구매한 경우")
    void orderFail3() throws Exception {
        //given
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        em.persist(productOwner1);
        em.persist(productOwner2);

        Product product1 = getProduct("상품", 12000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 8000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(1, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(3, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");

        //when

        //then
        assertThatThrownBy(() -> orderService.order(productOwner1.getId(), orderParamList, orderAddress))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 등록한 상품은 구매할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 취소 성공")
    void orderCancelSuccess() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when
        OrderProduct findOrderProduct = findOrder.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getCount() == 5)
                .findFirst().orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문 내역입니다"));

        orderService.orderCancel(findOrderProduct.getId(), productBuyer.getId()); // 주문 취소(count =5 인 주문 취소)

        //then
        OrderProduct cancelOrderProduct = orderProductRepository.findById(findOrderProduct.getId()).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));
        assertThat(cancelOrderProduct.getCount()).isEqualTo(5);
        assertThat(cancelOrderProduct.getPrice()).isEqualTo(3000);
        assertThat(cancelOrderProduct.getTotalPrice()).isEqualTo(15000);
        assertThat(cancelOrderProduct.getStatus()).isEqualTo(OrderStatus.CANCEL);

    }

    @Test
    @DisplayName("주문 취소 실패, 주문 내역 존재하지 않는 경우")
    void orderCancelFail1() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when

        //then
        assertThatThrownBy(() -> orderService.orderCancel(-1L, productBuyer.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("주문 내역이 존재하지 않습니다.");

    }

    @Test
    @DisplayName("주문 취소 실패, 자신의 상품 주문을 취소하는 경우")
    void orderCancelFail2() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when
        OrderProduct findOrderProduct = findOrder.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getCount() == 5)
                .findFirst().orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문 내역입니다"));

        //then
        // 성호창3이 주문한 상품을 성호창q 가 취소하려고 하는 경우
        assertThatThrownBy(() -> orderService.orderCancel(findOrderProduct.getId(), productOwner1.getId()))
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("주문 취소 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문 취소 실패, 삭제된 상품을 취소하려는 경우")
    void orderCancelFail3() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when

        // 상품 삭제
        productService.deleteProduct(product2.getId(), productOwner2.getId());

        // 주무 내역 조회
        OrderProduct findOrderProduct = findOrder.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getCount() == 5)
                .findFirst().orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문 내역입니다"));

        //then

        // 삭제된 상품을 취소하려는 경우
        assertThatThrownBy(() -> orderService.orderCancel(findOrderProduct.getId(), productBuyer.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 상품입니다.");
    }

    @Test
    @DisplayName("주문 취소 실패, 이미 취소된 주문을 취소하려는 경우")
    void orderCancelFail4() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when
        OrderProduct findOrderProduct = findOrder.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getCount() == 5)
                .findFirst().orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문 내역입니다"));

        orderService.orderCancel(findOrderProduct.getId(), productBuyer.getId()); // 주문 취소(count =5 인 주문 취소)

        //then

        // 취소했던 주문 다시 취소
        assertThatThrownBy(() -> orderService.orderCancel(findOrderProduct.getId(), productBuyer.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 주문이 취소되었습니다.");

    }

    @Test
    @DisplayName("주문 취소 실패, 구매일로부터 특정 시간 이내에 취소하지 않는경우")
    void orderCancelFail5() throws Exception {
        //given
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("성호창q", "123433", "3123334", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("성호창w", "133234", "2123334", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productBuyer);

        Product product1 = getProduct("상품", 10000, "상품입니다", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 433L, productOwner2);
        Product product3 = getProduct("상품3", 40000, "상품입니다3", 434L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(3, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(5, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        orderParamList.add(orderProductParam1);
        orderParamList.add(orderProductParam2);
        orderParamList.add(orderProductParam3);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Long orderId = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다"));

        //when
        OrderProduct findOrderProduct = findOrder.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getCount() == 5)
                .findFirst().orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문 내역입니다"));
        // 주문 날짜를 오늘 기준 3일 전으로
        findOrderProduct.getOrder().changeCreatedDate(LocalDateTime.now().minusDays(3));

        //then
        //주문 취소(count =5 인 주문 취소)
        assertThatThrownBy(() -> orderService.orderCancel(findOrderProduct.getId(), productBuyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(("주문일로부터 " + ORDER_CANCEL_AVAILABLE_DAY + "일 이내에 주문 취소가 가능합니다."));
    }

    private Product getProduct(String name, int price, String info, Long kindGradeId, Seller seller) {
        KindGrade kindGrade = kindGradeRepository.findById(kindGradeId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다"));
        Product product = createProduct(name, price, info, kindGrade, seller);
        productRepository.save(product);
        return product;
    }

    private Product createProduct(String name, int price, String info, KindGrade kindGrade, Seller seller) {
        return Product.builder()
                .name(name)
                .price(price)
                .info(info)
                .kindGrade(kindGrade)
                .user(seller).build();
    }

    private Seller createSeller(String name, String loginId, String pw, String birth, String email, String phoneNumber, Address address, String businessName) {
        return Seller.builder().name(name)
                .loginId(loginId)
                .password(pw)
                .birth(birth)
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address)
                .businessName(businessName).build();
    }

    private Buyer createBuyer(String name, String loginId, String pw, String birth, String email, String phoneNumber, Address address) {
        return Buyer.builder().name(name)
                .loginId(loginId)
                .password(pw)
                .birth(birth)
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address).build();
    }

    private Address createAddress(String jibun, String road, int zipcode, String detailAddress) {
        return Address.builder()
                .jibun(jibun)
                .road(road)
                .zipcode(zipcode)
                .detailAddress(detailAddress).build();
    }
}