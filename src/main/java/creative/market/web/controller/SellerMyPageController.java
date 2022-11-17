package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.query.OrderProductQueryService;
import creative.market.service.query.ProductQueryService;
import creative.market.util.PagingUtils;
import creative.market.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller-mypage")
@Slf4j
public class SellerMyPageController {

    private final ProductQueryService productQueryService;
    private final ProductRepository productRepository;
    private final OrderProductQueryRepository orderProductQueryRepository;
    private final OrderProductQueryService orderProductQueryService;
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

    @GetMapping("/order-price-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderPriceByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO,
                                           @Login LoginUserDTO loginUserDTO) {
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        checkRightPeriod(startDate, endDate);

        return new ResultRes<>(orderProductQueryService.findSellerTotalPriceCompareByPeriod(startDate,endDate,categoryParamDTO,loginUserDTO.getId()));
    }

    @GetMapping("/order-price-percentile-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderPricePercentileByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO,
                                                     @Login LoginUserDTO loginUserDTO) {
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        checkRightPeriod(startDate, endDate);

        return new ResultRes<>(orderProductQueryService.findSellerPercentileList(startDate,endDate,categoryParamDTO, loginUserDTO.getId()));
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
