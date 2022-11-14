package creative.market.repository.query;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.SellerTotalPricePerPeriodDTO;
import creative.market.repository.order.OrderRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.OrderService;
import creative.market.service.dto.OrderProductParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class OrderProductQueryRepositoryTempTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    KindGradeRepository kindGradeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    OrderProductQueryRepository orderProductQueryRepository;

    @Autowired
    OrderProductQueryRepositoryTemp orderProductQueryRepositoryTemp;

    @BeforeEach
    public void before() throws InterruptedException {
        Address buyerAddress = createAddress("1111", "봉사산로3", 11111, "3동4호");
        Seller productOwner1 = createSeller("강대현", "1", "11", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        Seller productOwner2 = createSeller("강병관", "2", "22", "19991212", "sd45fwf@mae.com", "010-3644-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Seller productOwner3 = createSeller("김현민", "5", "52", "19991212", "sd245fwf@mae.com", "010-4444-3333", createAddress("1111", "봉사산로2", 12315, "2동2호"), "상호명2");
        Buyer productBuyer = createBuyer("성호창3", "3", "33", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productOwner1);
        em.persist(productOwner2);
        em.persist(productOwner3);
        em.persist(productBuyer);

        Product product1 = getProduct("상품1", 10000, "상품입니다1", 432L, productOwner1);
        Product product2 = getProduct("상품2", 3000, "상품입니다2", 432L, productOwner2);
        Product product3 = getProduct("상품3", 5000, "상품입니다3", 432L, productOwner2);
        Product product4 = getProduct("상품4", 50000, "상품입니다4", 432L, productOwner3);
        Product product5 = getProduct("상품5", 40000, "상품입니다5", 433L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(5, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(2, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        OrderProductParamDTO orderProductParam4 = new OrderProductParamDTO(2, product4.getId());
        OrderProductParamDTO orderProductParam5 = new OrderProductParamDTO(1, product5.getId());

        List<OrderProductParamDTO> orderParamList = addOrderProductParamDTO(orderProductParam1, orderProductParam2, orderProductParam3, orderProductParam4, orderProductParam5);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");

        // 1달전 저장한 주문 내역
        Long orderId1 = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder1 = orderRepository.findById(orderId1).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
        findOrder1.changeCreatedDate(LocalDateTime.now().minusMonths(1));

        // 1시간전 저장한 주문 내역
        Long orderId2 = orderService.order(productBuyer.getId(), orderParamList, orderAddress);
        Order findOrder2 = orderRepository.findById(orderId2).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
        findOrder2.changeCreatedDate(LocalDateTime.now().minusHours(1));

    }

    private List<OrderProductParamDTO> addOrderProductParamDTO(OrderProductParamDTO... orderProductParamDTOS) {
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        Arrays.stream(orderProductParamDTOS).forEach(orderProductParamDTO -> orderParamList.add(orderProductParamDTO));
        return orderParamList;
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

    @Test
    @DisplayName("기간별 카테고리 전체 판매액, 카테고리 = kindGradeId 기준")
    void sellerTotalPriceByPeriodAndCategory1() throws Exception {
        Address buyerAddress = createAddress("1122", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        sellerTotalPriceGraphInit(productOwner1, productOwner2, productBuyer);

        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류-배추-봄-상품 (2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result1 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 474L));

        //식량작물-쌀-일반계-상품(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result2 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 432L));

        // 채소류-상추-적-상품 (2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result3 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 492L));

        //then
        // 채소류-배추-봄-상품 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 2000L, 1000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀-일반계-상품(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 161000L, 161000L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 채소류-상추-적-상품 (2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매액, 카테고리 = kindId 기준")
    void sellerTotalPriceByPeriodAndCategory2() throws Exception {
        Address buyerAddress = createAddress("1122", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        sellerTotalPriceGraphInit(productOwner1, productOwner2, productBuyer);

        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류-배추-봄 (2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result1 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1662L, null));

        //식량작물-쌀-일반계(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result2 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1613L, null));

        // 채소류-상추-적(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result3 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1680L, null));

        //then
        // 채소류-배추-봄 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 6000L, 3000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀-일반계(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 201000L, 201000L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 채소류-상추-적 (2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매액, 카테고리 = itemId 기준")
    void sellerTotalPriceByPeriodAndCategory3() throws Exception {
        Address buyerAddress = createAddress("1122", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        sellerTotalPriceGraphInit(productOwner1, productOwner2, productBuyer);

        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류-배추 (2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result1 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 211, null, null));

        //식량작물-쌀(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result2 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 111, null, null));

        // 채소류-상추(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result3 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 214, null, null));

        //then
        // 채소류-배추 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 20000L, 10000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 201000L, 201000L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 채소류-상추(2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매액, 카테고리 = itemCategory 기준")
    void sellerTotalPriceByPeriodAndCategory4() throws Exception {
        Address buyerAddress = createAddress("1122", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        sellerTotalPriceGraphInit(productOwner1, productOwner2, productBuyer);

        //과일류-사과-홍옥-상품 결제 내역 추가
        Product product1 = getProduct("사과상품2222222", 120000, "사과입니다.", 702L, productOwner2); // 과일류-사과-홍옥-상품

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(1, product1.getId());
        List<OrderProductParamDTO> orderParamList1 = addOrderProductParamDTO(orderProductParam1);
        orderService.order(productBuyer.getId(), orderParamList1, buyerAddress);


        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result1 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(200, null, null, null));

        //식량작물(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result2 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(100, null, null, null));

        // 과일류(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result3 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(400, null, null, null));

        // 모든 상품(2달전 ~ 이번달)
        List<SellerTotalPricePerPeriodDTO> result4 = orderProductQueryRepositoryTemp
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, null));

        //then
        // 채소류(2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 20000L, 10000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 201000L, 201000L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 과일류(2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 120000L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 모든상품(2달전 ~ 이번달)
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("totalPrice").containsExactly(0L, 221000L, 331000L);
        assertThat(result4).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    private void sellerTotalPriceGraphInit(Seller productOwner1, Seller productOwner2, Buyer productBuyer) {
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");

        Product product1 = getProduct("상품111", 1000, "상품입니다111", 474L, productOwner1); // 채소류-배추-봄-상품
        Product product2 = getProduct("상품222", 2000, "상품입니다222", 475L, productOwner1); // 채소류-배추-봄-중품
        Product product3 = getProduct("상품111", 3000, "상품입니다333", 477L, productOwner2); // 채소류-배추-고랭지-상품
        Product product4 = getProduct("상품222", 4000, "상품입니다444", 480L, productOwner2); // 채소류 배추-가을-상품

        for (int i = 0; i < 2; i++) { // 주문(이번달 ~ 1달전까지)
            LocalDateTime now = LocalDateTime.now();

            OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(i + 1, product1.getId());
            OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(i + 1, product2.getId());
            List<OrderProductParamDTO> orderParamList1 = addOrderProductParamDTO(orderProductParam1, orderProductParam2);

            Long orderId = orderService.order(productBuyer.getId(), orderParamList1, orderAddress);
            Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
            findOrder.changeCreatedDate(now.minusMonths(i).withMinute(1));

            OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(i + 1, product3.getId());
            OrderProductParamDTO orderProductParam4 = new OrderProductParamDTO(i + 1, product4.getId());
            List<OrderProductParamDTO> orderParamList2 = addOrderProductParamDTO(orderProductParam3, orderProductParam4);

            Long orderId2 = orderService.order(productBuyer.getId(), orderParamList2, orderAddress);
            Order findOrder2 = orderRepository.findById(orderId2).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
            findOrder2.changeCreatedDate(now.minusMonths(i).withMinute(2));
        }
    }
}