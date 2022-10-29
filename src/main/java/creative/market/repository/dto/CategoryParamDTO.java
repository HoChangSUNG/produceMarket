package creative.market.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryParamDTO {

    Integer itemCategoryCode;
    Integer itemCode;
    Long kindId;
    Long kindGradeId;
}
