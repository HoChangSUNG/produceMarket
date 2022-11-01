package creative.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.QReview;
import creative.market.domain.Review;
import creative.market.domain.product.QProduct;
import creative.market.domain.user.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static creative.market.domain.QReview.*;
import static creative.market.domain.product.QProduct.*;
import static creative.market.domain.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Long save(Review review) {
        em.persist(review);

        return review.getId();
    }

    public List<Review> findByProductIdAndUserId(Long productId, Long userId) {
        return queryFactory
                .selectFrom(review)
                .where(review.product.id.eq(productId).and(review.user.id.eq(userId)))
                .fetch();
    }

    public List<Review> findByProductId(Long productId) {
        return queryFactory
                .selectFrom(review)
                .join(review.user, user).fetchJoin()
                .where(review.product.id.eq(productId))
                .fetch();
    }

    public Optional<Review> findById(Long reviewId) {
        Review review = em.find(Review.class, reviewId);
        return Optional.ofNullable(review);
    }

    public List<Review> findByIdWithProduct(Long reviewId) {
        return queryFactory
                .selectFrom(review)
                .join(review.product, product).fetchJoin()
                .where(review.id.eq(reviewId))
                .fetch();
    }
}
