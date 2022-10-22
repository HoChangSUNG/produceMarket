package creative.market.repository;

import creative.market.domain.Address;
import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.domain.user.Seller;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.repository.user.SellerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired EntityManager em;
    @Autowired ProductRepository productRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired
    KindGradeRepository kindGradeRepository;

    @BeforeEach
    void before () throws Exception{
        Address address = createAddress("1","1",12,"234");
        Seller seller = createSeller("김현민","loginId1","pw1",address);
        em.persist(seller);

        KindGrade kindGrade1 = createKindGrade(432L);
        ProductImage sigImg1 = createSigImg("sigP1.jpg", "/ssf/sigP1.jpg");
        List<ProductImage> ordinalImgList1 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal1", "/ordinal1.jpg")));
        Product product1 = createProduct("쌀-일반계-상품", 10000, "쌀 맛있어요1", seller, kindGrade1,sigImg1,ordinalImgList1);
        em.persist(product1);

        KindGrade kindGrade2 = createKindGrade(433L);
        ProductImage sigImg2 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList2 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product2 =createProduct( "쌀-일반계-중품", 20000, "쌀 맛있어요2", seller, kindGrade2,sigImg2,ordinalImgList2);
        em.persist(product2);

        KindGrade kindGrade3 = createKindGrade(404L);
        ProductImage sigImg3 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList3 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product3 =createProduct( "쌀-백미-등급없음", 13000, "백미 맛있어요3", seller, kindGrade3,sigImg3,ordinalImgList3);
        em.persist(product3);

        KindGrade kindGrade4 = createKindGrade(474L);
        ProductImage sigImg4 = createSigImg("sigP2.jpg", "/ssf/sigP2.jpg");
        List<ProductImage> ordinalImgList4 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product4 =createProduct( "배추-봄-상품", 50000, "배추 맛있어요3", seller, kindGrade4,sigImg4,ordinalImgList4);
        em.persist(product4);

    }

    private Address createAddress(String jibun, String raod, int zipcode, String detailAddress) {
        return Address.builder().jibun(jibun).road(raod).zipcode(zipcode).detailAddress(detailAddress).build();
    }

    private Seller createSeller(String name, String loginId, String pw, Address address) {
        Seller seller = Seller.builder().name(name).loginId(loginId).password(pw).address(address).build();
        return seller;
    }

    private Product createProduct( String productName, int price, String productInfo, Seller seller, KindGrade kindGrade,ProductImage sigImg,List<ProductImage> ordinalImgList) {
        Product product = Product.builder().name(productName).price(price).info(productInfo).user(seller)
                .kindGrade(kindGrade)
                .ordinalProductImages(new ArrayList<ProductImage>())
                .signatureProductImage(sigImg)
                .ordinalProductImages(ordinalImgList)
                .build();
        em.persist(product);
        return product;
    }

    private ProductImage createProductImage(String imgName,String imgPath,ProductImageType imageType) {
        return ProductImage.builder().name(imgName).path(imgPath).type(imageType).build();
    }

    private ProductImage createSigImg(String imgName,String imgPath) {
        return createProductImage(imgName,imgPath,ProductImageType.SIGNATURE);
    }

    private List<ProductImage> createOrdinalImgList(List<ImageDTO> imageDTOList) {
        return imageDTOList.stream()
                .map(imageDTO -> createProductImage(imageDTO.getName(),imageDTO.getPath(),ProductImageType.ORDINAL))
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class ImageDTO {
        private String name;
        private String path;
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

    @Test
    @DisplayName("카테고리별 상품 평균 가격")
    void priceAvg() throws Exception{
        //given
        Address address = createAddress("12","21",212,"2342222");
        Seller seller = createSeller("성호창","loginId2","pw2",address);
        em.persist(seller);

        KindGrade kindGrade1 = createKindGrade(432L);
        ProductImage sigImg1 = createSigImg("sigP1222.jpg", "/ssf/sigP1222.jpg");
        List<ProductImage> ordinalImgList1 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal11", "/ordinal11.jpg")));
        Product product1 = createProduct("쌀-일반계-상품2", 15000, "쌀 맛있어요2", seller, kindGrade1,sigImg1,ordinalImgList1);
        em.persist(product1);

        KindGrade kindGrade2 = createKindGrade(433L);
        ProductImage sigImg2 = createSigImg("sigP2ㄴㄴ.jpg", "/ss23fㄴ/sig22P2.jpg");
        List<ProductImage> ordinalImgList2 = createOrdinalImgList(Arrays.asList(new ImageDTO("ordinal2", "/ordinal2.jpg")));
        Product product2 =createProduct( "쌀-일반계-중품", 33333, "쌀 맛있어요22", seller, kindGrade2,sigImg2,ordinalImgList2);
        em.persist(product2);
        //when

        Double avg1 = productRepository.findProductAvgPrice(kindGrade1.getId());
        Double avg2 = productRepository.findProductAvgPrice(499L);
        Double avg3 = productRepository.findProductAvgPrice(kindGrade2.getId());


        //then
        Assertions.assertThat(avg1.intValue()).isEqualTo(12500);
        Assertions.assertThat(avg2).isNull(); // 존재하지 않는 kindGradeId 사용
        Assertions.assertThat(avg3.intValue()).isEqualTo(26666);

    }

    private KindGrade createKindGrade(Long kindGradeId) {
        return kindGradeRepository.findById(kindGradeId)
                .orElseThrow(()->new IllegalArgumentException("잘못된 카테고리입니다"));
    }
}