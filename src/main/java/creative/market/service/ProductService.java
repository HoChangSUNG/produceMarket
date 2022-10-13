package creative.market.service;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.domain.user.User;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.ProductRepository;
import creative.market.repository.user.UserRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 상품 등록
    @Transactional
    public Long register(RegisterProductDTO registerProductDTO) {
        //판매자 존재 체크
        Long userId = registerProductDTO.getSellerId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoginAuthenticationException("사용자가 존재하지 않습니다"));

        // 카테고리가 존재하는지 체크
        KindGrade kindGrade = kindGradeRepository.findById(registerProductDTO.getKindGradeId())
                .orElseThrow(() -> new NoSuchElementException("올바른 카테고리가 아닙니다"));

        // 일반 사진 생성
        List<UploadFileDTO> ordinalImages = registerProductDTO.getOrdinalImg();
        List<ProductImage> ordinalProductImages = ordinalImages.stream()
                .map(ordinalImage -> createProductImage(ordinalImage,ProductImageType.ORDINAL))
                .collect(Collectors.toList());

        // 대표 사진 생성
        ProductImage sigProductImage = createProductImage(registerProductDTO.getSigImg(),ProductImageType.SIGNATURE);
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

    private ProductImage createProductImage(UploadFileDTO ordinalImage,ProductImageType type) {
        return ProductImage.builder().name(ordinalImage.getUploadFileName())
                .path(FileSubPath.PRODUCT_PATH + ordinalImage.getStoreFileName())
                .type(type)
                .build();
    }
}
