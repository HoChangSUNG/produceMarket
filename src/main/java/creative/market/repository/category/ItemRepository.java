package creative.market.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.category.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<Item> findAll() {
        return em.createQuery("select i from Item i")
                .getResultList();
    }
}
