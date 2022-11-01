package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PercentAndPriceRes {
    private String name;
    private long price;
    private String percent;

    public PercentAndPriceRes(String name, long price, float percent) {
        this.name = name;
        this.price = price;
        this.percent = String.format("%.1f" , percent);
    }
}
