package creative.market.domain.business;

import creative.market.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private long id;

    private String businessNumber;

    private String businessName;

    @Enumerated(EnumType.STRING)
    private BusinessStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime changeDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "image_id")
    private BusinessImage businessImage;

    @Builder
    public BusinessHistory(String businessNumber, String businessName, BusinessStatus status, LocalDateTime createdDate, LocalDateTime changeDate, User user, BusinessImage businessImage) {
        this.businessNumber = businessNumber;
        this.businessName = businessName;
        this.status = status;
        this.createdDate = createdDate;
        this.changeDate = changeDate;
        this.user = user;
        this.businessImage = businessImage;
    }
}
