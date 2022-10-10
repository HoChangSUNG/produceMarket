package creative.market.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class LatestConvertPriceDTO {

    private LocalDate latestDay;
    private int price;

    public LatestConvertPriceDTO(int year, int month, int day, int price) {
        this.price = price;
        latestDay = LocalDate.of(year,month,day);
    }
}
