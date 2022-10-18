package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.order.OrderProduct;
import creative.market.domain.order.OrderStatus;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.OrderRepository;
import creative.market.repository.ProductRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.dto.OrderProductParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        assertThat(findOrder.getUser()).isEqualTo(productBuyer);
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
        Assertions.assertThatThrownBy(() -> orderService.order(productBuyer.getId() + 1, orderParamList, orderAddress))
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

        //when

        //then
        Assertions.assertThatThrownBy(() -> orderService.order(productBuyer.getId(), orderParamList, orderAddress))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("주문한 상품이 존재하지 않습니다.");


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