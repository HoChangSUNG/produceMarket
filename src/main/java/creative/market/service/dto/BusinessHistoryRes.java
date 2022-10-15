package creative.market.service.dto;

import creative.market.domain.business.BusinessHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BusinessHistoryRes {

    private Long businessId;

    private String userName;

    private String phoneNumber;

    private String imgSrc;

    private String businessNumber;

    private String businessName;

    private String createdDate;

    public BusinessHistoryRes(BusinessHistory businessHistory) {
        this.businessId = businessHistory.getId();
        this.userName = businessHistory.getUser().getName();
        this.phoneNumber = businessHistory.getUser().getPhoneNumber();
        this.imgSrc = businessHistory.getBusinessImage().getPath();
        this.businessNumber = businessHistory.getBusinessNumber();
        this.businessName = businessHistory.getBusinessName();
        this.createdDate = businessHistory.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
    }
}
