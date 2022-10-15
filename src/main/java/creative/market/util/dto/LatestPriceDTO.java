package creative.market.util.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class LatestPriceDTO {
    private Integer price;
    private String latestDate;

    public LatestPriceDTO(Integer price, LocalDate latestDate) {
        this.price = price;
        this.latestDate = latestDate != null ? latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }
}