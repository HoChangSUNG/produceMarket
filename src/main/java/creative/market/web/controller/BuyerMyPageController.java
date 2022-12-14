package creative.market.web.controller;


import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.repository.dto.BuyerTotalPricePerPeriodDTO;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.OrderHistoryDTO;
import creative.market.service.query.OrderProductQueryService;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buyer-mypage")
@Slf4j
public class BuyerMyPageController {

    private final OrderProductQueryService orderProductQueryService;
    private final OrderProductQueryRepository orderProductQueryRepository;
    private final YearMonthPeriodReqValidator yearMonthPeriodValidator;

    @InitBinder(value = "yearMonthPeriodReq")
    public void init(WebDataBinder dataBinder){
        dataBinder.addValidators(yearMonthPeriodValidator);
    }
    @GetMapping("/order-history")
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public PagingResultPriceRes getOrderHistoryByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @Login LoginUserDTO loginUserDTO) {

        int offset = PagingUtils.getOffset(pageNum, pageSize);

        LocalDateTime startDate = startMonthOfDayLocalDateTime(yearMonthPeriodReq.getStartDate()); // ?????? ??????
        LocalDateTime endDate = endMonthOfDayLocalDateTime(yearMonthPeriodReq.getEndDate()); // ?????? ??????

        int total = orderProductQueryRepository.findBuyerOrderPerPeriodTotalCount(startDate, endDate, loginUserDTO.getId()).intValue();
        List<OrderHistoryDTO> result = orderProductQueryService.findBuyerOrderPerPeriod(startDate, endDate, loginUserDTO.getId(), offset, pageSize);
        Long totalPrice = orderProductQueryRepository.findBuyerOrderTotalPricePerPeriod(startDate, endDate, loginUserDTO.getId());

        return new PagingResultPriceRes(result, totalPrice, pageNum, total);
    }

    @GetMapping("/order-price-statistics")
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes getOrderPriceByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, @Login LoginUserDTO loginUserDTO) {

        checkRightPeriod(yearMonthPeriodReq.getStartDate(), yearMonthPeriodReq.getEndDate());

        List<BuyerTotalPricePerPeriodDTO> buyerTotalPricePerPeriod = orderProductQueryRepository.findBuyerTotalPricePerPeriod(yearMonthPeriodReq.getStartDate(), yearMonthPeriodReq.getEndDate(), loginUserDTO.getId());
        return new ResultRes<>(new BuyerTotalPricePerPeriodRes(buyerTotalPricePerPeriod));
    }

    private void checkRightPeriod(YearMonth startDate, YearMonth endDate) {
        if (!isRightPeriod(startDate, endDate)) {
            throw new IllegalArgumentException("????????? ???????????? ????????????");
        }
    }

    private boolean isRightPeriod(YearMonth startDate, YearMonth endDate) {
        // ?????? ????????? ????????????(??????????????? ?????????????????? ??????????????????, ??????????????? ?????? ???????????? ????????? ?????????)
        return startDate.compareTo(endDate) <= 0 && endDate.compareTo(YearMonth.now()) <= 0;
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
