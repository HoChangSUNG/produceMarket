package creative.market.repository.dto;

import creative.market.domain.category.KindGrade;
import creative.market.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateFormRes {

    private String category;
    private Long kindGradeId;
    private String productName;
    private int price;
    private String info;
    private String retailUnit;
    private List<ProductImageInfoDTO> ordinalImgList;
    private ProductImageInfoDTO signatureImg;

    public ProductUpdateFormRes(Product product) {
        KindGrade kindGrade = product.getKindGrade();
        String gradeName = kindGrade.getGrade().getGradeName();
        String kindName = kindGrade.getKind().getName();
        String itemName = kindGrade.getKind().getItem().getName();
        String itemCategoryName = kindGrade.getKind().getItem().getItemCategory().getName();

        this.category = itemCategoryName + " / " + itemName + " / " + kindName + " / " + gradeName;
        this.kindGradeId = kindGrade.getId();
        this.productName=product.getName();
        this.price=product.getPrice();
        this.info = product.getInfo();
        this.retailUnit = kindGrade.getKind().getRetailsaleUnit();
        this.ordinalImgList = product.getOrdinalProductImage().stream()
                .map(ordinalImg -> new ProductImageInfoDTO(ordinalImg))
                .collect(Collectors.toList());
        this.signatureImg = new ProductImageInfoDTO(product.getSignatureProductImage());

    }
}
