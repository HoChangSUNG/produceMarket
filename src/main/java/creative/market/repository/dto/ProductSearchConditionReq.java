package creative.market.repository.dto;

import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ProductSearchConditionReq {

    private String productName;
    private String orderBy;
    private Integer itemCategoryCode;
    private Integer itemCode;
    private Long kindId;
    private Long kindGradeId;

}
