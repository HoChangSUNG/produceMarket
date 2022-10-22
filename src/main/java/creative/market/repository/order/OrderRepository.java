package creative.market.repository.order;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    public Long save(Order order) {
        em.persist(order);
        return order.getId();
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class,id));
    }
}
