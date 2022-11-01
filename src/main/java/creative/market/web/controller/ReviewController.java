package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.Review;
import creative.market.domain.product.Product;
import creative.market.repository.ProductRepository;
import creative.market.service.ProductService;
import creative.market.service.ReviewService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import creative.market.web.dto.ReviewReq;
import creative.market.web.dto.ReviewRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ResultRes createReview(@PathVariable Long productId, @RequestBody @Valid ReviewReq reviewReq, @Login LoginUserDTO loginUserDTO) {

        Review review = Review.builder()
                .rate(reviewReq.getRate())
                .content(reviewReq.getContent())
                .build();

        reviewService.save(review, productId, loginUserDTO.getId());

        return new ResultRes(new MessageRes("리뷰 등록 성공"));
    }

    @GetMapping("/{productId}")
    public ResultRes getReviews(@PathVariable Long productId) {

        List<Review> reviews = reviewService.findByProductId(productId);

        List<ReviewRes> result = reviews.stream()
                .map(ReviewRes::new)
                .collect(Collectors.toList());

        return new ResultRes(result);
    }

    @PatchMapping("/{reviewId}")
    public ResultRes updateReview(@PathVariable Long reviewId, @RequestBody @Valid ReviewReq reviewReq, @Login LoginUserDTO loginUserDTO) {

        reviewService.update(reviewId, reviewReq, loginUserDTO.getId());

        return new ResultRes(new MessageRes("리뷰 수정 성공"));
    }

    @DeleteMapping("/{reviewId}")
    public ResultRes deleteReview(@PathVariable Long reviewId, @Login LoginUserDTO loginUserDTO) {

        reviewService.delete(reviewId, loginUserDTO.getId());

        return new ResultRes(new MessageRes("리뷰 삭제 성공"));
    }
}
