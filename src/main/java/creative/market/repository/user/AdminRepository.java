package creative.market.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Admin;
import creative.market.domain.user.QAdmin;
import creative.market.domain.user.Seller;
import creative.market.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.user.QAdmin.*;
import static creative.market.domain.user.QSeller.seller;
import static creative.market.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Optional<Admin> findByLoginIdAndPassword(String loginId, String password) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(admin)
                        .where(admin.loginId.eq(loginId), (admin.password.eq(password)), adminExistCheck())
                        .fetchOne());
    }

    private BooleanExpression adminExistCheck() {
        return admin.status.eq(UserStatus.EXIST);
    }


}
