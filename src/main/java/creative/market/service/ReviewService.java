package creative.market.service;

import creative.market.domain.Review;
import creative.market.domain.product.Product;
import creative.market.domain.user.User;
import creative.market.exception.DuplicateException;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.ProductRepository;
import creative.market.repository.ReviewRepository;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.user.UserRepository;
import creative.market.web.dto.ReviewReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public Long save(Review review, Long productId, Long userId) {

        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        if(!reviewRepository.findByProductIdAndUserId(findProduct.getId(), findUser.getId()).isEmpty()) {
            throw new DuplicateException("리뷰를 중복 등록할수 없습니다.");
        }

        if(orderProductRepository.findByProductIdAndUserId(productId, userId).isEmpty()) {
            throw new NoSuchElementException("주문내역이 존재하지 않는 상품입니다.");
        }

        review.changeUser(findUser);
        review.changeProduct(findProduct);

        reviewRepository.save(review);

        return review.getId();
    }

    @Transactional
    public void update(Long reviewId, ReviewReq reviewReq, Long userId) {

        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰가 존재하지 않습니다."));

        checkUser(findReview, userId);

        findReview.changeReview(reviewReq.getRate(), reviewReq.getContent());
    }

    public List<Review> findByProductId(Long productId) {

        productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));

        return reviewRepository.findByProductId(productId);
    }

    @Transactional
    public void delete(Long reviewId, Long userId) {

        List<Review> findReview = reviewRepository.findByIdWithProduct(reviewId);
        if(findReview.isEmpty()) {
            throw new NoSuchElementException("리뷰가 존재하지 않습니다.");
        }

        Review review = findReview.get(0);

        checkUser(review, userId);

        review.getProduct().getReviews().remove(review);
    }

    private void checkUser(Review review, Long userId) {
        if(!review.getUser().getId().equals(userId)) {
            throw new LoginAuthenticationException("권한이 없습니다.");
        }
    }
}
