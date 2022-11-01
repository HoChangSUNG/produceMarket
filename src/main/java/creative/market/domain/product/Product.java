package creative.market.domain.product;

import creative.market.domain.CreatedDate;
import creative.market.domain.Review;
import creative.market.domain.category.KindGrade;
import creative.market.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends CreatedDate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private int price;

    private String info;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "kind_grade_id")
    private KindGrade kindGrade;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Builder
    public Product(String name, int price, String info, KindGrade kindGrade, User user, List<ProductImage> ordinalProductImages , ProductImage signatureProductImage) {
        this.name = name;
        this.price = price;
        this.info = info;
        this.kindGrade = kindGrade;
        this.user = user;
        if (ordinalProductImages != null) {
            ordinalProductImages.forEach(productImage -> productImage.changeProduct(this));
        }
        if (signatureProductImage != null) {
            signatureProductImage.changeProduct(this);
        }

    }

    public ProductImage getSignatureProductImage() { // 대표사진 가져오기
        Optional<ProductImage> signatureImage = productImages.stream()
                .filter(productImage -> productImage.getType() == ProductImageType.SIGNATURE)
                .findFirst();
        if (signatureImage.isPresent()) {
            return signatureImage.get();
        }
        return null;
    }

    public List<ProductImage> getOrdinalProductImage() { // 일반사진 가져오기
       return productImages.stream()
                .filter(productImage -> productImage.getType() == ProductImageType.ORDINAL)
                .collect(Collectors.toList());
    }

    public void changeProduct(KindGrade kindGrade, String productName,int price, String info) {
        this.kindGrade = kindGrade;
        this.name = productName;
        this.price = price;
        this.info = info;
    }

    public void deleteProductImage(ProductImage productImage) {
        productImages.remove(productImage);
    }


    public void addProductOrdinalImage(ProductImage productOrdinalImage){
        productOrdinalImage.changeProduct(this);
    }

    public void addProductSignatureImage(ProductImage productSignatureImage) {
        productSignatureImage.changeProduct(this);
    }

}
