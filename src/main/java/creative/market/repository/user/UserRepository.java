package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.QUser;
import creative.market.domain.user.Seller;
import creative.market.domain.user.User;
import creative.market.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(user)
                        .where(user.id.eq(id))
                        .fetchOne());
    }

    public void delete(Long id) {
        User findUser = em.find(User.class, id);
        em.remove(findUser);
    }

}
