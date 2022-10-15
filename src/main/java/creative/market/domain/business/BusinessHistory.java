package creative.market.domain.business;

import creative.market.domain.ChangeDate;
import creative.market.domain.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessHistory extends ChangeDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private Long id;

    private String businessNumber;

    private String businessName;

    @Enumerated(EnumType.STRING)
    private BusinessStatus status;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "image_id")
    private BusinessImage businessImage;

    @Builder
    public BusinessHistory(String businessNumber, String businessName, BusinessStatus status, User user, BusinessImage businessImage) {
        this.businessNumber = businessNumber;
        this.businessName = businessName;
        this.status = status;
        this.user = user;
        this.businessImage = businessImage;
    }

    public void changeStatus(BusinessStatus status) {
        this.status = status;
    }
}
