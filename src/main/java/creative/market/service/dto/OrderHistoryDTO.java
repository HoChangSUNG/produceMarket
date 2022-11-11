package creative.market.service.dto;

import creative.market.repository.dto.BuyerOrderPerPeriodDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDTO {

    private Long orderId;

    private int totalCount;

    private int totalPrice;

    private String createdDate;

    private String status;

    private List<BuyerOrderPerPeriodDTO> productList;
}
