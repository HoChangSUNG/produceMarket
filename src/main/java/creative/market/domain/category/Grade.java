package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    private int productId;

    private int gradeRank;

    private String gradeName;

    @Builder
    public Grade(int productId, int gradeRank, String gradeName) {
        this.productId = productId;
        this.gradeRank = gradeRank;
        this.gradeName = gradeName;
    }
}
