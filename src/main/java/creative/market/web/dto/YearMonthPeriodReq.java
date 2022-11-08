package creative.market.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.YearMonth;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class YearMonthPeriodReq {

    @DateTimeFormat(pattern = "yyyy-MM")
    @NotNull(message = "시작 날짜를 입력해주세요")
    YearMonth startDate;

    @DateTimeFormat(pattern = "yyyy-MM")
    @NotNull(message = "종료 날짜를 입력해주세요")
    YearMonth endDate;
}
