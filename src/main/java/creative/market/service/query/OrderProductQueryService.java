package creative.market.service.query;

import creative.market.domain.order.OrderProduct;
import creative.market.domain.order.OrderStatus;
import creative.market.repository.dto.BuyerOrderPerPeriodDTO;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.service.dto.OrderHistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderProductQueryService {

    private final OrderProductQueryRepository orderProductQueryRepository;

    public List<OrderHistoryDTO> findBuyerOrderPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        List<BuyerOrderPerPeriodDTO> list = orderProductQueryRepository.findBuyerOrderPerPeriod(startDate, endDate, userId);
        Map<Long, List<BuyerOrderPerPeriodDTO>> collect = list.stream()
                .collect(Collectors.groupingBy(BuyerOrderPerPeriodDTO::getOrderId));

        List<OrderHistoryDTO> result = new ArrayList<>();

        for (Long key : collect.keySet()) {
            List<BuyerOrderPerPeriodDTO> dtos = collect.get(key);
            int sum = dtos.stream()
                    .filter(dto -> dto.getStatus().equals(OrderStatus.ORDER.toString()))
                    .mapToInt(BuyerOrderPerPeriodDTO::getPrice)
                    .sum();

            int cnt = (int) dtos.stream()
                    .filter(dto -> dto.getStatus().equals(OrderStatus.CANCEL.toString()))
                    .count();

            result.add(new OrderHistoryDTO(key, dtos.size()-cnt, sum, dtos.get(0).getCreatedDate(), dtos));
        }

        return result;
    }
}
