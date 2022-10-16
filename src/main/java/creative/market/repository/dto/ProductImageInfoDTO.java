package creative.market.repository.dto;

import creative.market.domain.product.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductImageInfoDTO {
    private String src;
    private String name;

    public ProductImageInfoDTO(ProductImage productImage) {
        this.src = productImage.getPath();
        this.name = productImage.getName();
    }
}
