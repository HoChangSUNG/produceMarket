package creative.market.web.dto;

import creative.market.service.dto.ItemCategoryMenuRes;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryRes {


    private List<ItemCategoryMenuRes> category;


}
