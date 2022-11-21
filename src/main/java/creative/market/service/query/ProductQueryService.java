package creative.market.service.query;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.*;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.ProductDetailRes;
import creative.market.service.dto.ProductShortInfoRes;
import creative.market.service.dto.SaleListRes;
import creative.market.util.WholesaleAndRetailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {
    private final ProductRepository productRepository;
    private final WholesaleAndRetailUtils wholesaleAndRetailUtils;
    private final OrderProductQueryRepository orderProductQueryRepository;

    public List<ProductShortInfoRes> productShortInfoList(ProductSearchConditionReq condition, int offset, int limit) {

        List<Product> findProducts = productRepository.findProductByCondition(condition, offset, limit);
        YearMonth startDate = YearMonth.now();
        YearMonth endDate = startDate;
        List<ProductShortInfoRes> result = new ArrayList<>();
        for (int i = 0; i < findProducts.size(); i++) {
            Product product = findProducts.get(i);
            Long sellerId = product.getUser().getId();

            // 신뢰점수 등급
            double trustScore = Double.parseDouble(orderProductQueryRepository.findSellerTrustScore(sellerId));
            String rank = trustScoreToRank(trustScore);

            //신뢰점수 백분위
            SellerTrustScorePercentileByPeriodDTO trustPercentileScore = orderProductQueryRepository.findSellerTrustScorePercentileByPeriod(startDate, endDate, sellerId).get(0);

            result.add(new ProductShortInfoRes(product, rank, trustPercentileScore.getPercentile()));
        }

        return result;
    }

    public ProductDetailRes productDetailInfo(Long productId) {
        Product product = productRepository.findByIdFetchJoinSellerAndKind(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
        KindGrade kindGrade = product.getKindGrade();

        LatestRetailAndWholesaleDTO retailAndWholesalePriceResult = wholesaleAndRetailUtils.getLatestPriceInfo(kindGrade);// 최근 도소매 정보(단위 변환 + 도소매 단위 다른 경우 처리)

        Long sellerId = product.getUser().getId();
        YearMonth startDate = YearMonth.now();
        YearMonth endDate = startDate;

        // 신뢰점수 등급
        double trustScore = Double.parseDouble(orderProductQueryRepository.findSellerTrustScore(sellerId));
        String rank = trustScoreToRank(trustScore);

        //신뢰점수 백분위
        SellerTrustScorePercentileByPeriodDTO trustPercentileScore = orderProductQueryRepository.findSellerTrustScorePercentileByPeriod(startDate, endDate, sellerId).get(0);

        int productAvgPrice = productRepository.findProductAvgPrice(kindGrade.getId()).intValue();// 상품 평균 가격

        return new ProductDetailRes(product, rank, trustPercentileScore.getPercentile(), productAvgPrice, retailAndWholesalePriceResult);
    }

    public ProductUpdateFormRes productUpdateForm(Long productId) {
        Product product = productRepository.findByIdFetchJoinItemCategory(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
        return new ProductUpdateFormRes(product);
    }

    public List<ProductMainPageRes> productMainPageByOrderCount(int offset, int limit, LocalDateTime startDate, LocalDateTime endDate) { // 메인 페이지 전체 상품 판매횟수순
        return productRepository.findProductIdByOrderCountDesc(offset, limit, startDate, endDate).stream()
                .map(productId -> new ProductMainPageRes(findProductById(productId)))
                .collect(Collectors.toList());
    }

    public List<ProductMainPageShortRes> productMainPageByReviewAvgRate(int offset, int limit, LocalDateTime startDate, LocalDateTime endDate) { //메인 페이지 별점 평균순
        return productRepository.findProductIdByReviewCountDesc(offset, limit, startDate, endDate).stream()
                .map(productId -> new ProductMainPageShortRes(findProductById(productId)))
                .collect(Collectors.toList());
    }

    public List<SaleListRes> getSaleList(Long userId, int offset, int limit) {
        return productRepository.findByUserId(userId, offset, limit).stream()
                .map(SaleListRes::new)
                .collect(Collectors.toList());
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
    }


    private String trustScoreToRank(double trustScore) {
        String rank = null;
        switch ((int) Math.ceil(trustScore / 10)) {
            case 10:
                rank = "A";
                break;
            case 9:
                rank = "B";
                break;
            case 8:
                rank = "C";
                break;
            default:
                rank = "D";
                break;
        }
        return rank;

    }
}
