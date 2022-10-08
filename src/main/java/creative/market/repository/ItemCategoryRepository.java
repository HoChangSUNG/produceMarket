package creative.market.repository;

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
public class ItemCategoryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public ItemCategoryRepository(EntityManager em) {
        this.em = em;
        queryFactory = new JPAQueryFactory(em);
    }

    public List<ItemCategory> findAll() {
        return queryFactory.selectFrom(itemCategory)
                .fetch();
    }

}
