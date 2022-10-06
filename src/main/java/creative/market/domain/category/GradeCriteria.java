package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeCriteria {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_criteria_id")
    private long id;

    private String name;

    private String path;

    @Builder
    public GradeCriteria(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
