package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.business.BusinessHistory;
import creative.market.domain.business.BusinessStatus;
import creative.market.domain.business.QBusinessHistory;
import creative.market.domain.business.QBusinessImage;
import creative.market.domain.user.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static creative.market.domain.business.QBusinessHistory.*;
import static creative.market.domain.business.QBusinessImage.*;
import static creative.market.domain.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class BusinessHistoryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Long save(BusinessHistory businessHistory) {
        em.persist(businessHistory);

        return businessHistory.getId();
    }

    public Optional<BusinessHistory> findByIdWithUser(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(businessHistory)
                .join(businessHistory.user, user).fetchJoin()
                .where(businessHistory.id.eq(id))
                .fetchOne());
    }

    public Optional<BusinessHistory> findById(Long id) {
        return Optional.ofNullable(em.find(BusinessHistory.class, id));
    }

    public Optional<BusinessHistory> findByUserIdAndStatus(Long userId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(businessHistory)
                .join(businessHistory.user, user).fetchJoin()
                .where(user.id.eq(userId).and(businessHistory.status.eq(BusinessStatus.WAIT)))
                .fetchOne());
    }

    public Optional<BusinessHistory> findByIdWithUserAndImage(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(businessHistory)
                .join(businessHistory.user, user).fetchJoin()
                .join(businessHistory.businessImage, businessImage).fetchJoin()
                .where(businessHistory.id.eq(id))
                .fetchOne());
    }

    public List<BusinessHistory> findByStatusWithUser() {
        return queryFactory
                .selectFrom(businessHistory)
                .join(businessHistory.user, user).fetchJoin()
                .where(businessHistory.status.eq(BusinessStatus.WAIT))
                .fetch();
    }

    public List<BusinessHistory> findByUser(Long userId) {
        return queryFactory
                .selectFrom(businessHistory)
                .where(businessHistory.user.id.eq(userId))
                .fetch();
    }
}
