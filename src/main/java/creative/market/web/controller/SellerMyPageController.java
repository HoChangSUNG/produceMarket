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
import creative.market.web.validation.YearMonthPeriodReqValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
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
    private final YearMonthPeriodReqValidator yearMonthPeriodValidator;
    @InitBinder(value = "yearMonthPeriodReq")
    public void init(WebDataBinder dataBinder){
        dataBinder.addValidators(yearMonthPeriodValidator);
    }

    @GetMapping("/sale-list")
    @LoginCheck(type = UserType.SELLER)
    public PagingResultRes getSaleList(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNum,
            @Login LoginUserDTO loginUserDTO) {

        Long total = productRepository.findByUserIdCount(loginUserDTO.getId());

        int offset = PagingUtils.getOffset(pageNum, pageSize);
        int totalPageNum = PagingUtils.getTotalPageNum(total, pageSize);

        return new PagingResultRes(productQueryService.getSaleList(loginUserDTO.getId(), offset, pageSize), pageNum, totalPageNum);
    }

    @GetMapping("/sale-history")
    @LoginCheck(type = UserType.SELLER)
    public PagingResultPriceRes getSaleHistory(
            @Valid YearMonthPeriodReq yearMonthPeriodReq,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNum,
            @Login LoginUserDTO loginUserDTO) {

        LocalDateTime startDate = startMonthOfDayLocalDateTime(yearMonthPeriodReq.getStartDate()); // ?????? ??????
        LocalDateTime endDate = endMonthOfDayLocalDateTime(yearMonthPeriodReq.getEndDate()); // ?????? ??????

        Long total = orderProductQueryRepository.findSaleHistoryPerPeriodCount(startDate, endDate, loginUserDTO.getId());
        int offset = PagingUtils.getOffset(pageNum, pageSize);
        int totalPageNum = PagingUtils.getTotalPageNum(total, pageSize);
        Long totalPrice = orderProductQueryRepository.findSaleHistoryTotalPricePerPeriod(startDate, endDate, loginUserDTO.getId());

        return new PagingResultPriceRes(orderProductQueryRepository.findSaleHistoryPerPeriod(startDate, endDate, loginUserDTO.getId(), offset, pageSize), totalPrice, pageNum, totalPageNum);
    }

    @GetMapping("trust-score")
    @LoginCheck(type = UserType.SELLER)
    public ResultRes getTrustScore(@Login LoginUserDTO loginUserDTO) {
        return new ResultRes(orderProductQueryRepository.findSellerTrustScore(loginUserDTO.getId()));
    }

    @GetMapping("/trust-score-statistics")
    @LoginCheck(type = UserType.SELLER)
    public ResultRes getTrustScoreByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, @Login LoginUserDTO loginUserDTO) { // ????????? ???????????? ?????????
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        return new ResultRes(orderProductQueryService.findSellerTrustScoreByPeriod(startDate, endDate, loginUserDTO.getId()));
    }

    @GetMapping("/trust-score-percentile-statistics")
    @LoginCheck(type = UserType.SELLER)
    public ResultRes getTrustScorePercentileByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, @Login LoginUserDTO loginUserDTO) { //????????? ???????????? ????????? ?????????
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        return new ResultRes(orderProductQueryService.findSellerTrustScorePercentileByPeriod(startDate, endDate, loginUserDTO.getId()));
    }

    @GetMapping("/order-price-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderPriceByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO,
                                           @Login LoginUserDTO loginUserDTO) { // ????????? ????????? ?????? ?????????
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        return new ResultRes<>(orderProductQueryService.findSellerTotalPriceCompareByPeriod(startDate,endDate,categoryParamDTO,loginUserDTO.getId()));
    }

    @GetMapping("/order-price-percentile-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderPricePercentileByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO,
                                                     @Login LoginUserDTO loginUserDTO) { // ????????? ?????? ????????? ????????? ????????? ?????????
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        return new ResultRes<>(orderProductQueryService.findSellerPercentileList(startDate,endDate,categoryParamDTO, loginUserDTO.getId()));
    }

    @GetMapping("/order-count-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderCountByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO,
                                           @Login LoginUserDTO loginUserDTO) { // ????????? ???????????? ?????? ?????????
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        return new ResultRes<>(orderProductQueryService.findSellerTotalOrderCountCompareByPeriod(startDate,endDate,categoryParamDTO,loginUserDTO.getId()));
    }

    private LocalDateTime startMonthOfDayLocalDateTime(YearMonth yearMonth) { // MonthYear -> ?????? LocalDateTime ?????? ??????
        // 2022??? 11??? -> 2022??? 11??? 01??? 00:00:00
        return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
    }

    private LocalDateTime endMonthOfDayLocalDateTime(YearMonth yearMonth) { // MonthYear -> ?????? LocalDateTime ?????? ??????
        // 2022??? 11??? -> 2022??? 11??? 30??? 23:59:999999
        LocalTime maxTime = LocalTime.MAX;
        LocalDate localDate = yearMonth.atEndOfMonth();
        return LocalDateTime.of(localDate, maxTime);
    }
}
