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
public class Kind {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kind_id")
    private Long id;

    private int code;

    private String name;

    private String wholesaleUnit;

    private int wholesaleSize;

    private String retailsaleUnit;

    private int retailsaleSize;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_code")
    private Item item;

    @OneToMany(mappedBy = "kind")
    private List<KindGrade> kindGrades = new ArrayList<>();

    @Builder
    public Kind(int code, String name, String wholesaleUnit, int wholesaleSize, String retailsaleUnit, int retailsaleSize, Item item) {
        this.code = code;
        this.name = name;
        this.wholesaleUnit = wholesaleUnit;
        this.wholesaleSize = wholesaleSize;
        this.retailsaleUnit = retailsaleUnit;
        this.retailsaleSize = retailsaleSize;
        this.kindGrades = new ArrayList<>();
        changeItem(item);
    }

    //==연관관계 메서드==//
    public void changeItem(Item item) {
        if (item != null) {
            this.item = item;
            item.getKinds().add(this);
        }
    }
}
