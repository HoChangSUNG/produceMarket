package creative.market.service.dto;

import creative.market.domain.category.ItemCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemCategoryMenuDTO {

    private int itemCategoryCode;
    private String name;
    private List<ItemMenuDTO> items;
    public ItemCategoryMenuDTO(ItemCategory itemCategory) {
        this.itemCategoryCode = itemCategory.getItemCategoryCode();
        this.name = itemCategory.getName();
        this.items = itemCategory.getItems().stream()
                .map(item -> new ItemMenuDTO(item))
                .collect(Collectors.toList());
    }
}
