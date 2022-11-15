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
@ToString
public class SaleHistoryRes {

    private Long productId;

    private int count;

    private int price;

    private String productName;

    private String createdDate;

    private String jibun;

    private String road;

    private String detailAddress;

    private Integer zipCode;

    private String phoneNumber;

    private String path;

    @QueryProjection
    public SaleHistoryRes(Long productId, int count, int price, String productName, LocalDateTime createdDate, String jibun, String road, String detailAddress, int zipCode, String phoneNumber, String path) {
        this.productId = productId;
        this.count = count;
        this.price = price;
        this.productName = productName;
        this.createdDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.jibun = jibun;
        this.road = road;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.phoneNumber = phoneNumber;
        this.path = path;
    }

}
