package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCategory {

    @Id
    private int itemCategoryCode;

    private String name;

    @Builder
    public ItemCategory(int itemCategoryCode, String name) {
        this.itemCategoryCode = itemCategoryCode;
        this.name = name;
    }
}
