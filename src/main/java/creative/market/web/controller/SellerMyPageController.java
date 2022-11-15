package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.BuyerTotalPricePerPeriodDTO;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.query.ProductQueryService;
import creative.market.util.PagingUtils;
import creative.market.web.dto.BuyerTotalPricePerPeriodRes;
import creative.market.web.dto.PagingResultRes;
import creative.market.web.dto.ResultRes;
import creative.market.web.dto.YearMonthPeriodReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller-mypage")
@Slf4j
public class SellerMyPageController {

    private final ProductQueryService productQueryService;
    private final ProductRepository productRepository;
    private final OrderProductQueryRepository orderProductQueryRepository;

    @GetMapping("/sale-list")
    @LoginCheck(type = UserType.SELLER)
    public PagingResultRes getSaleList(
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Login LoginUserDTO loginUserDTO) {

        Long total = productRepository.findByUserIdCount(loginUserDTO.getId());

        int offset = PagingUtils.getOffset(pageNum, pageSize);
        int totalPageNum = PagingUtils.getTotalPageNum(total, pageSize);

        return new PagingResultRes(productQueryService.getSaleList(loginUserDTO.getId(), offset, pageSize), pageNum, totalPageNum);
    }

    @GetMapping("/sale-history")
    @LoginCheck(type = UserType.SELLER)
    public PagingResultRes getSaleHistory(
            @Valid YearMonthPeriodReq yearMonthPeriodReq,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Login LoginUserDTO loginUserDTO) {

        checkRightPeriod(yearMonthPeriodReq.getStartDate(), yearMonthPeriodReq.getEndDate());

        LocalDateTime startDate = startMonthOfDayLocalDateTime(yearMonthPeriodReq.getStartDate()); // 시작 날짜
        LocalDateTime endDate = endMonthOfDayLocalDateTime(yearMonthPeriodReq.getEndDate()); // 종료 날짜

        Long total = orderProductQueryRepository.findSaleHistoryPerPeriodCount(startDate, endDate, loginUserDTO.getId());
        int offset = PagingUtils.getOffset(pageNum, pageSize);
        int totalPageNum = PagingUtils.getTotalPageNum(total, pageSize);

        return new PagingResultRes(orderProductQueryRepository.findSaleHistoryPerPeriod(startDate, endDate, loginUserDTO.getId(), offset, pageSize), pageNum, totalPageNum);
    }


    private void checkRightPeriod(YearMonth startDate, YearMonth endDate) {
        if (!isRightPeriod(startDate, endDate)) {
            throw new IllegalArgumentException("기간이 올바르지 않습니다");
        }
    }

    private boolean isRightPeriod(YearMonth startDate, YearMonth endDate) {
        // 날짜 기간이 올바른지(시작날짜가 종료날짜보다 같거나빠른지, 종료날짜가 현재 날짜보다 같거나 빠른지)
        return startDate.compareTo(endDate) <= 0 && endDate.compareTo(YearMonth.now()) <= 0;
    }

    private LocalDateTime startMonthOfDayLocalDateTime(YearMonth yearMonth) { // MonthYear -> 시작 LocalDateTime 으로 변경
        // 2022년 11월 -> 2022년 11월 01일 00:00:00
        return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
    }

    private LocalDateTime endMonthOfDayLocalDateTime(YearMonth yearMonth) { // MonthYear -> 종료 LocalDateTime 으로 변경
        // 2022년 11월 -> 2022년 11월 30일 23:59:999999
        LocalTime maxTime = LocalTime.MAX;
        LocalDate localDate = yearMonth.atEndOfMonth();
        return LocalDateTime.of(localDate, maxTime);
    }
}
