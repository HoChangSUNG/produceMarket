package creative.market.service.dto;

import creative.market.domain.business.BusinessHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class UserBusinessHistoryRes {

    private String createdDate;

    private String changeDate;

    private String businessNumber;

    private String businessName;

    private String status;

    public UserBusinessHistoryRes(BusinessHistory businessHistory) {
        this.createdDate = businessHistory.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        if(businessHistory.getChangeDate() == null) {
            this.changeDate = "변경없음";
        } else {
            this.changeDate = businessHistory.getChangeDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        }
        this.businessNumber = businessHistory.getBusinessNumber();
        this.businessName = businessHistory.getBusinessName();
        this.status = businessHistory.getStatus().name();
    }
}
