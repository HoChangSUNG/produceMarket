package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.Cart;
import creative.market.domain.category.QKind;
import creative.market.domain.category.QKindGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static creative.market.domain.QCart.*;
import static creative.market.domain.category.QKind.*;
import static creative.market.domain.category.QKindGrade.*;
import static creative.market.domain.product.QProduct.*;

@Repository
@RequiredArgsConstructor
public class CartRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Optional<Cart> findById(Long cartId) {
        return Optional.ofNullable(em.find(Cart.class, cartId));
    }

    public Long save(Cart cart) {
        em.persist(cart);
        return cart.getId();
    }

    public List<Cart> findByUserIdFetchJoinProductAndKind(Long userId) {
        return queryFactory.selectFrom(cart)
                .join(cart.product, product).fetchJoin()
                .join(product.kindGrade, kindGrade)
                .join(kindGrade.kind, kind)
                .where(cart.user.id.eq(userId))
                .fetch();
    }

    public List<Cart> findByUserId(Long userId) {
        return queryFactory.selectFrom(cart)
                .where(cart.user.id.eq(userId))
                .fetch();
    }

    public void delete(Cart cart) {
        em.remove(cart);
    }
}
