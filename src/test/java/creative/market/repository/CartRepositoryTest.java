package creative.market.repository;

import creative.market.domain.Address;
import creative.market.domain.Cart;
import creative.market.domain.product.Product;
import creative.market.domain.user.Seller;
import creative.market.service.CartService;
import creative.market.service.ProductService;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
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
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
class CartRepositoryTest {

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
    @DisplayName("장바구니 조회")
    void readCartList() throws Exception {
        //given
        Seller seller1 = createSeller("성호창222", createAddress("122", "122", 12222, "2311114"));
        Seller seller2 = createSeller("강대현", createAddress("12222", "133322", 12252, "2동 234호"));
        em.persist(seller1);
        em.persist(seller2);

        int count1 = 4;
        int count2 = 3;

        Product product1 = productRepository.findAll().get(0);
        Product product2 = productRepository.findAll().get(1);

        Long findCartId1 = cartService.register(product1.getId(), count1, seller1.getId());
        Long findCartId2 = cartService.register(product2.getId(), count2, seller1.getId());

        //when
        List<Cart> findCart1 = cartRepository.findByUserIdFetchJoinProductAndKind(seller1.getId());
        List<Cart> findCart2 = cartRepository.findByUserIdFetchJoinProductAndKind(seller2.getId());

        //then
        Assertions.assertThat(findCart1.size()).isEqualTo(2);
        Assertions.assertThat(findCart1).extracting("id").contains(findCartId1, findCartId2);
        Assertions.assertThat(findCart2.size()).isEqualTo(0);

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