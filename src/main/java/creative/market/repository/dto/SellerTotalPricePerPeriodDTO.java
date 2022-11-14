package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class SellerTotalPricePerPeriodDTO {

    private String date;
    private Long totalPrice;

    public SellerTotalPricePerPeriodDTO(BigInteger totalPrice, String date) {
        this.date = date;
        this.totalPrice = totalPrice.longValue();
    }
}
