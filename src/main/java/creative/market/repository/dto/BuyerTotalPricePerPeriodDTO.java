package creative.market.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BuyerTotalPricePerPeriodDTO {

    private String date;
    private Long price;

    @QueryProjection

    public BuyerTotalPricePerPeriodDTO(Long price, Integer year, Integer month) {
        if (year != null && month != null) {
            date =String.format("%4d.%02d",year,month);
        }
        this.price = price;
    }
}
