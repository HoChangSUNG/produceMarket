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
    private int gradeId;

    private int gradeRank;

    private String gradeName;

    @Builder
    public Grade(int gradeId, int gradeRank, String gradeName) {
        this.gradeId = gradeId;
        this.gradeRank = gradeRank;
        this.gradeName = gradeName;
    }
}
