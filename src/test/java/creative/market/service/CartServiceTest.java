package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.Cart;
import creative.market.domain.product.Product;
import creative.market.domain.user.Seller;
import creative.market.domain.user.User;
import creative.market.exception.DuplicateException;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.CartRepository;
import creative.market.repository.ProductRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class CartServiceTest {

    @Value("${images}")
    private String rootPath;
    @Autowired
    EntityManager em;
    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CartService cartService;
    @Autowired
    CartRepository cartRepository;

    @BeforeEach
    private void before() throws IOException {
        //사진 생성
        MockMultipartFile signatureMultipart1 = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart1 = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");
        List<MultipartFile> ordinalMultipartList1 = new ArrayList<>();
        ordinalMultipartList1.add(ordinalMultipart1);

        MockMultipartFile signatureMultipart2 = createMultipart("image", "깻잎.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "깻잎.png");
        MockMultipartFile ordinalMultipart2 = createMultipart("image", "녹두.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "녹두.png");
        List<MultipartFile> ordinalMultipartList2 = new ArrayList<>();
        ordinalMultipartList2.add(ordinalMultipart2);

        //사진 저장
        UploadFileDTO sigUploadFile1 = FileStoreUtils.storeFile(signatureMultipart1, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile1 = FileStoreUtils.storeFiles(ordinalMultipartList1, rootPath, FileSubPath.PRODUCT_PATH);

        UploadFileDTO sigUploadFile2 = FileStoreUtils.storeFile(signatureMultipart2, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile2 = FileStoreUtils.storeFiles(ordinalMultipartList2, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct1 = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "상품 쌀 맛있어요1", seller.getId(), sigUploadFile1, ordinalUploadFile1);
        RegisterProductDTO registerProduct2 = new RegisterProductDTO(433L, "쌀-일반계-중품", 10000, "중품 쌀 맛있어요1", seller.getId(), sigUploadFile2, ordinalUploadFile2);

        Long productId1 = productService.register(registerProduct1);
        Long productId2 = productService.register(registerProduct2);
    }

    @AfterEach
    private void afterEach() {
        // 저장된 사진 제거
        List<Product> findProducts = productRepository.findAll();
        findProducts.forEach(product -> product.getProductImages()
                .forEach(productImage -> deleteFile(productImage.getPath())));
    }

    @Test
    @DisplayName("장바구니 등록 성공")
    void registerSuccess() throws Exception {
        //given
        Product product = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count = 4;

        //when
        Long findCartId = cartService.register(product.getId(), count, seller.getId());

        //then
        Cart findCart = cartRepository.findById(findCartId).orElseThrow(() -> new NoSuchElementException("장바구니가 존재하지 않습니다."));
        assertThat(findCart.getUser().getId()).isEqualTo(seller.getId());
        assertThat(findCart.getProduct().getId()).isEqualTo(product.getId());
        assertThat(findCart.getCount()).isEqualTo(count);
    }

    @Test
    @DisplayName("장바구니 등록 실패, 상품이 존재하지 않는 경우")
    void registerFail1() throws Exception {
        //given
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count = 4;

        //then
        assertThatThrownBy(() -> cartService.register(-1L, count, seller.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("상품이 존재하지 않습니다.");

    }

    @Test
    @DisplayName("장바구니 등록 실패, 유저가 존재하지 않는 경우")
    void registerFail2() throws Exception {
        //given
        Product product = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count = 4;

        //then
        assertThatThrownBy(() -> cartService.register(product.getId(), count, seller.getId() + 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원이 존재하지 않습니다.");

    }

    @Test
    @DisplayName("장바구니 등록 실패, 이미 등록된 상품을 장바구니에 등록하는 경우")
    void registerFail3() throws Exception {
        //given
        Product product = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count = 4;

        //when
        Long findCartId = cartService.register(product.getId(), count, seller.getId());

        //then
        //이미 등록된 상품을 다시 장바구니에 저장
        assertThatThrownBy(() -> cartService.register(product.getId(), count, seller.getId()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 장바구니에 해당 상품이 존재합니다.");
    }

    @Test
    @DisplayName("장바구니 등록 실패, 본인이 등록한 상품을 장바구니에 등록하려는 경우")
    void registerFail4() throws Exception {
        //given
        Product product = productRepository.findAll().get(0);
        User seller = product.getUser();
        int count = 4;

        //when

        //then
        assertThatThrownBy(() -> cartService.register(product.getId(), count, seller.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 등록한 상품은 구매할 수 없습니다.");
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteSuccess() throws Exception {
        //given
        Product product1 = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count1 = 4;

        Product product2 = productRepository.findAll().get(1);
        int count2 = 3;

        Long findCartId1 = cartService.register(product1.getId(), count1, seller.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller.getId());

        //when
        cartService.delete(findCartId1, seller.getId());

        //then
        List<Cart> findCartList = cartRepository.findByUserIdFetchJoinProductAndKind(seller.getId());
        assertThat(findCartList.size()).isEqualTo(1);
        assertThat(findCartList).extracting("id").contains(findCartId2);
        assertThat(findCartList).extracting("id").doesNotContain(findCartId1);
    }

    @Test
    @DisplayName("장바구니 삭제 성공, 원하는 productId를 가지는 장바구니만 삭제")
    void deleteCartByProductIds() throws Exception {
        //given
        Product product1 = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count1 = 4;

        Product product2 = productRepository.findAll().get(1);
        int count2 = 3;

        Long findCartId1 = cartService.register(product1.getId(), count1, seller.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller.getId());

        List<Long> deleteProductIds = List.of(product1.getId());

        //when
        cartService.deleteCartListByProductIds(deleteProductIds, seller.getId());

        //then
        List<Cart> findCartList = cartRepository.findByUserIdFetchJoinProductAndKind(seller.getId());
        assertThat(findCartList.size()).isEqualTo(1);
        assertThat(findCartList).extracting("product").contains(product2);
        assertThat(findCartList).extracting("product").doesNotContain(product1);
    }

    @Test
    @DisplayName("장바구니 삭제 실패, 장바구니에 존재하지 않는 상품 삭제시")
    void deleteFail1() throws Exception {
        //given
        Product product1 = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count1 = 4;

        Product product2 = productRepository.findAll().get(1);
        int count2 = 3;

        Long findCartId1 = cartService.register(product1.getId(), count1, seller.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller.getId());

        //when

        //then
        assertThatThrownBy(() -> cartService.delete(-1L, seller.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("장바구니에 상품이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("장바구니 삭제 실패, 존재하지 않는 유저로 접근시")
    void deleteFail2() throws Exception {
        //given
        Product product1 = productRepository.findAll().get(0);
        Seller seller = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        em.persist(seller);
        int count1 = 4;

        Product product2 = productRepository.findAll().get(1);
        int count2 = 3;

        Long findCartId1 = cartService.register(product1.getId(), count1, seller.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller.getId());

        //when

        //then
        assertThatThrownBy(() -> cartService.delete(findCartId2, seller.getId() + 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }

    @Test
    @DisplayName("장바구니 삭제 실패, 삭제 권한이 없는 유저가 삭제 시도하는 경우")
    void deleteFail3() throws Exception {
        //given
        Product product1 = productRepository.findAll().get(0);
        Seller seller1 = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        Seller seller2 = createSeller("강대현", createAddress("12222", "133322", 12252, "2동 234호"));
        em.persist(seller1);
        em.persist(seller2);
        int count1 = 4;

        Product product2 = productRepository.findAll().get(1);
        int count2 = 3;

        Long findCartId1 = cartService.register(product1.getId(), count1, seller1.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller1.getId());

        //when

        //then
        assertThatThrownBy(() -> cartService.delete(findCartId2, seller2.getId()))
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }

    private Seller createSeller(String name, Address address) {
        Seller seller = Seller.builder()
                .name(name)
                .address(address).build();
        return seller;
    }

    private Address createAddress(String jibun, String raod, int zipcode, String detailAddress) {
        return Address.builder().jibun(jibun).road(raod).zipcode(zipcode).detailAddress(detailAddress).build();
    }

    private MockMultipartFile createMultipart(String name, String originalFileName, String contentType, String rootPath, String subPath) throws IOException {
        return new MockMultipartFile(name, originalFileName, contentType, new FileInputStream(FileStoreUtils.getFullPath(rootPath, subPath)));
    }

    private void deleteFile(String path) {
        String storeFilePath = FileStoreUtils.getFullPath(rootPath, path);
        File file = new File(storeFilePath);
        file.delete();
    }

}