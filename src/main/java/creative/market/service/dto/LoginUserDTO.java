package creative.market.service.dto;

import creative.market.aop.UserType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {

    private Long id;
    private String name;
    private UserType userType;
}
