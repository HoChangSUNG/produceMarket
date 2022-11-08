package creative.market.domain.user;

import creative.market.domain.Address;
import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buyer extends User {

    @Embedded
    private Address address;

    @Builder
    public Buyer(String name, String loginId, String password, String birth, String email, String phoneNumber, Address address) {
        super(name, loginId, password, birth, email, phoneNumber);
        this.address = address;
    }

    @Override
    public void updateUser(String name, String loginId, String password, String birth, String email, String phoneNumber, Address address) {
        super.updateUser(name, loginId, password, birth, email, phoneNumber, address);
        this.address = address;
    }
}
