package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    private int itemCode;

    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_category_code")
    private ItemCategory itemCategory;

    @Builder
    public Item(int itemCode, String name, ItemCategory itemCategory) {
        this.itemCode = itemCode;
        this.name = name;
        this.itemCategory = itemCategory;
    }
}
