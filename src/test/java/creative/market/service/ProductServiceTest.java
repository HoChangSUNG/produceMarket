package creative.market.service;

import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.repository.ProductRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired ProductService productService;
    @Autowired
    ProductRepository productRepository;

    @Test
    @DisplayName("상품 등록시 상품 저장과 상품 이미지 저장이 되는지 확인")
    void createProduct() throws Exception{
        //given
        UploadFileDTO sigImg = new UploadFileDTO("sig.png", "sigStore.png");
        UploadFileDTO ordImg1 = new UploadFileDTO("ord1.png", "ordStore1.png");
        UploadFileDTO ordImg2 = new UploadFileDTO("ord2.png", "ordStore2.png");
        List<UploadFileDTO> ordinalImg = new ArrayList<>();
        ordinalImg.add(ordImg1);
        ordinalImg.add(ordImg2);
        RegisterProductDTO registerProductDTO = new RegisterProductDTO(432L, "쌀팔기", 10000, "맛있어요", null,sigImg, ordinalImg);

        //when
        Long productId = productService.register(registerProductDTO);
        Product findProduct = productRepository.findById(productId);
        List<ProductImage> findProductImages = findProduct.getProductImages();

        //then
        assertThat(findProduct.getName()).isEqualTo("쌀팔기");
        assertThat(findProduct.getPrice()).isEqualTo(10000);
        assertThat(findProduct.getInfo()).isEqualTo("맛있어요");
        assertThat(findProduct.getKindGrade().getId()).isEqualTo(432L);
        assertThat(findProduct.getProductImages()).containsAll(findProductImages);
    }

    @Test
    @DisplayName("잘못된 kindGradeId가 넘어온 경우")
    void createProductFail() throws Exception{
        //given
        RegisterProductDTO registerProductDTO = new RegisterProductDTO(2L, "쌀팔기", 10000, "맛있어요", null,null, null);

        //when

        //then
        Assertions.assertThatThrownBy(()->productService.register(registerProductDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바른 카테고리가 아닙니다");
    }
}
