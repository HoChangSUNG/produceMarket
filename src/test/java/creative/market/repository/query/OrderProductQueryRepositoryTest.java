package creative.market.repository.query;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.BuyerTotalPricePerPeriodDTO;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.SellerAndTotalPricePerCategoryDTO;
import creative.market.repository.order.OrderRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.OrderService;
import creative.market.service.dto.OrderProductParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Slf4j
class OrderProductQueryRepositoryTest {

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

    @BeforeEach
    public void before(){
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

    @Test
    @DisplayName("기간별 특정 카테고리에서 판매자 총 판매액, 총 판매액 > 0 ")
    void findCategorySellerNameAndPriceSuccess1() throws Exception {
        //given
        String loginId = "2";
        String pw = "22";
        Long kindGradeId = 432L;
        Seller seller = sellerRepository.findByLoginIdAndPassword(loginId, pw).orElseThrow(NoSuchElementException::new);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(2);
        CategoryParamDTO categoryParamDTO = new CategoryParamDTO(null, null, null, kindGradeId);

        //when
        SellerAndTotalPricePerCategoryDTO result = orderProductQueryRepository.findCategorySellerNameAndPrice(categoryParamDTO, startDate, endDate, seller.getId());

        //then
        assertThat(result.getSellerName()).isEqualTo(seller.getName());

        // 3000 * 2 + 5000 * 1
        assertThat(result.getTotalPrice()).isEqualTo(11000);
    }

    @Test
    @DisplayName("기간별 특정 카테고리에서 특정 판매자 총 판매액, 총 판매액이 = 0")
    void findCategorySellerNameAndPriceSuccess2() throws Exception {
        //given
        String loginId = "2";
        String pw = "22";
        Long kindGradeId = 432L;
        Seller seller = sellerRepository.findByLoginIdAndPassword(loginId, pw).orElseThrow(NoSuchElementException::new);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMinutes(10);
        CategoryParamDTO categoryParamDTO = new CategoryParamDTO(null, null, null, kindGradeId);

        //when
        SellerAndTotalPricePerCategoryDTO result = orderProductQueryRepository.findCategorySellerNameAndPrice(categoryParamDTO, startDate, endDate, seller.getId());

        //then
        assertThat(result.getSellerName()).isNull();
        assertThat(result.getTotalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("기간별 특정 카테고리에서 상위 판매자 판매액과 이름 리스트, 해당 판매자와 판매액이 있는 경우(list.size > 0)")
    void findCategorySellerNameAndPriceTopRankListSuccess1() throws Exception {
        //given
        Long kindGradeId = 432L;
        int rankCount1 = 2; // 상위 2명
        int rankCount2 = 3; // 상위 3명
        int rankCount3 = 6; // 상위 6명

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(3);

        CategoryParamDTO categoryParamDTO = new CategoryParamDTO(null, null, null, kindGradeId);

        //when
        List<SellerAndTotalPricePerCategoryDTO> result1 = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount1);
        List<SellerAndTotalPricePerCategoryDTO> result2 = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount2);
        List<SellerAndTotalPricePerCategoryDTO> result3 = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount3);

        //then

        // kindGradeId=432 일 경우 기간내 판매액
        // -> 김현민 : 200,000원
        // -> 강대현 : 100,000원
        // -> 강병관 : 22,000원
        assertThat(result1.size()).isEqualTo(2);
        assertThat(result1).extracting("totalPrice").containsExactly(200000L, 100000L);
        assertThat(result1).extracting("sellerName").containsExactly("김현민", "강대현");

        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(200000L, 100000L, 22000L);
        assertThat(result2).extracting("sellerName").containsExactly("김현민", "강대현", "강병관");

        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(200000L, 100000L, 22000L);
        assertThat(result3).extracting("sellerName").containsExactly("김현민", "강대현", "강병관");
    }

    @Test
    @DisplayName("기간별 특정 카테고리에서 상위 판매자 판매액과 이름 리스트, 해당 판매자와 판매액이 없는 경우(list.size == 0)")
    void findCategorySellerNameAndPriceTopRankListSuccess2() throws Exception {
        //given
        Long kindGradeId = 434L;
        int rankCount1 = 2; // 상위 2명
        int rankCount2 = 3; // 상위 3명

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMinutes(1);

        CategoryParamDTO categoryParamDTO = new CategoryParamDTO(null, null, null, kindGradeId);

        //when
        List<SellerAndTotalPricePerCategoryDTO> result1 = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount1);
        List<SellerAndTotalPricePerCategoryDTO> result2 = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount2);

        //then

        // kindGradeId=434 일 경우 기간내 판매액이 없음
        assertThat(result1.size()).isEqualTo(0);
        assertThat(result2.size()).isEqualTo(0);


    }

    @Test
    void findBuyerTotalPricePerPeriod() throws Exception {
        //given
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", orderAddress);
        em.persist(productBuyer);

        Product product1 = getProduct("상품111", 1000, "상품입니다111", 432L, productOwner1);
        Product product2 = getProduct("상품222", 5000, "상품입니다222", 432L, productOwner1);
        Product product3 = getProduct("상품111", 10000, "상품입니다333", 432L, productOwner1);
        Product product4 = getProduct("상품222", 20000, "상품입니다444", 432L, productOwner1);

        for (int i = 0; i < 4; i++) { // 주문
            LocalDateTime now = LocalDateTime.now();

            OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(i + 1, product1.getId());
            OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(i + 2, product2.getId());
            List<OrderProductParamDTO> orderParamList1 = addOrderProductParamDTO(orderProductParam1, orderProductParam2);

            Long orderId = orderService.order(productBuyer.getId(), orderParamList1, orderAddress);
            Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
            findOrder.changeCreatedDate(now.minusMonths(i).withMinute(1));

            OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(i + 1, product3.getId());
            OrderProductParamDTO orderProductParam4 = new OrderProductParamDTO(i + 2, product4.getId());
            List<OrderProductParamDTO> orderParamList2 = addOrderProductParamDTO(orderProductParam3, orderProductParam4);

            Long orderId2 = orderService.order(productBuyer.getId(), orderParamList2, orderAddress);
            Order findOrder2 = orderRepository.findById(orderId2).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
            findOrder2.changeCreatedDate(now.minusMonths(i).withMinute(2));
        }
//        기간별 구매자 결제 금액
//        이번달 : 1000*1 + 5000 * 2 + 10000* 1 + 20000 * 2 = 61000
//        1달전 : 1000*2 + 5000 * 3 + 10000* 2 + 20000 *3 = 77000
//        2달전 : 1000*3 + 5000 * 4 + 10000* 3 + 20000 *4 = 133000
//        3달전 : 1000*4 + 5000 * 5 + 10000* 4 + 20000 *5 = 169000
//        4달전 : 0
//        5달전 : 0

        //when
        YearMonth startDate1 = YearMonth.now().minusMonths(5);// 5달전
        YearMonth endDate1 = YearMonth.now(); // 이번달
        List<BuyerTotalPricePerPeriodDTO> result1 = orderProductQueryRepository.findBuyerTotalPricePerPeriod(startDate1, endDate1, productBuyer.getId());

        YearMonth startDate2 = YearMonth.now().minusMonths(3); // 3달전
        YearMonth endDate2 = YearMonth.now().minusMonths(2); // 2달전
        List<BuyerTotalPricePerPeriodDTO> result2 = orderProductQueryRepository.findBuyerTotalPricePerPeriod(startDate2, endDate2, productBuyer.getId());

        //then

        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전
        String fourMonthsAgo = LocalDateTime.now().minusMonths(4).format(DateTimeFormatter.ofPattern("yyyy-MM")); //4달전
        String threeMonthsAgo = LocalDateTime.now().minusMonths(3).format(DateTimeFormatter.ofPattern("yyyy-MM")); //3달전
        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        //6달전 ~ 이번달
        assertThat(result1.size()).isEqualTo(6);
        assertThat(result1).extracting("date").containsExactly(fiveMonthsAgo, fourMonthsAgo, threeMonthsAgo, twoMonthsAgo, oneMonthsAgo, now);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 0L, 169000L, 133000L, 97000L, 61000L);

        //2달전~ 3달전
        assertThat(result2.size()).isEqualTo(2);
        assertThat(result2).extracting("date").containsExactly(threeMonthsAgo, twoMonthsAgo);
        assertThat(result2).extracting("totalPrice").containsExactly(169000L, 133000L);

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

    private LocalDateTime startMonthOfDayLocalDateTime(YearMonth yearMonth) {
        return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
    }

    private LocalDateTime endMonthOfDayLocalDateTime(YearMonth yearMonth) {
        // 2022년 11월 -> 2022년 11월 30일 23:59:999999
        LocalTime maxTime = LocalTime.MAX;
        LocalDate localDate = yearMonth.atEndOfMonth();
        return LocalDateTime.of(localDate, maxTime);
    }
}