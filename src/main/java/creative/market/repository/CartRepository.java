package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.Cart;
import creative.market.domain.QCart;
import creative.market.domain.product.QProduct;
import creative.market.domain.user.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static creative.market.domain.QCart.*;
import static creative.market.domain.product.QProduct.*;
import static creative.market.domain.user.QUser.*;

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

    public List<Cart> findByUserIdWithProduct(Long userId) {
        return queryFactory.selectFrom(cart)
                .join(cart.product, product).fetchJoin()
                .where(cart.user.id.eq(userId))
                .fetch();
    }
}
