package creative.market.repository;

import creative.market.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final EntityManager em;

    public Long save(Product product) {
        em.persist(product);
        return product.getId();
    }

    public Product findById(Long id) {
        return em.find(Product.class,id);
    }
}
