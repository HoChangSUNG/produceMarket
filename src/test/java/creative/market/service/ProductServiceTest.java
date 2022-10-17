package creative.market.service;

import creative.market.domain.Address;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.user.Seller;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.ProductRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UpdateProductFormReq;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import org.assertj.core.api.Assertions;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;
    @Autowired
    KindGradeRepository kindGradeRepository;
    @Value("${images}")
    private String rootPath;

    @Test
    @DisplayName("상품 등록 성공, 등록시 상품 저장과 상품 이미지 저장이 되는지 확인")
    void createProductSuccess() throws Exception {
        //given
        UploadFileDTO sigImg = new UploadFileDTO("sig.png", "sigStore.png");
        UploadFileDTO ordImg1 = new UploadFileDTO("ord1.png", "ordStore1.png");
        UploadFileDTO ordImg2 = new UploadFileDTO("ord2.png", "ordStore2.png");
        List<UploadFileDTO> ordinalImg = new ArrayList<>();
        ordinalImg.add(ordImg1);
        ordinalImg.add(ordImg2);
        Seller seller = createSeller("강병관", new Address("10", "20", 1, "천안"));
        em.persist(seller);

        RegisterProductDTO registerProductDTO = new RegisterProductDTO(432L, "쌀팔기", 10000, "맛있어요", seller.getId(), sigImg, ordinalImg);

        //when
        Long productId = productService.register(registerProductDTO);
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("카테고리가 존재하지 않습니다"));
        List<ProductImage> findProductImages = findProduct.getProductImages();

        //then
        assertThat(findProduct.getName()).isEqualTo("쌀팔기");
        assertThat(findProduct.getPrice()).isEqualTo(10000);
        assertThat(findProduct.getInfo()).isEqualTo("맛있어요");
        assertThat(findProduct.getKindGrade().getId()).isEqualTo(432L);
        assertThat(findProduct.getProductImages()).containsAll(findProductImages);
    }

    @Test
    @DisplayName("상품 등록 실패, 등록시 잘못된 kindGradeId가 넘어온 경우")
    void createProductFail() throws Exception {
        //given
        Seller seller = createSeller("강병관", new Address("10", "20", 1, "천안"));
        em.persist(seller);
        RegisterProductDTO registerProductDTO = new RegisterProductDTO(2L, "쌀팔기", 10000, "맛있어요", seller.getId(), null, null);

        //when

        //then
        assertThatThrownBy(() -> productService.register(registerProductDTO))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("올바른 카테고리가 아닙니다");
    }

    @Test
    @DisplayName("상품 등록 실패, 등록시 존재하지 않는 sellerId가 넘어온 경우")
    void createProductFail2() throws Exception {
        //given
        Seller seller = createSeller("강병관", new Address("10", "20", 1, "천안"));
        em.persist(seller);
        RegisterProductDTO registerProductDTO = new RegisterProductDTO(2L, "쌀팔기", 10000, "맛있어요", seller.getId() + 1, null, null);

        //when

        //then
        assertThatThrownBy(() -> productService.register(registerProductDTO))
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("사용자가 존재하지 않습니다");
    }


    @Test
    @DisplayName("상품 내용 수정 성공, 사진은 기존 사진 그대로 사용")
    void updateProductSuccess1() throws Exception {
        //given
        //사진 생성
        MockMultipartFile signatureMultipart = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");

        List<MultipartFile> ordinalMultipartList = new ArrayList<>();
        ordinalMultipartList.add(ordinalMultipart);

        //사진 저장
        UploadFileDTO sigUploadFile = FileStoreUtils.storeFile(signatureMultipart, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile = FileStoreUtils.storeFiles(ordinalMultipartList, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller.getId(), sigUploadFile, ordinalUploadFile);
        Long productId = productService.register(registerProduct);
        em.flush();
        em.clear();

        //when
        UpdateProductFormReq updateProductForm = new UpdateProductFormReq(433L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", ordinalMultipartList, signatureMultipart);
        Long updateProductId = productService.update(productId, updateProductForm, seller.getId());

        //then
        Product updateProduct = getProduct(updateProductId);
        String[] resultOrdinalName = getUploadImageName(ordinalUploadFile);
        String[] resultOrdinalPath = getImagePath(ordinalUploadFile);

        // 상품 내용 수정 확인
        assertThat(updateProduct.getKindGrade().getId()).isEqualTo(433L);
        assertThat(updateProduct.getName()).isEqualTo("쌀-일반계-상품");
        assertThat(updateProduct.getPrice()).isEqualTo(10000);
        assertThat(updateProduct.getInfo()).isEqualTo("쌀 맛있어요1");

        // 사진 수정 확인(기존 사진 그대로 사용)
        assertThat(updateProduct.getOrdinalProductImage()).extracting("name").contains(resultOrdinalName);
        assertThat(updateProduct.getSignatureProductImage().getName()).isEqualTo(sigUploadFile.getUploadFileName());
        assertThat(updateProduct.getOrdinalProductImage()).extracting("path").contains(resultOrdinalPath);
        assertThat(updateProduct.getSignatureProductImage().getPath()).isEqualTo(FileSubPath.PRODUCT_PATH+sigUploadFile.getStoreFileName());

        // 저장된 사진 제거
        updateProduct.getProductImages().forEach(productImage -> deleteFile(productImage.getPath()));

    }

    @Test
    @DisplayName("상품 내용 수정 성공, 상품 내용은 수정하지 않고 사진만 수정")
    void updateProductSuccess2() throws Exception {
        //given
        //사진 생성
        MockMultipartFile signatureMultipart1 = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart1 = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");
        MockMultipartFile ordinalMultipart2 = createMultipart("image", "감자.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감자.png");

        List<MultipartFile> ordinalMultipartList = new ArrayList<>();
        ordinalMultipartList.add(ordinalMultipart1);
        ordinalMultipartList.add(ordinalMultipart2);

        //사진 저장
        UploadFileDTO sigUploadFileBefore = FileStoreUtils.storeFile(signatureMultipart1, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFileBefore = FileStoreUtils.storeFiles(ordinalMultipartList, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller.getId(), sigUploadFileBefore, ordinalUploadFileBefore);
        Long productId = productService.register(registerProduct);
        em.flush();
        em.clear();

        //when
        // 변경할 사진 생성
        // 대표 사진 : 쌀.png -> 고구마.png
        // 일반 사진 : [감귤.png, 감자.png] -> [감귤.png, 녹두.png]
        MockMultipartFile signatureMultipartAfter = createMultipart("image", "고구마.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "고구마.png");
        MockMultipartFile ordinalMultipartAfter1 = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");
        MockMultipartFile ordinalMultipartAfter2 = createMultipart("image", "녹두.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "녹두.png");

        List<MultipartFile> ordinalMultipartListAfter = new ArrayList<>();
        ordinalMultipartListAfter.add(ordinalMultipartAfter1);
        ordinalMultipartListAfter.add(ordinalMultipartAfter2);
        UpdateProductFormReq updateProductForm = new UpdateProductFormReq(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", ordinalMultipartListAfter, signatureMultipartAfter);

        Long updateProductId = productService.update(productId, updateProductForm, seller.getId());

        //then
        Product updateProduct = getProduct(updateProductId);

        UploadFileDTO deletedImg = ordinalUploadFileBefore.stream().filter(ordinalUpload -> ordinalUpload.getUploadFileName().equals("감자.png")).collect(Collectors.toList()).get(0);
        File newSigFile = new File(FileStoreUtils.getFullPath(rootPath, (updateProduct.getSignatureProductImage().getPath())));
        File beforeUpdateSigFile = new File(FileStoreUtils.getFullPath(rootPath,FileSubPath.PRODUCT_PATH + sigUploadFileBefore.getStoreFileName()));

        File deletedUOrdinalFile = new File(FileStoreUtils.getFullPath(rootPath,FileSubPath.PRODUCT_PATH + deletedImg.getStoreFileName()));
        File newUOrdinalFile1 = new File(FileStoreUtils.getFullPath(rootPath, updateProduct.getOrdinalProductImage().get(0).getPath()));
        File newUOrdinalFile2 = new File(FileStoreUtils.getFullPath(rootPath,updateProduct.getOrdinalProductImage().get(1).getPath()));

        // 상품 내용 수정 확인
        assertThat(updateProduct.getKindGrade().getId()).isEqualTo(432L);
        assertThat(updateProduct.getName()).isEqualTo("쌀-일반계-상품");
        assertThat(updateProduct.getPrice()).isEqualTo(10000);
        assertThat(updateProduct.getInfo()).isEqualTo("쌀 맛있어요1");


        //signature 변경된 사진 확인
        assertThat(updateProduct.getSignatureProductImage().getName()).isEqualTo(signatureMultipartAfter.getOriginalFilename());
        Assertions.assertThat(newSigFile.exists()).isTrue(); // 고구마.png
        Assertions.assertThat(beforeUpdateSigFile.exists()).isFalse();// 쌀.png

        // 일반 사진 수정 확인
        assertThat(updateProduct.getOrdinalProductImage().size()).isEqualTo(2);
        Assertions.assertThat(deletedUOrdinalFile.exists()).isFalse(); // 감자.png
        Assertions.assertThat(newUOrdinalFile1.exists()).isTrue();
        Assertions.assertThat(newUOrdinalFile2.exists()).isTrue();


        // 저장된 사진 제거
        updateProduct.getProductImages().forEach(productImage -> deleteFile(productImage.getPath()));

    }

    @Test
    @DisplayName("상품 내용 수정 실패, 변경할 상품이 존재하지 않는 경우")
    void updateProductFail1() throws Exception {
        //given
        //사진 생성
        MockMultipartFile signatureMultipart = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");

        List<MultipartFile> ordinalMultipartList = new ArrayList<>();
        ordinalMultipartList.add(ordinalMultipart);

        //사진 저장
        UploadFileDTO sigUploadFile = FileStoreUtils.storeFile(signatureMultipart, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile = FileStoreUtils.storeFiles(ordinalMultipartList, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller.getId(), sigUploadFile, ordinalUploadFile);
        Long productId = productService.register(registerProduct);
        em.flush();
        em.clear();

        //when
        UpdateProductFormReq updateProductForm = new UpdateProductFormReq(433L, "쌀-일반계-상품", 10000, "쌀 맛있어222요1", ordinalMultipartList, signatureMultipart);

        //then
        Assertions.assertThatThrownBy(()->productService.update(productId+1, updateProductForm, seller.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("상품이 존재하지 않습니다.");

        // 저장된 사진 제거
        Product beforeUpdateProduct = getProduct(productId);
        beforeUpdateProduct.getProductImages().forEach(productImage -> deleteFile(productImage.getPath()));

    }

    @Test
    @DisplayName("상품 내용 수정 실패, 변경할 카테고리가 존재하지 않는 경우")
    void updateProductFail2() throws Exception {
        //given
        //사진 생성
        MockMultipartFile signatureMultipart = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");

        List<MultipartFile> ordinalMultipartList = new ArrayList<>();
        ordinalMultipartList.add(ordinalMultipart);

        //사진 저장
        UploadFileDTO sigUploadFile = FileStoreUtils.storeFile(signatureMultipart, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile = FileStoreUtils.storeFiles(ordinalMultipartList, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller.getId(), sigUploadFile, ordinalUploadFile);
        Long productId = productService.register(registerProduct);
        em.flush();
        em.clear();

        //when
        UpdateProductFormReq updateProductForm = new UpdateProductFormReq(1L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", ordinalMultipartList, signatureMultipart);

        //then
        Assertions.assertThatThrownBy(()->productService.update(productId, updateProductForm, seller.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 카테고리입니다.");

        // 저장된 사진 제거
        Product beforeUpdateProduct = getProduct(productId);
        beforeUpdateProduct.getProductImages().forEach(productImage -> deleteFile(productImage.getPath()));

    }

    @Test
    @DisplayName("상품 내용 수정 실패, 상품을 등록한 판매자가 아닌 다른 판매자가 상품 내용을 수정할 경우")
    void updateProductFail3() throws Exception {
        //given
        //사진 생성
        MockMultipartFile signatureMultipart = createMultipart("image", "쌀.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");
        MockMultipartFile ordinalMultipart = createMultipart("image", "감귤.png", "image/png", rootPath, FileSubPath.GRADE_CRITERIA_PATH + "감귤.png");

        List<MultipartFile> ordinalMultipartList = new ArrayList<>();
        ordinalMultipartList.add(ordinalMultipart);

        //사진 저장
        UploadFileDTO sigUploadFile = FileStoreUtils.storeFile(signatureMultipart, rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalUploadFile = FileStoreUtils.storeFiles(ordinalMultipartList, rootPath, FileSubPath.PRODUCT_PATH);

        // 판매자 등록
        Seller seller = createSeller("김현민", createAddress("1", "1", 12, "234"));
        em.persist(seller);

        //상품 등록
        RegisterProductDTO registerProduct = new RegisterProductDTO(432L, "쌀-일반계-상품", 10000, "쌀 맛있어요1", seller.getId(), sigUploadFile, ordinalUploadFile);
        Long productId = productService.register(registerProduct);
        em.flush();
        em.clear();

        //when
        UpdateProductFormReq updateProductForm = new UpdateProductFormReq(433L, "쌀-일반22계-상품", 100040, "쌀 맛65있어요1", ordinalMultipartList, signatureMultipart);

        //then
        Assertions.assertThatThrownBy(()->productService.update(productId, updateProductForm, seller.getId()+1))
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("접근 권한이 없습니다.");

        // 저장된 사진 제거
        Product beforeUpdateProduct = getProduct(productId);
        beforeUpdateProduct.getProductImages().forEach(productImage -> deleteFile(productImage.getPath()));

    }

    private MockMultipartFile createMultipart(String name, String originalFileName, String contentType, String rootPath, String subPath) throws IOException {
        return new MockMultipartFile(name, originalFileName, contentType, new FileInputStream(FileStoreUtils.getFullPath(rootPath, subPath)));
    }

    private void deleteFile(String path) {
        String storeFilePath = FileStoreUtils.getFullPath(rootPath, path);
        File file = new File(storeFilePath);
        file.delete();
    }

    private Address createAddress(String jibun, String raod, int zipcode, String detailAddress) {
        return Address.builder().jibun(jibun).road(raod).zipcode(zipcode).detailAddress(detailAddress).build();
    }

    private Seller createSeller(String name, Address address) {
        Seller seller = Seller.builder()
                .name(name)
                .address(address).build();
        return seller;
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다"));
    }

    private String[] getUploadImageName(List<UploadFileDTO> ordinalUploadFile) {
        return ordinalUploadFile.stream()
                .map(UploadFileDTO::getUploadFileName).toArray(String[]::new);
    }

    private String[] getImagePath(List<UploadFileDTO> ordinalUploadFile) {
        return ordinalUploadFile.stream()
                .map(ordinal -> FileSubPath.PRODUCT_PATH + ordinal.getStoreFileName()).toArray(String[]::new);
    }
}
