package creative.market.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerAndTotalPricePerCategoryDTO {

    private String sellerName;
    private Long totalPrice;

    @QueryProjection
    public SellerAndTotalPricePerCategoryDTO(String sellerName, Long totalPrice) {
        this.sellerName = sellerName;
        this.totalPrice = totalPrice;
    }
}
