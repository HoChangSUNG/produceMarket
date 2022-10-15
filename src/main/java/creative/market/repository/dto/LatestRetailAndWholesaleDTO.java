package creative.market.repository.dto;

import creative.market.util.dto.LatestPriceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LatestRetailAndWholesaleDTO {

    private LatestPriceDTO retail;
    private LatestPriceDTO wholesale;

}
