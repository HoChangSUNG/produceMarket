package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerTrustScoreByPeriodDTO {

    private String trustScore;
    private String date;

    public SellerTrustScoreByPeriodDTO(Double trustScore, String date) {
        this.trustScore = String.format("%.2f", trustScore);
        this.date = date;
    }
}
