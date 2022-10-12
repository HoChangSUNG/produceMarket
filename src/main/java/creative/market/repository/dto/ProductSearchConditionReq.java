package creative.market.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchConditionReq {

    private String productName;
    private String orderBy;
    private Integer itemCategoryCode;
    private Integer itemCode;
    private Long kindId;
    private Long kindGradeId;
}
