package creative.market.web.dto;

import creative.market.service.dto.OrderProductParamDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderReq {

    @Valid
    List<OrderProductParamDTO> orderProducts;

    @NotBlank
    private String jibun;
    @NotBlank
    private String road;

    @Max(99999)
    @Min(1)
    private Integer zipcode;
    @NotBlank
    private String detailAddress;
}
