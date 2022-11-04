package creative.market.web.dto;

import creative.market.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRes {

    private Long reviewId;

    private String name;

    private Float rate;

    private String content;

    private String createdDate;

    private Long userId;

    public ReviewRes(Review review) {
        this.reviewId = review.getId();
        this.name = review.getUser().getName();
        this.rate = review.getRate();
        this.content = review.getContent();
        this.createdDate = review.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        this.userId = review.getUser().getId();
    }
}
