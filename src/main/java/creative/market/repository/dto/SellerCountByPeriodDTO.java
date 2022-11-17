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

    public SellerCountByPeriodDTO(BigInteger count, String date) {
        this.date = date;
        this.count = count.longValue();
    }
}
