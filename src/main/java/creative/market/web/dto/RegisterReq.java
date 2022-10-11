package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {

    @NotEmpty
    private String name;

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotEmpty
    private String birth;

    @Email
    private String email;

    @NotEmpty
    private String phoneNumber;

    @NotEmpty
    private String jibun;

    @NotEmpty
    private String road;

    @NotEmpty
    private int zipcode;

    @NotEmpty
    private String detailAddress;
}