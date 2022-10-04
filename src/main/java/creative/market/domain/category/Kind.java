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
public class Kind {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kind_id")
    private long id;

    private int code;

    private String name;

    private String wholesaleUnit;

    private int wholesaleSize;

    private String retailsaleUnit;

    private int retailsaleSize;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_code")
    private Item item;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @Builder
    public Kind(int code, String name, String wholesaleUnit, int wholesaleSize, String retailsaleUnit, int retailsaleSize, Item item, Grade grade) {
        this.code = code;
        this.name = name;
        this.wholesaleUnit = wholesaleUnit;
        this.wholesaleSize = wholesaleSize;
        this.retailsaleUnit = retailsaleUnit;
        this.retailsaleSize = retailsaleSize;
        this.item = item;
        this.grade = grade;
    }
}
