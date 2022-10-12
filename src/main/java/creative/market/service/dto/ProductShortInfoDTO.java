package creative.market.service.dto;

import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductShortInfoDTO {
    private Long productId;
    private String productName;
    private int price;
    private Long sellerId;
    private String sellerName;
    private String sellerRank;
    private int sellerPercent;
    private String signatureImgSrc;

    public ProductShortInfoDTO(Product product,String rank,int percent) {
        productId = product.getId();
        productName = product.getName();
        price = product.getPrice();
        sellerId =product.getUser().getId();
        sellerName = product.getUser().getName();
        sellerRank = rank;
        sellerPercent = percent;
        signatureImgSrc = product.getSignatureProductImage().getPath();

    }
}
