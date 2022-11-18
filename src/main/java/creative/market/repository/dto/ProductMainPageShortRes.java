package creative.market.repository.dto;

import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductMainPageShortRes {

    private String imgSigSrc;
    private Long productId;

    public ProductMainPageShortRes(Product product) {
        this.imgSigSrc = product.getSignatureProductImage().getPath();
        this.productId = product.getId();
    }



}
