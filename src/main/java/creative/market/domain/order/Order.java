package creative.market.domain.order;

import creative.market.domain.Address;
import creative.market.domain.CreatedDate;
import creative.market.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends CreatedDate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Embedded
    private Address address;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
    @Builder
    public Order(Address address, User user,List<OrderProduct> orderProducts) {
        this.address = address;
        this.user = user;
        orderProducts.forEach(orderProduct -> orderProduct.changeOrder(this));
    }

    public int getTotalPrice() {
        return orderProducts.stream()
                .mapToInt(OrderProduct::getTotalPrice)
                .sum();
    }

}
