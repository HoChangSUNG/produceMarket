package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.order.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.order.QOrderProduct.*;

@Repository
@RequiredArgsConstructor
public class OrderProductRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public Optional<OrderProduct> findById(Long orderProductId) {
        return Optional.ofNullable(
                em.find(OrderProduct.class,orderProductId)
        );
    }

    public Optional<OrderProduct> findByIdWithOrder(Long orderProductId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(orderProduct)
                        .join(orderProduct.order).fetchJoin()
                        .where(orderProduct.id.eq(orderProductId))
                        .fetchOne()
        );
    }
}
