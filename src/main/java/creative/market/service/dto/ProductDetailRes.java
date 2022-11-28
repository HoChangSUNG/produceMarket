package creative.market.service.dto;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductImage;
import creative.market.repository.dto.LatestRetailAndWholesaleDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProductDetailRes {

    private Long productId;
    private String category;
    private Long kindGradeId;
    private String productName;
    private int price;
    private String info;
    private String createdDate;
    private String retailUnit;
    private String sellerName;
    private String sellerRank;
    private String sellerPercent;
    private List<String> ordinalImgSrc;
    private String signatureImgSrc;
    private int productAvgPrice;
    private LatestRetailAndWholesaleDTO latestMarketPrice;

    public ProductDetailRes(Product product, String sellerRank, String sellerPercent,int productAvgPrice, LatestRetailAndWholesaleDTO latestMarketPrice) {
        this.productId = product.getId();
        this.kindGradeId = product.getKindGrade().getId();
        this.productName = product.getName();
        this.price = product.getPrice();
        this.info = product.getInfo();
        this.createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.retailUnit = product.getKindGrade().getKind().getRetailsaleUnit();
        this.sellerName = product.getUser().getName();
        this.sellerRank = sellerRank;
        this.sellerPercent = sellerPercent;
        this.ordinalImgSrc = product.getOrdinalProductImage().stream()
                .map(ProductImage::getPath)
                .collect(Collectors.toList());
        this.signatureImgSrc = product.getSignatureProductImage().getPath();
        this.productAvgPrice = productAvgPrice;
        this.latestMarketPrice = latestMarketPrice;

        KindGrade kindGrade = product.getKindGrade();
        String gradeName = kindGrade.getGrade().getGradeName();
        String kindName = kindGrade.getKind().getName();
        String itemName = kindGrade.getKind().getItem().getName();
        String itemCategoryName = kindGrade.getKind().getItem().getItemCategory().getName();
        this.category = itemCategoryName + " / " + itemName + " / " + kindName + " / " + gradeName;

    }
}
