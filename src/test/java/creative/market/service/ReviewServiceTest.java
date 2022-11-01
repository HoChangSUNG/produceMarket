package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.Review;
import creative.market.domain.category.KindGrade;
import creative.market.domain.order.OrderProduct;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.ReviewRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.user.BuyerRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.repository.user.UserRepository;
import creative.market.service.dto.OrderProductParamDTO;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.ReviewReq;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    KindGradeRepository kindGradeRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderProductRepository orderProductRepository;
    @Autowired
    BuyerRepository buyerRepository;
    @Autowired
    EntityManager em;

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
        Product product5 = getProduct("상품3", 40000, "상품입니다5", 433L, productOwner2);

        OrderProductParamDTO orderProductParam1 = new OrderProductParamDTO(5, product1.getId());
        OrderProductParamDTO orderProductParam2 = new OrderProductParamDTO(2, product2.getId());
        OrderProductParamDTO orderProductParam3 = new OrderProductParamDTO(1, product3.getId());
        OrderProductParamDTO orderProductParam4 = new OrderProductParamDTO(2, product4.getId());
        OrderProductParamDTO orderProductParam5 = new OrderProductParamDTO(1, product5.getId());

        List<OrderProductParamDTO> orderParamList = addOrderProductParamDTO(orderProductParam1, orderProductParam2, orderProductParam3, orderProductParam4, orderProductParam5);

        Address orderAddress = createAddress("1111", "봉사산로", 12345, "동호수");
        orderService.order(productBuyer.getId(), orderParamList, orderAddress);

        Thread.sleep(1100);
        orderService.order(productBuyer.getId(), orderParamList, orderAddress);

    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void saveSuccess() {
        //given
        Product product = productRepository.findAll().get(0);
        Buyer buyer = buyerRepository.findByLoginIdAndPassword("3", "33")
                .orElseThrow(() -> new NoSuchElementException("구매자가 존재하지 않습니다"));
        String content = "리뷰123123";
        Review review = createReview(content);

        //when
        Long reviewId = reviewService.save(review, product.getId(), buyer.getId());

        //then
        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰가 존재하지 않습니다."));
        assertThat(findReview.getId()).isEqualTo(reviewId);
        assertThat(findReview.getProduct().getId()).isEqualTo(product.getId());
        assertThat(findReview.getUser().getId()).isEqualTo(buyer.getId());
        assertThat(findReview.getContent()).isEqualTo(content);
        assertThat(findReview.getRate()).isEqualTo(4.5f);
    }

    @Test
    @DisplayName("리뷰 등록 실패, 리뷰를 중복 등록하는 경우")
    void saveFail() {

    }

    @Test
    @DisplayName("리뷰 등록 실패, 주문내역이 없는 상품 리뷰 작성하는 경우")
    void saveFail2() {

    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteSuccess() {
        //given
        Product product = productRepository.findAll().get(0);
        Buyer buyer = buyerRepository.findByLoginIdAndPassword("3", "33")
                .orElseThrow(() -> new NoSuchElementException("구매자가 존재하지 않습니다"));
        String content = "리뷰123123";
        Review review = createReview(content);
        Long reviewId = reviewService.save(review, product.getId(), buyer.getId());

        //when
        reviewService.delete(reviewId, buyer.getId());
        em.flush();

        //then
        assertThatThrownBy(() -> reviewRepository.findById(reviewId).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateSuccess() {
        //given
        Product product = productRepository.findAll().get(0);
        Buyer buyer = buyerRepository.findByLoginIdAndPassword("3", "33")
                .orElseThrow(() -> new NoSuchElementException("구매자가 존재하지 않습니다"));
        String content = "리뷰123123";
        Review review = createReview(content);
        Long reviewId = reviewService.save(review, product.getId(), buyer.getId());

        ReviewReq reviewReq = new ReviewReq(5.0f , "리뷰수정");

        //when
        reviewService.update(reviewId, reviewReq, buyer.getId());

        //then
        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰가 존재하지 않습니다."));
        assertThat(findReview.getContent()).isEqualTo("리뷰수정");
        assertThat(findReview.getRate()).isEqualTo(5.0f);
    }

    private Review createReview(String content) {
        return Review.builder()
                .rate(4.5f)
                .content(content)
                .build();
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
    private Address createAddress(String jibun, String raod, int zipcode, String detailAddress) {
        return Address.builder().jibun(jibun).road(raod).zipcode(zipcode).detailAddress(detailAddress).build();
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

    private List<OrderProductParamDTO> addOrderProductParamDTO(OrderProductParamDTO... orderProductParamDTOS) {
        List<OrderProductParamDTO> orderParamList = new ArrayList<>();
        Arrays.stream(orderProductParamDTOS).forEach(orderProductParamDTO -> orderParamList.add(orderProductParamDTO));
        return orderParamList;
    }

}