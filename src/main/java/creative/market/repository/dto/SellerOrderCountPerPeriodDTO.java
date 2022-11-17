package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class SellerOrderCountPerPeriodDTO {

    private String date;
    private Long totalCount;

    public SellerOrderCountPerPeriodDTO(BigInteger totalCount, String date) {
        this.date = date;
        this.totalCount = totalCount.longValue();
    }

    public SellerOrderCountPerPeriodDTO(Long totalCount, String date) {
        this.date = date;
        this.totalCount = totalCount.longValue();
    }
}
