package creative.market.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class BuyerOrderPerPeriodDTO {

    private Long orderId;

    private Long productId;

    private String createdDate;

    private String productName;

    private int count;

    private int price;

    private String path;

    private String status;

    @QueryProjection
    public BuyerOrderPerPeriodDTO(Long orderId, Long productId, LocalDateTime createDate, String productName, int count, int price, String path, String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.createdDate = createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        this.productName = productName;
        this.count = count;
        this.price = price;
        this.path = path;
        this.status = status;
    }
}
