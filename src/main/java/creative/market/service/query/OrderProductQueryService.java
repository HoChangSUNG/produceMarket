package creative.market.service.query;

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

    public List<OrderHistoryDTO> findBuyerOrderPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId, int offset, int limit) {
        List<BuyerOrderPerPeriodDTO> list = orderProductQueryRepository.findBuyerOrderPerPeriod(startDate, endDate, userId, offset, limit);
        Map<Long, List<BuyerOrderPerPeriodDTO>> collect = list.stream()
                .collect(Collectors.groupingBy(BuyerOrderPerPeriodDTO::getOrderId));

        List<OrderHistoryDTO> result = new ArrayList<>();

        for (Long key : collect.keySet()) {
            List<BuyerOrderPerPeriodDTO> dtos = collect.get(key);
            int sum = dtos.stream()
                    .mapToInt(BuyerOrderPerPeriodDTO::getPrice)
                    .sum();

            result.add(new OrderHistoryDTO(key, dtos.size(), sum, dtos.get(0).getCreatedDate(), dtos.get(0).getStatus(), dtos));
        }

        return result;
    }
}
