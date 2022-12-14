package creative.market.repository;

import creative.market.domain.Address;
import creative.market.domain.Review;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.Order;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.domain.user.User;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.repository.order.OrderRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.OrderService;
import creative.market.service.dto.OrderProductParamDTO;
import creative.market.util.PagingUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class ProductRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    KindGradeRepository kindGradeRepository;
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @BeforeEach
    void before() throws Exception {
        Address address = createAddress("1", "1", 12, "234");
        Seller seller = createSeller("?????????", "loginId1", "pw1", address);
        em.persist(seller);

        KindGrade kindGrade1 = createKindGrade(432L);
        ProductImage sigImg1 = createSigImg("sigP1.jpg", "/ssf/sigP1.jpg");
        List<ProductImage> ordinalImgList1 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal1", "/ordinal1.jpg")));
        Product product1 = createProduct("???-?????????-??????", 10000, "??? ????????????1", seller, kindGrade1, sigImg1, ordinalImgList1);
        em.persist(product1);

        KindGrade kindGrade2 = createKindGrade(433L);
        ProductImage sigImg2 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList2 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product2 = createProduct("???-?????????-??????", 20000, "??? ????????????2", seller, kindGrade2, sigImg2, ordinalImgList2);
        em.persist(product2);

        KindGrade kindGrade3 = createKindGrade(404L);
        ProductImage sigImg3 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList3 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product3 = createProduct("???-??????-????????????", 13000, "?????? ????????????3", seller, kindGrade3, sigImg3, ordinalImgList3);
        em.persist(product3);

        KindGrade kindGrade4 = createKindGrade(474L);
        ProductImage sigImg4 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList4 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product4 = createProduct("??????-???-??????", 50000, "?????? ????????????3", seller, kindGrade4, sigImg4, ordinalImgList4);
        em.persist(product4);

        em.flush();
        em.clear();
        log.info("===================????????? ???????????? clear======================");
    }

    private Address createAddress(String jibun, String raod, int zipcode, String detailAddress) {
        return Address.builder().jibun(jibun).road(raod).zipcode(zipcode).detailAddress(detailAddress).build();
    }

    private Seller createSeller(String name, String loginId, String pw, Address address) {
        Seller seller = Seller.builder().name(name).loginId(loginId).password(pw).address(address).build();
        return seller;
    }

    private Product createProduct(String productName, int price, String productInfo, Seller seller, KindGrade kindGrade, ProductImage sigImg, List<ProductImage> ordinalImgList) {
        Product product = Product.builder().name(productName).price(price).info(productInfo).user(seller).kindGrade(kindGrade).ordinalProductImages(ordinalImgList).signatureProductImage(sigImg).ordinalProductImages(ordinalImgList).build();
        em.persist(product);
        return product;
    }

    private ProductImage createProductImage(String imgName, String imgPath, ProductImageType imageType) {
        return ProductImage.builder().name(imgName).path(imgPath).type(imageType).build();
    }

    private ProductImage createSigImg(String imgName, String imgPath) {
        return createProductImage(imgName, imgPath, ProductImageType.SIGNATURE);
    }

    private List<ProductImage> createOrdinalImgList(List<ImageDTO> imageDTOList) {
        return imageDTOList.stream().map(imageDTO -> createProductImage(imageDTO.getName(), imageDTO.getPath(), ProductImageType.ORDINAL)).collect(Collectors.toList());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class ImageDTO {
        private String name;
        private String path;
    }

    @Test
    @DisplayName("?????? ????????? ??????, kindGradeId??? ??????")
    void findByKindGradeId() throws Exception {
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "latest", null, null, null, 432L);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq("???", "latest", null, null, null, 432L);
        ProductSearchConditionReq condition3 = new ProductSearchConditionReq(null, "latest", null, null, null, 475L);
        ProductSearchConditionReq condition4 = new ProductSearchConditionReq("???", "latest", null, null, null, 474L);

        System.out.println(condition2.getProductName());
        //when
        Long totalCount1 = productRepository.findProductByConditionTotalCount(condition1);
        Long totalCount2 = productRepository.findProductByConditionTotalCount(condition2);
        Long totalCount3 = productRepository.findProductByConditionTotalCount(condition3);
        Long totalCount4 = productRepository.findProductByConditionTotalCount(condition4);

        List<Product> result1 = productRepository.findProductByCondition(condition1, 0, totalCount1.intValue() + 1);
        List<Product> result2 = productRepository.findProductByCondition(condition2, 0, totalCount2.intValue() + 1);
        List<Product> result3 = productRepository.findProductByCondition(condition3, 0, totalCount3.intValue() + 1);
        List<Product> result4 = productRepository.findProductByCondition(condition4, 0, totalCount4.intValue() + 1);

        //then
        assertThat(result1.size()).isEqualTo(1);
        assertThat(result1).extracting("name").contains("???-?????????-??????");

        assertThat(result2.size()).isEqualTo(1);
        assertThat(result2).extracting("name").contains("???-?????????-??????");

        assertThat(result3.size()).isEqualTo(0); // ?????? ?????? ??????

        assertThat(result4.size()).isEqualTo(0); // ??????????????? ?????? ????????? ?????? ?????? ??????
    }

    @Test
    @DisplayName("?????? ????????? ??????, kindId??? ??????")
    void findByKindId() throws Exception {
        //given
        ProductSearchConditionReq condition = new ProductSearchConditionReq(null, "latest", null, null, 1613L, null);

        //when
        Long totalCount = productRepository.findProductByConditionTotalCount(condition);
        List<Product> result = productRepository.findProductByCondition(condition, 0, totalCount.intValue() + 1);

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("name").contains("???-?????????-??????", "???-?????????-??????");
    }

    @Test
    @DisplayName("?????? ????????? ??????, itemCode??? ??????, ?????????")
    void findByItemCode() throws Exception {
        //given
        ProductSearchConditionReq condition = new ProductSearchConditionReq(null, "price", null, 111, null, null);

        //when
        Long totalCount = productRepository.findProductByConditionTotalCount(condition);
        List<Product> result = productRepository.findProductByCondition(condition, 0, totalCount.intValue() + 1);


        //then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).extracting("name").containsExactly("???-?????????-??????", "???-??????-????????????", "???-?????????-??????");
        assertThat(result).extracting("price").containsExactly(10000, 13000, 20000); // ?????? ???
    }

    @Test
    @DisplayName("?????? ????????? ??????, itemCategoryCode??? ??????, ?????????")
    void findByItemCategoryCode() throws Exception {
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "price", 100, null, null, null);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq(null, "price", 200, null, null, null);

        //when
        Long totalCount1 = productRepository.findProductByConditionTotalCount(condition1);
        Long totalCount2 = productRepository.findProductByConditionTotalCount(condition2);
        List<Product> result1 = productRepository.findProductByCondition(condition1, 0, totalCount1.intValue() + 1);
        List<Product> result2 = productRepository.findProductByCondition(condition2, 0, totalCount2.intValue() + 1);


        //then
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("name").contains("???-?????????-??????", "???-?????????-??????", "???-??????-????????????");// ???????????? ???????????? ??????

        assertThat(result2.size()).isEqualTo(1);
        assertThat(result2).extracting("name").contains("??????-???-??????");// ????????? ???????????? ??????
    }

    @Test
    @DisplayName("?????? ????????? ??????, ?????? ?????? ?????? ??????. ?????????")
    void findByAll() throws Exception {
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "price", null, null, null, null);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq("", "price", null, null, null, null);

        //when
        Long totalCount1 = productRepository.findProductByConditionTotalCount(condition1);
        Long totalCount2 = productRepository.findProductByConditionTotalCount(condition2);
        List<Product> result1 = productRepository.findProductByCondition(condition1, 0, totalCount1.intValue() + 1);
        List<Product> result2 = productRepository.findProductByCondition(condition2, 0, totalCount2.intValue() + 1);


        //then
        assertThat(result1.size()).isEqualTo(4);
        assertThat(result1).extracting("name").containsExactly("???-?????????-??????",  "???-??????-????????????", "???-?????????-??????", "??????-???-??????");// ?????? ?????? ?????? ?????? ???
        assertThat(result1).extracting("price").containsExactly(10000, 13000, 20000, 50000);

        assertThat(result2.size()).isEqualTo(4);
        assertThat(result2).extracting("name").containsExactly("???-?????????-??????", "???-??????-????????????", "???-?????????-??????", "??????-???-??????");// ?????? ?????? ?????? ?????? ???
        assertThat(result2).extracting("price").containsExactly(10000, 13000, 20000, 50000);
    }

    @Test
    @DisplayName("?????? ????????? ??????, ?????? ?????? ?????? ?????? ??????")
    void findByAllWithName() throws Exception {
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq("???", "price", null, null, null, null);

        //when
        Long totalCount1 = productRepository.findProductByConditionTotalCount(condition1);
        List<Product> result1 = productRepository.findProductByCondition(condition1, 0, totalCount1.intValue() + 1);

        //then
        assertThat(result1.size()).isEqualTo(3);
        assertThat(result1).extracting("name").contains("???-?????????-??????", "???-??????-????????????", "???-?????????-??????");// ?????? ?????? ?????????

    }


    @Test
    @DisplayName("?????? ????????? ??????, ????????? ??????")
    void findByKindGradePaging() throws Exception {
        //given
        Address address = createAddress("122", "11", 12, "234");
        Seller seller = createSeller("?????????11111", "loginId22221", "pw13333", address);
        em.persist(seller);

        KindGrade kindGrade1 = createKindGrade(450L);

        for (int i = 0; i < 100; i++) {
            ProductImage sigImg1 = createSigImg("sigP1.jpg", "/ssf/sigP1.jpg");
            List<ProductImage> ordinalImgList1 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal" + i, "/ordinal1.jpg")));
            Product product = createProduct("??????" + i, 10000 + i, "?????? ????????????" + i, seller, kindGrade1, sigImg1, ordinalImgList1);
            em.persist(product);
        }

        ProductSearchConditionReq condition = new ProductSearchConditionReq(null, "price", null, null, null, 450L);

        //then

        int pageSize = 13;
        int offset = 0;
        Long totalCount = productRepository.findProductByConditionTotalCount(condition);
        int totalPageNum = PagingUtils.getTotalPageNum(totalCount, pageSize);
        int lastPageCnt = totalCount.intValue() - (totalPageNum - 1) * pageSize; // ????????? ????????? ????????? size

        for (int i = 1; i < totalPageNum; i++) { // ????????? ????????? ?????????
            offset = PagingUtils.getOffset(i, pageSize);
            List<Product> result = productRepository.findProductByCondition(condition, offset, pageSize);
            assertThat(result.size()).isEqualTo(pageSize);
        }

        //????????? ?????????
        offset = PagingUtils.getOffset(totalPageNum, pageSize);
        List<Product> result = productRepository.findProductByCondition(condition, offset, pageSize);
        assertThat(result.size()).isEqualTo(lastPageCnt);
    }

    @Test
    @DisplayName("??????????????? ?????? ?????? ??????")
    void priceAvg() throws Exception {
        //given
        Address address = createAddress("12", "21", 212, "2342222");
        Seller seller = createSeller("?????????", "loginId2", "pw2", address);
        em.persist(seller);

        KindGrade kindGrade1 = createKindGrade(432L);
        ProductImage sigImg1 = createSigImg("sigP1222.jpg", "/ssf/sigP1222.jpg");
        List<ProductImage> ordinalImgList1 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal11", "/ordinal11.jpg")));
        Product product1 = createProduct("???-?????????-??????2", 15000, "??? ????????????2", seller, kindGrade1, sigImg1, ordinalImgList1);
        em.persist(product1);

        KindGrade kindGrade2 = createKindGrade(433L);
        ProductImage sigImg2 = createSigImg("sigP2??????.jpg", "/ss23f???/sig22P2.jpg");
        List<ProductImage> ordinalImgList2 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product2 = createProduct("???-?????????-??????", 33333, "??? ????????????22", seller, kindGrade2, sigImg2, ordinalImgList2);
        em.persist(product2);
        //when

        Double avg1 = productRepository.findProductAvgPrice(kindGrade1.getId());
        Double avg2 = productRepository.findProductAvgPrice(499L);
        Double avg3 = productRepository.findProductAvgPrice(kindGrade2.getId());


        //then
        assertThat(avg1.intValue()).isEqualTo(12500);
        assertThat(avg2).isEqualTo(0D); // ???????????? ?????? kindGradeId ??????
        assertThat(avg3.intValue()).isEqualTo(26666);

    }

    @Test
    @DisplayName("?????? ?????????????????? ???????????? ?????? ???????????? ????????? productId, ????????? ?????? ?????? ???")
    void orderCountDesc() throws Exception {
        //given
        int limit = 2;
        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(12);
        //when
        List<Long> result = productRepository.findProductIdByOrderCountDesc(offset, limit, startDate, endDate);

        //then
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("?????? ?????????????????? ???????????? ?????? ???????????? ????????? productId")
    void orderCountDesc2() throws Exception {
        //given
        List<Product> findProducts = productRepository.findAll();
        Product product1 = findProducts.get(0);
        Product product2 = findProducts.get(1);
        Product product3 = findProducts.get(2);
        Product product4 = findProducts.get(3);

        Address address = createAddress("111", "221", 13332, "22234");
        Seller seller = createSeller("?????????", "sfwefs", "3124sss", address);
        em.persist(seller);

        // product1 ->  5??? ??????
        Long orderId1 = orderProduct(seller.getId(), product1.getId(), address, 1);
        Long orderId2 = orderProduct(seller.getId(), product1.getId(), address, 1);
        LocalDateTime changeOrderDate = LocalDateTime.now().minusMonths(1);
        changeOrderDate(orderId1, changeOrderDate); // 1???????????? ?????? ?????? ??????
        changeOrderDate(orderId2, changeOrderDate); // 1???????????? ?????? ?????? ??????

        // product2 ->  3??? ??????
        orderProduct(seller.getId(), product2.getId(), address, 2);
        orderProduct(seller.getId(), product2.getId(), address, 1);
        orderProduct(seller.getId(), product2.getId(), address, 4);

        // product3 ->  2??? ??????
        orderProduct(seller.getId(), product3.getId(), address, 1);
        orderProduct(seller.getId(), product3.getId(), address, 1);

        // product4 ->  1??? ??????
        orderProduct(seller.getId(), product4.getId(), address, 1);

        //when
        int limit = 2;
        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(1);
        List<Long> findProductIds = productRepository.findProductIdByOrderCountDesc(offset, limit, startDate, endDate);

        //then
        assertThat(findProductIds.size()).isEqualTo(2);
        assertThat(findProductIds).containsExactly(product2.getId(), product3.getId()); //2 3??? product ???
    }

    @Test
    @DisplayName("?????? ?????????????????? ???????????? ?????? ???????????? ????????? productId, ???????????? ????????? ?????? ?????? ????????? ??????????????? ??????")
    void orderCountDesc3() throws Exception {
        //given
        List<Product> findProducts = productRepository.findAll();
        Product product1 = findProducts.get(0);
        Product product2 = findProducts.get(1);

        Address address = createAddress("111", "221", 13332, "22234");
        Seller seller = createSeller("?????????", "sfwefs", "3124sss", address);
        em.persist(seller);

        // product1 ->  2??? ??????
        orderProduct(seller.getId(), product1.getId(), address, 1);
        orderProduct(seller.getId(), product1.getId(), address, 1);

        // product2 ->  2??? ??????
        orderProduct(seller.getId(), product2.getId(), address, 2);
        orderProduct(seller.getId(), product2.getId(), address, 1);

        //when
        int limit = 2;
        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(1);
        List<Long> findProductIds = productRepository.findProductIdByOrderCountDesc(offset, limit, startDate, endDate);

        //then
        assertThat(findProductIds.size()).isEqualTo(2);
        assertThat(findProductIds).containsExactly(product2.getId(), product1.getId()); //2 1??? product ???
    }

    @Test
    @DisplayName("?????? ?????????????????? ?????? ?????? ?????? ?????? ???????????? ????????? productId, ????????? ????????? ???????????? ?????? ??????")
    void reviewAvgDesc1() throws Exception {
        //given
        int limit = 2;
        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now(); // ??????
        LocalDateTime startDate = endDate.minusMonths(12); //1??? ???

        //when
        List<Long> result = productRepository.findProductIdByReviewCountDesc(offset, limit, startDate, endDate);

        //then
        // 1?????? ~ ??????
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("?????? ?????????????????? ?????? ?????? ?????? ???????????? productId")
    void reviewAvgDesc2() throws Exception {
        //given
        int limit = 5;
        int offset = 0;

        //when
        List<Product> findProducts = productRepository.findAll();

        Product product1 = findProducts.get(0);
        Product product2 = findProducts.get(1);
        Product product3 = findProducts.get(2);
        Product product4 = findProducts.get(3);

        Address buyerAddress = createAddress("1111", "????????????3", 11111, "3???4???");
        Buyer productBuyer = createBuyer("?????????3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        //product1 ?????? ??????
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(1F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);

        //product2 ?????? ??????
        saveReview(1F, "??????", productBuyer, product2);
        saveReview(1F, "??????", productBuyer, product2);
        saveReview(1F, "??????", productBuyer, product2);

        //product3 ?????? ??????
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);

        //product4 ?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????

        LocalDateTime endDate = LocalDateTime.now(); // ??????
        LocalDateTime startDate = endDate.minusMonths(1); // ?????????

        List<Long> result = productRepository.findProductIdByReviewCountDesc(offset, limit, startDate, endDate);

        //then
        // 1?????? ~ ??????
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactly(product3.getId(), product1.getId(), product2.getId());

    }

    @Test
    @DisplayName("?????? ?????????????????? ?????? ?????? ?????? ???????????? productId, ?????? ?????? ????????? ????????? ?????? ?????? ?????? ???????????? ???????????? ??????")
    void reviewAvgDesc3() throws Exception {
        //given
        int limit = 5;
        int offset = 0;

        //when
        List<Product> findProducts = productRepository.findAll();

        Product product1 = findProducts.get(0);
        Product product2 = findProducts.get(1);
        Product product3 = findProducts.get(2);
        Product product4 = findProducts.get(3);

        Address buyerAddress = createAddress("1111", "????????????3", 11111, "3???4???");
        Buyer productBuyer = createBuyer("?????????3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        //product1 ?????? ??????
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);

        //product2 ?????? ??????
        saveReview(2F, "??????", productBuyer, product2);
        saveReview(2F, "??????", productBuyer, product2);
        saveReview(2F, "??????", productBuyer, product2);

        //product3 ?????? ??????
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);

        //product4 ?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????

        LocalDateTime endDate = LocalDateTime.now(); // ??????
        LocalDateTime startDate = endDate.minusMonths(1); // ?????????

        List<Long> result = productRepository.findProductIdByReviewCountDesc(offset, limit, startDate, endDate);

        //then
        // 1?????? ~ ??????
        assertThat(result.size()).isEqualTo(3);
        // ?????? ????????? product1??? product2?????? ??????
        assertThat(result).containsExactly(product3.getId(), product1.getId(), product2.getId());

    }

    @Test
    @DisplayName("?????? ?????????????????? ?????? ?????? ?????? ???????????? productId, ?????? ?????? ?????? + ?????? ????????? ????????? ?????? ?????? ?????? ????????????")
    void reviewAvgDesc4() throws Exception {
        //given
        int limit = 5;
        int offset = 0;

        //when
        List<Product> findProducts = productRepository.findAll();

        Product product1 = findProducts.get(0);
        Product product2 = findProducts.get(1);
        Product product3 = findProducts.get(2);
        Product product4 = findProducts.get(3);

        Address buyerAddress = createAddress("1111", "????????????3", 11111, "3???4???");
        Buyer productBuyer = createBuyer("?????????3", "133234", "133234", "19990512", "sdfw67f@mae.com", "010-3774-5555", buyerAddress);
        em.persist(productBuyer);

        //product1 ?????? ??????
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);
        saveReview(2F, "??????", productBuyer, product1);

        //product2 ?????? ??????
        saveReview(2F, "??????", productBuyer, product2);
        saveReview(2F, "??????", productBuyer, product2);
        saveReview(2F, "??????", productBuyer, product2);
        saveReview(2F, "??????", productBuyer, product2);

        //product3 ?????? ??????
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);
        saveReview(4F, "??????", productBuyer, product3);

        //product4 ?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????
        saveReview(5F, "??????", productBuyer, product4, LocalDateTime.now().minusMonths(2)); // 2?????? ??????

        LocalDateTime endDate = LocalDateTime.now(); // ??????
        LocalDateTime startDate = endDate.minusMonths(3); // 3??????

        List<Long> result = productRepository.findProductIdByReviewCountDesc(offset, limit, startDate, endDate);

        //then
        // 3?????? ~ ??????
        assertThat(result.size()).isEqualTo(4);
        // product2??? product1?????? ????????? ?????????
        assertThat(result).containsExactly(product4.getId(), product3.getId(), product2.getId(), product1.getId());

    }

    public void saveReview(Float rate, String content, User user, Product product, LocalDateTime createdDate) {
        Review review = Review.builder()
                .user(user)
                .product(product)
                .content(content)
                .rate(rate)
                .build();

        reviewRepository.save(review);
        reviewRepository.findById(review.getId()).orElseThrow(() -> new NoSuchElementException("????????? ???????????? ????????????."))
                .changeCreatedDate(createdDate);
    }

    public void saveReview(Float rate, String content, User user, Product product) {
        Review review = Review.builder()
                .user(user)
                .product(product)
                .content(content)
                .rate(rate)
                .build();

        reviewRepository.save(review);
    }

    private void changeOrderDate(Long orderId, LocalDateTime changeDate) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException());
        order.changeCreatedDate(changeDate);
    }

    private Long orderProduct(Long userId, Long productId, Address address, int count) {
        OrderProductParamDTO param = new OrderProductParamDTO(count, productId);
        List<OrderProductParamDTO> params = List.of(param);
        return orderService.order(userId, params, address);
    }

    private KindGrade createKindGrade(Long kindGradeId) {
        return kindGradeRepository.findById(kindGradeId).orElseThrow(() -> new IllegalArgumentException("????????? ?????????????????????"));
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
}