package creative.market.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends User{

    @Builder
    public Admin(String name, String loginId, String password, int age, String email, String phoneNumber) {
        super(name, loginId, password, age, email, phoneNumber);
    }
}
