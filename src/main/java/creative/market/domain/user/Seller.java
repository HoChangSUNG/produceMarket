package creative.market.domain.user;

import creative.market.domain.Address;
import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends User {

    @Embedded
    private Address address;

    private String businessNumber;

    private String businessName;

    private LocalDateTime changeDate;

    @Builder
    public Seller(String name, String loginId, String password, String birth, String email, String phoneNumber, Address address, String businessNumber, String businessName, LocalDateTime changeDate) {
        super(name, loginId, password, birth, email, phoneNumber);
        this.address = address;
        this.businessNumber = businessNumber;
        this.businessName = businessName;
        this.changeDate = changeDate;
    }

    @Override
    public void updateUser(String name, String loginId, String password, String birth, String email, String phoneNumber, Address address) {
        super.updateUser(name, loginId, password, birth, email, phoneNumber, address);
        this.address = address;
    }
}
