package creative.market.service;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.domain.product.ProductImageType;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.ProductRepository;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final KindGradeRepository kindGradeRepository;

    @Transactional
    public Long register(RegisterProductDTO registerProductDTO) {// 상품 등록

        Long sellerId = registerProductDTO.getSellerId();
        // seller find 해서 연결해줘야 한다 꼭!!!!!!

        KindGrade kindGrade = kindGradeRepository.findById(registerProductDTO.getKindGradeId())
                .orElseThrow(() -> new IllegalArgumentException("올바른 카테고리가 아닙니다"));

        // 일반 사진 생성
        List<UploadFileDTO> ordinalImages = registerProductDTO.getOrdinalImg();
        List<ProductImage> ordinalProductImages = ordinalImages.stream()
                .map(ordinalImage -> createProductImage(ordinalImage,ProductImageType.ORDINAL))
                .collect(Collectors.toList());

        // 대표 사진 생성
        ProductImage sigProductImage = createProductImage(registerProductDTO.getSigImg(),ProductImageType.SIGNATURE);


        // 상품 생성한 Seller 찾아서 연결해줘야 함!!!!
        //상품 저장
        Product product = Product.builder()
                .name(registerProductDTO.getName())
                .price(registerProductDTO.getPrice())
                .info(registerProductDTO.getInfo())
                .kindGrade(kindGrade)
                .seller(null)  // 연결해주기!!!!!!!
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
