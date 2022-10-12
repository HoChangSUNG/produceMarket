package creative.market.repository;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.domain.user.Seller;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.repository.user.SellerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired EntityManager em;
    @Autowired ProductRepository productRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired KindGradeRepository kindGradeRepository;

    @BeforeEach
    void before () throws Exception{
        Address address = Address.builder().jibun("1").road("1").zipcode(12).detailAddress("234").build();
        Seller seller = getSeller("김현민","loginId1","pw1",address);

        KindGrade kindGrade1 = createKindGrade(432L);
        createProduct("대표사진.jpg", "/sef.jpg", "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller, kindGrade1);

        KindGrade kindGrade2 = createKindGrade(433L);
        createProduct("대표사진2.jpg", "/sef.jpg", "쌀-일반계-중품", 20000, "쌀 맛있어요2", seller, kindGrade2);

        KindGrade kindGrade3 = createKindGrade(404L);
        createProduct("대표사진3.jpg", "/seㄴ3f.jpg", "쌀-백미-등급없음", 13000, "백미 맛있어요3", seller, kindGrade3);

        KindGrade kindGrade4 = createKindGrade(474L);
        createProduct("대표사진4.jpg", "/seㄴ4f.jpg", "배추-봄-상품", 50000, "배추 맛있어요3", seller, kindGrade4);
    }

    private Seller getSeller(String name,String loginId, String pw,Address address) {
        Seller seller = Seller.builder().name(name).loginId(loginId).password(pw).address(address).build();
        em.persist(seller);
        return seller;
    }

    private Product createProduct(String sigImgName, String sigImgPath, String productName, int price, String productInfo, Seller seller, KindGrade kindGrade) {
        ProductImage sigImg = ProductImage.builder().name(sigImgName).path(sigImgPath).type(ProductImageType.SIGNATURE).build();
        Product product = Product.builder().name(productName).price(price).info(productInfo).user(seller)
                .kindGrade(kindGrade)
                .ordinalProductImages(new ArrayList<ProductImage>())
                .signatureProductImage(sigImg)
                .build();
        em.persist(product);
        return product;
    }

    @Test
    @DisplayName("kindGradeId로 조회")
    void findByKindGradeId() throws Exception{
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "latest", null, null, null, 432L);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq("쌀", "latest", null, null, null, 432L);
        ProductSearchConditionReq condition3 = new ProductSearchConditionReq(null, "latest", null, null, null, 475L);
        ProductSearchConditionReq condition4 = new ProductSearchConditionReq("쌀", "latest", null, null, null, 474L);

        System.out.println(condition2.getProductName());
        //when
        List<Product> result1 = productRepository.findProductByCondition(condition1);
        List<Product> result2 = productRepository.findProductByCondition(condition2);
        List<Product> result3 = productRepository.findProductByCondition(condition3);
        List<Product> result4 = productRepository.findProductByCondition(condition4);


        //then
        Assertions.assertThat(result1.size()).isEqualTo(1);
        Assertions.assertThat(result1).extracting("name").contains("쌀-일반계-상품");

        Assertions.assertThat(result2.size()).isEqualTo(1);
        Assertions.assertThat(result2).extracting("name").contains("쌀-일반계-상품");

        Assertions.assertThat(result3.size()).isEqualTo(0); // 없는 상품 조회

        Assertions.assertThat(result4.size()).isEqualTo(0); // 존재하지만 상품 이름이 맞지 않는 경우


    }

    @Test
    @DisplayName("kindId로 조회")
    void findByKindId() throws Exception{
        //given
        ProductSearchConditionReq condition = new ProductSearchConditionReq(null, "latest", null, null, 1613L, null);

        //when
        List<Product> result = productRepository.findProductByCondition(condition);

        //then
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result).extracting("name").contains("쌀-일반계-상품","쌀-일반계-중품");
    }

    @Test
    @DisplayName("itemCode로 조회, 가격순")
    void findByItemCode() throws Exception{
        //given
        ProductSearchConditionReq condition = new ProductSearchConditionReq(null, "price", null, 111, null, null);

        //when
        List<Product> result = productRepository.findProductByCondition(condition);


        //then
        Assertions.assertThat(result.size()).isEqualTo(3);
        Assertions.assertThat(result).extracting("name").containsExactly("쌀-일반계-중품","쌀-백미-등급없음","쌀-일반계-상품");
        Assertions.assertThat(result).extracting("price").containsExactly(20000,13000,10000); // 가격 순

    }

    @Test
    @DisplayName("itemCategoryCode로 조회, 가격순")
    void findByItemCategoryCode() throws Exception{
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "price", 100, null, null, null);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq(null, "price", 200, null, null, null);

        //when
        List<Product> result1 = productRepository.findProductByCondition(condition1);
        List<Product> result2 = productRepository.findProductByCondition(condition2);


        //then
        Assertions.assertThat(result1.size()).isEqualTo(3);
        Assertions.assertThat(result1).extracting("name").contains("쌀-일반계-상품","쌀-일반계-중품","쌀-백미-등급없음");// 식량작물 카테고리 상품

        Assertions.assertThat(result2.size()).isEqualTo(1);
        Assertions.assertThat(result2).extracting("name").contains("배추-봄-상품");// 채소류 카테고리 상품
    }

    @Test
    @DisplayName("모든 상품 대상 조회, 가격순")
    void findByAll() throws Exception{
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq(null, "price", null, null, null, null);
        ProductSearchConditionReq condition2 = new ProductSearchConditionReq("", "price", null, null, null, null);

        //when
        List<Product> result1 = productRepository.findProductByCondition(condition1);
        List<Product> result2 = productRepository.findProductByCondition(condition2);


        //then
        Assertions.assertThat(result1.size()).isEqualTo(4);
        Assertions.assertThat(result1).extracting("name").containsExactly("배추-봄-상품","쌀-일반계-중품","쌀-백미-등급없음","쌀-일반계-상품");// 모든 상품 가격순
        Assertions.assertThat(result1).extracting("price").containsExactly(50000,20000,13000,10000);

        Assertions.assertThat(result2.size()).isEqualTo(4);
        Assertions.assertThat(result2).extracting("name").containsExactly("배추-봄-상품","쌀-일반계-중품","쌀-백미-등급없음","쌀-일반계-상품");// 모든 상품 가격순
        Assertions.assertThat(result2).extracting("price").containsExactly(50000,20000,13000,10000);
    }

    @Test
    @DisplayName("모든 상품 대상 제목 조회")
    void findByAllWithName() throws Exception{
        //given
        ProductSearchConditionReq condition1 = new ProductSearchConditionReq("쌀", "price", null, null, null, null);

        //when
        List<Product> result1 = productRepository.findProductByCondition(condition1);

        //then
        Assertions.assertThat(result1.size()).isEqualTo(3);
        Assertions.assertThat(result1).extracting("name").contains("쌀-일반계-중품","쌀-백미-등급없음","쌀-일반계-상품");// 모든 상품 가격순

    }

    private KindGrade createKindGrade(Long kindGradeId) {
        return kindGradeRepository.findById(kindGradeId)
                .orElseThrow(()->new IllegalArgumentException("잘못된 카테고리입니다"));
    }
}