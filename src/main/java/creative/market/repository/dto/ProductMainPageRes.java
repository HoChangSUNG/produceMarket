package creative.market.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductMainPageRes {

    private String imgSigSrc;
    private Long productId;
    private String productName;
    private Integer price;
    private String retailUnit;

    @QueryProjection
    public ProductMainPageRes(String imgSigSrc, Long productId, String productName, Integer price, String retailUnit) {
        this.imgSigSrc = imgSigSrc;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.retailUnit = retailUnit;
    }

    public ProductMainPageRes(Product product) {
        this.imgSigSrc = product.getSignatureProductImage().getPath();
        this.productId = product.getId();
        this.productName = product.getName();
        this.price = product.getPrice();
        this.retailUnit = product.getKindGrade().getKind().getRetailsaleUnit();
    }
}
