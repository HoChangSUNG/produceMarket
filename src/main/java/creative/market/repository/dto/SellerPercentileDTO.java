package creative.market.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerPercentileDTO {

    private String date;
    private String percentile ;

    public SellerPercentileDTO(Double percentile,String date) {
        this.date = date;
        this.percentile = String.format("%.2f", percentile);
    }
}
