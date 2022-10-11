package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.QBuyer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.user.QBuyer.*;
import static creative.market.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class BuyerRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Long register(Buyer buyer) {
        em.persist(buyer);

        return buyer.getId();
    }

    public Optional<Buyer> findByLoginIdAndPassword(String loginId, String password) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(buyer)
                        .where(buyer.loginId.eq(loginId).and(buyer.password.eq(password)))
                        .fetchOne());
    }

    public Optional<Buyer> findByLoginId(String loginId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(buyer)
                        .where(buyer.loginId.eq(loginId))
                        .fetchOne());
    }
}
