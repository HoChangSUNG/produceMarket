package creative.market.domain.product;

import creative.market.domain.category.ItemCategory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    private String name;

    private String path;

    @Enumerated(EnumType.STRING)
    private ProductImageType type;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public ProductImage(String name, String path, ProductImageType type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public void changeProduct(Product product) {
        if (product != null) {
            this.product = product;
            product.getProductImages().add(this);
        }
    }

    public String getStoreImgName() {
        int position = path.lastIndexOf("/");
        return path.substring(position + 1);
    }
}
