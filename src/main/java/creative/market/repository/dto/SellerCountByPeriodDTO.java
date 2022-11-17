package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class SellerCountByPeriodDTO {
    private String date;
    private Long count;

    public SellerCountByPeriodDTO( String date, BigInteger count) {
        this.count = count.longValue();
        this.date = date;
    }
}
