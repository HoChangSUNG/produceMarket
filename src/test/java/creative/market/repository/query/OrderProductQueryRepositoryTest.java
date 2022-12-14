package creative.market.repository.query;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.*;
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

    @Test
    @DisplayName("기간별 카테고리 전체 판매자 판매액, 카테고리 = kindGrade 기준")
    void allSellerTotalPriceByPeriodAndCategory1() throws Exception {
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 474L));

        //식량작물-쌀-일반계-상품(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 432L));

        // 채소류-상추-적-상품 (2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
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
    @DisplayName("기간별 카테고리 전체 판매자 판매액, 카테고리 = kind 기준")
    void allSellerTotalPriceByPeriodAndCategory2() throws Exception {
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1662L, null));

        //식량작물-쌀-일반계(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1613L, null));

        // 채소류-상추-적(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
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
    @DisplayName("기간별 카테고리 전체 판매자 판매액, 카테고리 = item 기준")
    void allSellerTotalPriceByPeriodAndCategory3() throws Exception {
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 211, null, null));

        //식량작물-쌀(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 111, null, null));

        // 채소류-상추(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
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
    @DisplayName("기간별 카테고리 전체 판매자 판매액, 카테고리 = itemCategory 기준")
    void allSellerTotalPriceByPeriodAndCategory4() throws Exception {
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(200, null, null, null));

        //식량작물(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(100, null, null, null));

        // 과일류(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
                .findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(400, null, null, null));

        // 모든 상품(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result4 = orderProductQueryRepository
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

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매액, 카테고리 = kindGrade 기준")
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 474L), productOwner1.getId());

        //식량작물-쌀-일반계-상품(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 432L), productOwner1.getId());

        // 채소류-상추-적-상품 (2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, 492L), productOwner1.getId());

        //then
        // 채소류-배추-봄-상품 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 2000L, 1000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀-일반계-상품(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 채소류-상추-적-상품 (2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매액, 카테고리 = kind 기준")
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
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1662L, null), productOwner1.getId());

        //식량작물-쌀-일반계(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1613L, null), productOwner1.getId());

        // 채소류-상추-적(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, 1680L, null), productOwner1.getId());

        //then
        // 채소류-배추-봄 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 6000L, 3000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀-일반계(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 채소류-상추-적 (2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매액, 카테고리 = item 기준")
    void sellerTotalPriceByPeriodAndCategory3() throws Exception {
        Address buyerAddress = createAddress("1122", "봉사산로", 12345, "동호수");
        Seller productOwner1 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("강대현2", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        sellerTotalPriceGraphInit(productOwner1, productOwner2, productBuyer);

        // 채소류-배추-고랭지-상품 결제 내역 추가
        Product product1 = getProduct("고랭지상품2222222", 100000, "고랭지 배추입니다.", 477L, productOwner1); //채소류-배추-고랭지-상품

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(2, product1.getId());
        List<OrderProductParamDTO> orderParamList1 = addOrderProductParamDTO(orderProductParam1);
        orderService.order(productBuyer.getId(), orderParamList1, buyerAddress);

        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류-배추 (2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 211, null, null), productOwner1.getId());

        //식량작물-쌀(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, 111, null, null), productOwner1.getId());

        //then
        // 채소류-배추 (2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 6000L, 203000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물-쌀(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매액, 카테고리 = itemCategory 기준")
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
        Product product1 = getProduct("사과상품2222222", 120000, "사과입니다.", 702L, productOwner1); // 과일류-사과-홍옥-상품

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(1, product1.getId());
        List<OrderProductParamDTO> orderParamList1 = addOrderProductParamDTO(orderProductParam1);
        orderService.order(productBuyer.getId(), orderParamList1, buyerAddress);

        // 채소류-배추-고랭지-상품 결제 내역 추가
        Product product2 = getProduct("고랭지상품2222222", 100000, "고랭지 배추입니다.", 477L, productOwner1); //채소류-배추-고랭지-상품

        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(2, product2.getId());
        List<OrderProductParamDTO> orderParamList2 = addOrderProductParamDTO(orderProductParam2);
        orderService.order(productBuyer.getId(), orderParamList2, buyerAddress);

        //when
        YearMonth startDate = YearMonth.now().minusMonths(2);
        YearMonth endDate = YearMonth.now();

        String twoMonthsAgo = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")); //2달전
        String oneMonthsAgo = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")); //1달전
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); //이번달

        // 채소류(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result1 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(200, null, null, null), productOwner1.getId());

        //식량작물(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result2 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(100, null, null, null), productOwner1.getId());

        // 과일류(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result3 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(400, null, null, null), productOwner1.getId());

        // 모든 상품(2달전 ~ 이번달)
        List<SellerPricePerPeriodDTO> result4 = orderProductQueryRepository
                .findSellerTotalPricePerPeriodAndCategory(startDate, endDate, new CategoryParamDTO(null, null, null, null), productOwner1.getId());

        //then
        // 채소류(2달전 ~ 이번달)
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("totalPrice").containsExactly(0L, 6000L, 203000L);
        assertThat(result1).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        //식량작물(2달전 ~ 이번달)
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("totalPrice").containsExactly(0L, 0L, 0L);
        assertThat(result2).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 과일류(2달전 ~ 이번달)
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("totalPrice").containsExactly(0L, 0L, 120000L);
        assertThat(result3).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());

        // 모든상품(2달전 ~ 이번달)
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("totalPrice").containsExactly(0L, 6000L, 323000L);
        assertThat(result4).extracting("date").containsExactly(twoMonthsAgo.toString(), oneMonthsAgo.toString(), now.toString());
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매자 판매횟수, 카테고리 = kindGrade 기준")
    void allSellerTotalCountByPeriodAndCategory1() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, null, null, 468L); //식량작물-감자-수미-상품
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, null, null, 469L); //식량작물-감자-수미-중품
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, null, null, 470L); //식량작물-감자-수미-하품
        CategoryParamDTO categoryParam4 = new CategoryParamDTO(null, null, null, 471L); //식량작물-감자-대지마-상품
        CategoryParamDTO categoryParam5 = new CategoryParamDTO(null, null, null, 669L); //특용작물-참깨-백색(국산)-상품
        CategoryParamDTO categoryParam6 = new CategoryParamDTO(null, null, null, 432L); //식량작물-쌀-일반계-상품


        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam1);
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam2);
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam3);
        List<SellerOrderCountPerPeriodDTO> result4 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam4);
        List<SellerOrderCountPerPeriodDTO> result5 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam5);
        List<SellerOrderCountPerPeriodDTO> result6 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam6);

        //then
        //식량작물-감자-수미-상품
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,5L,3L);

        //식량작물-감자-수미-중품
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,0L,0L);

        //식량작물-감자-수미-하품
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,4L,3L);

        //식량작물-감자-대지마-상품
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result4).extracting("totalCount").containsExactly(0L,5L,4L);

        //특용작물-참깨-백색(국산)-상품
        assertThat(result5.size()).isEqualTo(3);
        assertThat(result5).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result5).extracting("totalCount").containsExactly(0L,6L,3L);

        //식량작물-쌀-일반계-상품
        assertThat(result6.size()).isEqualTo(3);
        assertThat(result6).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result6).extracting("totalCount").containsExactly(0L,11L,11L);
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매자 판매횟수, 카테고리 = kind 기준")
    void allSellerTotalCountByPeriodAndCategory2() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, null, 1652L, null); //식량작물-감자-수미
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, null, 1655L, null); //식량작물-감자-대지마
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, null, 1874L, null); //특용작물-참깨-중국


        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam1 );
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam2);
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam3 );

        //then
        //식량작물-감자-수미
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,9L,6L);

        //식량작물-감자-대지마
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,5L,4L);

        //특용작물-참깨-중국
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,0L,0L);
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매자 판매횟수, 카테고리 = item 기준")
    void allSellerTotalCountByPeriodAndCategory3() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, 152, null, null); //식량작물-감자
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, 312, null, null); //특용작물-참깨
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, 111, null, null); //식량작물-쌀
        CategoryParamDTO categoryParam4 = new CategoryParamDTO(null, 112, null, null); //식량작물-찹쌀

        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam1);
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam2);
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam3);
        List<SellerOrderCountPerPeriodDTO> result4 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam4);

        //then
        //식량작물-감자
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,14L,10L);

        //특용작물-참깨
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,6L,3L);

        //식량작물-쌀
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,11L,11L);

        ///식량작물-찹쌀
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result4).extracting("totalCount").containsExactly(0L,0L,0L);
    }

    @Test
    @DisplayName("기간별 카테고리 전체 판매자 판매횟수, 카테고리 = itemCategory 기준")
    void allSellerTotalCountByPeriodAndCategory4() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(100, null, null, null); //식량작물
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(300, null, null, null); //특용작물
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(400, null, null, null); //과일류
        CategoryParamDTO categoryParam4 = new CategoryParamDTO(null, null, null, null); //전체

        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam1);
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam2);
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam3);
        List<SellerOrderCountPerPeriodDTO> result4 = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParam4);

        //then
        //식량작물
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,25L,21L);

        //특용작물
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,6L,3L);

        //과일류
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,0L,0L);

        // 전체
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result4).extracting("totalCount").containsExactly(0L,31L,24L);
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매횟수, 카테고리 = kindGrade 기준")
    void sellerTotalCountByPeriodAndCategory1() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, null, null, 468L); //식량작물-감자-수미-상품
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, null, null, 469L); //식량작물-감자-수미-중품
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, null, null, 470L); //식량작물-감자-수미-하품
        CategoryParamDTO categoryParam4 = new CategoryParamDTO(null, null, null, 471L); //식량작물-감자-대지마-상품

        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam1, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam2, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam3, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result4 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam4, productOwner1.getId());

        //then
        //식량작물-감자-수미-상품
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,3L,2L);

        //식량작물-감자-수미-중품
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,0L,0L);

        //식량작물-감자-수미-하품
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,4L,3L);

        //식량작물-감자-대지마-상품
        assertThat(result4.size()).isEqualTo(3);
        assertThat(result4).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result4).extracting("totalCount").containsExactly(0L,5L,4L);
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매횟수, 카테고리 = kind 기준")
    void sellerTotalCountByPeriodAndCategory2() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, null, 1652L, null); //식량작물-감자-수미
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, null, 1655L, null); //식량작물-감자-대지마
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, null, 1874L, null); //특용작물-참깨-중국


        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam1, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam2, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam3, productOwner1.getId());

        //then
        //식량작물-감자-수미
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,7L,5L);

        //식량작물-감자-대지마
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,5L,4L);

        //특용작물-참깨-중국
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,0L,0L);
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매횟수, 카테고리 = item 기준")
    void sellerTotalCountByPeriodAndCategory3() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(null, 152, null, null); //식량작물-감자
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(null, 312, null, null); //특용작물-참깨

        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam1, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam2, productOwner1.getId());

        //then
        //식량작물-감자
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,12L,9L);

        //특용작물-참깨
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,2L,1L);
    }

    @Test
    @DisplayName("기간별 카테고리 특정 판매자 판매횟수, 카테고리 = itemCategory 기준")
    void sellerTotalCountByPeriodAndCategory4() throws Exception{
        //given
        Address buyerAddress = createAddress("11ㅈ22", "봉사산로ㅈㅈ", 12345, "동호ㅈ수");
        Seller productOwner1 = createSeller("김시관1", "111", "11", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner1);
        Seller productOwner2 = createSeller("김시관2", "22222222", "11sdfw", "19990212", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(productOwner2);

        Buyer productBuyer = createBuyer("성호창32", "311111", "332222222", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        YearMonth startDate = YearMonth.now().minusMonths(7);
        YearMonth endDate = YearMonth.now().minusMonths(5);

        String sevenMonthsAgo = LocalDateTime.now().minusMonths(7).format(DateTimeFormatter.ofPattern("yyyy-MM")); //7달전
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM")); //6달전
        String fiveMonthsAgo = LocalDateTime.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM")); //5달전

        // 초기 값
        sellerOrderCountGraphInit(productOwner1,productOwner2,productBuyer);

        //when
        CategoryParamDTO categoryParam1 = new CategoryParamDTO(100, null, null, null); //식량작물
        CategoryParamDTO categoryParam2 = new CategoryParamDTO(300, null, null, null); //특용작물
        CategoryParamDTO categoryParam3 = new CategoryParamDTO(null, null, null, null); //전체

        List<SellerOrderCountPerPeriodDTO> result1 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam1, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result2 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam2, productOwner1.getId());
        List<SellerOrderCountPerPeriodDTO> result3 = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParam3, productOwner1.getId());

        //then
        //식량작물
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result1).extracting("totalCount").containsExactly(0L,22L,11L);

        //특용작물
        assertThat(result2.size()).isEqualTo(3);
        assertThat(result2).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result2).extracting("totalCount").containsExactly(0L,2L,1L);

        // 전체
        assertThat(result3.size()).isEqualTo(3);
        assertThat(result3).extracting("date").containsExactly(sevenMonthsAgo,sixMonthsAgo,fiveMonthsAgo);
        assertThat(result3).extracting("totalCount").containsExactly(0L,24L,12L);
    }

    private void sellerTotalPriceGraphInit(Seller productOwner1, Seller productOwner2, Buyer productBuyer) {
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");

        Product product1 = getProduct("상품111", 1000, "상품입니다111", 474L, productOwner1); // 채소류-배추-상품
        Product product2 = getProduct("상품222", 2000, "상품입니다222", 475L, productOwner1); // 채소류-봄-배추-봄-중품
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

    private void orderProductForOrderCountInit(int count, Product product,LocalDateTime localDateTime,Buyer buyer) {
        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        OrderProductParamDTO orderProductParam = new OrderProductParamDTO(1, product.getId());
        List<OrderProductParamDTO> orderParamList = addOrderProductParamDTO(orderProductParam);

        for (int i = 0; i < count; i++) { // count 번 주문
            Long orderId = orderService.order(buyer.getId(), orderParamList, orderAddress);
            Order findOrder = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("주문 내역이 존재하지 않습니다."));
            findOrder.changeCreatedDate(localDateTime);
        }
    }
    private void sellerOrderCountGraphInit(Seller productOwner1, Seller productOwner2, Buyer productBuyer) {

        Product product1 = getProduct("식량작물-감자-수미-상-1", 1000, "상품입니다111", 468L, productOwner1); // 식량작물-감자-수미-상품
        Product product2 = getProduct("식량작물-감자-수미-상-2", 1000, "상품입니다111", 468L, productOwner2); // 식량작물-감자-수미-상품

        Product product3 = getProduct("식량작물-감자-수미-하", 1000, "상품입니다111", 470L, productOwner1); // 식량작물-감자-수미-하품
        Product product4 = getProduct("식량작물-감자-대지마-상품", 1000, "상품입니다111", 471L, productOwner1); // 식량작물-감자-대지마-상품
        Product product5 = getProduct("특용작물-참깨-백색(국산)-상품-1", 1000, "상품입니다111", 669L, productOwner1); // 특용작물-참깨-백색(국산)-상품
        Product product6 = getProduct("특용작물-참깨-백색(국산)-상품-2", 1000, "상품입니다111", 669L, productOwner2); // 특용작물-참깨-백색(국산)-상품

        Product product7 = getProduct("식량작물-쌀-일반계-상", 1000, "상품입니다111", 432L, productOwner1); // 식량작물-쌀-일반계-상품
        Product product8 = getProduct("식량작물-쌀-일반계-상", 1000, "상품입니다111", 432L, productOwner2); // 식량작물-쌀-일반계-상품


        orderProductForOrderCountInit(3,product1,LocalDateTime.now().minusMonths(6),productBuyer); //식량작물-감자-수미-상품, 6달전 3번, productOwner1
        orderProductForOrderCountInit(2,product1,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-감자-수미-상품, 5달전 2번, productOwner1

        orderProductForOrderCountInit(2,product2,LocalDateTime.now().minusMonths(6),productBuyer); //식량작물-감자-수미-상품, 6달전 2번, productOwner2
        orderProductForOrderCountInit(1,product2,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-감자-수미-상품, 5달전 1번, productOwner2

        orderProductForOrderCountInit(4,product3,LocalDateTime.now().minusMonths(6),productBuyer);//식량작물-감자-수미-하품, 6달전 4번, productOwner1
        orderProductForOrderCountInit(3,product3,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-감자-수미-하품, 5달전 3번, productOwner1

        orderProductForOrderCountInit(5,product4,LocalDateTime.now().minusMonths(6),productBuyer);//식량작물-감자-대지마-상품, 6달전 5번, productOwner1
        orderProductForOrderCountInit(4,product4,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-감자-대지마-상품, 5달전 4번, productOwner1

        orderProductForOrderCountInit(2,product5,LocalDateTime.now().minusMonths(6),productBuyer);//특용작물-참깨-백색(국산)-상품, 6달전 2번, productOwner1
        orderProductForOrderCountInit(1,product5,LocalDateTime.now().minusMonths(5),productBuyer);//특용작물-참깨-백색(국산)-상품, 5달전 1번, productOwner1

        orderProductForOrderCountInit(4,product6,LocalDateTime.now().minusMonths(6),productBuyer);//특용작물-참깨-백색(국산)-상품, 6달전 4번, productOwner2
        orderProductForOrderCountInit(2,product6,LocalDateTime.now().minusMonths(5),productBuyer);//특용작물-참깨-백색(국산)-상품, 5달전 2번, productOwner2

        orderProductForOrderCountInit(10,product7,LocalDateTime.now().minusMonths(6),productBuyer);//식량작물-쌀-일반계-상품, 6달전 10번, productOwner1
        orderProductForOrderCountInit(2,product7,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-쌀-일반계-상품, 5달전 2번, productOwner1

        orderProductForOrderCountInit(1,product8,LocalDateTime.now().minusMonths(6),productBuyer);//식량작물-쌀-일반계-상품, 6달전 1번, productOwner2
        orderProductForOrderCountInit(9,product8,LocalDateTime.now().minusMonths(5),productBuyer);//식량작물-쌀-일반계-상품, 5달전 9번, productOwner2
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

}