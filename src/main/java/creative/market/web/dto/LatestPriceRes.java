package creative.market.web.dto;

import creative.market.util.LatestPriceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LatestPriceRes {

    private LatestPriceDTO retail;
    private LatestPriceDTO wholesale;

}
