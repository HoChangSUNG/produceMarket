package creative.market.domain.product;

import creative.market.domain.category.Kind;
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
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private long id;

    private String name;

    private int price;

    private String info;

    private LocalDateTime createdDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "kind_id")
    private Kind kind;

    @Builder
    public Product(String name, int price, String info, LocalDateTime createdDate, Kind kind) {
        this.name = name;
        this.price = price;
        this.info = info;
        this.createdDate = createdDate;
        this.kind = kind;
    }
}
