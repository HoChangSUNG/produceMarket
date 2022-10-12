package creative.market.service.query;

import creative.market.domain.product.Product;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.service.dto.ProductShortInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {
    private final ProductRepository productRepository;
    // 신뢰 등급, 백분위 얻는 서비스 있어야 함

    //신뢰 등급, 백분위 얻는 코드 추가해야 함
    public List<ProductShortInfoDTO> productShortInfoList(ProductSearchConditionReq condition) {
        List<Product> findProducts = productRepository.findProductByCondition(condition);

        //신뢰등급, 백분위 찾아오는 함수 넣어야 함
        return findProducts.stream()
                .map(product -> new ProductShortInfoDTO(product, "미완성등급", 0)) // 신뢰등급, 백분위 찾아오는 함수 넣어야 함.
                .collect(Collectors.toList());
    }
}
