package creative.market.service.dto;

import creative.market.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class SaleListRes {

    private Long productId;
    private String productName;
    private String item;
    private int price;
    private String createdDate;
    private String signatureImgSrc;

    public SaleListRes(Product product) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.item = product.getKindGrade().getKind().getItem().getName();
        this.price = product.getPrice();
        this.createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.signatureImgSrc = product.getSignatureProductImage().getPath();
    }
}
