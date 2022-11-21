package creative.market.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.QBuyer;
import creative.market.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.user.QAdmin.admin;
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
                        .where(loginIdEq(loginId), passwordEq(password), buyerExistCheck())
                        .fetchOne());
    }

    public Optional<Buyer> findByLoginId(String loginId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(buyer)
                        .where(loginIdEq(loginId), buyerExistCheck())
                        .fetchOne());
    }

    public Optional<Buyer> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(buyer)
                .where(buyerExistCheck(), buyerEq(id))
                .fetchOne()
        );
    }

    public void updateType(Long id) {
        em.createNativeQuery("update User b set b.dtype = 'Seller' where b.user_id = :id")
                .setParameter("id", id)
                .executeUpdate();

        em.flush();
        em.clear();
    }

    private BooleanExpression buyerExistCheck() {
        return buyer.status.eq(UserStatus.EXIST);
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? buyer.loginId.eq(loginId) : null;
    }

    private BooleanExpression passwordEq(String password) {
        return password != null ? buyer.password.eq(password) : null;
    }

    private BooleanExpression buyerEq(Long buyerId) {
        return buyerId != null ? buyer.id.eq(buyerId) : null;
    }
}
