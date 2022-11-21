package creative.market.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Seller;
import creative.market.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.Optional;

import static creative.market.domain.user.QBuyer.buyer;
import static creative.market.domain.user.QSeller.*;

@Repository
@RequiredArgsConstructor
public class SellerRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public Optional<Seller> findByLoginIdAndPassword(String loginId, String password) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(seller)
                        .where(loginIdEq(loginId), passwordEq(password), sellerExistCheck())
                        .fetchOne());
    }

    public Optional<Seller> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(seller)
                .where(sellerEq(id), sellerExistCheck())
                .fetchOne());
    }

    private BooleanExpression sellerExistCheck() {
        return seller.status.eq(UserStatus.EXIST);
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? seller.loginId.eq(loginId) : null;
    }

    private BooleanExpression passwordEq(String password) {
        return password != null ? seller.password.eq(password) : null;
    }

    private BooleanExpression sellerEq(Long sellerId) {
        return sellerId != null ? seller.id.eq(sellerId) : null;
    }

}
