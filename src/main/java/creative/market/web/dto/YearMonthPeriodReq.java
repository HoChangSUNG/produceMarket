package creative.market.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.YearMonth;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class YearMonthPeriodReq {

    @DateTimeFormat(pattern = "yyyy-MM")
    YearMonth start;

    @DateTimeFormat(pattern = "yyyy-MM")
    YearMonth end;
}
