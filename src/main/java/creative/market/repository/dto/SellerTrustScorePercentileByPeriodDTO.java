package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerTrustScorePercentileByPeriodDTO {

    private String percentile;
    private String date;

    public SellerTrustScorePercentileByPeriodDTO(Double percentile, String date) {
        this.percentile = String.format("%.2f", percentile);
        this.date = date;
    }
}
