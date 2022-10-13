package creative.market.service.dto;

import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProductDetailDTO {

    private Long productId;
    private Long kindGradeId;
    private String productName;
    private int price;
    private String info;
    private String createdDate;
    private String sellerName;
    private String sellerRank;
    private int sellerPercent;
    private List<String> ordinalImgSrc;

    public ProductDetailDTO(Product product,String sellerRank,int sellerPercent) {
        this.productId = product.getId();
        this.kindGradeId = product.getKindGrade().getId();
        this.productName = product.getName();
        this.price = product.getPrice();
        this.info = product.getInfo();
        this.createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.sellerName = product.getUser().getName();
        this.sellerRank = sellerRank;
        this.sellerPercent = sellerPercent;
        this.ordinalImgSrc = product.getOrdinalProductImage().stream()
                .map(ProductImage::getPath)
                .collect(Collectors.toList());
    }
}
