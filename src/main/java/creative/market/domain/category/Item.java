package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "grade_criteria_id")
    private GradeCriteria gradeCriteria;

    @OneToMany(mappedBy = "item")
    private List<Kind> kinds = new ArrayList<>();

    @Builder
    public Item(int itemCode, String name, ItemCategory itemCategory, GradeCriteria gradeCriteria) {
        this.itemCode = itemCode;
        this.name = name;
        changeItemCategory(itemCategory);
        this.gradeCriteria = gradeCriteria;
    }

    //==연관관계 메서드==//
    public void changeItemCategory(ItemCategory itemCategory) {
        if (itemCategory != null) {
            this.itemCategory = itemCategory;
            itemCategory.getItems().add(this);
        }
    }

}
