package creative.market.domain.product;

import creative.market.domain.CreatedDate;
import creative.market.domain.category.Kind;
import creative.market.domain.category.KindGrade;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends CreatedDate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private long id;

    private String name;

    private int price;

    private String info;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "kind_grade_id")
    private KindGrade kindGrade;

    @Builder
    public Product(String name, int price, String info, KindGrade kindGrade) {
        this.name = name;
        this.price = price;
        this.info = info;
        this.kindGrade = kindGrade;
    }
}
