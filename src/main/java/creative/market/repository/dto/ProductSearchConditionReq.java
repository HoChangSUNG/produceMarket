package creative.market.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchConditionReq {

    private String productName;
    private String orderBy="latest";
    private Integer itemCategoryCode;
    private Integer itemCode;
    private Long kindId;
    private Long kindGradeId;
}
