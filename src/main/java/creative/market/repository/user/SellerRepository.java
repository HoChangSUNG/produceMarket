package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
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
        Seller seller = em.find(Seller.class, id);
        return Optional.ofNullable(seller);
    }

    public Long findAllSellerCountWithExistAndDeletedSeller() { // 삭제된 seller + 존재하는 seller 모두 count
        return queryFactory.select(seller.count().longValue())
                .from(seller)
                .fetchOne();
    }
}
