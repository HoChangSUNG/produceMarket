package creative.market.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSigSrcAndIdRes {

    private String imgSigSrc;
    private Long productId;


    @QueryProjection
    public ProductSigSrcAndIdRes(String imgSigSrc, Long productId) {
        this.imgSigSrc = imgSigSrc;
        this.productId = productId;
    }

    public ProductSigSrcAndIdRes(Product product) {
        this.imgSigSrc = product.getSignatureProductImage().getPath();
        this.productId = product.getId();
    }
}
