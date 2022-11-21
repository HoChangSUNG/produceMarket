package creative.market.domain.user;

import creative.market.domain.Address;
import creative.market.domain.product.Product;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private String loginId;

    private String password;

    private String birth;

    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user")
    private List<Product> products = new ArrayList<>();

    public User(String name, String loginId, String password, String birth, String email, String phoneNumber) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void updateUser(String name, String loginId, String password, String birth, String email, String phoneNumber, Address address) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @PrePersist
    private void prePersist() {
        status = UserStatus.EXIST;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }
}
