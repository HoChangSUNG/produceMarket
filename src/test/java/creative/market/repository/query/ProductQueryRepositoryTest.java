package creative.market.repository.query;

import creative.market.domain.Address;
import creative.market.domain.product.Product;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.category.KindGradeRepository;
import creative.market.repository.dto.ProductSigSrcAndIdRes;
import creative.market.service.ProductService;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
@Slf4j
class ProductQueryRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    KindGradeRepository kindGradeRepository;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductQueryRepository productQueryRepository;

    @BeforeEach
    void before() {
        Seller seller = createSeller("강대현", "1", "11", "19990112", "sd12fwf@mae.com", "010-3544-4444", createAddress("1111", "봉사산로", 12345, "1동1호"), "상호명1");
        em.persist(seller);

        Buyer buyer = createBuyer("성호창3", "3", "33", "19990512", "sdfw67f@mae.com", "010-3774-5555", createAddress("1111", "봉사산로3", 11111, "3동4호"));
        em.persist(buyer);

        List<UploadFileDTO> ordinalImg = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            UploadFileDTO sigImg = new UploadFileDTO("sig" + i + ".png", "sigStore" + i + ".png");
            UploadFileDTO ordImg1 = new UploadFileDTO("ord1" + i + ".png", "ordStore1" + i + ".png");
            UploadFileDTO ordImg2 = new UploadFileDTO("ord2" + i + ".png", "ordStore2" + i + ".png");
            ordinalImg.add(ordImg1);
            ordinalImg.add(ordImg2);

            RegisterProductDTO registerProductDTO = new RegisterProductDTO(432L, "쌀팔기" + i, 10000 + i, "맛있어요" + i, seller.getId(), sigImg, ordinalImg);
            Long productId = productService.register(registerProductDTO);

            ordinalImg.clear();
        }
    }

    private Address createAddress(String jibun, String road, int zipcode, String detailAddress) {
        return Address.builder()
                .jibun(jibun)
                .road(road)
                .zipcode(zipcode)
                .detailAddress(detailAddress).build();
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

    @Test
    @DisplayName("메인 페이지 전체 상품 최근 등록 순")
    void productListOrderByCreatedDateDesc() throws Exception {
        //given
        int limit = 4;
        //when
        List<ProductSigSrcAndIdRes> result = productQueryRepository.findProductSigImgAndIdByLatestCreatedDate(limit);

        //then
        List<Long> productIds = productRepository.findProductListOrderByCreatedDateDesc(limit).stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        assertThat(result.size()).isEqualTo(limit);
        assertThat(result).extracting("productId").containsExactlyElementsOf(productIds);
    }

}