package creative.market.service.dto;

import creative.market.domain.category.Item;
import creative.market.domain.category.ItemCategory;
import creative.market.domain.category.Kind;
import creative.market.domain.category.KindGrade;
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
    private String sellerPercent;
    private String signatureImgSrc;
    private String category;


    public ProductShortInfoRes(Product product, String rank, String percent) {
        KindGrade kindGrade = product.getKindGrade();
        String gradeName = kindGrade.getGrade().getGradeName();
        String kindName = kindGrade.getKind().getName();
        String itemName = kindGrade.getKind().getItem().getName();
        String itemCategoryName = kindGrade.getKind().getItem().getItemCategory().getName();

        productId = product.getId();
        productName = product.getName();
        price = product.getPrice();
        createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        sellerName = product.getUser().getName();
        sellerRank = rank;
        sellerPercent = percent;
        signatureImgSrc = product.getSignatureProductImage().getPath();
        this.category = itemCategoryName + " / " + itemName + " / " + kindName + " / " + gradeName;

    }
}
