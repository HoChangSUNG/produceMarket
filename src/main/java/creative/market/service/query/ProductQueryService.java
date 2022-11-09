package creative.market.service.query;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.repository.dto.ProductSigSrcAndIdRes;
import creative.market.repository.dto.ProductUpdateFormRes;
import creative.market.service.dto.ProductDetailRes;
import creative.market.service.dto.ProductShortInfoRes;
import creative.market.util.PagingUtils;
import creative.market.util.WholesaleAndRetailUtils;
import creative.market.repository.dto.LatestRetailAndWholesaleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {
    private final ProductRepository productRepository;
    private final WholesaleAndRetailUtils wholesaleAndRetailUtils;
    // 신뢰 등급, 백분위 얻는 서비스 있어야 함

    //신뢰 등급, 백분위 얻는 코드 추가해야 함
    public List<ProductShortInfoRes> productShortInfoList(ProductSearchConditionReq condition, int offset, int limit) {

        List<Product> findProducts = productRepository.findProductByCondition(condition, offset, limit);

        //신뢰등급, 백분위 찾아오는 함수 넣어야 함
        return findProducts.stream()
                .map(product -> new ProductShortInfoRes(product, "미완성등급", 0)) // 신뢰등급, 백분위 찾아오는 함수 넣어야 함.
                .collect(Collectors.toList());
    }

    public ProductDetailRes productDetailInfo(Long productId) {
        Product product = productRepository.findByIdFetchJoinSellerAndKind(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
        KindGrade kindGrade = product.getKindGrade();

        LatestRetailAndWholesaleDTO retailAndWholesalePriceResult = wholesaleAndRetailUtils.getLatestPriceInfo(kindGrade);// 최근 도소매 정보(단위 변환 + 도소매 단위 다른 경우 처리)
        //신뢰등급, 백분위 찾아오는 함수 넣어야 함
        String sellerRank = "미완성 등급";
        int sellerPercent = 0;
        int productAvgPrice = productRepository.findProductAvgPrice(kindGrade.getId()).intValue();// 상품 평균 가격

        return new ProductDetailRes(product, sellerRank, sellerPercent, productAvgPrice, retailAndWholesalePriceResult);
    }

    public ProductUpdateFormRes productUpdateForm(Long productId) {
        Product product = productRepository.findByIdFetchJoinItemCategory(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
        return new ProductUpdateFormRes(product);
    }

    public List<ProductSigSrcAndIdRes> productSigSrcAndIdByOrderCount(int offset, int limit, LocalDateTime startDate, LocalDateTime endDate) { // // 메인 페이지 전체 상품 판매횟수순
        return productRepository.findProductIdByOrderCountDesc(offset, limit, startDate, endDate).stream()
                .map(productId -> new ProductSigSrcAndIdRes(findProductById(productId)))
                .collect(Collectors.toList());
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
    }
}
