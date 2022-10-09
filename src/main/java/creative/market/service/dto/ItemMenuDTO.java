package creative.market.service.dto;

import creative.market.domain.category.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemMenuDTO {
    private int itemCode;
    private String name;
    private List<KindMenuDTO> kinds;

    public ItemMenuDTO(Item item) {
        this.itemCode =item.getItemCode();
        this.name = item.getName();
        this.kinds = item.getKinds().stream()
                .map(kind -> new KindMenuDTO(kind))
                .collect(Collectors.toList());
    }
}