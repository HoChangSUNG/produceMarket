package creative.market.domain;

import creative.market.domain.category.Item;
import creative.market.domain.product.Product;
import creative.market.domain.user.User;
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
public class Review extends CreatedDate{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private Float rate;

    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public Review(Float rate, String content, User user, Product product) {
        this.rate = rate;
        this.content = content;
        this.user = user;
        changeProduct(product);
    }

    public void changeProduct(Product product) {
        if (product != null) {
            this.product = product;
            product.getReviews().add(this);
        }
    }

    public void changeUser(User user) {
        this.user = user;
    }

    public void changeReview(Float rate, String content) {
        this.rate = rate;
        this.content = content;
    }
}
