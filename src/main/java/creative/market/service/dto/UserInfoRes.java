package creative.market.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoRes {

    private String name;

    private String loginId;

    private String password;

    private String birth;

    private String email;

    private String phoneNumber;

    private String jibun;

    private String road;

    private Integer zipcode;

    private String detailAddress;
}
