package creative.market.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.category.GradeCriteria;
import creative.market.domain.category.QGradeCriteria;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

import static creative.market.domain.category.QGradeCriteria.*;

@Repository
@RequiredArgsConstructor
public class GradeCriteriaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Long save(String name, String path) {
        GradeCriteria criteria = GradeCriteria.builder()
                .name(name)
                .path(path)
                .build();
        em.persist(criteria);
        return criteria.getId();
    }

    public Optional<GradeCriteria> findByName(String name) {
        GradeCriteria result = queryFactory.selectFrom(gradeCriteria)
                .where(gradeCriteria.name.eq(name))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
