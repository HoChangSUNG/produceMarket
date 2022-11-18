package creative.market.service.query;

import creative.market.domain.order.OrderStatus;
import creative.market.exception.NotExistOrder;
import creative.market.repository.dto.*;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.OrderCountCompareByPeriodRes;
import creative.market.service.dto.OrderHistoryDTO;
import creative.market.service.dto.PriceCompareByPeriodRes;
import creative.market.service.dto.OrderPricePercentileGraphByPeriodRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderProductQueryService {

    private final OrderProductQueryRepository orderProductQueryRepository;
    private final OrderProductRepository orderProductRepository;

    public List<OrderHistoryDTO> findBuyerOrderPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId, int offset, int pageSize) {
        List<BuyerOrderPerPeriodDTO> list = orderProductQueryRepository.findBuyerOrderPerPeriod(startDate, endDate, userId, offset, pageSize);
        Map<Long, List<BuyerOrderPerPeriodDTO>> collect = list.stream()
                .collect(Collectors.groupingBy(BuyerOrderPerPeriodDTO::getOrderId));

        List<OrderHistoryDTO> result = new ArrayList<>();

        for (Long key : collect.keySet()) {
            List<BuyerOrderPerPeriodDTO> dtos = collect.get(key);
            int sum = dtos.stream()
                    .filter(dto -> dto.getStatus().equals(OrderStatus.ORDER.toString()))
                    .mapToInt(v -> v.getCount() * v.getPrice())
                    .sum();

            int cnt = (int) dtos.stream()
                    .filter(dto -> dto.getStatus().equals(OrderStatus.ORDER.toString()))
                    .count();

            result.add(new OrderHistoryDTO(key, cnt, sum, dtos.get(0).getCreatedDate(), dtos));
        }

        return result;
    }

    public PriceCompareByPeriodRes findSellerTotalPriceCompareByPeriod(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long sellerId) { // 기간별 판매액 비교 그래프

        checkOrderProductExist(sellerId); // 특정 판매자의 상품이 판매된 적이 있는지

        //전체 판매자
        List<SellerPricePerPeriodDTO> allSellerTotalPrice = orderProductQueryRepository.findAllSellerTotalPricePerPeriodAndCategory(startDate, endDate, categoryParamDTO);
        List<SellerPricePerPeriodDTO> allSellerAvgPricePerPeriodList = convertToTotalSellerAvgPrice(allSellerTotalPrice, startDate, endDate, categoryParamDTO);

        //특정 판매자
        List<SellerPricePerPeriodDTO> sellerTotalPricePerPeriodList = orderProductQueryRepository.findSellerTotalPricePerPeriodAndCategory(startDate, endDate, categoryParamDTO, sellerId);
        return convertToPriceCompareByPeriodDTO(allSellerAvgPricePerPeriodList, sellerTotalPricePerPeriodList);
    }

    public OrderPricePercentileGraphByPeriodRes findSellerPercentileList(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long sellerId) { // 기간별 판매자 판매액 백분위 그래프

        checkOrderProductExist(sellerId); // 특정 판매자의 상품이 판매된 적이 있는지

        // 월별 특정 판매자 판매액 백분위
        List<SellerPercentileDTO> sellerPricePercentile = orderProductQueryRepository.findSellerTotalPricePercentileByPeriodAndCategory(startDate, endDate, categoryParamDTO, sellerId);
        // 월별 특정 판매자 판매횟수
        List<SellerOrderCountPerPeriodDTO> sellerTotalOrderCountPerPeriodList = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParamDTO, sellerId);

        // 판매횟수 없는 경우 백분위 0으로 변경
        List<SellerPercentileDTO> convertedSellerPricePercentile = convertNotExistOrderPercentile(sellerPricePercentile,sellerTotalOrderCountPerPeriodList);

        return convertToOrderPricePercentileGraphByPeriod(convertedSellerPricePercentile);
    }

    private List<SellerPercentileDTO> convertNotExistOrderPercentile(List<SellerPercentileDTO> sellerPricePercentiles, List<SellerOrderCountPerPeriodDTO> sellerTotalOrderCountPerPeriods) {
        List<SellerPercentileDTO> result = new ArrayList<>();
        for (int i = 0; i < sellerPricePercentiles.size(); i++) {// 월별(2022-07, 2022-08, ...)
            String curPercentile = sellerPricePercentiles.get(i).getPercentile();
            Long orderCount = sellerTotalOrderCountPerPeriods.get(i).getTotalCount();
            String date = sellerTotalOrderCountPerPeriods.get(i).getDate();

            if (orderCount.equals(0L)) {
                result.add(new SellerPercentileDTO(0D, date));
            } else {
                result.add(new SellerPercentileDTO(curPercentile, date));
            }
        }
        return result;
    }

    public OrderCountCompareByPeriodRes findSellerTotalOrderCountCompareByPeriod(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long sellerId) { // 기간별 판매횟수 비교 그래프

        checkOrderProductExist(sellerId); // 특정 판매자의 상품이 판매된 적이 있는지

        //전체 판매자
        List<SellerOrderCountPerPeriodDTO> allSellerTotalOrderCount = orderProductQueryRepository.findAllSellerTotalCountPerPeriodAndCategory(startDate, endDate, categoryParamDTO);
        List<SellerOrderCountPerPeriodDTO> allSellerAvgOrderCountPerPeriodList = convertToTotalSellerAvgOrderCount(allSellerTotalOrderCount, startDate, endDate, categoryParamDTO);

        //특정 판매자
        List<SellerOrderCountPerPeriodDTO> sellerTotalOrderCountPerPeriodList = orderProductQueryRepository.findSellerTotalOrderCountPerPeriodAndCategory(startDate, endDate, categoryParamDTO, sellerId);
        return convertToOrderCountCompareByPeriodDTO(allSellerAvgOrderCountPerPeriodList, sellerTotalOrderCountPerPeriodList);
    }

    private void checkOrderProductExist(Long sellerId) {// 특정 판매자의 상품이 판매된 적이 있는지
        Long count = orderProductRepository.findOrderProductCountBySeller(sellerId);
        if (count == 0) {
            throw new NotExistOrder("해당 판매자의 판매 기록이 없으므로, 그래프 조회가 불가능합니다.");
        }

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

        return new PriceCompareByPeriodRes(dateList, sellerPrices, avgList);
    }

    private List<SellerPricePerPeriodDTO> convertToTotalSellerAvgPrice(List<SellerPricePerPeriodDTO> allSellerTotalPrice, YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) {
        List<SellerPricePerPeriodDTO> sellerPricePerPeriodDTOS = new ArrayList<>();
        List<SellerCountByPeriodDTO> countByPeriod = orderProductQueryRepository.findSellerCountOrderProductExistByPeriod(startDate, endDate, categoryParamDTO);

        for (int i = 0; i < countByPeriod.size(); i++) {
            Long sellerCount = countByPeriod.get(i).getCount(); // 월별 판매자 수
            String date = countByPeriod.get(i).getDate(); // 년월  (2020-01)
            Long totalPrice = allSellerTotalPrice.get(i).getTotalPrice(); // 월별 카테고리별 총 판매액

            if (sellerCount == 0) {
                sellerPricePerPeriodDTOS.add(new SellerPricePerPeriodDTO(0L, date));
            } else {
                sellerPricePerPeriodDTOS.add(new SellerPricePerPeriodDTO(totalPrice / sellerCount, date));
            }
        }
        return sellerPricePerPeriodDTOS;
    }

    private OrderPricePercentileGraphByPeriodRes convertToOrderPricePercentileGraphByPeriod(List<SellerPercentileDTO> sellerPercentileList) {
        List<String> dateList = sellerPercentileList.stream()
                .map(SellerPercentileDTO::getDate)
                .collect(Collectors.toList());
        List<String> percentileList = sellerPercentileList.stream()
                .map(SellerPercentileDTO::getPercentile)
                .collect(Collectors.toList());
        return new OrderPricePercentileGraphByPeriodRes(dateList, percentileList);
    }


    private OrderCountCompareByPeriodRes convertToOrderCountCompareByPeriodDTO(List<SellerOrderCountPerPeriodDTO> allSellerAvgOrderCountList, List<SellerOrderCountPerPeriodDTO> sellerOrderCountList) {
        List<String> dateList = allSellerAvgOrderCountList.stream()
                .map(SellerOrderCountPerPeriodDTO::getDate)
                .collect(Collectors.toList());
        List<Long> avgList = allSellerAvgOrderCountList.stream()
                .map(SellerOrderCountPerPeriodDTO::getTotalCount)
                .collect(Collectors.toList());
        List<Long> sellerOrderCounts = sellerOrderCountList.stream()
                .map(SellerOrderCountPerPeriodDTO::getTotalCount)
                .collect(Collectors.toList());

        return new OrderCountCompareByPeriodRes(dateList, sellerOrderCounts, avgList);
    }

    private List<SellerOrderCountPerPeriodDTO> convertToTotalSellerAvgOrderCount(List<SellerOrderCountPerPeriodDTO> allSellerOrderCountList, YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) {
        List<SellerOrderCountPerPeriodDTO> sellerOrderCountPerPeriodDTOS = new ArrayList<>();
        List<SellerCountByPeriodDTO> countByPeriod = orderProductQueryRepository.findSellerCountOrderProductExistByPeriod(startDate, endDate, categoryParamDTO);

        for (int i = 0; i < countByPeriod.size(); i++) {
            Long sellerCount = countByPeriod.get(i).getCount(); // 월별 판매자 수
            String date = countByPeriod.get(i).getDate(); // 년월  (2020-01)
            Long totalOrderCount = allSellerOrderCountList.get(i).getTotalCount(); // 월별 카테고리별 총 판매 횟수

            if (sellerCount == 0) {
                sellerOrderCountPerPeriodDTOS.add(new SellerOrderCountPerPeriodDTO(0L, date));
            } else {
                sellerOrderCountPerPeriodDTOS.add(new SellerOrderCountPerPeriodDTO(totalOrderCount / sellerCount, date));
            }
        }
        return sellerOrderCountPerPeriodDTOS;
    }



}
