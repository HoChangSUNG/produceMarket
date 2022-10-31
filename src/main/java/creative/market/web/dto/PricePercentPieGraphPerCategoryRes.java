package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PricePercentPieGraphPerCategoryRes {

    private List<PercentAndPriceRes> topPieGraphPercent;
    private PercentAndPriceRes sellerPercent;
}
