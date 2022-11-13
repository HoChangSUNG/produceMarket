package creative.market.web.dto;

import creative.market.repository.dto.BuyerTotalPricePerPeriodDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyerTotalPricePerPeriodRes {

    private List<String> dateList;
    private List<Long> priceList;

    public BuyerTotalPricePerPeriodRes(List<BuyerTotalPricePerPeriodDTO> buyerTotalPricePerPeriodDTO) {
        this.dateList = buyerTotalPricePerPeriodDTO.stream()
                .map(BuyerTotalPricePerPeriodDTO::getDate)
                .collect(Collectors.toList()); // 월별 리스트

        this.priceList = buyerTotalPricePerPeriodDTO.stream()
                .map(BuyerTotalPricePerPeriodDTO::getTotalPrice).collect(Collectors.toList()); // 가격 리스트
    }
}
