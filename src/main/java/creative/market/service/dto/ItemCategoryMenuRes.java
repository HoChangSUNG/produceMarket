package creative.market.service.dto;

import creative.market.domain.category.ItemCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemCategoryMenuRes {

    private int id;
    private String name;
    private List<ItemMenuDTO> category;
    public ItemCategoryMenuRes(ItemCategory itemCategory) {
        this.id = itemCategory.getItemCategoryCode();
        this.name = itemCategory.getName();
        this.category = itemCategory.getItems().stream()
                .map(item -> new ItemMenuDTO(item))
                .collect(Collectors.toList());
    }
}
