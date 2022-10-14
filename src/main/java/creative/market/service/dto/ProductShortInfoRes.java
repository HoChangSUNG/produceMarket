package creative.market.service.dto;

import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ProductShortInfoRes {
    private Long productId;
    private String productName;
    private int price;

    private String createdDate;
    private String sellerName;
    private String sellerRank;
    private int sellerPercent;
    private String signatureImgSrc;

    public ProductShortInfoRes(Product product, String rank, int percent) {
        productId = product.getId();
        productName = product.getName();
        price = product.getPrice();
        createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        sellerName = product.getUser().getName();
        sellerRank = rank;
        sellerPercent = percent;
        signatureImgSrc = product.getSignatureProductImage().getPath();

    }
}
