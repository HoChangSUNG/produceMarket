package creative.market.domain.order;

import creative.market.domain.product.Product;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    private int count;

    private int price;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public OrderProduct(int count, int price, Product product, OrderStatus status) {
        this.count = count;
        this.price = price;
        this.product = product;
        this.status = status;
    }

    //== 연관관계 편의 메서드==//
    public void changeOrder(Order order) {
        if (order != null) {
            this.order = order;
            order.getOrderProducts().add(this);
        }
    }

    public int getTotalPrice() {
        return count * price;
    }

    public void cancel() {
        this.status = OrderStatus.CANCEL;
    }
}
