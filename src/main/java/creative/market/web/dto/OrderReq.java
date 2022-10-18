package creative.market.web.dto;

import creative.market.service.dto.OrderProductParamDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderReq {

    List<OrderProductParamDTO> orderProducts;

    @NotBlank
    private String jibun;
    @NotBlank
    private String road;
    @NotNull
    @Size(min = 5, max = 5)
    private Integer zipcode;
    @NotBlank
    private String detailAddress;
}
