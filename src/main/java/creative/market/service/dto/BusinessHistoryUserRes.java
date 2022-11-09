package creative.market.service.dto;

import creative.market.domain.business.BusinessHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BusinessHistoryUserRes {

    private Long businessId;

    private String userName;

    private String businessName;

    private String createdDate;

    public BusinessHistoryUserRes(BusinessHistory businessHistory) {
        this.businessId = businessHistory.getId();
        this.userName = businessHistory.getUser().getName();
        this.businessName = businessHistory.getBusinessName();
        this.createdDate = businessHistory.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
    }
}
