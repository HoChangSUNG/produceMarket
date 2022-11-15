package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.SellerPricePerPeriodDTO;
import creative.market.repository.query.OrderProductQueryRepositoryTemp;
import creative.market.repository.user.SellerRepository;
import creative.market.service.dto.LoginUserDTO;
import creative.market.web.dto.PriceCompareByPeriodRes;
import creative.market.web.dto.ResultRes;
import creative.market.web.dto.YearMonthPeriodReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller-mypage/test")
@Slf4j
public class SellerMyPageControllerTemp {

    private final OrderProductQueryRepositoryTemp orderProductQueryRepositoryTemp;
    private final SellerRepository sellerRepository;

    @GetMapping("/order-price-statistics")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getOrderPriceByPeriod(@Valid YearMonthPeriodReq yearMonthPeriodReq, CategoryParamDTO categoryParamDTO, @Login LoginUserDTO loginUserDTO) {
        YearMonth startDate = yearMonthPeriodReq.getStartDate();
        YearMonth endDate = yearMonthPeriodReq.getEndDate();

        checkRightPeriod(startDate, endDate);

        List<SellerPricePerPeriodDTO> allSellerTotalPrice = orderProductQueryRepositoryTemp.findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, categoryParamDTO);
        List<SellerPricePerPeriodDTO> allSellerAvgPricePerPeriodList = convertToTotalSellerAvgPrice(allSellerTotalPrice);

        List<SellerPricePerPeriodDTO> sellerTotalPricePerPeriodList = orderProductQueryRepositoryTemp.findSellerTotalPricePerPeriodAndCategory(startDate, endDate, categoryParamDTO, loginUserDTO.getId());

        return new ResultRes<>(convertToPriceCompareByPeriodDTO(allSellerAvgPricePerPeriodList,sellerTotalPricePerPeriodList));
    }

    private PriceCompareByPeriodRes convertToPriceCompareByPeriodDTO(List<SellerPricePerPeriodDTO> allSellerAvgPriceList, List<SellerPricePerPeriodDTO> sellerPriceList) {
        List<String> dateList = allSellerAvgPriceList.stream()
                .map(SellerPricePerPeriodDTO::getDate)
                .collect(Collectors.toList());
        List<Long> avgList = allSellerAvgPriceList.stream()
                .map(SellerPricePerPeriodDTO::getTotalPrice)
                .collect(Collectors.toList());
        List<Long> sellerPrices = sellerPriceList.stream()
                .map(SellerPricePerPeriodDTO::getTotalPrice)
                .collect(Collectors.toList());

        return new PriceCompareByPeriodRes(dateList,sellerPrices,avgList);
    }

    private List<SellerPricePerPeriodDTO> convertToTotalSellerAvgPrice(List<SellerPricePerPeriodDTO> allSellerTotalPrice) {
        Long totalCount = sellerRepository.findAllSellerCountWithExistAndDeletedSeller();
        if (totalCount == 0) {
            return allSellerTotalPrice.stream()
                    .map(priceDTO -> new SellerPricePerPeriodDTO(0L, priceDTO.getDate()))
                    .collect(Collectors.toList());
        } else {
            return allSellerTotalPrice.stream()
                    .map(priceDTO -> new SellerPricePerPeriodDTO(priceDTO.getTotalPrice()/totalCount, priceDTO.getDate()))
                    .collect(Collectors.toList());
        }
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
}
