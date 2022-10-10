package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.QSeller;
import creative.market.domain.user.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

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
                        .where(seller.loginId.eq(loginId).and(seller.password.eq(password)))
                        .fetchOne());
    }

    public Optional<Seller> findById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(seller)
                        .where(seller.id.eq(id))
                        .fetchOne());
    }
}
