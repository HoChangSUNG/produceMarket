package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.category.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static creative.market.domain.category.QItem.*;
import static creative.market.domain.category.QItemCategory.*;
import static creative.market.domain.category.QKind.*;
import static creative.market.domain.category.QKindGrade.*;

@Repository
@RequiredArgsConstructor
public class KindGradeRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<KindGrade> findById(Long kindGradeId) {
        return Optional.ofNullable(queryFactory.select(kindGrade)
                .from(kindGrade)
                .join(kindGrade.grade).fetchJoin()
                .join(kindGrade.kind, kind).fetchJoin()
                .join(kind.item, item).fetchJoin()
                .join(item.itemCategory, itemCategory).fetchJoin()
                .where(kindGrade.id.eq(kindGradeId))
                .fetchOne());
    }
}
