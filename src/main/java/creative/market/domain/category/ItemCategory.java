package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCategory {

    @Id
    private int itemCategoryCode;

    private String name;

    @OneToMany(mappedBy = "itemCategory")
    private List<Item> items = new ArrayList<>();

    @Builder
    public ItemCategory(int itemCategoryCode, String name) {
        this.itemCategoryCode = itemCategoryCode;
        this.name = name;
    }

}
