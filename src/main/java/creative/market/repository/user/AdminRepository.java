package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Admin;
import creative.market.domain.user.QAdmin;
import creative.market.domain.user.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.user.QAdmin.*;
import static creative.market.domain.user.QSeller.seller;

@Repository
@RequiredArgsConstructor
public class AdminRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Optional<Admin> findByLoginIdAndPassword(String loginId, String password) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(admin)
                        .where(admin.loginId.eq(loginId).and(admin.password.eq(password)))
                        .fetchOne());
    }
}
