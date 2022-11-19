package creative.market.web.validation;

import creative.market.web.dto.YearMonthPeriodReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.YearMonth;

@Component
@Slf4j
public class YearMonthPeriodReqValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        log.info("YearMonthPeriodReq Validator 실행");
        return YearMonthPeriodReq.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        YearMonthPeriodReq yearMonthPeriodReq = (YearMonthPeriodReq) target;

        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        if (startDate == null) {
            errors.rejectValue("startDate","NotNull");
        }
        if (endDate == null) {
            errors.rejectValue("endDate","NotNull");
        }

        if (errors.hasErrors()) {
            return;
        }

        if (!isRightPeriod(startDate, endDate)) {
            errors.reject("InvalidYearMonthPeriod");
        }

    }

    private boolean isRightPeriod(YearMonth startDate, YearMonth endDate) {
        // 날짜 기간이 올바른지(시작날짜가 종료날짜보다 같거나 빠른지, 종료날짜가 현재 날짜보다 같거나 빠른지)
        return startDate.compareTo(endDate) <= 0 && endDate.compareTo(YearMonth.now()) <= 0;
    }
}
