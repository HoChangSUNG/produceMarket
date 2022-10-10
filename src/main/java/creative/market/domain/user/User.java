package creative.market.domain.user;

import lombok.*;

import javax.persistence.*;

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

    public User(String name, String loginId, String password, String birth, String email, String phoneNumber) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void updateUser(String name, String loginId, String password, String birth, String email, String phoneNumber) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
