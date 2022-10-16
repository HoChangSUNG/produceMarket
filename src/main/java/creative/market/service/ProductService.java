package creative.market.service;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.domain.user.User;
import creative.market.exception.FileSaveException;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.ProductRepository;
import creative.market.repository.user.UserRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UpdateProductFormReq;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final KindGradeRepository kindGradeRepository;
    private final UserRepository userRepository;
    @Value("${images}")
    private String rootPath;

    // 상품 등록
    @Transactional
    public Long register(RegisterProductDTO registerProductDTO) {
        //판매자 존재 체크
        User user = userRepository.findById(registerProductDTO.getSellerId())
                .orElseThrow(() -> new LoginAuthenticationException("사용자가 존재하지 않습니다"));

        // 카테고리가 존재하는지 체크
        KindGrade kindGrade = kindGradeRepository.findById(registerProductDTO.getKindGradeId())
                .orElseThrow(() -> new NoSuchElementException("올바른 카테고리가 아닙니다"));

        // 일반 사진 생성
        List<UploadFileDTO> ordinalImages = registerProductDTO.getOrdinalImg();
        List<ProductImage> ordinalProductImages = createProductOrdinalImages(ordinalImages);

        // 대표 사진 생성
        ProductImage sigProductImage = createProductImage(registerProductDTO.getSigImg(), ProductImageType.SIGNATURE);

        //상품 저장
        Product product = Product.builder()
                .name(registerProductDTO.getName())
                .price(registerProductDTO.getPrice())
                .info(registerProductDTO.getInfo())
                .kindGrade(kindGrade)
                .user(user)
                .ordinalProductImages(ordinalProductImages)
                .signatureProductImage(sigProductImage)
                .build();

        productRepository.save(product);
        return product.getId();
    }

    private List<ProductImage> createProductOrdinalImages(List<UploadFileDTO> ordinalImages) {
        return ordinalImages.stream()
                .map(ordinalImage -> createProductImage(ordinalImage, ProductImageType.ORDINAL))
                .collect(Collectors.toList());
    }

    private ProductImage createProductImage(UploadFileDTO ordinalImage, ProductImageType type) {
        return ProductImage.builder().name(ordinalImage.getUploadFileName())
                .path(FileSubPath.PRODUCT_PATH + ordinalImage.getStoreFileName())
                .type(type)
                .build();
    }

    public Long update(Long productId, UpdateProductFormReq updateFormReq, Long userId) { // 상품 수정
        // product 있는지 확인
        Product findProduct = productRepository.findByIdFetchJoinSellerAndKind(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));

        // kindGrade 있는지 확인
        KindGrade findKindGrade = kindGradeRepository.findById(updateFormReq.getKindGradeId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        // 상품을 등록한 사람인지
        if (!findProduct.getUser().getId().equals(userId)) {
            throw new LoginAuthenticationException("변경 권한이 없습니다.");
        }

        //상푸 수정
        findProduct.changeProduct(findKindGrade, updateFormReq.getName(), updateFormReq.getPrice(), updateFormReq.getInfo());

        //사진 수정
        changeSignatureImage(findProduct, updateFormReq.getSigImg());
        changeOrdinalImage(findProduct, updateFormReq.getImg());


        return findProduct.getId();
    }

    private void changeSignatureImage(Product product, MultipartFile sigImg) {
        try {
            ProductImage signatureProductImage = product.getSignatureProductImage();
            String existingName = signatureProductImage.getName();// 기존 대표 이미지 이름

            if (!FileStoreUtils.getOriginalFileName(sigImg).equals(existingName)) { //기존 사진과 이름이 동일하지 않은 경우
                deleteImage(product, signatureProductImage); // 사진 제거
                UploadFileDTO uploadFileDTO = FileStoreUtils.storeFile(sigImg, rootPath, FileSubPath.PRODUCT_PATH);
                product.addProductSignatureImage(createProductImage(uploadFileDTO, ProductImageType.SIGNATURE)); // 사진 추가
            }
        } catch (IOException e) {
            throw new FileSaveException("대표 이미지 변경에 실패했습니다. 다시 시도해주세요");
        }

    }

    private void changeOrdinalImage(Product product, List<MultipartFile> ordinalImgList) {
        try {
            List<ProductImage> productOrdinalImages = product.getOrdinalProductImage();

            // 저장된 사진 측 -> 들어온 것중 기존 파일 이름과 같은 것이 없으면 삭제
            String[] inputName = ordinalImgList.stream().map(MultipartFile::getOriginalFilename).toArray(String[]::new);
            List<ProductImage> deleteProductImageList = productOrdinalImages.stream()
                    .filter(productImage -> !StringUtils.containsAny(productImage.getName(), inputName))
                    .collect(Collectors.toList());

            deleteProductImageList.forEach(deleteProductImage -> deleteImage(product, deleteProductImage));

            // 들어온 사진 측 -> 들어온 파일 이름과 기존 파일 이름 같은 것 없으면 추가
            String[] ordinalImgNames = productOrdinalImages.stream().map(ProductImage::getName).toArray(String[]::new);
            List<MultipartFile> uploadFiles = ordinalImgList.stream()
                    .filter(ordinalImg -> !StringUtils.containsAny(ordinalImg.getOriginalFilename(), ordinalImgNames)).collect(Collectors.toList());

            List<UploadFileDTO> uploadFileDTOS = FileStoreUtils.storeFiles(uploadFiles, rootPath, FileSubPath.PRODUCT_PATH);// 이미지 저장
            List<ProductImage> ordinalProductImages = createProductOrdinalImages(uploadFileDTOS);

            ordinalProductImages.stream().forEach(product::addProductOrdinalImage);

        } catch (IOException e) {
            throw new FileSaveException("일반 이미지 변경에 실패했습니다. 다시 시도해주세요");
        }


    }

    private void deleteImage(Product product, ProductImage productImage) {
        File file = new File(FileStoreUtils.getFullPath(rootPath, productImage.getPath()));
        if (file.exists()) {
            file.delete();
        }
        product.deleteProductImage(productImage);
    }
}
