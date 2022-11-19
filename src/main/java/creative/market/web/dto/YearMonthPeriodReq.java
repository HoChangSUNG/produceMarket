package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class YearMonthPeriodReq {

    @DateTimeFormat(pattern = "yyyy-MM")
    YearMonth startDate;

    @DateTimeFormat(pattern = "yyyy-MM")
    YearMonth endDate;
}
