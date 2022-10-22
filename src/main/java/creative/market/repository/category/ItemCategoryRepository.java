package creative.market.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.category.ItemCategory;
import creative.market.domain.category.QItemCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static creative.market.domain.category.QItemCategory.*;

@Repository
@RequiredArgsConstructor
public class ItemCategoryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<ItemCategory> findAll() {
        return queryFactory.selectFrom(itemCategory)
                .fetch();
    }

}
